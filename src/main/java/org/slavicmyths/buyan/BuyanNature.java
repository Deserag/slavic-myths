package org.slavicmyths.buyan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage two is a deterministic overlay on a completed stage-one foundation.
 * No block decisions depend on chunk traversal, time or mutable Random state.
 */
public final class BuyanNature {
    public enum TreeKind { OAK, BIRCH, GOLD_BIRCH, SPRUCE }

    private static final double TWO_PI = Math.PI * 2.0;
    private static final long HEIGHT_SALT = 0xB22A0101L;
    private static final long FOREST_SALT = 0xB22A0202L;

    private BuyanNature() {}

    private static double clamp01(double n) {
        return Math.clamp(n, 0.0, 1.0);
    }

    private static double smooth(double n) {
        double t = clamp01(n);
        return t * t * (3.0 - 2.0 * t);
    }

    private static double blend(double a, double b, double weight) {
        return a + (b - a) * clamp01(weight);
    }

    private static double noise(long seed, double x, double z, double scale) {
        return BuyanTerrain.noise(seed, x, z, scale);
    }

    private static double bayAngle(long seed, int cx, int cz) {
        int sector = (int) Math.floorMod(BuyanTerrain.hash(seed ^ 0xB11A0L, cx, cz, 0), 16L);
        return sector * TWO_PI / 16.0;
    }

    public static double shoreDistance(long seed, int cx, int cz, int x, int z) {
        return BuyanTerrain.shoreRadius(seed, cx, cz, x, z)
            - Math.hypot((double) x - cx, (double) z - cz);
    }

    /** Rotated, irregular, nearly level 80-95 block clearing at the center. */
    public static double clearingMetric(long seed, int cx, int cz, int x, int z) {
        double angle = bayAngle(seed, cx, cz) + 0.31;
        double dx = x - (double) cx;
        double dz = z - (double) cz;
        double u = (dx * Math.cos(angle) + dz * Math.sin(angle)) / 46.0;
        double v = (-dx * Math.sin(angle) + dz * Math.cos(angle)) / 39.0;
        return Math.hypot(u, v) + 0.045 * noise(seed ^ 0xC1E4L, x, z, 31.0);
    }

    private static double path(long seed, int cx, int cz, int x, int z, int index) {
        double angle = bayAngle(seed, cx, cz)
            + switch (index) {
                case 0 -> 0.0;          // natural bay in the narrow shore
                case 1 -> 2.11;         // light mixed forest
                default -> Math.PI + 0.36; // rocky flank of the wider side
            };
        double dx = x - (double) cx;
        double dz = z - (double) cz;
        double radial = dx * Math.cos(angle) + dz * Math.sin(angle);
        double side = -dx * Math.sin(angle) + dz * Math.cos(angle);
        double shore = BuyanTerrain.shoreRadius(seed, cx, cz,
            cx + (int) Math.round(Math.cos(angle) * 250.0),
            cz + (int) Math.round(Math.sin(angle) * 250.0));
        double end = Math.max(51.0, shore - 5.0);
        if (radial < 32.0 || radial > end) return 0.0;
        double t = clamp01((radial - 32.0) / Math.max(1.0, end - 32.0));
        double bend = Math.sin(t * 5.1 + index * 1.7) * Math.sin(Math.PI * t)
            * (index == 2 ? 5.0 : 3.3);
        double width = index == 0 ? 20.0 : index == 1 ? 12.0 : 9.0;
        double transverse = Math.abs(side - bend);
        double feather = smooth((width * 0.5 + 5.0 - transverse) / 5.0);
        double endpoints = smooth((radial - 32.0) / 9.0)
            * smooth((end - radial) / 16.0);
        return feather * endpoints;
    }

    /** Route widths and directions adapt to the seed and narrow/wide coasts. */
    public static double passage(long seed, int cx, int cz, int x, int z) {
        return Math.max(path(seed, cx, cz, x, z, 0),
            Math.max(path(seed, cx, cz, x, z, 1),
                     path(seed, cx, cz, x, z, 2)));
    }

