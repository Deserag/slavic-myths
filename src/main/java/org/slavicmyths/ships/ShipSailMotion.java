package org.slavicmyths.ships;

/**
 * Converts installed legacy mast and sailcloth equipment into bounded helm
 * authority. Physical top speed remains limited by ShipRigidMotion.
 */
public final class ShipSailMotion {
    private static final double MAX_LEGACY_THRUST = 4.5D;
    private static final double MIN_SAIL_POWER = 0.80D;
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
        double effectiveThrust = thrust * rig.rigMultiplier();
        return MIN_SAIL_POWER + (1D - MIN_SAIL_POWER)
                * Math.min(1D, (effectiveThrust - 1D) / (MAX_LEGACY_THRUST - 1D));
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
