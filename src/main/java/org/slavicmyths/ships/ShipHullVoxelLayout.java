package org.slavicmyths.ships;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic voxel blueprint for the planned wooden ship classes.
 *
 * Coordinates use +Z toward bow, -Z toward stern. This class intentionally
 * has no Minecraft dependency: the real block palette can be validated and
 * compared before any server world mutation occurs.
 *
 * WARNING: Existing legacy 7x11 V2-V8 geometry is NOT replaced by this
 * blueprint. World transactions must explicitly opt into a versioned layout.
 */
public final class ShipHullVoxelLayout {
    public record Cell(int x, int y, int z) {}
    public enum Part {
        KEEL, HULL, FRAME, DECK, RAIL, LADDER, MAST, YARD, SAIL,
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM,
        HATCH, SHEER_SLAB, THIN_MAST, YARD_SLAB
    }

    private static int beam(int width, int length, int z) {
        int halfWidth = width / 2, halfLength = length / 2;
        if (Math.abs(z) > halfLength) return -1;
        int taper = Math.max(0, Math.abs(z) - (halfLength - 4));
        int reduction = (int)Math.ceil(taper * halfWidth / 4D);
        int tip = z == halfLength ? 0 : z == -halfLength ? 1 : 0;
        return Math.max(tip, halfWidth - reduction);
    }

    public static int halfBeam(ShipSuperstructurePlan plan, int z) {
        return beam(plan.hullWidth(), plan.hullLength(), z);
    }


    /** Shared coordinates for physical v3 sail markers and moving cloth renderer. */
    public static final int KHRABR_SAIL_BOTTOM = 7;
    public static final int KHRABR_SAIL_TOP = 11;
    public static final int KHRABR_YARD_Y = 12;
    public static final int KHRABR_SAIL_HALF_SPAN = 2;

    /**
     * Version 2 Khrabr may be lengthened (11 -> 15 blocks), without switching
     * back to the original deep-hulled modular ship. Beam is always seven.
     */
    public static int halfBeam(ShipSuperstructurePlan plan, int z, boolean khrabrV3) {
        if (!khrabrV3) return halfBeam(plan, z);
        int halfLength = plan.hullLength() / 2;
        if (Math.abs(z) > halfLength) return -1;
        int fromEnd = halfLength - Math.abs(z);
        int maxBeam = plan.hullWidth() / 2;
        if (z == halfLength) return 0;
        if (z == -halfLength) return 1;
        return Math.min(maxBeam, Math.max(1, fromEnd));
    }

