package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Real upper-deck clearance for NEW LARGE/LARGEST geometry 4 only.
 * Uses the structural (no-rigging) palette so the final sail schedule is
 * independent of the order that the player bought decks, masts and fabric.
 * Legacy hull geometries never call this class.
 */
public final class ShipClearanceSailPlan {
    public static final int MIN_WALKING_AIR_BLOCKS = 3;

    public record Sail(int mastZ, int tier, int bottom, int top,
                       int halfSpan, int highestDeckY) {
        public Sail {
            if (tier < 1 || tier > 3 || halfSpan < 2 || top < bottom
                    || bottom - 1 - highestDeckY - 1 < MIN_WALKING_AIR_BLOCKS
                    || top + 2 > 32)
                throw new IllegalArgumentException("Unsafe large ship sail dimensions");
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

    public static Layout forPlan(ShipSuperstructurePlan plan) {
        if (plan == null || (plan.shipClass() != ShipSectionPlan.ShipClass.LARGE
                && plan.shipClass() != ShipSectionPlan.ShipClass.LARGEST))
            throw new IllegalArgumentException("Large sail plan requires LARGE/LARGEST");
        ShipSuperstructurePlan bare = new ShipSuperstructurePlan(
                plan.shipClass(), plan.hullWidth(), plan.hullLength(),
                List.of(), plan.decks(), plan.bridge(), plan.cabin());
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull =
                ShipHullVoxelLayout.generate(bare, 2);
        List<Sail> result = new ArrayList<>();
        for (int i = 0; i < plan.sailTiers().size(); i++) {
            int tier = plan.sailTiers().get(i), z = plan.mastPositions().get(i);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            int highest = 4;
            for (var entry : hull.entrySet()) {
                var cell = entry.getKey();
                var part = entry.getValue();
                if ((part == ShipHullVoxelLayout.Part.DECK
                        || part == ShipHullVoxelLayout.Part.BRIDGE)
                        && Math.abs(cell.x()) <= span
                        && (cell.z() == z || cell.z() == z + 1))
                    highest = Math.max(highest, cell.y());
            }
            int bottom = highest + MIN_WALKING_AIR_BLOCKS + 2;
            int top = bottom + (tier == 1 ? 4 : tier == 2 ? 7 : 10);
            result.add(new Sail(z, tier, bottom, top, span, highest));
        }
        return new Layout(result);
    }

    private ShipClearanceSailPlan() {}
}
