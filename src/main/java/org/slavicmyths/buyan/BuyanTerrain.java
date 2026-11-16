package org.slavicmyths.buyan;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage one: the above-water geological foundation only. Hills, mountain
 * ranges, beaches and other landforms will be layered in a later stage.
 * All calculations are stable across chunk load order and world restarts.
 */
public final class BuyanTerrain {
    private BuyanTerrain() {}

    /** Shore distance varies continuously between these two limits. */
    public static final int MIN_RADIUS = 100;
    public static final int MAX_RADIUS = 350;
    public static final int DEFAULT_RADIUS = MAX_RADIUS;
    private static final int SHORE_SECTORS = 16;
    private static final double TWO_PI = Math.PI * 2.0;

    /**
     * No underwater shelf outside the coast in this FOUNDATION-only stage.
     * The original ocean terrain remains in place beyond the shoreline.
     */
    public static int margin(int radius) {
        return 0;
    }

    public enum Surface {
        SAND_BEACH, STONY_BEACH, MEADOW, ROCK
    }

    private static long mix(long h) {
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private static long hash(long seed, int x, int z, int y) {
        return mix(seed ^ ((long) x * 0x9E3779B97F4A7C15L)
            ^ ((long) z * 0xC2B2AE3D27D4EB4FL)
            ^ ((long) y * 0x165667B19E3779F9L));
    }

    private static double value(long seed, int x, int z, int y) {
        return (hash(seed, x, z, y) >>> 11) / (double) (1L << 53);
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double noise(long seed, double x, double z, double scale) {
        double gx = x / scale;
        double gz = z / scale;
        int ix = (int) Math.floor(gx);
        int iz = (int) Math.floor(gz);
        double u = smooth(gx - ix);
        double v = smooth(gz - iz);
        double a = value(seed, ix, iz, 0);
        double b = value(seed, ix + 1, iz, 0);
        double c = value(seed, ix, iz + 1, 0);
        double d = value(seed, ix + 1, iz + 1, 0);
        return ((a + (b - a) * u) * (1.0 - v)
            + (c + (d - c) * u) * v) * 2.0 - 1.0;
    }

    /** A deterministic control point for one of the 16 curved shoreline sectors. */
    private static double shorelineKnot(long seed, int cx, int cz, int index) {
        int minIndex = (int) Math.floorMod(hash(seed ^ 0xB11A0L, cx, cz, 0), SHORE_SECTORS);
        int wrapped = Math.floorMod(index, SHORE_SECTORS);
        if (wrapped == minIndex) return MIN_RADIUS;
        if (wrapped == (minIndex + SHORE_SECTORS / 2) % SHORE_SECTORS) return MAX_RADIUS;
        return 135.0 + 180.0 * value(seed ^ 0xB11A1L, cx, cz, wrapped);
    }

    /**
     * Smooth angular interpolation means different directions can be 100, 200
     * or 350 blocks from the center, without chunk-sized random jumps.
     */
    public static double shoreRadius(long seed, int cx, int cz, int x, int z) {
        double angle = Math.atan2((double) z - cz, (double) x - cx);
        if (angle < 0.0) angle += TWO_PI;
        double sector = angle * SHORE_SECTORS / TWO_PI;
        int index = (int) Math.floor(sector);
        double t = smooth(sector - index);
        double a = shorelineKnot(seed, cx, cz, index);
        double b = shorelineKnot(seed, cx, cz, index + 1);
        return a + (b - a) * t;
    }

    /** Raised, almost-level terrain: always above the actual ocean sea level. */
    public static int top(long seed, int cx, int cz, int radius, int x, int z, int seaLevel) {
        double dx = (double) x - cx;
        double dz = (double) z - cz;
        double fromEdge = shoreRadius(seed, cx, cz, x, z) - Math.hypot(dx, dz);
        if (fromEdge < 0.0) return Integer.MIN_VALUE;
        // A shallow, walkable sloping bank replaces abrupt seawalls.
        double interior = smooth(Math.min(1.0, fromEdge / 27.0));
        return seaLevel + 2 + (int) Math.round(6.0 * interior);
    }

    public static Surface surface(long seed, int cx, int cz, int x, int z, int sea,
                                  int height) {
        double dx = (double) x - cx;
        double dz = (double) z - cz;
        double edge = shoreRadius(seed, cx, cz, x, z) - Math.hypot(dx, dz);
        double patch = surfaceCover(seed, x, z);
        if (edge < 15.0) {
            return patch > 0.24 ? Surface.STONY_BEACH : Surface.SAND_BEACH;
        }
        if (patch > 0.38) return Surface.ROCK;
        return Surface.MEADOW;
    }

    /** Large coherent patches rather than random per-block speckling. */
    public static double surfaceCover(long seed, int x, int z) {
        return noise(seed ^ 0x4B7L, x, z, 42.0);
    }

    public static int surfaceRockRoll(long seed, int x, int z) {
        return (int) Math.clamp(50.0 + 110.0 * noise(seed ^ 0x621L, x, z, 48.0),
            0.0, 99.0);
    }

    public static BlockState block(long seed, int x, int y, int z, int height,
                                   Surface surface, double cover, int rockRoll) {
        int depth = height - y;
        int vein = depth > 5
            ? (int) (value(seed ^ 0x620L, Math.floorDiv(x, 16),
                Math.floorDiv(z, 16), Math.floorDiv(y, 12)) * 100.0)
            : rockRoll;
        if (depth == 0) {
            return switch (surface) {
                case SAND_BEACH -> Blocks.SAND.defaultBlockState();
                case STONY_BEACH -> cover > 0.58
                    ? Blocks.ANDESITE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
                case ROCK -> vein > 60
                    ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                case MEADOW -> Blocks.GRASS_BLOCK.defaultBlockState();
            };
        }
        if (depth <= 4) {
            return switch (surface) {
                case SAND_BEACH -> depth < 3
                    ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                case STONY_BEACH -> vein > 70
                    ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                case MEADOW -> Blocks.DIRT.defaultBlockState();
                case ROCK -> Blocks.STONE.defaultBlockState();
            };
        }
        if (y < 0) {
            if (vein < 72) return Blocks.DEEPSLATE.defaultBlockState();
            if (vein < 86) return Blocks.TUFF.defaultBlockState();
        }
        if (vein < 68) return Blocks.STONE.defaultBlockState();
        if (vein < 86) return Blocks.ANDESITE.defaultBlockState();
        if (vein < 95) return Blocks.TUFF.defaultBlockState();
        return Blocks.GRANITE.defaultBlockState();
    }
}
