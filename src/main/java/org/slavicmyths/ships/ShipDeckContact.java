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

    /** Determine an available floor at this player's feet, highest first. */
    public static int contactFloor(FirstShipEntity ship,
            double x, double z, double absoluteFeetY) {
        ShipBuildConfiguration config = ship.modularConfiguration();
        boolean brigantine = config != null
                && config.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && config.hullRevision() == 3;
        int[] order = brigantine ? new int[]{4, 3, 2, 1, 0}
                : config != null && config.bridge()
                ? new int[]{3, 4, 2, 1, 0} : new int[]{3, 2, 1, 0};
        for (int floor : order) {
            double target = ship.getY() + (brigantine
                    ? floor == 0 ? 2D : floor == 1 ? 6D
                    : floor == 2 ? 11D : floor == 3 ? 16D : 21D
                    : height(floor));
            if (absoluteFeetY >= target - .35D
                    && absoluteFeetY <= target + .55D
                    && supports(ship, x, z, floor)) return floor;
        }
        return -1;
    }

    private ShipDeckContact() {}
}
