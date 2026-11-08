package org.slavicmyths.ships;

/**
 * Source-of-truth footprint of the FIRST two-deck wooden ship.
 * Pure Java 21: both the renderer and the deck-contact adapter consume it.
 *
 * Coordinates: +Z points to the bow, -Z to the stern, Y is measured
 * from the vessel's waterline. The hold floor remains above the water
 * surface so vanilla players cannot accidentally drown inside an air hull.
 */
public final class FirstShipHull {
    public static final int WIDTH = 7;
    public static final int LENGTH = 11;
    public static final double HOLD_FLOOR = 0.42D;
    public static final double MAIN_DECK = 3.22D;
    public static final double KEEL = -1.13D;
    public static final double HELM_Z = -3.62D;
    public static final double MAST_Z = -0.70D;
    public static final double LADDER_X = 0.45D;
    public static final double LADDER_Z = 2.00D;
    public static final double RAIL_HEIGHT = 0.92D;
    private static final double HALF_LENGTH = 5.50D;

    /**
     * Asymmetric traditional wooden hull: fine pointed bow (+Z), fuller stern
     * (-Z) and widest beam amidships. Same curve is used for visual planking,
     * walkable boundaries and water-placement checks.
     */
    public static double halfBeam(double z) {
        if (z < -HALF_LENGTH || z > HALF_LENGTH) return 0D;
        if (z < -2.70D) {
            double t = (-z - 2.70D) / 2.80D;
            return 3.35D - 1.55D * Math.pow(t, 1.70D);
        }
        if (z > 2.20D) {
            double t = (z - 2.20D) / 3.30D;
            return 3.35D - 2.62D * Math.pow(t, 1.55D);
        }
        return 3.35D;
    }

    /** The lower hold is genuinely narrower where the clinker planks slope in. */
    public static double holdBeam(double z) {
        return 0.78D * halfBeam(z);
    }

    public static boolean withinHull(double x, double z, double margin) {
        return Math.abs(z) <= HALF_LENGTH - margin
                && Math.abs(x) <= halfBeam(z) - margin;
    }

    /** Two-block hatch in the deck; the hold floor remains solid below it. */
    public static boolean hatch(double x, double z) {
        return x > -0.94D && x < 1.06D && z > 1.06D && z < 2.98D;
    }

    public static boolean mainDeckSupports(double x, double z) {
        return withinHull(x, z, 0.18D) && !hatch(x, z);
    }

    public static boolean holdSupports(double x, double z) {
        return Math.abs(z) <= HALF_LENGTH - 0.34D && Math.abs(x) <= holdBeam(z) - 0.34D;
    }

    public static boolean onLadder(double x, double z) {
        return Math.abs(x - LADDER_X) <= 0.52D && Math.abs(z - LADDER_Z) <= 0.52D;
    }

    /** Only the lower cabin needs virtual wall constraints; the upper deck is open. */
    public static Point confineHold(double x, double z) {
        double zz = clamp(z, -HALF_LENGTH + 0.46D, HALF_LENGTH - 0.46D);
        return new Point(clamp(x, -holdBeam(zz) + 0.44D, holdBeam(zz) - 0.44D), zz);
    }

    /** The weather deck is wider than the lower hold; never use hold bounds on it. */
    public static Point confineDeck(double x, double z) {
        double zz = clamp(z, -HALF_LENGTH + .46D, HALF_LENGTH - .46D);
        return new Point(clamp(x, -halfBeam(zz) + .46D, halfBeam(zz) - .46D), zz);
    }

    /** World-space change of basis; NEVER reapply the absolute ship pose to a walking player. */
    public static Point toWorld(double x, double z, double cx, double cz, double yawDegrees) {
        double a = Math.toRadians(yawDegrees);
        return new Point(cx + x * Math.cos(a) - z * Math.sin(a),
                cz + x * Math.sin(a) + z * Math.cos(a));
    }

    public static Point toLocal(double wx, double wz, double cx, double cz, double yawDegrees) {
        double a = Math.toRadians(yawDegrees);
        double dx = wx - cx, dz = wz - cz;
        return new Point(dx * Math.cos(a) + dz * Math.sin(a),
                -dx * Math.sin(a) + dz * Math.cos(a));
    }

    /**
     * From the OLD frame to the NEW frame, taking a player's personal
     * walking delta as part of the old-world position, not discarding it.
     */
    public static Point transfer(double wx, double wz, double oldX, double oldZ,
                                 double oldYaw, double newX, double newZ, double newYaw) {
        Point local = toLocal(wx, wz, oldX, oldZ, oldYaw);
        return toWorld(local.x(), local.z(), newX, newZ, newYaw);
    }

    public static double clamp(double n, double min, double max) {
        return Math.min(max, Math.max(min, n));
    }

    public record Point(double x, double z) {}
    private FirstShipHull() {}
}
