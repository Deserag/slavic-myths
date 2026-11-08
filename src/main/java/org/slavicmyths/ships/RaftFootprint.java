package org.slavicmyths.ships;

/**
 * Geometry shared by water placement and future ship-local collision checks.
 * The old placement check used world-axis-aligned 3x5/5x7 rectangles even when
 * the ship was rotated, which could allow the bow to clip a shore.
 */
public final class RaftFootprint {
    public record Point(double x, double z) {}

    public static Point rotate(double x, double z, double yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double cos = Math.cos(yaw), sin = Math.sin(yaw);
        return new Point(x * cos - z * sin, x * sin + z * cos);
    }

    public static Point sample(int width, int length, int subX, int subZ, double yawDegrees) {
        if (width < 3 || length < 3 || width > 7 || length > 7 ||
                subX < 0 || subZ < 0 || subX >= width * 2 || subZ >= length * 2)
            throw new IllegalArgumentException("Invalid raft footprint sample");
        double x = (subX + .5D) / 2D - width / 2.0D;
        double z = (subZ + .5D) / 2D - length / 2.0D;
        return rotate(x, z, yawDegrees);
    }

    private RaftFootprint() {}
}
