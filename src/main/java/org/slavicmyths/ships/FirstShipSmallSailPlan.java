package org.slavicmyths.ships;

import java.util.List;

/**
 * Physical rigging and client cloth share these exact SMALL hull coordinates.
 * V12 only: legacy V11 saves keep their original mast/yards/mesh until migrated.
 * Deck surface is at Y=6 (solid deck blocks occupy Y=5).
 */
public final class FirstShipSmallSailPlan {
    public static final int FIRST_FREE_DECK_Y = 6;
    public static final int MIN_WALKING_AIR_BLOCKS = 3;
    public static final int FIRST_SAIL_Y = 10;

    public record Sail(int bottom, int top, int halfSpan) {
        public Sail {
            if (bottom < FIRST_SAIL_Y || top < bottom || halfSpan < 2)
                throw new IllegalArgumentException("Unsafe small ship sail");
        }
        public int rowHalfSpan(int y) {
            if (y < bottom || y > top) throw new IllegalArgumentException("Outside sail");
            return Math.max(1, halfSpan - ((y == bottom || y == top) ? 1 : 0));
        }
        public int lowerYardY() { return bottom - 1; }
        public int upperYardY() { return top + 1; }
    }

    public record Layout(int mastTop, List<Sail> sails) {
        public Layout { sails = List.copyOf(sails); }
    }

    private FirstShipSmallSailPlan() {}

    public static Layout forRig(int rig, int fabric) {
        if (rig < 0 || rig > 4 || fabric < 0 || fabric > 3
                || (rig == 0) != (fabric == 0))
            throw new IllegalArgumentException("Invalid small ship rig or fabric");
        int wide = fabric == 3 ? 1 : 0;
        return switch (rig) {
            case 0 -> new Layout(12, List.of());
            case 1 -> {
                int top = fabric == 1 ? 13 : fabric == 2 ? 14 : 15;
                int span = fabric == 1 ? 2 : 3;
                yield new Layout(top + 2, List.of(new Sail(FIRST_SAIL_Y, top, span)));
            }
            case 2 -> new Layout(21, List.of(
                    new Sail(FIRST_SAIL_Y, 14, 3 + wide),
                    new Sail(17, 19, 2 + wide)));
            case 3 -> new Layout(20, List.of(
                    new Sail(FIRST_SAIL_Y, 18, 3 + wide)));
            default -> new Layout(22, List.of(
                    new Sail(FIRST_SAIL_Y, 15, 3 + wide),
                    new Sail(18, 20, 2 + wide)));
        };
    }
}
