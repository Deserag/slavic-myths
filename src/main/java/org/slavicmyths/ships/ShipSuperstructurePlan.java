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
        int decks, boolean bridge, boolean cabin, int hullRevision) {
    public ShipSuperstructurePlan(ShipSectionPlan.ShipClass shipClass,
            int hullWidth, int hullLength, List<Integer> sailTiers,
            int decks, boolean bridge, boolean cabin) {
        this(shipClass, hullWidth, hullLength, sailTiers, decks, bridge, cabin, 1);
    }


    public static final int MAST_CLEARANCE = 4;
    public static final double[] SAIL_THRUST = {0D, 1D, 1.7D, 2.5D};

    public ShipSuperstructurePlan {
        if (hullRevision < 1 || hullRevision > 3
                || (hullRevision == 2 && shipClass != ShipSectionPlan.ShipClass.LARGE
                        && shipClass != ShipSectionPlan.ShipClass.LARGEST)
                || (hullRevision == 3 && shipClass != ShipSectionPlan.ShipClass.LARGE))
            throw new IllegalArgumentException("Unsupported hull revision");
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
        for (int z : mastPositionsFor(sailTiers.size(), hullLength, cabin,
                shipClass == ShipSectionPlan.ShipClass.LARGE && hullRevision == 3)) {
            if (z - 1 <= -hullLength / 2 + 8 && cabin)
                throw new IllegalArgumentException("Mast would intersect captain's cabin");
            if (z + 1 >= hullLength / 2)
                throw new IllegalArgumentException("Mast would intersect the bow");
        }
    }

    /** Centered mast stations; the same result follows after hull enlargement. */
    private static List<Integer> mastPositionsFor(int count, int hullLength, boolean cabin,
            boolean brigantine) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < count; i++)
            positions.add(brigantine ? (count == 1 ? 0
                    : count == 2 ? (i == 0 ? -3 : 6)
                    : -7 + i * 8)
                    : (2 * i - (count - 1)) * 2);
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
        return mastPositionsFor(sailTiers.size(), hullLength, cabin,
                shipClass == ShipSectionPlan.ShipClass.LARGE && hullRevision == 3);
    }

    public double rawThrust() {
        double result = 0D;
        for (int tier : sailTiers) result += SAIL_THRUST[tier];
        return result;
    }

    /** Approximate displacement proportional to deck area and additional floor load. */
    public double loadFactor() {
        if (shipClass == ShipSectionPlan.ShipClass.MEDIUM) {
            // Medium load is normalized to its own 13x21 trading hull.
            // Additional structure has diminishing, but nonzero, resistance.
            double footprint = (double)hullWidth * hullLength / (13D * 21D);
            return 1D + .22D * (footprint - 1D)
                    + .15D * (decks - 1)
                    + (bridge ? .08D : 0D) + (cabin ? .10D : 0D);
        }
        if (shipClass == ShipSectionPlan.ShipClass.LARGE
                || shipClass == ShipSectionPlan.ShipClass.LARGEST) {
            // Tier III/IV hulls start at 21x33, not the 7x11 starter.
            // Their mass must not reduce a fully rigged vessel to near-zero
            // throttle. Extensions and upperworks still add resistance.
            double footprint = (double)hullWidth * hullLength / (21D * 33D);
            return 1D + .22D * (footprint - 1D) + .13D * (decks - 1)
                    + (bridge ? .07D : 0D) + (cabin ? .09D : 0D);
        }
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
        if (shipClass == ShipSectionPlan.ShipClass.LARGE && hullRevision == 3) {
            // Two basic sails give useful progress; better sail fabric must
            // still be materially faster despite additional upper decks.
            return Math.min(1D, .52D + .48D * (rawThrust() / 5D) / loadFactor());
        }
        if (shipClass == ShipSectionPlan.ShipClass.MEDIUM) {
            // Medium ships must not lose 28% of their sail thrust simply
            // for belonging to the trading class. The existing motion cap
            // still bounds the speed, and extra decks/cabins add resistance.
            return Math.min(1D, rawThrust() / loadFactor());
        }
        return Math.min(1D, rawThrust() / loadFactor());
    }

    public ShipSuperstructurePlan withSail(int tier) {
        List<Integer> next = new ArrayList<>(sailTiers);
        next.add(tier);
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, next, decks, bridge, cabin, hullRevision);
    }

    public ShipSuperstructurePlan withDecks(int count) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, count, bridge, cabin, hullRevision);
    }

    public ShipSuperstructurePlan withBridge(boolean enabled) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, decks, enabled, cabin, hullRevision);
    }

    public ShipSuperstructurePlan withCabin(boolean enabled) {
        return new ShipSuperstructurePlan(shipClass, hullWidth, hullLength, sailTiers, decks, bridge, enabled, hullRevision);
    }
}
