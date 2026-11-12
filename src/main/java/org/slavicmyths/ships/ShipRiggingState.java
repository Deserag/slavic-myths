package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.List;

/** Minecraft-free validated mast configuration for isolated Java 21 geometry tests. */
public record ShipRiggingState(int height, int strength, int width, int yards,
        int area, int layers, int fabric) {
    public ShipRiggingState {
        if (height < 0 || height > 2 || strength < 1 || strength > 3
                || width < 0 || width > 2 || yards < 1 || yards > 4
                || area < 0 || area > 2 || layers < 1 || layers > yards
                || fabric < 1 || fabric > 3)
            throw new IllegalArgumentException("Invalid independent sail rigging");
    }

    public ShipRiggingState increase(int aspect) {
        return switch (aspect) {
            case 0 -> new ShipRiggingState(height + 1, strength, width, yards, area, layers, fabric);
            case 1 -> new ShipRiggingState(height, strength + 1, width, yards, area, layers, fabric);
            case 2 -> new ShipRiggingState(height, strength, width + 1, yards, area, layers, fabric);
            case 3 -> new ShipRiggingState(height, strength, width, yards + 1, area, layers, fabric);
            case 4 -> new ShipRiggingState(height, strength, width, yards, area + 1, layers, fabric);
            case 5 -> new ShipRiggingState(height, strength, width, yards, area, layers + 1, fabric);
            case 6 -> new ShipRiggingState(height, strength, width, yards, area, layers, fabric + 1);
            default -> throw new IllegalArgumentException("Unknown rigging upgrade");
        };
    }    public static List<ShipRiggingState> defaults(List<Integer> tiers) {
        List<ShipRiggingState> result = new ArrayList<>();
        for (int tier : tiers) result.add(new ShipRiggingState(0, 1, 0, 1, 0, 1, tier));
        return List.copyOf(result);
    }
}
