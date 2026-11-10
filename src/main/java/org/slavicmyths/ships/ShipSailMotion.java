package org.slavicmyths.ships;

/**
 * Converts installed legacy mast and sailcloth equipment into bounded helm
 * authority. Physical top speed remains limited by ShipRigidMotion.
 */
public final class ShipSailMotion {
    private static final double TIER_III_THRUST = 2.5D;
    private static final double ROWING_FACTOR = 0.16D;

    public static double power(int rigLevel, int fabricQuality) {
        ShipRigging rig = new ShipRigging(rigLevel, fabricQuality);
        if (rig.level() == 0) return ROWING_FACTOR;
        double thrust = switch (rig.fabric()) {
            case 1 -> 1D;
            case 2 -> 1.7D;
            case 3 -> 2.5D;
            default -> throw new IllegalArgumentException("Unsupported sail quality");
        };
        return Math.min(1D, thrust * rig.rigMultiplier() / TIER_III_THRUST);
    }

    /** Steering continues to operate even while the sails are furled. */
    public static ShipRigidMotion.Control apply(ShipRigidMotion.Control input,
                                                 int rigLevel, int fabricQuality) {
        if (input == null) throw new IllegalArgumentException("Missing helm control");
        return new ShipRigidMotion.Control(input.throttle() * power(rigLevel, fabricQuality),
                input.rudder());
    }

    private ShipSailMotion() {}
}
