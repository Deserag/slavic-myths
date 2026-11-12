package org.slavicmyths.ships;

/**
 * Pure-Java coordinate plan of the full-block two-deck ship.
 * Actual collision belongs to vanilla world planks and ladders, NOT a tick adapter.
 *
 * Coordinates: +Z points to the bow, -Z to the stern, Y is measured
 * from the vessel's waterline. The hold floor remains above the water
 * surface so vanilla players cannot accidentally drown inside an air hull.
 */
public final class FirstShipHull {
    public static final int WIDTH = 7;
    public static final int LENGTH = 11;
    public static final double HOLD_FLOOR = 1.00D;
    public static final double MAIN_DECK = 5.00D;
    public static final double KEEL = 0.00D;
    public static final double HELM_Z = -3.00D;
    public static final double MAST_Z = -1.00D;
    public static final double LADDER_X = -1.00D;
    public static final double LADDER_Z = 2.00D;
    public static final double RAIL_HEIGHT = 0.92D;
    private static final double HALF_LENGTH = 5.50D;

    /**
     * Actual whole-block row footprint of the ship; every row is built from
     * full vanilla blocks in FirstShipBlockHull (not rendered fake cubes).
     * +Z is the bow ending at exactly ONE block (z=5, x=0).
     */
    public static double halfBeam(double z) {
        int row = (int)Math.round(z);
        if (z < -5.5D || z > 5.5D) return 0D;
        int[] widths = {1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0};
        return widths[Math.max(0, Math.min(10, row + 5))] + .49D;
    }

    /**
     * V11 only. The chine widens over two real voxel layers while the
     * unchanged 7x11 waterline/deck outline remains the upper boundary.
     * Earlier geometries continue using halfBeam/halfWidth unchanged.
     */
    public static int halfWidthV11(int z, int y) {
        if (z < -5 || z > 5) return -1;
        int deckBeam = (int)Math.floor(halfBeam(z));
        int inset = y <= 0 ? 2 : y == 1 ? 1 : 0;
        return Math.max(0, deckBeam - inset);
    }

    /**
     * The outer face of a narrowing bow/stern row must be solid at the chine.
     * Cutting that face away leaves a visible one-block opening below the
     * upper hull even though the center section still has the right beam.
     * This applies only to V11; legacy V10 geometry remains unchanged.
     */
    public static boolean exposedEndV11(int x, int z) {
        if (z >= 2 && z < 5)
            return Math.abs(x) > (int)Math.floor(halfBeam(z + 1));
        if (z <= -3 && z > -5)
            return Math.abs(x) > (int)Math.floor(halfBeam(z - 1));
        return false;
    }

    public static double holdBeam(double z) {
        return Math.max(.45D, halfBeam(z) - .98D);
    }

    public static boolean withinHull(double x, double z, double margin) {
        return Math.abs(z) <= HALF_LENGTH - margin
                && Math.abs(x) <= halfBeam(z) - margin;
    }

    /** Two-block hatch in the deck; the hold floor remains solid below it. */
    public static boolean hatch(double x, double z) {
        return x > -1.49D && x < .49D && z > .50D && z < 2.50D;
    }

    public static boolean mainDeckSupports(double x, double z) {
        return withinHull(x, z, 0.18D) && !hatch(x, z);
    }

    public static boolean holdSupports(double x, double z) {
        return Math.abs(z) <= HALF_LENGTH - 0.34D && Math.abs(x) <= holdBeam(z) - 0.34D;
    }

    public static boolean onLadder(double x, double z) {
        return Math.abs(x - LADDER_X) <= 0.49D && Math.abs(z - LADDER_Z) <= 0.49D;
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
