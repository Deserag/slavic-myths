package org.slavicmyths.ships;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Shared local deck support positions for both moving and parked ship logic.
 * Legacy 7x11 ships keep their exact existing support and hatch behavior.
 */
public final class ShipDeckContact {
    private static final Map<ShipBuildConfiguration,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>> CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** True deck height for both renderer-independent contact and crew carrying. */
    public static double height(ShipBuildConfiguration config, int floor) {
        if (config != null && config.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4)
            return switch (floor) {
                case 0 -> 2D;
                case 1 -> 7D;
                case 2 -> 12D;
                case 3 -> 17D;
                case 4 -> 22D;
                default -> -1000D;
            };
        if (config != null && config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && config.hullRevision() == 3)
            return switch (floor) {
                case 0 -> 2D;
                case 1 -> 6D;
                case 2 -> 11D;
                case 3 -> 16D;
                case 4 -> 21D;
                default -> -1000D;
            };
        return height(floor);
    }

    private static Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> layout(
            ShipBuildConfiguration config) {
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(config,
                    key -> ShipHullVoxelLayout.generate(key.structure()));
        }
    }

    public static double height(int floor) {
        return switch (floor) {
            case 0 -> 1D;
            case 1 -> 5D;
            case 2 -> 9D;
            case 3 -> 13D;
            case 4 -> 10D;
            default -> -1000D;
        };
    }

    public static boolean supports(FirstShipEntity ship,
            double localX, double localZ, int floor) {
        ShipBuildConfiguration config = ship.modularConfiguration();
        if (config == null) {
            return floor == 0 ? FirstShipHull.holdSupports(localX, localZ)
                    : floor == 1 && FirstShipHull.mainDeckSupports(localX, localZ);
        }
        if (config.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4) {
            if (!Double.isFinite(localX) || !Double.isFinite(localZ)) return false;
            int x = (int)Math.round(localX), z = (int)Math.round(localZ);
            if (Math.abs(localX - x) >= .50D || Math.abs(localZ - z) >= .50D)
                return false;
            int maximum = config.decks() >= 2 || config.bridge() ? 4 : 3;
            if (floor < 0 || floor > maximum) return false;
            int y = floor == 0 ? 1 : floor == 1 ? 6
                    : floor == 2 ? 11 : floor == 3 ? 16 : 21;
            var part = layout(config).get(new ShipHullVoxelLayout.Cell(x, y, z));
            return floor == 0 ? part == ShipHullVoxelLayout.Part.HULL
                    || part == ShipHullVoxelLayout.Part.KEEL
                    : part == ShipHullVoxelLayout.Part.DECK
                    || part == ShipHullVoxelLayout.Part.BRIDGE;
        }
        if (config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && config.hullRevision() == 3) {
            if (!Double.isFinite(localX) || !Double.isFinite(localZ)) return false;
            int top = ShipHullVoxelLayout.brigantineTopDeck(config.structure());
            int maxFloor = top == 20 ? 4 : top == 15 ? 3 : 2;
            if (floor < 0 || floor > maxFloor) return false;
            int x = (int)Math.round(localX), z = (int)Math.round(localZ);
            if (Math.abs(localX - x) >= .50D || Math.abs(localZ - z) >= .50D)
                return false;
            int floorY = floor == 0 ? 1 : floor == 1 ? 5
                    : floor == 2 ? 10 : floor == 3 ? 15 : 20;
            var part = layout(config).get(new ShipHullVoxelLayout.Cell(x, floorY, z));
            return floor == 0 ? part == ShipHullVoxelLayout.Part.HULL
                    || part == ShipHullVoxelLayout.Part.KEEL
                    : part == ShipHullVoxelLayout.Part.DECK
                    || part == ShipHullVoxelLayout.Part.BRIDGE;
        }
        boolean roof = config.cabin()
                && config.shipClass() == ShipSectionPlan.ShipClass.MEDIUM;
        if (floor < 0 || floor > (config.bridge() ? 4 : Math.max(config.decks(), roof ? 2 : 1))
                || (floor == 4 && !config.bridge())
                || (floor == 3 && config.decks() < 3)
                || (floor == 2 && config.decks() < 2 && !roof)) return false;
        if (!Double.isFinite(localX) || !Double.isFinite(localZ)) return false;
        int x = (int)Math.round(localX), z = (int)Math.round(localZ);
        if (Math.abs(localX - x) >= .50D || Math.abs(localZ - z) >= .50D)
            return false;
        var material = layout(config).get(new ShipHullVoxelLayout.Cell(
                x, (int)height(floor) - 1, z));
        return floor == 0
                ? material == ShipHullVoxelLayout.Part.HULL
                    || material == ShipHullVoxelLayout.Part.KEEL
                : material == ShipHullVoxelLayout.Part.DECK
                    || material == ShipHullVoxelLayout.Part.BRIDGE;
    }

    /**
     * Real moving IV stair contact between named decks. The high half of
     * every +Z flight is on the forward side of the stair block.
     * Return NaN outside an actual physical stair voxel.
     */
    public static double stairSurfaceHeight(ShipBuildConfiguration config,
            double localX, double localZ, double feetRelativeY) {
        if (config == null || config.shipClass() != ShipSectionPlan.ShipClass.LARGEST
                || config.hullRevision() != 4 || !Double.isFinite(localX)
                || !Double.isFinite(localZ) || !Double.isFinite(feetRelativeY))
            return Double.NaN;
        int x = (int)Math.round(localX), z = (int)Math.round(localZ);
        if (Math.abs(localX - x) >= .5D || Math.abs(localZ - z) >= .5D)
            return Double.NaN;
        var hull = layout(config);
        for (int y = 2; y <= 21; y++) {
            if (hull.get(new ShipHullVoxelLayout.Cell(x, y, z))
                    != ShipHullVoxelLayout.Part.STAIR) continue;
            double surface = y + (localZ >= z ? 1D : .5D);
            if (feetRelativeY >= surface - .65D
                    && feetRelativeY <= surface + .9D)
                return surface;
        }
        return Double.NaN;
    }

    /** Determine an available floor at this player's feet, highest first. */
    public static int contactFloor(FirstShipEntity ship,
            double x, double z, double absoluteFeetY) {
        ShipBuildConfiguration config = ship.modularConfiguration();
        boolean brigantine = config != null
                && config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && config.hullRevision() == 3;
        boolean frigate = config != null
                && config.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && config.hullRevision() == 4;
        int[] order = brigantine || frigate ? new int[]{4, 3, 2, 1, 0}
                : config != null && config.bridge()
                ? new int[]{3, 4, 2, 1, 0} : new int[]{3, 2, 1, 0};
        for (int floor : order) {
            double target = ship.getY() + height(config, floor);
            if (absoluteFeetY >= target - .35D
                    && absoluteFeetY <= target + .55D
                    && supports(ship, x, z, floor)) return floor;
        }
        return -1;
    }

    private ShipDeckContact() {}
}
