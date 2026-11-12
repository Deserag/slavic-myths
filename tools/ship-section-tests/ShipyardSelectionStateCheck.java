package org.slavicmyths.verify;

import org.slavicmyths.ships.client.ShipyardSelectionState;

public final class ShipyardSelectionStateCheck {
    private static final int[][] GROUPS = {{0, 1}, {6, 2, 3, 4, 5}, {7}, {8, 9}};

    private static void expect(int actual, int expected, String context) {
        if (actual != expected) throw new AssertionError(context + ": " + actual + " != " + expected);
    }

    public static void main(String[] args) {
        ShipyardSelectionState state = new ShipyardSelectionState();
        state.set(1, false, 2, true, GROUPS[1], 6);
        expect(state.get(1, false, 2, true, GROUPS[1]), 6, "mast selection");
        expect(state.get(1, false, 2, false, GROUPS[1]), 0, "crash regression: 6 / 6");
        state.set(1, false, 2, false, GROUPS[1], 5);
        expect(state.get(1, false, 2, false, GROUPS[1]), 5, "legacy ship selection");
        expect(state.get(1, false, 1, false, GROUPS[1]), 0, "raft after legacy ship");
        state.set(1, false, 1, false, GROUPS[1], 8);
        expect(state.get(1, false, 1, false, GROUPS[1]), 8, "raft selection");
        expect(state.get(1, false, 2, false, GROUPS[1]), 5, "ship selection retained");
        expect(state.get(1, false, 2, true, GROUPS[1]), 6, "modular selection retained");
        expect(state.get(1, false, 2, true, GROUPS[0]), 0, "category switch");
        expect(state.get(1, false, 0, false, GROUPS[0]), -1, "empty slot");
        state.set(0, false, 0, false, GROUPS[0], 3);
        expect(state.get(0, false, 0, false, GROUPS[0]), 3, "create ship");
        state.set(0, true, 0, false, GROUPS[0], 4);
        expect(state.get(0, true, 0, false, GROUPS[0]), 4, "create legacy");
        expect(state.get(0, false, 0, false, GROUPS[0]), 3, "create ship retained");
        for (int index = -20; index <= 20; index++) {
            for (int category = 0; category < GROUPS.length; category++) {
                int[] group = GROUPS[category];
                for (int type = 0; type <= 2; type++) {
                    for (int tab = 0; tab <= 1; tab++) {
                        for (boolean available : new boolean[]{false, true}) {
                            state.set(tab, true, type, available, group, index);
                            int result = state.get(tab, true, type, available, group);
                            if (result < -1 || (tab == 0 && result >= 5)
                                    || (tab == 1 && available && result >= 10)
                                    || (tab == 1 && !available && type == 2 && result >= 6)
                                    || (tab == 1 && !available && type == 1 && result >= 9))
                                throw new AssertionError("invalid selection " + result);
                        }
                    }
                }
            }
        }
        System.out.println("ShipyardSelectionStateCheck PASS: scoped indices and context transitions");
    }
}
