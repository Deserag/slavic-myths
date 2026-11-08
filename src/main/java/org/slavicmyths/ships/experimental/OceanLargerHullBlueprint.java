package org.slavicmyths.ships.experimental;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pure-Java provisional geometry for 13x21 and 21x33 native-block hulls.
 *
 * NOT REGISTERED in Minecraft: no construction, movement or container migration
 * can be activated until block-entity transaction tests and performance budgets
 * pass. No legacy raft/plot data is touched.
 */
public final class OceanLargerHullBlueprint {
    public enum Size {
        MEDIUM(13, 21, 8), LARGE(21, 33, 10);
        public final int width, length, cabinLength;
        Size(int width, int length, int cabinLength) {
            this.width = width; this.length = length; this.cabinLength = cabinLength;
        }
        public int beam() { return width / 2; }
        public int bow() { return length / 2; }
        public int stern() { return -length / 2; }
    }
    public enum Part {
        KEEL, SIDE, LOWER_CEILING, WEATHER_DECK, BULWARK, HATCH_LADDER,
        MAST, CABIN_WALL, CABIN_GLASS, CABIN_DOOR, CABIN_ROOF,
        CABIN_LADDER, HELM_PEDESTAL, HELM_WHEEL, HELM_SPOKE
    }
    public record Cell(int x, int y, int z) {}
    public record Budget(int logs, int planks, int fasteners, int strings,
            int deckSections, int railModules, int helmModules) {}
    public record Plan(Size size, Map<Cell, Part> blocks, Budget materials, int estimatedWrites) {
        public int blockCount() { return blocks.size(); }
        public boolean movementApproved() { return false; } // Requires profiling and a cargo-safe engine.
    }

    private static int halfBeam(Size size, int z) {
        if (z < size.stern() || z > size.bow()) return -1;
        int fromRear = z - size.stern(), fromBow = size.bow() - z;
        if (fromBow == 0) return 0;
        if (fromBow == 1) return Math.max(1, size.beam() / 3);
        if (fromBow <= 4) return Math.max(1, Math.min(size.beam(), fromBow * size.beam() / 5));
        if (fromRear == 0) return Math.max(1, size.beam() / 2);
        if (fromRear == 1) return Math.max(2, size.beam() * 3 / 4);
        return size.beam();
    }

    public static boolean inside(Size size, int x, int z) {
        return Math.abs(x) <= halfBeam(size, z);
    }
    public static boolean hatch(int x, int z) {
        return (x == -1 || x == 0) && (z == 1 || z == 2);
    }
    private static boolean edge(Size s, int x, int z) {
        return !inside(s, x - 1, z) || !inside(s, x + 1, z)
                || !inside(s, x, z - 1) || !inside(s, x, z + 1);
    }
    private static void put(Map<Cell, Part> grid, int x, int y, int z, Part part) {
        grid.put(new Cell(x, y, z), part);
    }

    /**
     * Returns reproducible voxels with a dry two-block-high hold, a 2x2 ladder
     * hatch, enclosed aft cabin, windows, door, external ladder and a raised
     * wheel deck. Only an architectural draft: all pieces need world-block
     * checks and actual rotation of their vanilla BlockStates.
     */
    public static Plan generate(Size size) {
        Map<Cell, Part> grid = new LinkedHashMap<>();
        for (int z = size.stern(); z <= size.bow(); z++)
            for (int x = -halfBeam(size, z); x <= halfBeam(size, z); x++) {
                put(grid, x, 0, z, Part.KEEL);
                boolean rim = edge(size, x, z);
                if (rim) {
                    for (int y = 1; y <= 3; y++) put(grid, x, y, z, Part.SIDE);
                } else if (!hatch(x, z)) {
                    put(grid, x, 3, z, Part.LOWER_CEILING);
                }
                if (!hatch(x, z)) put(grid, x, 4, z, Part.WEATHER_DECK);
                if (rim) put(grid, x, 5, z, Part.BULWARK);
            }

        for (int y = 1; y <= 3; y++) put(grid, -2, y, 2, Part.SIDE);
        for (int y = 1; y <= 4; y++) put(grid, -1, y, 2, Part.HATCH_LADDER);

        // Leave at least one full voxel between the forward cabin door and
        // the center mast on medium ships: otherwise mast overwrites door.
        int rear = size.stern() + 1;
        int front = rear + size.cabinLength - 1;
        int width = size == Size.MEDIUM ? 2 : 4;
        for (int z = rear; z <= front; z++) {
            for (int x = -width; x <= width; x++) {
                for (int y = 5; y <= 8; y++) {
                    boolean wall = Math.abs(x) == width || z == rear || z == front;
                    if (!wall) continue;
                    Part part = x == 0 && z == front && (y == 5 || y == 6)
                            ? Part.CABIN_DOOR
                            : Math.abs(x) == width && y == 6 && z > rear && z < front
                                    && (z - rear) % 3 == 0 ? Part.CABIN_GLASS
                                    : Part.CABIN_WALL;
                    put(grid, x, y, z, part);
                }
                put(grid, x, 9, z, Part.CABIN_ROOF);
            }
        }

        // A climbable external route to the raised captain's deck.
        for (int y = 5; y <= 9; y++)
            put(grid, width + 1, y, front - 1, Part.CABIN_LADDER);
        int wheelZ = rear + 3;
        put(grid, 0, 10, wheelZ, Part.HELM_PEDESTAL);
        put(grid, 0, 11, wheelZ, Part.HELM_WHEEL);
        for (int x = -1; x <= 1; x++)
            for (int y = 10; y <= 12; y++)
                if (x != 0 || y != 11) put(grid, x, y, wheelZ, Part.HELM_SPOKE);

        // Masts only: separate fabric and yard tiers are installed in gameplay.
        int mastTop = size == Size.MEDIUM ? 19 : 27;
        for (int y = 5; y <= mastTop; y++) put(grid, 0, y, 0, Part.MAST);

        Budget bill = materials(size);
        return new Plan(size, Map.copyOf(grid), bill, grid.size() * 2);
    }

    /** Proportional upgrade preview only; absolutely no inventory is consumed. */
    public static Budget materials(Size size) {
        double scale = (double)(size.width * size.length) / (7 * 11);
        return new Budget((int)Math.ceil(48 * scale), (int)Math.ceil(96 * scale),
                (int)Math.ceil(32 * scale), (int)Math.ceil(24 * scale),
                (int)Math.ceil(32 * scale), (int)Math.ceil(4 * scale), 1);
    }

    private OceanLargerHullBlueprint() {}
}
