package org.slavicmyths.ships;

/** Pure Java local-deck geometry. Invalid cells must never silently disappear on upgrades. */
public final class ShipyardLayout {
    public static int remap(int cell, int oldWidth, int oldLength, int newWidth, int newLength) {
        if (oldWidth <= 0 || oldLength <= 0 || newWidth < oldWidth || newLength < oldLength ||
                cell < 0 || cell >= oldWidth * oldLength ||
                (newWidth - oldWidth) % 2 != 0 || (newLength - oldLength) % 2 != 0)
            throw new IllegalArgumentException("Invalid shipyard deck expansion");
        int x = cell % oldWidth + (newWidth - oldWidth) / 2;
        int z = cell / oldWidth + (newLength - oldLength) / 2;
        int destination = z * newWidth + x;
        if (destination < 0 || destination >= newWidth * newLength)
            throw new IllegalArgumentException("Deck mapping overflow");
        return destination;
    }

    private ShipyardLayout() {}
}
