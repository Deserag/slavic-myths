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
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM
    }

    private static int beam(int width, int length, int z) {
        int halfWidth = width / 2, halfLength = length / 2;
        if (Math.abs(z) > halfLength) return -1;
        int taper = Math.max(0, Math.abs(z) - (halfLength - 4));
        int reduction = (int)Math.ceil(taper * halfWidth / 4D);
        int tip = z == halfLength ? 0 : z == -halfLength ? 1 : 0;
        return Math.max(tip, halfWidth - reduction);
    }

    /** V2 has a long rounded bow and a broad transom instead of a rectangular barge. */
    private static int shapedBeam(int width, int length, int z) {
        int halfWidth = width / 2, halfLength = length / 2;
        if (Math.abs(z) > halfLength) return -1;
        if (z == halfLength) return 0;
        int bowDistance = halfLength - z, sternDistance = halfLength + z;
        double bowRun = Math.max(5D, Math.round(length * .28D));
        double sternRun = Math.max(4D, Math.round(length * .21D));
        int bow = Math.max(1, (int)Math.ceil(halfWidth
                * Math.sqrt(Math.min(1D, bowDistance / bowRun))));
        int stern = Math.max(1, (int)Math.ceil(halfWidth
                * Math.sqrt(Math.min(1D, (sternDistance + 1D) / sternRun))));
        return Math.min(halfWidth, Math.min(bow, stern));
    }

    public static int halfBeam(ShipSuperstructurePlan plan, int z) {
        return halfBeam(plan, z, 1);
    }

    public static int halfBeam(ShipSuperstructurePlan plan, int z, int version) {
        if (version == 1) return beam(plan.hullWidth(), plan.hullLength(), z);
        if (version == 2) return shapedBeam(plan.hullWidth(), plan.hullLength(), z);
        throw new IllegalArgumentException("Unsupported ship hull layout");
    }

    /** Legacy callers keep the exact V1 geometry. */
    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        return generate(plan, 1);
    }

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan, int version) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        if (version != 1 && version != 2)
            throw new IllegalArgumentException("Unsupported ship hull layout");
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;

        // V1 is byte-for-byte the existing hull. V2 adds a long curved sheer,
        // broad immersed chine, narrowed upper deck and timber bulwarks.
        // The closed full-width floor stays at Y=0 to preserve the dry hold.
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeam(plan, z, version);
            int deckHalf = version == 2 ? Math.max(0, half - 1) : half;
            int previous = halfBeam(plan, z - 1, version);
            int next = halfBeam(plan, z + 1, version);
            for (int x = -half; x <= half; x++) {
                put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                boolean edge = Math.abs(x) == half
                        || Math.abs(x) > previous || Math.abs(x) > next;
                if (version == 1) {
                    if (edge) for (int y = 1; y <= 3; y++)
                        put(blocks, x, y, z, Part.HULL);
                    else if (Math.floorMod(z, 3) == 0)
                        put(blocks, x, 3, z, Part.FRAME);
                    if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
                    if (edge) put(blocks, x, 5, z, Part.RAIL);
                } else {
                    // Stepped outer chine makes the upper deck visibly narrower
                    // while the continuous keel and bilge keep water out.
                    if (edge) for (int y = 1; y <= 2; y++)
                        put(blocks, x, y, z, Part.HULL);
                    boolean upperEdge = Math.abs(x) == deckHalf
                            || Math.abs(x) > Math.max(0, previous - 1)
                            || Math.abs(x) > Math.max(0, next - 1);
                    if (Math.abs(x) <= deckHalf) {
                        if (upperEdge) put(blocks, x, 3, z, Part.HULL);
                        else if (Math.floorMod(z, 4) == 0)
                            put(blocks, x, 3, z, Part.FRAME);
                        if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
                        if (upperEdge && !mainHatch(x, z)) {
                            boolean highBulwark = z <= -halfLength + 4
                                    || z >= halfLength - 5;
                            Part rim = highBulwark ? Part.HULL
                                    : Math.floorMod(z, 6) == 0 ? Part.FRAME : Part.RAIL;
                            put(blocks, x, 5, z, rim);
                            if (z == -halfLength && Math.abs(x) == deckHalf)
                                put(blocks, x, 6, z, Part.FRAME);
                        }
                    }
                    if (x == 0 && z >= halfLength - 1) {
                        put(blocks, x, 5, z, Part.HULL);
                        if (z == halfLength) put(blocks, x, 6, z, Part.HULL);
                    }
                }
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
                int half = Math.max(0, halfBeam(plan, z, version)
                        - (version == 2 ? 2 : 1));
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