    /** Final absolute height relative to sea, never an addition to old terrain. */
    public static int top(long seed, int cx, int cz, int radius,
                          int x, int z, int sea) {
        if (BuyanTerrain.top(seed, cx, cz, radius, x, z, sea)
            == Integer.MIN_VALUE) return Integer.MIN_VALUE;

        double edge = shoreDistance(seed, cx, cz, x, z);
        double coast = 2.0 + 4.0 * smooth(edge / 27.0);
        double broad = noise(seed ^ HEIGHT_SALT, x, z, 122.0);
        double texture = noise(seed ^ 0xB22A0102L, x, z, 43.0);
        double meadow = 10.0 + 2.8 * broad + 1.7 * texture;
        double hills = 19.0 * Math.pow(Math.max(0.0,
            noise(seed ^ 0xB22A0103L, x, z, 90.0) + 0.33), 1.6);

        // An off-center, non-conical ridge along the wider half of the island.
        double wide = bayAngle(seed, cx, cz) + Math.PI;
        double dx = x - (double) cx;
        double dz = z - (double) cz;
        double u = dx * Math.cos(wide) + dz * Math.sin(wide);
        double v = -dx * Math.sin(wide) + dz * Math.cos(wide);
        double ridge = 49.0 * Math.exp(-Math.pow((u - 175.0) / 86.0, 2.0)
                                        - Math.pow((v - 22.0) / 47.0, 2.0))
            * (0.88 + 0.12 * noise(seed ^ 0xB22A0104L, x, z, 36.0));
        double land = meadow + hills + ridge;
        double height = blend(coast, land, smooth(edge / 52.0));

        double clearing = smooth((clearingMetric(seed, cx, cz, x, z) - 0.94) / 0.45);
        double clearingHeight = 10.3 + 0.65 * noise(seed ^ 0xB22A0105L, x, z, 37.0);
        height = blend(clearingHeight, height, clearing);

        // Gently descending natural paths. This changes only the height,
        // not the material: these are not roads or artificial clear-cuts.
        double corridor = passage(seed, cx, cz, x, z);
        if (corridor > 0.0) {
            double radial = Math.hypot(dx, dz);
            double destination = blend(10.0, 3.0, smooth((radial - 35.0) / 125.0));
            destination = blend(coast, destination, smooth(edge / 22.0));
            height = blend(height, destination, corridor);
        }
        return sea + (int) Math.round(Math.clamp(height, 2.0, 60.0));
    }

    private record CoastSample(double value, double length) {}

    /**
     * Weighted shoreline quantiles: approximately 40% sand, 30% gravel,
     * 30% rocky coast by coast LENGTH, not by bounding-box land area.
     */
    public static double[] coastCutoffs(long seed, int cx, int cz) {
        final int sections = 360;
        int[] xs = new int[sections];
        int[] zs = new int[sections];
        double[] values = new double[sections];
        for (int i = 0; i < sections; ++i) {
            double angle = TWO_PI * i / sections;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double shore = BuyanTerrain.shoreRadius(seed, cx, cz,
                cx + (int) Math.round(300.0 * dx),
                cz + (int) Math.round(300.0 * dz));
            xs[i] = cx + (int) Math.round((shore - 7.0) * dx);
            zs[i] = cz + (int) Math.round((shore - 7.0) * dz);
            values[i] = noise(seed ^ 0xB22A0301L, xs[i], zs[i], 107.0);
        }
        List<CoastSample> samples = new ArrayList<>(sections);
        double totalLength = 0.0;
        for (int i = 0; i < sections; ++i) {
            int next = (i + 1) % sections;
            double length = Math.hypot(xs[next] - xs[i], zs[next] - zs[i]);
            samples.add(new CoastSample(values[i], length));
            totalLength += length;
        }
        samples.sort(Comparator.comparingDouble(CoastSample::value));
        double[] limits = new double[2];
        double covered = 0.0;
        int index = 0;
        for (CoastSample sample : samples) {
            covered += sample.length();
            if (index < 2 && covered >= totalLength * (index == 0 ? 0.4 : 0.7)) {
                limits[index++] = sample.value();
            }
        }
        if (index != 2) throw new IllegalStateException("Unable to classify Buyan coast");
        return limits;
    }

    public static BuyanTerrain.Surface surface(long seed, int cx, int cz,
                                                int x, int z, int sea, int height) {
        return surface(seed, cx, cz, x, z, sea, height, -0.12, 0.22);
    }

