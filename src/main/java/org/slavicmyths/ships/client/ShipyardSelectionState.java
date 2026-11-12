package org.slavicmyths.ships.client;

/**
 * Keeps shipyard selections independent of the item currently in the slot.
 * Every read is validated against the active context, including reads during
 * the first render frame after a slot switch (before containerTick).
 */
public final class ShipyardSelectionState {
    private int creationShip;
    private int creationRaft;
    private int modular;
    private int legacyShip;
    private int legacyRaft;

    private static int bounded(int value, int count) {
        return Math.max(0, Math.min(value, count - 1));
    }

    private static int modularChoice(int selected, int[] group) {
        if (group == null || group.length == 0) return -1;
        for (int option : group) if (option == selected) return selected;
        return group[0];
    }

    public int get(int tab, boolean createRafts, int vesselKind,
                   boolean modularAvailable, int[] modularGroup) {
        if (tab == 0) return createRafts ? bounded(creationRaft, 5) : bounded(creationShip, 4);
        if (tab != 1) return -1;
        if (modularAvailable) return modularChoice(modular, modularGroup);
        return switch (vesselKind) {
            case 2 -> bounded(legacyShip, 6);
            case 1 -> bounded(legacyRaft, 9);
            default -> -1;
        };
    }

    public void set(int tab, boolean createRafts, int vesselKind,
                    boolean modularAvailable, int[] modularGroup, int index) {
        if (tab == 0) {
            if (createRafts) creationRaft = bounded(index, 5);
            else creationShip = bounded(index, 4);
        } else if (tab == 1) {
            if (modularAvailable) modular = modularChoice(index, modularGroup);
            else if (vesselKind == 2) legacyShip = bounded(index, 6);
            else if (vesselKind == 1) legacyRaft = bounded(index, 9);
        }
    }
}
