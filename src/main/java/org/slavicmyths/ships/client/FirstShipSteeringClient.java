package org.slavicmyths.ships.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;

/**
 * Dedicated helm controls: ONLY while riding the real stern wheel.
 *
 * Newly assembled smooth ships combine W/S throttle and A/D continuous rudder
 * simultaneously. Older world-block ships keep the original safe cardinal
 * fallback until repacked. Leave the helm with Shift.
 * The server rate-limits each request and rejects unsafe destinations.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipSteeringClient {
    private static long lastSent = -100L;
    private static int lastShip = -1;

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            lastShip = -1;
            return;
        }
        if (!(mc.player.getVehicle() instanceof FirstShipEntity ship)
                || !ship.isNativeHull()) {
            lastShip = -1;
            return;
        }
        if (ship.isMoving()) {
            // Player.tick may replace the passenger's previous coordinates
            // after the hull has advanced. Render the captain from the SAME
            // previous rigid transform as the hull and wheel.
            FirstShipHull.Point previousSeat = FirstShipHull.toWorld(0D,
                    ship.helmSeatZ(), ship.xo, ship.zo, ship.yRotO);
            mc.player.xo = previousSeat.x();
            mc.player.yo = ship.yo + ship.helmSeatHeight() + .08D;
            mc.player.zo = previousSeat.z();
        }
        if (mc.screen != null) return;
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
        ship.setClientThrottleDirection(forward == reverse ? 0 : forward ? 1 : -1);
        // A/D must not be silently ignored while W/S is held (vanilla boat
        // control commonly combines keys). Opposite keys neutralize an axis.
        if (ship.isMoving() || ship.geometryVersion() >= 5) {
            int throttle = forward == reverse ? 0 : forward ? 1 : -1;
            int turn = left == right ? 0 : left ? -1 : 1;
            if (throttle > 0 && turn < 0) action = FirstShipNetwork.FORWARD_LEFT;
            else if (throttle > 0 && turn > 0) action = FirstShipNetwork.FORWARD_RIGHT;
            else if (throttle < 0 && turn < 0) action = FirstShipNetwork.BACKWARD_LEFT;
            else if (throttle < 0 && turn > 0) action = FirstShipNetwork.BACKWARD_RIGHT;
            else if (throttle > 0) action = FirstShipNetwork.FORWARD;
            else if (throttle < 0) action = FirstShipNetwork.BACKWARD;
            else if (turn < 0) action = FirstShipNetwork.LEFT;
            else if (turn > 0) action = FirstShipNetwork.RIGHT;
            else action = FirstShipNetwork.HELM_IDLE;
            if (action == FirstShipNetwork.HELM_IDLE && !ship.isMoving()) return;
        } else {
            if (left != right) action = left ? FirstShipNetwork.LEFT : FirstShipNetwork.RIGHT;
            else if (forward != reverse)
                action = forward ? FirstShipNetwork.FORWARD : FirstShipNetwork.BACKWARD;
            if (action == 0) return;
        }
        PacketDistributor.sendToServer(new FirstShipNetwork.Command(ship.getId(), action));
        lastSent = now;
    }

    private FirstShipSteeringClient() {}
}
