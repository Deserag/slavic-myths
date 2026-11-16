package org.slavicmyths.buyan;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deterministic Buyan foundation terrain only. No trees, temples, loot or NPCs.
 * All geometry is in world X/Z; radius is the nominal horizontal coastline radius.
 */
public final class BuyanTerrain {
    private BuyanTerrain() {}

    public static final int MIN_RADIUS = 500;
    public static final int MAX_RADIUS = 1200;

    public static int margin(int radius) {
        return Math.max(160, radius / 3);
    }

    private static double angleDistance(double a, double b) {
        return Math.atan2(Math.sin(a - b), Math.cos(a - b));
    }

    private static double ridge(long seed, double x, double z) {
        double phase = (seed & 65535L) * 0.000031;
        return Math.sin(x * 0.012 + phase) * Math.cos(z * 0.011 - phase)
            + 0.45 * Math.sin(x * 0.027 + z * 0.018 + phase * 2.0)
            + 0.20 * Math.cos(z * 0.052 - x * 0.022 - phase);
    }

    public static int top(long seed, int cx, int cz, int radius, int x, int z, int seaLevel) {
        double dx = (double) x - cx;
        double dz = (double) z - cz;
        double distance = Math.hypot(dx, dz);
        double angle = Math.atan2(dz, dx);
        double phase = (seed & 8191L) * 0.00018;

        // A readable silhouette with headlands and two sheltered coves.
        double shore = radius * (0.91
            + 0.085 * Math.sin(angle * 3.0 + phase)
            + 0.065 * Math.cos(angle * 5.0 - 0.6)
            + 0.035 * Math.sin(angle * 8.0 + 1.2 + phase));
        shore -= radius * 0.13 * Math.exp(-Math.pow(angleDistance(angle, -0.80) / 0.25, 2));
        shore -= radius * 0.10 * Math.exp(-Math.pow(angleDistance(angle, 1.75) / 0.21, 2));

        double distanceFromCoast = distance - shore;
        if (distanceFromCoast > margin(radius)) {
            return Integer.MIN_VALUE;
        }

        double detail = ridge(seed, dx, dz);
        if (distanceFromCoast > 0.0) {
            double t = Math.min(1.0, distanceFromCoast / margin(radius));
            // An underwater shelf that gradually meets the existing seabed.
            return seaLevel - 3 - (int) Math.round(t * t * 46.0 + detail * (1 - t) * 2);
        }

        double inward = Math.max(0.0, 1.0 - distance / shore);
        double northwestCliff = Math.exp(-Math.pow(angleDistance(angle, 2.55) / 0.68, 2));
        double southBeach = Math.exp(-Math.pow(angleDistance(angle, -1.1) / 0.72, 2));
        double centralHill = 52.0 * Math.exp(-((dx - radius * 0.08) * (dx - radius * 0.08)
            + (dz + radius * 0.05) * (dz + radius * 0.05))
            / (radius * radius * 0.085));
        double westHill = 17.0 * Math.exp(-((dx + radius * 0.39) * (dx + radius * 0.39)
            + (dz - radius * 0.16) * (dz - radius * 0.16))
            / (radius * radius * 0.055));
        double eastHill = 12.0 * Math.exp(-((dx - radius * 0.43) * (dx - radius * 0.43)
            + (dz - radius * 0.26) * (dz - radius * 0.26))
            / (radius * radius * 0.068));
        double land = 3.0 + 31.0 * Math.pow(inward, 1.13) + centralHill + westHill + eastHill
            + 20.0 * northwestCliff * Math.exp(-Math.pow((inward - 0.11) / 0.22, 2))
            - 6.0 * southBeach * Math.exp(-Math.pow(inward / 0.23, 2))
            + detail * (2.1 + 3.0 * inward);
        return seaLevel + Math.max(2, (int) Math.round(land));
    }

    private static long mix(long seed, int x, int z, int y) {
        long h = seed ^ ((long) x * 0x9E3779B97F4A7C15L)
            ^ ((long) z * 0xC2B2AE3D27D4EB4FL)
            ^ ((long) y * 0x165667B19E3779F9L);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    public static BlockState block(long seed, int x, int y, int z, int surface, int seaLevel) {
        int roll = (int) Math.floorMod(mix(seed, x >> 2, z >> 2, y >> 3), 100);
        if (y == surface) {
            if (surface <= seaLevel + 2) {
                return roll < 65 ? Blocks.SAND.defaultBlockState()
                    : roll < 88 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState();
            }
            if (surface <= seaLevel + 7) {
                return roll < 67 ? Blocks.SAND.defaultBlockState()
                    : Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return roll < 11 ? Blocks.STONE.defaultBlockState()
                : Blocks.GRASS_BLOCK.defaultBlockState();
        }
        if (y > surface - 4 && surface > seaLevel + 7) {
            return roll < 82 ? Blocks.DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
        }
        if (y < -8 && roll < 18) return Blocks.DEEPSLATE.defaultBlockState();
        if (roll < 9) return Blocks.ANDESITE.defaultBlockState();
        if (roll < 15) return Blocks.DIORITE.defaultBlockState();
        if (roll < 22) return Blocks.GRANITE.defaultBlockState();
        if (roll < 27) return Blocks.TUFF.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }
}
