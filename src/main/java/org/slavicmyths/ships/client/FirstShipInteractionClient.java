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

/**
 * The helm and sides are visible geometry outside the small Boat hitbox.
 * Ray-based client input selects a nearby painted ship element; every command
 * is revalidated against the player's real position by the SERVER.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipInteractionClient {
    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || mc.player.isPassenger() || mc.player.isShiftKeyDown()
                || !mc.player.getMainHandItem().isEmpty()) return;
        Vec3 eye = mc.player.getEyePosition(), direction = mc.player.getLookAngle();
        FirstShipEntity selected = null;
        int action = 0;
        double nearest = Double.POSITIVE_INFINITY;
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(11D, 5D, 11D))) {
            if (ship.isRemoved()) continue;
            FirstShipHull.Point at = FirstShipHull.toLocal(mc.player.getX(), mc.player.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            if (Math.abs(mc.player.getY() - ship.deckY()) < .9D
                    && Math.abs(at.x()) < 1.65D
                    && Math.abs(at.z() - FirstShipHull.HELM_Z) < 1.7D) {
                FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, FirstShipHull.HELM_Z,
                        ship.getX(), ship.getZ(), ship.getYRot());
                Vec3 aim = new Vec3(wheel.x(), ship.deckY() + 1.5D, wheel.z()).subtract(eye);
                if (aim.lengthSqr() > .09D && aim.lengthSqr() < 25D
                        && aim.normalize().dot(direction) >= .76D && aim.lengthSqr() < nearest) {
                    selected = ship;
                    action = FirstShipNetwork.HELM;
                    nearest = aim.lengthSqr();
                }
            }
            if (action == FirstShipNetwork.HELM) continue;
            if (mc.player.getY() >= ship.deckY() - 1.1D
                    || ship.getDeltaMovement().horizontalDistanceSqr() > .0025D) continue;
            double z = FirstShipHull.clamp(at.z(), -4.7D, 4.7D);
            double half = FirstShipHull.halfBeam(z);
            if (Math.abs(at.z()) > 6.25D || Math.abs(at.x()) > half + 1.6D
                    || (Math.abs(at.x()) < half - 1.1D && Math.abs(at.z()) < 4.9D)) continue;
            // The line of sight must cross the actual outside hull shape,
            // not just stand near the unclickable, large entity.
            for (double step = .5D; step <= 5D; step += .20D) {
                Vec3 hit = eye.add(direction.scale(step));
                if (hit.y < ship.getY() - 1.2D || hit.y > ship.deckY() + .25D) continue;
                FirstShipHull.Point target = FirstShipHull.toLocal(
                        hit.x, hit.z, ship.getX(), ship.getZ(), ship.getYRot());
                if (FirstShipHull.withinHull(target.x(), target.z(), .14D)) {
                    if (step * step < nearest) {
                        selected = ship;
                        action = FirstShipNetwork.BOARD;
                        nearest = step * step;
                    }
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
