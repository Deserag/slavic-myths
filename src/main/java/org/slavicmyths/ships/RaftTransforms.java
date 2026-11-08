package org.slavicmyths.ships;

/**
 * One shared coordinate frame for 3D rendering, walking, equipment targets
 * and the future pilot/controller. Vanilla Boat renderer rotates the deck by
 * (180 - entityYaw), NOT by entityYaw. Mixing those rotations made players
 * appear to slide sideways or jump to opposite deck cells when turning.
 */
public final class RaftTransforms {
    public record Flat(double x, double z) {}
    public static Flat toWorld(double localX, double localZ, double entityYaw) {
        double angle = Math.toRadians(180.0D - entityYaw);
        double cosine = Math.cos(angle), sine = Math.sin(angle);
        return new Flat(localX * cosine + localZ * sine,
                -localX * sine + localZ * cosine);
    }
    public static Flat toLocal(double worldX, double worldZ, double entityYaw) {
        double angle = Math.toRadians(180.0D - entityYaw);
        double cosine = Math.cos(angle), sine = Math.sin(angle);
        return new Flat(worldX * cosine - worldZ * sine,
                worldX * sine + worldZ * cosine);
    }
    private RaftTransforms() {}
}
