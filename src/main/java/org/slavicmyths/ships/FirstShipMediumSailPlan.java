package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared MEDIUM geometry-3 sail schedule. Legacy geometry 1/2 never calls it.
 * The lowest physical yard is above three headroom blocks measured from the
 * highest real floor underneath each individual mast/cloth footprint.
 */
public final class FirstShipMediumSailPlan {
    public static final int MIN_WALKING_AIR_BLOCKS = 3;

    public record Sail(int mastZ, int tier, int bottom, int top, int halfSpan, int highestDeckY) {
        public Sail {
            if (tier < 1 || tier > 2 || halfSpan < 2 || top < bottom
                    || bottom - 1 - highestDeckY - 1 < MIN_WALKING_AIR_BLOCKS)
                throw new IllegalArgumentException("Unsafe medium sail");
        }
        public int lowerYardY() { return bottom - 1; }
        public int upperYardY() { return top + 1; }
        public int mastTopY() { return top + 2; }
        public int rowHalfSpan(int y) {
            if (y < bottom || y > top) throw new IllegalArgumentException("Outside sail");
            return Math.max(1, halfSpan - ((y == bottom || y == top) ? 1 : 0));
        }
    }

    public record Layout(List<Sail> sails) {
        public Layout { sails = List.copyOf(sails); }
    }

    private FirstShipMediumSailPlan() {}

    public static Layout forPlan(ShipSuperstructurePlan plan) {
        if (plan == null || plan.shipClass() != ShipSectionPlan.ShipClass.MEDIUM)
            throw new IllegalArgumentException("MEDIUM sail plan requires MEDIUM hull");
        // Read actual surviving floors from the frozen V2 construction, not
        // guessed Y offsets. Important for two decks, cabin roofs and bridge.
        // V2 rigging overwrites some upper-deck cells with mast/yard blocks.
        // Inspect the structural deck BEFORE any rigging is installed, or
        // a later deck upgrade can leave the sails too low.
        ShipSuperstructurePlan bare = new ShipSuperstructurePlan(
                plan.shipClass(), plan.hullWidth(), plan.hullLength(),
                List.of(), plan.decks(), plan.bridge(), plan.cabin());
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull =
                ShipHullVoxelLayout.generate(bare, 2);
        List<Sail> sails = new ArrayList<>();
        for (int i = 0; i < plan.sailTiers().size(); i++) {
            int tier = plan.sailTiers().get(i), z = plan.mastPositions().get(i);
            int span = tier == 1 ? 2 : 3;
            int highest = 4;
            for (var entry : hull.entrySet()) {
                var cell = entry.getKey();
                var part = entry.getValue();
                if ((part == ShipHullVoxelLayout.Part.DECK || part == ShipHullVoxelLayout.Part.BRIDGE)
                        && Math.abs(cell.x()) <= span && (cell.z() == z || cell.z() == z + 1))
                    highest = Math.max(highest, cell.y());
            }
            // Floor Y=8 => free Y=9..11, yard Y=12, cloth Y=13+.
            int bottom = highest + MIN_WALKING_AIR_BLOCKS + 2;
            int top = bottom + (tier == 1 ? 4 : 7);
            sails.add(new Sail(z, tier, bottom, top, span, highest));
        }
        return new Layout(sails);
    }
}
