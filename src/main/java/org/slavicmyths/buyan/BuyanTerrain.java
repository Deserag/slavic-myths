package org.slavicmyths.buyan;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deterministic, natural terrain for Buyan. This is the empty island foundation,
 * not the later forest, buildings, mobs or gameplay content.
 */
public final class BuyanTerrain {
    private BuyanTerrain() {}

    public static final int MIN_RADIUS = 300;
    public static final int DEFAULT_RADIUS = 350;
    public static final int MAX_RADIUS = 400;

    /** A narrow underwater transition, not another island-sized square. */
    public static int margin(int radius) {
        return Math.clamp(radius / 4, 72, 96);
    }

    public enum Surface {
        UNDERWATER, SAND_BEACH, STONY_BEACH, MEADOW, HEATH, ROCK, MOUNTAIN
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }

    private static double fade(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double angleDistance(double a, double b) {
        return Math.atan2(Math.sin(a - b), Math.cos(a - b));
    }

    private static long hash(long seed, int x, int z, int y) {
        long h = seed ^ ((long) x * 0x9E3779B97F4A7C15L)
            ^ ((long) z * 0xC2B2AE3D27D4EB4FL)
            ^ ((long) y * 0x165667B19E3779F9L);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private static double lattice(long seed, int x, int z) {
        return ((hash(seed, x, z, 0) >>> 11) / (double) (1L << 53)) * 2.0 - 1.0;
    }

    /** Smooth low-frequency patches with no seams between chunks. */
    private static double noise(long seed, double x, double z, double scale) {
        double gx = x / scale;
        double gz = z / scale;
        int ix = (int) Math.floor(gx);
        int iz = (int) Math.floor(gz);
        double u = fade(gx - ix);
        double v = fade(gz - iz);
        double a = lattice(seed, ix, iz);
        double b = lattice(seed, ix + 1, iz);
        double c = lattice(seed, ix, iz + 1);
        double d = lattice(seed, ix + 1, iz + 1);
        return (a + (b - a) * u) * (1.0 - v)
            + (c + (d - c) * u) * v;
    }

    private static double bump(double dx, double dz, double x, double z, double rx, double rz) {
        double u = (dx - x) / rx;
        double v = (dz - z) / rz;
        return Math.exp(-2.1 * (u * u + v * v));
    }

    public static int top(long seed, int cx, int cz, int radius, int x, int z,
                          int seaLevel, int oceanFloor) {
        double dx = (double) x - cx;
        double dz = (double) z - cz;
        double distance = Math.hypot(dx, dz);
        double angle = Math.atan2(dz, dx);
        double phase = (seed & 8191L) * 0.00018;

        // Promontories, two sheltered coves, and irregular erosion at different scales.
        double shore = radius * (0.94
            + 0.092 * Math.sin(3.0 * angle + phase)
            + 0.056 * Math.cos(5.0 * angle - 0.6)
            + 0.031 * Math.sin(8.0 * angle + 1.2 + phase)
            + 0.036 * noise(seed ^ 0x153L, dx, dz, 83.0)
            + 0.012 * noise(seed ^ 0x162L, dx, dz, 27.0));
        shore -= radius * 0.115 * Math.exp(-Math.pow(angleDistance(angle, -0.80) / 0.27, 2));
        shore -= radius * 0.080 * Math.exp(-Math.pow(angleDistance(angle, 1.75) / 0.23, 2));

        double outside = distance - shore;
        if (outside >= margin(radius)) return Integer.MIN_VALUE;

        double broad = noise(seed ^ 0xB91L, dx, dz, 86.0);
        double medium = noise(seed ^ 0xC73L, dx, dz, 31.0);
        double fine = noise(seed ^ 0xD34L, dx, dz, 11.0);

        if (outside > 0.0) {
            // Anchor the outside of the skirt to the *actual* vanilla seabed,
            // so it ends naturally instead of as a submerged square wall.
            double t = clamp(outside / margin(radius), 0.0, 1.0);
            double blend = fade(t);
            double coastalShelf = seaLevel - 3.0 + 2.0 * broad + fine;
            return (int) Math.round(coastalShelf * (1.0 - blend)
                + oceanFloor * blend);
        }

        double inward = clamp(1.0 - distance / Math.max(1.0, shore), 0.0, 1.0);
        double northwestCliffs = Math.exp(-Math.pow(angleDistance(angle, 2.55) / 0.69, 2));
        double eastRocks = Math.exp(-Math.pow(angleDistance(angle, 0.40) / 0.48, 2));
        double southCove = Math.exp(-Math.pow(angleDistance(angle, -0.85) / 0.68, 2));

        // Asymmetric uplands: a central massif, a ridge and two independent hills.
        double highPeak = 55.0 * bump(dx, dz, radius * 0.11, -radius * 0.08,
            radius * 0.27, radius * 0.31);
        double mountainRidge = 38.0 * bump(dx, dz, -radius * 0.22, radius * 0.18,
            radius * 0.24, radius * 0.37);
        double westernHill = 23.0 * bump(dx, dz, -radius * 0.46, -radius * 0.25,
            radius * 0.20, radius * 0.23);
        double easternHill = 20.0 * bump(dx, dz, radius * 0.47, radius * 0.33,
            radius * 0.25, radius * 0.23);

        double cliffs = 25.0 * northwestCliffs
            * Math.exp(-Math.pow((inward - 0.105) / 0.19, 2))
            + 12.0 * eastRocks * Math.exp(-Math.pow((inward - 0.13) / 0.20, 2));
        double terrain = 2.0 + 18.0 * Math.pow(inward, 0.86)
            + highPeak + mountainRidge + westernHill + easternHill + cliffs
            + inward * (11.0 * broad + 5.5 * medium + 2.3 * fine)
            - 4.7 * southCove * Math.exp(-Math.pow(inward / 0.20, 2));
        return seaLevel + Math.max(1, (int) Math.round(terrain));
    }

    /** Classification is per column, avoiding costly noise for every vertical block. */
    public static Surface surface(long seed, int cx, int cz, int radius, int x, int z,
                                  int sea, int height, int oceanFloor) {
        if (height <= sea - 1) return Surface.UNDERWATER;
        double dx = (double) x - cx;
        double dz = (double) z - cz;
        double angle = Math.atan2(dz, dx);
        double rockySector = Math.max(
            Math.exp(-Math.pow(angleDistance(angle, 2.55) / 0.77, 2)),
            Math.exp(-Math.pow(angleDistance(angle, 0.40) / 0.53, 2)));
        int ex = top(seed, cx, cz, radius, x + 4, z, sea, oceanFloor);
        int ez = top(seed, cx, cz, radius, x, z + 4, sea, oceanFloor);
        double slope = Math.max(Math.abs(height - ex), Math.abs(height - ez)) / 4.0;
        double patch = noise(seed ^ 0xF75L, dx, dz, 37.0);

        if (height <= sea + 9) {
            return rockySector > 0.38 || slope > 0.74 || patch > 0.48
                ? Surface.STONY_BEACH : Surface.SAND_BEACH;
        }
        if (height >= sea + 63 || (height >= sea + 43 && slope > 0.65))
            return Surface.MOUNTAIN;
        if (slope > 0.90 || (rockySector > 0.53 && slope > 0.45)
            || (height > sea + 39 && patch > 0.46))
            return Surface.ROCK;
        if (patch < -0.26 || (height > sea + 30 && slope > 0.43))
            return Surface.HEATH;
        return Surface.MEADOW;
    }

    public static double surfaceCover(long seed, int x, int z) {
        return noise(seed ^ 0x4B7L, x, z, 21.0);
    }

    public static int surfaceRockRoll(long seed, int x, int z) {
        return (int) Math.round(clamp(50.0
            + 105.0 * noise(seed ^ 0x621L, x, z, 27.0), 0.0, 99.0));
    }

    private static BlockState rock(int roll, boolean exposed) {
        if (roll < 58) return Blocks.STONE.defaultBlockState();
        if (roll < 79) return Blocks.ANDESITE.defaultBlockState();
        if (roll < 90) return Blocks.TUFF.defaultBlockState();
        if (roll < 96) return Blocks.GRANITE.defaultBlockState();
        return exposed ? Blocks.CALCITE.defaultBlockState()
            : Blocks.DIORITE.defaultBlockState();
    }

    public static BlockState block(long seed, int x, int y, int z, int height,
                                   Surface surface, double cover, int surfaceRockRoll) {
        int depth = height - y;
        // Surface exposures blend smoothly; deep geology forms larger veins.
        int rockRoll = depth <= 5 ? surfaceRockRoll
            : (int) Math.floorMod(hash(seed ^ 0x621L,
                x >> 4, z >> 4, y >> 3), 100);

        if (depth == 0) {
            switch (surface) {
                case UNDERWATER:
                    if (cover > 0.25) return Blocks.GRAVEL.defaultBlockState();
                    if (cover < -0.32) return Blocks.CLAY.defaultBlockState();
                    return cover < 0.10 ? Blocks.SAND.defaultBlockState()
                        : Blocks.STONE.defaultBlockState();
                case SAND_BEACH:
                    return cover < -0.25 ? Blocks.GRAVEL.defaultBlockState()
                        : cover > 0.42 ? Blocks.COARSE_DIRT.defaultBlockState()
                        : Blocks.SAND.defaultBlockState();
                case STONY_BEACH:
                    if (cover > 0.10) return Blocks.GRAVEL.defaultBlockState();
                    return rock(rockRoll, true);
                case ROCK:
                case MOUNTAIN:
                    return rock(rockRoll, true);
                case HEATH:
                    if (cover > 0.29) return Blocks.COARSE_DIRT.defaultBlockState();
                    if (cover < -0.23) return rock(rockRoll, true);
                    return Blocks.GRASS_BLOCK.defaultBlockState();
                case MEADOW:
                default:
                    if (cover < -0.42) return Blocks.COARSE_DIRT.defaultBlockState();
                    if (cover > 0.42) return Blocks.MOSS_BLOCK.defaultBlockState();
                    return Blocks.GRASS_BLOCK.defaultBlockState();
            }
        }

        if (depth <= 5) {
            switch (surface) {
                case SAND_BEACH:
                    return depth <= 3 ? Blocks.SAND.defaultBlockState()
                        : Blocks.SANDSTONE.defaultBlockState();
                case STONY_BEACH:
                    return cover > 0.16 && depth < 3
                        ? Blocks.GRAVEL.defaultBlockState() : rock(rockRoll, true);
                case MEADOW:
                case HEATH:
                    return cover < -0.20 ? Blocks.COARSE_DIRT.defaultBlockState()
                        : Blocks.DIRT.defaultBlockState();
                default:
                    return rock(rockRoll, surface == Surface.ROCK || surface == Surface.MOUNTAIN);
            }
        }

        if (y < 0) {
            if (rockRoll < 68) return Blocks.DEEPSLATE.defaultBlockState();
            if (rockRoll < 82) return Blocks.TUFF.defaultBlockState();
            return Blocks.STONE.defaultBlockState();
        }
        return rock(rockRoll, false);
    }
}
