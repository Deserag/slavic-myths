package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipNetwork;

/**
 * Dedicated helm controls: ONLY while riding the real stern wheel.
 *
 * W/S advances/reverses the whole real-block ship one voxel.
 * A/D rotates the same footprint by ninety degrees. Leave helm with Shift.
 * The server rate-limits each request and rejects unsafe destinations.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipSteeringClient {
    private static long lastSent = -100L;
    private static int lastShip = -1;

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || !(mc.player.getVehicle() instanceof FirstShipEntity ship)
                || !ship.isNativeHull()) {
            lastShip = -1;
            return;
        }
        if (ship.getId() != lastShip) {
            lastShip = ship.getId();
            lastSent = -100L;
        }
        long now = mc.level.getGameTime();
        // Send one input while held per client tick; only the server decides
        // which steps are permitted. This preserves 10/9/8-tick fabric differences.
        if (now - lastSent < 1L) return;
        int action = 0;
        boolean left = mc.options.keyLeft.isDown(), right = mc.options.keyRight.isDown();
        boolean forward = mc.options.keyUp.isDown(), reverse = mc.options.keyDown.isDown();
        // A/D must not be silently ignored while W/S is held (vanilla boat
        // control commonly combines keys). Opposite keys neutralize an axis.
        if (left != right) action = left ? FirstShipNetwork.LEFT : FirstShipNetwork.RIGHT;
        else if (forward != reverse)
            action = forward ? FirstShipNetwork.FORWARD : FirstShipNetwork.BACKWARD;
        if (action == 0) return;
        PacketDistributor.sendToServer(new FirstShipNetwork.Command(ship.getId(), action));
        lastSent = now;
    }

    private FirstShipSteeringClient() {}
}
