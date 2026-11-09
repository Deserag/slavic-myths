package org.slavicmyths.ships;

/**
 * Independent fixed-tick steering model for ship contraptions.
 * Unlike the old native-block helm, neither position nor heading is rounded
 * to a BlockPos or a quarter-turn. Physics is independent of Minecraft APIs
 * so it can be regression-tested by plain javac and in a real GameTest world.
 *
 * +Z is the bow, +yaw rotates local forward towards -X (Minecraft heading).
 */
public final class FirstShipMotion {
    public static final double MAX_AHEAD = 0.19D;
    public static final double MAX_ASTERN = 0.075D;
    public static final double ACCEL = 0.013D;
    public static final double DRAG = 0.92D;
    public static final double TURN_RATE = 2.25D;
    public static final double TURN_ACCEL = 0.35D;
    public static final double YAW_DRAG = 0.73D;

    public record State(double x, double z, double yaw, double speed, double yawSpeed) {}
    public record Control(int throttle, int rudder) {
        public Control {
            if (throttle < -1 || throttle > 1 || rudder < -1 || rudder > 1)
                throw new IllegalArgumentException("Invalid ship controls");
        }
    }

    public static State advance(State state, Control controls, double propulsion) {
        double maxAhead = MAX_AHEAD * Math.max(0.5D, Math.min(2D, propulsion));
        double maxAstern = MAX_ASTERN * Math.max(0.5D, Math.min(2D, propulsion));
        double speed = controls.throttle() == 0
                ? state.speed() * DRAG
                : state.speed() + ACCEL * controls.throttle() * propulsion;
        speed = Math.max(-maxAstern, Math.min(maxAhead, speed));
        if (Math.abs(speed) < .0005D && controls.throttle() == 0) speed = 0D;

        // Wheel can rotate a stationary ship gently, without instantly
        // snapping its complete 7x11 footprint by ninety degrees.
        double rudderRate = controls.rudder() * TURN_RATE
                * (0.35D + 0.65D * Math.min(1D, Math.abs(speed) / MAX_AHEAD));
        double yawSpeed = controls.rudder() == 0 ? state.yawSpeed() * YAW_DRAG
                : state.yawSpeed() + Math.max(-TURN_ACCEL, Math.min(TURN_ACCEL,
                    rudderRate - state.yawSpeed()));
        yawSpeed = Math.max(-TURN_RATE, Math.min(TURN_RATE, yawSpeed));
        if (Math.abs(yawSpeed) < .012D && controls.rudder() == 0) yawSpeed = 0D;
        double yaw = Math.IEEEremainder(state.yaw() + yawSpeed, 360D);
        double radians = Math.toRadians(yaw);
        return new State(state.x() - Math.sin(radians) * speed,
                state.z() + Math.cos(radians) * speed,
                yaw, speed, yawSpeed);
    }

    public static Control fromMask(int mask) {
        int forward = (mask & 1) != 0 ? 1 : 0;
        int backward = (mask & 2) != 0 ? 1 : 0;
        int left = (mask & 4) != 0 ? 1 : 0;
        int right = (mask & 8) != 0 ? 1 : 0;
        return new Control(forward - backward, right - left);
    }

    private FirstShipMotion() {}
}
