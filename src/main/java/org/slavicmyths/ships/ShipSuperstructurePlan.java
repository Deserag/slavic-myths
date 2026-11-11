package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable, pure-Java design validation for the second shipbuilding iteration.
 * Units are actual hull BLOCKS, not upgrade sections. This is a planning layer:
 * a validated plan must still pass world-space collision and material checks
 * before the existing native-block ship generator can construct it.
 */
public record ShipSuperstructurePlan(ShipSectionPlan.ShipClass shipClass,
        int hullWidth, int hullLength, List<Integer> sailTiers,
        int decks, boolean bridge, boolean cabin) {

    public static final int MAST_CLEARANCE = 4;
    public static final double[] SAIL_THRUST = {0D, 1D, 1.7D, 2.5D};

    public ShipSuperstructurePlan {
        if (shipClass == null || sailTiers == null || hullWidth < 7 || hullLength < 11
                || hullWidth % 2 == 0 || hullLength % 2 == 0)
            throw new IllegalArgumentException("Invalid ship dimensions or class");
        sailTiers = List.copyOf(sailTiers);
        if (sailTiers.size() > shipClass.maxMasts || sailTiers.size() > shipClass.maxSails)
            throw new IllegalArgumentException("Too many masts or primary sails");
        for (Integer tier : sailTiers)
            if (tier == null || tier < 1 || tier > shipClass.maxSailTier)
                throw new IllegalArgumentException("Unsupported sail tier");
        if (decks < 1 || decks > shipClass.maxDecks)
            throw new IllegalArgumentException("Unsupported number of decks");
        if (decks >= 2 && (hullWidth < 9 || hullLength < 13))
            throw new IllegalArgumentException("Second deck needs at least 9x13");
        if (decks >= 3 && (hullWidth < 13 || hullLength < 21))
            throw new IllegalArgumentException("Third deck needs at least 13x21");
        if (bridge && (shipClass == ShipSectionPlan.ShipClass.SMALL
                || hullWidth < 9 || hullLength < 13))
            throw new IllegalArgumentException("Captain's bridge requires medium-sized hull");
        if (cabin && (shipClass == ShipSectionPlan.ShipClass.SMALL
                || hullWidth < 9 || hullLength < 15))
            throw new IllegalArgumentException("Captain's cabin needs stern clearance");
        if (bridge && cabin && (hullWidth < 11 || hullLength < 19))
            throw new IllegalArgumentException("Combined bridge and cabin need 11x19");
        if (!sailTiers.isEmpty() && hullLength < 7 + (sailTiers.size() - 1) * MAST_CLEARANCE)
            throw new IllegalArgumentException("Masts cannot fit clear of bow and stern");

        // The aft cabin occupies a seven-block deep protected stern region.
        // Reject any mast whose center or its one-block support touches it.
        for (int z : mastPositionsFor(sailTiers.size(), hullLength, cabin)) {
            if (z - 1 <= -hullLength / 2 + 8 && cabin)
                throw new IllegalArgumentException("Mast would intersect captain's cabin");
            if (z + 1 >= hullLength / 2)
                throw new IllegalArgumentException("Mast would intersect the bow");
        }
    }

    /** Centered mast stations; the same result follows after hull enlargement. */
    private static List<Integer> mastPositionsFor(int count, int hullLength, boolean cabin) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < count; i++)
            positions.add((2 * i - (count - 1)) * 2);
        if (cabin && !positions.isEmpty()) {
            int minMastZ = -hullLength / 2 + 10;
            int shift = Math.max(0, minMastZ - positions.get(0));
            for (int i = 0; i < positions.size(); i++)
                positions.set(i, positions.get(i) + shift);
        }
        return List.copyOf(positions);
    }

    /** Coordinates in local ship space, stern negative, bow positive. */
    public List<Integer> mastPositions() {
        return mastPositionsFor(sailTiers.size(), hullLength, cabin);
    }

    /**
     * V2 rigging is spread along the available working deck. Old V1 mast
     * stations and validation remain intact so old stored ships reconstruct
     * their exact physical block layout.
     */
    public List<Integer> mastPositionsV2() {
        int count = sailTiers.size();
        if (count == 0) return List.of();
        int half = hullLength / 2;
        int first = (cabin || bridge) ? -half + 10 : -half + 4;
        int last = half - 4;
        if (count == 1) {
            int z = Math.max(first, Math.min(last, 0));
            return List.of(decks > 1 && Math.abs(z) <= 1 && last >= 2 ? 2 : z);
        }
        if (last - first < (count - 1) * MAST_CLEARANCE)
            throw new IllegalArgumentException("V2 masts cannot fit cabin and bow clearance");
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < count; i++)
            positions.add(first + (int)Math.round((last - first) * (double)i / (count - 1)));
        // The stairwell is two blocks wide at z=0/1. Physical yards or
        // canvas at those coordinates must never replace a player's ladder.
        if (decks > 1) {
            for (int i = 0; i < positions.size(); i++) {
                int z = positions.get(i);
                if (Math.abs(z) > 1) continue;
                int fore = i + 1 < positions.size() ? positions.get(i + 1) : last + MAST_CLEARANCE;
                int aft = i > 0 ? positions.get(i - 1) : first - MAST_CLEARANCE;
                if (z <= 1 && fore - 2 >= MAST_CLEARANCE && 2 <= last)
                    positions.set(i, 2);
                else if (-2 - aft >= MAST_CLEARANCE && -2 >= first)
                    positions.set(i, -2);
                else throw new IllegalArgumentException("V2 masts would block ladder shaft");
            }
        }
        return List.copyOf(positions);
    }

    public double rawThrust() {
        double result = 0D;
        for (int tier : sailTiers) result += SAIL_THRUST[tier];
        return result;
    }

    /** Approximate displacement proportional to deck area and additional floor load. */
    public double loadFactor() {
        double footprint = (double)hullWidth * hullLength / (7D * 11D);
        double floors = 1D + (decks - 1) * .30D;
        double rooms = (bridge ? .12D : 0D) + (cabin ? .18D : 0D);
        return footprint * (floors + rooms);
    }

    /**
     * Provisional bounded thrust-efficiency factor, not blocks/tick.
     * Caller must keep the established rigid-body speed and acceleration caps.
     */
    public double propulsionFactor() {
        if (sailTiers.isEmpty()) return 0D;
        double efficiency = Math.min(1D, rawThrust() / loadFactor());
        double sailUpgrade = Math.min(1D, (rawThrust() - 1D) / 3.5D);
        return 0.60D + 0.20D * efficiency + 0.20D * sailUpgrade;
    }

    public ShipSuperstructurePlan withSail(int tier) {
        List<Integer> next = new ArrayList<>(sailTiers);
        next.add(tier);
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, next, decks, bridge, cabin);
    }

    public ShipSuperstructurePlan withDecks(int count) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, count, bridge, cabin);
    }

    public ShipSuperstructurePlan withBridge(boolean enabled) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, decks, enabled, cabin);
    }

    public ShipSuperstructurePlan withCabin(boolean enabled) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, decks, bridge, enabled);
    }
}
