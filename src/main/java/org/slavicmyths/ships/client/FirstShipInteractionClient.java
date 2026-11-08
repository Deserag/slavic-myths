package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;
import org.slavicmyths.ships.RaftContent;

/**
 * Helm and hull consist of normal Minecraft blocks (not entity hitboxes).
 * This input adapter chooses the nearest known ship with server-side validation.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipInteractionClient {
    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.player.isPassenger()) return;
        boolean key = mc.player.getMainHandItem().is(RaftContent.SHIPYARD_KEY.get())
                || mc.player.getOffhandItem().is(RaftContent.SHIPYARD_KEY.get());
        if (mc.player.isShiftKeyDown() && !key) return;
        if (!mc.player.isShiftKeyDown() && !mc.player.getMainHandItem().isEmpty()) return;

        Vec3 eye = mc.player.getEyePosition(), direction = mc.player.getLookAngle();
        FirstShipEntity selected = null;
        int action = 0;
        double nearest = Double.POSITIVE_INFINITY;
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(12D, 9D, 12D))) {
            if (ship.isRemoved() || (!ship.isNativeHull() && !(mc.player.isShiftKeyDown() && key))) continue;
            FirstShipHull.Point at = FirstShipHull.toLocal(mc.player.getX(), mc.player.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            if (mc.player.isShiftKeyDown() && key) {
                if (mc.player.distanceToSqr(ship) > 160D) continue;
                for (double step = .2D; step <= 5D; step += .2D) {
                    Vec3 probe = eye.add(direction.scale(step));
                    FirstShipHull.Point hit = FirstShipHull.toLocal(
                            probe.x, probe.z, ship.getX(), ship.getZ(), ship.getYRot());
                    if (FirstShipHull.withinHull(hit.x(), hit.z(), .05D)
                            && probe.y >= ship.holdY() - .3D && probe.y <= ship.deckY() + 2D
                            && step * step < nearest) {
                        selected = ship; action = FirstShipNetwork.PACK; nearest = step * step;
                        break;
                    }
                }
                continue;
            }
            if (Math.abs(mc.player.getY() - ship.deckY()) < .95D
                    && Math.abs(at.x()) < 1.8D
                    && Math.abs(at.z() - FirstShipHull.HELM_Z) < 1.8D) {
                FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, -4D,
                        ship.getX(), ship.getZ(), ship.getYRot());
                Vec3 aim = new Vec3(wheel.x(), ship.deckY() + 1.25D, wheel.z()).subtract(eye);
                if (aim.lengthSqr() > .1D && aim.lengthSqr() < 25D
                        && aim.normalize().dot(direction) > .68D && aim.lengthSqr() < nearest) {
                    selected = ship; action = FirstShipNetwork.HELM; nearest = aim.lengthSqr();
                }
            }
            if (action == FirstShipNetwork.HELM) continue;
            if (mc.player.getY() >= ship.deckY() - 1.1D) continue;
            double z = FirstShipHull.clamp(at.z(), -4.7D, 4.7D);
            double half = FirstShipHull.halfBeam(z);
            if (Math.abs(at.z()) > 6.25D || Math.abs(at.x()) > half + 1.7D
                    || (Math.abs(at.x()) < half - 1.1D && Math.abs(at.z()) < 4.8D)) continue;
            for (double step = .4D; step <= 5D; step += .25D) {
                Vec3 probe = eye.add(direction.scale(step));
                if (probe.y < ship.getY() || probe.y > ship.deckY() + .2D) continue;
                FirstShipHull.Point hit = FirstShipHull.toLocal(
                        probe.x, probe.z, ship.getX(), ship.getZ(), ship.getYRot());
                if (FirstShipHull.withinHull(hit.x(), hit.z(), .1D) && step * step < nearest) {
                    selected = ship; action = FirstShipNetwork.BOARD; nearest = step * step;
                    break;
                }
            }
        }
        if (selected == null) return;
        PacketDistributor.sendToServer(new FirstShipNetwork.Command(selected.getId(), action));
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private FirstShipInteractionClient() {}
}
