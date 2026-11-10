package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipNetwork;

/**
 * The steering wheel is a custom rendered mesh above a vanilla fence, not a
 * block model with its own full clickable volume. Check exactly that mesh
 * when using right click and have the SERVER validate the helm request.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class ShipHelmClientInteraction {
    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || mc.player.isPassenger() || mc.player.isSecondaryUseActive()) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle();
        FirstShipEntity selected = null;
        double best = 25D;
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(30D, 16D, 30D))) {
            if (ship.isRemoved() || ship.isMoving() || !ship.isNativeHull()
                    || ship.modularConfiguration() == null || ship.isVehicle()) continue;
            BlockPos wheel = FirstShipBlockHull.world(ship.blockAnchor(), ship.quarterTurns(),
                    0, (int)ship.helmSeatHeight(), (int)ship.helmWheelZ());
            Vec3 target = new Vec3(wheel.getX() + .5D,
                    wheel.getY() + 1.78D, wheel.getZ() + .5D);
            Vec3 toWheel = target.subtract(eye);
            double distance = toWheel.lengthSqr();
            if (distance < .12D || distance > best) continue;
            double alignment = toWheel.normalize().dot(look);
            if (alignment < .91D) continue;
            selected = ship;
            best = distance;
        }
        if (selected != null) {
            PacketDistributor.sendToServer(new FirstShipNetwork.Command(
                    selected.getId(), FirstShipNetwork.HELM));
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    private ShipHelmClientInteraction() {}
}