    /** Pure-Java selectable layout: no Minecraft/NBT dependency in this class. */
    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan, boolean khrabrV3) {
        return khrabrV3 ? khrabrV3(plan) : generate(plan);
    }

    /**
     * Four-block shallow open hull at the actual surface waterline:
     * keel Y0, stepped chine Y1, walking deck Y2, low gunwale Y3.
     * No buried compartment, trapped air, or suspended wooden belly.
     */
    private static Map<Cell, Part> khrabrV3(ShipSuperstructurePlan plan) {
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeam(plan, z, true);
            put(blocks, 0, 0, z, Part.KEEL);
            if (half >= 2)
                for (int x = -1; x <= 1; x++) put(blocks, x, 1, z, Part.HULL);
            if (half >= 3)
                for (int x : new int[]{-2, 2}) put(blocks, x, 1, z, Part.HULL);
            for (int x = -Math.max(0, half - 1); x <= Math.max(0, half - 1); x++)
                put(blocks, x, 2, z, Part.DECK);
            if (half > 0)
                for (int x : new int[]{-half, half}) {
                    put(blocks, x, 2, z, Part.HULL);
                    put(blocks, x, 3, z, Part.HULL);
                }
            // Only the stem and stern are raised, rather than tall side walls.
            if (Math.abs(z) == halfLength)
                for (int x = -half; x <= half; x++)
                    put(blocks, x, 3, z, Part.SHEER_SLAB);
        }
        // Every installed mast is physically represented, including added masts
        // and higher sail tiers. The same stations drive moving sail rendering.
        for (int i = 0; i < plan.sailTiers().size(); i++) {
            int station = khrabrMastZ(plan, i);
            int tier = plan.sailTiers().get(i);
            int top = khrabrSailTop(tier), span = khrabrSailSpan(tier);
            for (int y = 3; y <= top + 1; y++)
                put(blocks, 0, y, station, Part.THIN_MAST);
            for (int x = -span - 1; x <= span + 1; x++)
                put(blocks, x, top + 1, station, Part.YARD_SLAB);
            for (int y = KHRABR_SAIL_BOTTOM; y <= top; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0) put(blocks, x, y, station + 1, Part.SAIL);
        }
        put(blocks, 0, 3, -1, Part.HELM);
        return Collections.unmodifiableMap(blocks);
    }

    public static int khrabrMastZ(ShipSuperstructurePlan plan, int index) {
        return plan.sailTiers().size() == 1 ? 1 : plan.mastPositions().get(index);
    }

    public static int khrabrSailTop(int tier) {
        return tier == 1 ? KHRABR_SAIL_TOP : tier == 2 ? 14 : 19;
    }

    public static int khrabrSailSpan(int tier) {
        return tier == 1 ? KHRABR_SAIL_HALF_SPAN : tier == 2 ? 3 : 4;
    }

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;

        // Full-block keel, dry hold and weather deck with shaped ends.
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeam(plan, z);
            for (int x = -half; x <= half; x++) {
                put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                boolean edge = Math.abs(x) == half
                        || Math.abs(x) > halfBeam(plan, z - 1)
                        || Math.abs(x) > halfBeam(plan, z + 1);
                if (edge) for (int y = 1; y <= 3; y++)
                    put(blocks, x, y, z, Part.HULL);
                else if (Math.floorMod(z, 3) == 0)
                    put(blocks, x, 3, z, Part.FRAME);
                if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
                if (edge) put(blocks, x, 5, z, Part.RAIL);
            }
        }
        // Access shaft and actual ladders, aligned through every built deck.
        for (int y = 1; y <= 4 + (plan.decks() - 1) * 4; y++) {
            put(blocks, -3, y, 1, Part.FRAME);
            put(blocks, -2, y, 1, Part.LADDER);
        }
        for (int y = 1; y <= 4; y++)
            blocks.remove(new Cell(-2, y, 0));

        // Elevated deck floors are stepped, not full-height rectangular towers.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int y = 4 + (floor - 1) * 4;
            int front = halfLength - 3 * (floor - 1);
            int rear = -halfLength + 2;
            for (int z = rear; z <= front; z++) {
                int half = Math.max(0, halfBeam(plan, z) - 1);
                for (int x = -half; x <= half; x++) {
                    boolean hatch = x == -2 && (z == 0 || z == 1);
                    if (!hatch) put(blocks, x, y, z, Part.DECK);
                    if (Math.abs(x) == half && !hatch)
                        put(blocks, x, y + 1, z, Part.RAIL);
                    if ((z - rear) % 4 == 0 && Math.abs(x) == half)
                        for (int sy = y - 3; sy < y; sy++)
                            put(blocks, x, sy, z, Part.FRAME);
                }
            }
        }

        // Real per-mast stations. A mast at one coordinate never overwrites
        // another because spacing is checked by ShipSuperstructurePlan.
        List<Integer> masts = plan.mastPositions();
        for (int i = 0; i < masts.size(); i++) {
            int z = masts.get(i), tier = plan.sailTiers().get(i);
            int top = tier == 1 ? 12 : tier == 2 ? 15 : 19;
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 5; y <= top + 1; y++) put(blocks, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(blocks, x, top + 1, z, Part.YARD);
                put(blocks, x, 7, z, Part.YARD);
            }
            for (int y = 8; y <= top; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !blocks.containsKey(new Cell(x, y, z + 1)))
                        put(blocks, x, y, z + 1, Part.SAIL);
        }

        // Closed stern cabin, with a two-block door aperture and real windows.
        if (plan.cabin()) {
            int rear = -halfLength + 2, front = rear + 6;
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++) {
                    boolean wall = Math.abs(x) == side || z == rear || z == front;
                    for (int y = 5; y <= 7; y++) {
                        if (z == front && x == 0 && y <= 6) {
                            blocks.remove(new Cell(x, y, z));
                        } else if (wall) {
                            Part part = (Math.abs(x) == side && z == rear + 3 && y == 6)
                                    ? Part.WINDOW : Part.CABIN_WALL;
                            put(blocks, x, y, z, part);
                        }
                    }
                    put(blocks, x, 8, z, Part.DECK);
                }
            // Outward opening door on the forward wall.
            put(blocks, 0, 5, front, Part.DOOR);
            put(blocks, 0, 6, front, Part.DOOR);
        }

        // The bridge is elevated over the stern; it uses the SAME HELM as the
        // rest of the vessel. Only one helm control position is generated.
        if (plan.bridge()) {
            // A shorter quarterdeck keeps the forward mast on medium ships clear.
            int rear = -halfLength + 2, front = rear + 4;
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++) {
                    put(blocks, x, 9, z, Part.BRIDGE);
                    if (Math.abs(x) == side || z == rear || z == front)
                        put(blocks, x, 10, z, Part.RAIL);
                }
            for (int y = 5; y <= 9; y++)
                put(blocks, -side + 1, y, front, Part.LADDER);
            put(blocks, 0, 10, rear + 2, Part.HELM);
        } else {
            put(blocks, 0, 5, -halfLength + (plan.cabin() ? 9 : 3), Part.HELM);
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static boolean mainHatch(int x, int z) {
        return x == -2 && (z == 0 || z == 1);
    }

    private static void put(Map<Cell, Part> blocks, int x, int y, int z, Part part) {
        if (y < 0 || y > 32) throw new IllegalArgumentException("Ship part outside vertical bounds");
        blocks.put(new Cell(x, y, z), part);
    }

    /** Count actual placed blocks, not arbitrary costs per button press. */
    public static Map<Part, Integer> count(Map<Cell, Part> palette) {
        Map<Part, Integer> counts = new EnumMap<>(Part.class);
        for (Part part : palette.values()) counts.merge(part, 1, Integer::sum);
        return Collections.unmodifiableMap(counts);
    }

    /**
     * Positive-only delta from the previous validated blueprint.
     * Removing a structure does not magically refund all its materials.
     */
    public static Map<Part, Integer> additions(Map<Cell, Part> previous,
                                                 Map<Cell, Part> next) {
        Map<Part, Integer> delta = new EnumMap<>(Part.class);
        for (var entry : next.entrySet())
            if (previous.get(entry.getKey()) != entry.getValue())
                delta.merge(entry.getValue(), 1, Integer::sum);
        return Collections.unmodifiableMap(delta);
    }

    private ShipHullVoxelLayout() {}
}
