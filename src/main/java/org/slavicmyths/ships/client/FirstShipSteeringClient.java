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
 * Create-like helm controls: send four simultaneous key bits, including a
 * RELEASE packet on key-up. The server integrates speed and yaw each tick.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipSteeringClient {
    private static long lastSent = -100L;
    private static int lastShip = -1;
    private static int lastMask = -1;

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || !(mc.player.getVehicle() instanceof FirstShipEntity ship)
                || !ship.isNativeHull()) {
            lastShip = -1;
            lastMask = -1;
            return;
        }
        if (ship.getId() != lastShip) {
            lastShip = ship.getId();
            lastSent = -100L;
            lastMask = -1;
        }
        long now = mc.level.getGameTime();
        int mask = (mc.options.keyUp.isDown() ? 1 : 0)
                | (mc.options.keyDown.isDown() ? 2 : 0)
                | (mc.options.keyLeft.isDown() ? 4 : 0)
                | (mc.options.keyRight.isDown() ? 8 : 0);
        // Both opposite keys neutralize their axis, not the remaining axis.
        if ((mask & 3) == 3) mask &= ~3;
        if ((mask & 12) == 12) mask &= ~12;
        if (mask == lastMask && now - lastSent < 2L) return;
        PacketDistributor.sendToServer(new FirstShipNetwork.Command(ship.getId(),
                FirstShipNetwork.CONTINUOUS_INPUT | mask));
        lastMask = mask;
        lastSent = now;
    }

    private FirstShipSteeringClient() {}
}
