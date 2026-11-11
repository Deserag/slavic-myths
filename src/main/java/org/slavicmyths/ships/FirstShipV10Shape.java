package org.slavicmyths.ships;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * V10 native 7x11 hull section. +Z is the one-block bow; -Z is the stern.
 * This is the physical block section, not a renderer-only approximation.
 * Shaped bow and stern reduce the weather-deck footprint while preserving
 * the fixed helm and ladder stations; the submerged floor tapers to the keel.
 */
public final class FirstShipV10Shape {
    public static final int MIN_Z = -5;
    public static final int MAX_Z = 5;
    private static final int[] DECK_BEAMS = {1, 2, 2, 3, 3, 3, 3, 2, 2, 1, 0};
    private static final int[] FLOOR_BEAMS = {0, 1, 1, 2, 2, 2, 2, 1, 1, 0, 0};

    public record Cell(int x, int y, int z) {}
    public enum Part { KEEL, FLOOR, SIDE, UPPER_HULL, DECK, RAIL }

    public static int deckHalfBeam(int z) {
        return z < MIN_Z || z > MAX_Z ? -1 : DECK_BEAMS[z - MIN_Z];
    }

    public static int floorHalfBeam(int z) {
        return z < MIN_Z || z > MAX_Z ? -1 : FLOOR_BEAMS[z - MIN_Z];
    }

    private static boolean edge(int x, int z) {
        return Math.abs(x - 1) > deckHalfBeam(z)
                || Math.abs(x + 1) > deckHalfBeam(z)
                || Math.abs(x) > deckHalfBeam(z - 1)
                || Math.abs(x) > deckHalfBeam(z + 1);
    }

    private static boolean hatch(int x, int z) {
        return (x == -1 || x == 0) && (z == 1 || z == 2);
    }

    /**
     * Every position in the lower shell is defined here, including the
     * absence of blocks. Stairs/ladders, mast and wheel are added separately
     * and are deliberately not part of the hull section.
     */
    public static Part partAt(int x, int y, int z) {
        if (y < 0 || y > 5 || Math.abs(x) > deckHalfBeam(z)) return null;
        if (y == 0) {
            if (Math.abs(x) > floorHalfBeam(z)) return null;
            return x == 0 ? Part.KEEL : Part.FLOOR;
        }
        if (y <= 2)
            return edge(x, z) || Math.abs(x) > floorHalfBeam(z) ? Part.SIDE : null;
        if (y == 3) return edge(x, z) || !hatch(x, z) ? Part.UPPER_HULL : null;
        if (y == 4) return hatch(x, z) ? null : Part.DECK;
        return edge(x, z) ? Part.RAIL : null;
    }

    public static Map<Cell, Part> sections() {
        Map<Cell, Part> cells = new LinkedHashMap<>();
        for (int z = MIN_Z; z <= MAX_Z; z++)
            for (int x = -deckHalfBeam(z); x <= deckHalfBeam(z); x++)
                for (int y = 0; y <= 5; y++) {
                    Part part = partAt(x, y, z);
                    if (part != null) cells.put(new Cell(x, y, z), part);
                }
        return Collections.unmodifiableMap(cells);
    }

    private FirstShipV10Shape() {}
}