    public static BuyanTerrain.Surface surface(long seed, int cx, int cz,
                                                int x, int z, int sea, int height,
                                                double sandLimit, double gravelLimit) {
        double edge = shoreDistance(seed, cx, cz, x, z);
        double geology = noise(seed ^ 0xB22A0301L, x, z, 107.0);
        if (edge < 14.0) {
            if (geology < sandLimit) return BuyanTerrain.Surface.SAND_BEACH;
            return geology < gravelLimit ? BuyanTerrain.Surface.STONY_BEACH
                : BuyanTerrain.Surface.ROCK;
        }
        if (height >= sea + 48
            || height >= sea + 30 && geology > 0.04
            || geology > 0.58 && height >= sea + 19) {
            return BuyanTerrain.Surface.ROCK;
        }
        return BuyanTerrain.Surface.MEADOW;
    }

    private static double forestScore(long seed, int x, int z) {
        return 0.63 * noise(seed ^ FOREST_SALT, x, z, 137.0)
            + 0.29 * noise(seed ^ 0xB22A0203L, x, z, 58.0)
            + 0.08 * noise(seed ^ 0xB22A0204L, x, z, 23.0);
    }

    private static boolean forestCandidate(long seed, int cx, int cz, int radius,
                                           int x, int z, int sea) {
        if (shoreDistance(seed, cx, cz, x, z) < 19.0
            || clearingMetric(seed, cx, cz, x, z) < 1.44
            || passage(seed, cx, cz, x, z) > 0.10) return false;
        int h = top(seed, cx, cz, radius, x, z, sea);
        return h >= sea + 7 && h <= sea + 45
            && surface(seed, cx, cz, x, z, sea, h) == BuyanTerrain.Surface.MEADOW;
    }

    /**
     * Calibrate forest coverage against actual dry land on this seed rather
     * than the ocean-sized bounding square. Sampled at 6-block spacing.
     */
    public static double forestCutoff(long seed, int cx, int cz, int radius, int sea) {
        List<Double> eligible = new ArrayList<>();
        int drySamples = 0;
        for (int z = cz - radius; z <= cz + radius; z += 6) {
            for (int x = cx - radius; x <= cx + radius; x += 6) {
                if (shoreDistance(seed, cx, cz, x, z) < 0.0) continue;
                drySamples++;
                if (forestCandidate(seed, cx, cz, radius, x, z, sea)) {
                    eligible.add(forestScore(seed, x, z));
                }
            }
        }
        if (eligible.isEmpty()) return Double.POSITIVE_INFINITY;
        Collections.sort(eligible);
        int count = Math.min(eligible.size(), (int) Math.round(drySamples * 0.40));
        return count <= 0 ? Double.POSITIVE_INFINITY
            : eligible.get(eligible.size() - count);
    }

    public static boolean forest(long seed, int cx, int cz, int radius,
                                 int x, int z, int sea, double cutoff) {
        return forestCandidate(seed, cx, cz, radius, x, z, sea)
            && forestScore(seed, x, z) >= cutoff;
    }

    public static TreeKind treeKind(long seed, int x, int z, int h, int sea) {
        double altitude = smooth((h - sea - 13.0) / 34.0);
        double roll = BuyanTerrain.value(seed ^ 0xB22A0551L, x, z, 0);
        double spruce = 0.10 + altitude * 0.35;
        double birch = 0.34 - altitude * 0.10;
        if (roll < spruce) return TreeKind.SPRUCE;
        if (roll < spruce + birch) return TreeKind.BIRCH;
        return roll < spruce + birch + 0.19 * (1.0 - altitude * 0.45)
            ? TreeKind.GOLD_BIRCH : TreeKind.OAK;
    }

    public static BlockState block(long seed, int cx, int cz, int radius,
                                   int x, int y, int z, int height, int sea,
                                   double cutoff, BuyanTerrain.Surface kind,
                                   double cover, int rockRoll) {
        if (y == height && kind == BuyanTerrain.Surface.MEADOW
            && forest(seed, cx, cz, radius, x, z, sea, cutoff)) {
            double under = noise(seed ^ 0xB22A0401L, x, z, 29.0);
            if (under < -0.28) return Blocks.PODZOL.defaultBlockState();
            if (under > 0.39) return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        BlockState natural = BuyanTerrain.block(seed, x, y, z, height, kind, cover, rockRoll);
        if (height - y > 5 && y > 0
            && cover < -0.40 && rockRoll > 78) {
            return Blocks.DIORITE.defaultBlockState();
        }
        if (height - y == 0 && kind == BuyanTerrain.Surface.ROCK
            && cover < -0.12) return Blocks.TUFF.defaultBlockState();
        return natural;
    }
}
