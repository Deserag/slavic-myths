package org.slavicmyths.ships;

import java.util.HashSet;
import java.util.Set;

/** Pure JDK21 assertions for every valid deck cell in every owner-approved tier. */
public final class ShipyardLayoutProof {
    private static int count;
    private static void check(boolean condition, String message) {
        count++;
        if (!condition) throw new AssertionError(message);
    }

    private static void transition(RaftSize from, RaftSize to) {
        Set<Integer> cells = new HashSet<>();
        for (int cell = 0; cell < from.width * from.length; cell++) {
            int mapped = ShipyardLayout.remap(cell, from.width, from.length, to.width, to.length);
            int x = cell % from.width, z = cell / from.width;
            int expectedX = x + (to.width - from.width) / 2;
            int expectedZ = z + (to.length - from.length) / 2;
            check(mapped == expectedZ * to.width + expectedX, from + " -> " + to + " mapped " + cell);
            check(cells.add(mapped), "Collision at cell " + mapped);
        }
        check(cells.size() == from.width * from.length, "Lost some local equipment cells");
    }

    private static void rejected(int cell, int ow, int ol, int nw, int nl) {
        count++;
        try {
            ShipyardLayout.remap(cell, ow, ol, nw, nl);
            throw new AssertionError("Invalid expansion accepted");
        } catch (IllegalArgumentException expected) {
            // Correct rejection.
        }
    }

    public static void main(String[] args) {
        transition(RaftSize.SMALL, RaftSize.LONG);
        transition(RaftSize.LONG, RaftSize.WIDE);
        transition(RaftSize.WIDE, RaftSize.CARGO);
        transition(RaftSize.SMALL, RaftSize.CARGO);
        for (int cell = 0; cell < 9; cell++) {
            int once = ShipyardLayout.remap(cell, 3, 3, 5, 7);
            int step1 = ShipyardLayout.remap(cell, 3, 3, 3, 5);
            int step2 = ShipyardLayout.remap(step1, 3, 5, 5, 5);
            int step3 = ShipyardLayout.remap(step2, 5, 5, 5, 7);
            check(once == step3, "Successive upgrades must retain deck-center coordinates");
        }
        rejected(-1, 3, 3, 3, 5);
        rejected(9, 3, 3, 3, 5);
        rejected(0, 5, 5, 3, 3);
        rejected(0, 0, 3, 5, 5);
        rejected(0, 3, 3, 4, 4);
        check(RaftSize.SMALL.maxSails() == 1, "Small raft: one sail");
        check(RaftSize.CARGO.maxSails() == 2, "Cargo raft: two sails");
        System.out.println("SHIPYARD DECK LAYOUT: PASS " + count + " pure Java21 assertions; in-game GUI/physics NOT_RUN.");
    }
}
