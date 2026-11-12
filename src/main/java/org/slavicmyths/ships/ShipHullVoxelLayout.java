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

    public static int halfBeam(ShipSuperstructurePlan plan, int z) {
        return beam(plan.hullWidth(), plan.hullLength(), z);
    }

    /** Geometry 1 is frozen for existing modular ship saves and packed items. */
    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan, int geometry) {
        return switch (geometry) {
            case 1 -> generate(plan);
            case 2 -> generateStepped(plan);
            case 3 -> {
                if (plan.shipClass() != ShipSectionPlan.ShipClass.MEDIUM)
                    throw new IllegalArgumentException("Geometry 3 is reserved for MEDIUM");
                yield generateMediumClearance(plan);
            }
            default -> throw new IllegalArgumentException("Unknown modular hull geometry");
        };
    }

    /**
     * The V2 weather-deck outline narrows gradually along the entire bow
     * and stern instead of keeping the full beam until the final four rows.
     * Never exceed the historical outline (cabins and turn clearance).
     */
    public static int steppedDeckBeam(ShipSuperstructurePlan plan, int z) {
        if (plan == null) return -1;
        int legacy = halfBeam(plan, z);
        if (legacy < 0) return -1;
        int radius = plan.hullLength() / 2, full = plan.hullWidth() / 2;
        int taper = (int)Math.floor(full * Math.pow(Math.abs(z) / (double)radius, 2.5D));
        int minimum = z == -radius ? 1 : 0;
        return Math.min(legacy, Math.max(minimum, full - taper));
    }

    /** Radius of the physical chine, from the keel to the weather-deck rim. */
    public static int steppedHalfBeam(ShipSuperstructurePlan plan, int z, int y) {
        if (plan == null || y < 0 || y > 3) return -1;
        int top = steppedDeckBeam(plan, z);
        if (top < 0) return -1;
        int full = plan.hullWidth() / 2;
        int target = switch (y) {
            case 0 -> Math.max(1, (full + 1) / 4);
            case 1 -> Math.max(1, (full + 1) / 2);
            case 2 -> full - Math.max(1, full / 5);
            default -> full;
        };
        return Math.max(0, top - (full - target));
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

    /**
     * New modular keel and chine. Generate the legacy furnishings/decks first,
     * then replace ONLY the bottom four timber layers. Mast, yards, sail
     * positions, helm, hatches, ladders and upper decks are not regenerated.
     * Each lower shoulder forms a solid floor for the air cell above it.
     */
    private static Map<Cell, Part> generateStepped(ShipSuperstructurePlan plan) {
        Map<Cell, Part> blocks = new LinkedHashMap<>(generate(plan));
        blocks.entrySet().removeIf(entry -> {
            Cell c = entry.getKey();
            Part part = entry.getValue();
            return c.y() <= 3 && (part == Part.HULL || part == Part.KEEL)
                    || c.y() == 3 && part == Part.FRAME
                            && Math.abs(c.x()) > steppedDeckBeam(plan, c.z())
                    || c.y() == 4 && part == Part.DECK
                    || c.y() == 5 && part == Part.RAIL;
        });
        int halfLength = plan.hullLength() / 2;
        for (int z = -halfLength; z <= halfLength; z++) {
            for (int y = 0; y <= 3; y++) {
                int width = steppedHalfBeam(plan, z, y);
                int lower = y == 0 ? -1 : steppedHalfBeam(plan, z, y - 1);
                for (int x = -width; x <= width; x++) {
                    Cell cell = new Cell(x, y, z);
                    Part existing = blocks.get(cell);
                    // Preserve the open two-cell hatch shaft below the main deck.
                    // The historical ladder is at (-2,1) with the entry at (-2,0).
                    if (existing == Part.LADDER || existing == Part.FRAME
                            || x == -2 && z == 0 && y > 0) continue;
                    boolean face = y == 0 || Math.abs(x) > lower
                            || Math.abs(x) == width
                            || Math.abs(x) > steppedHalfBeam(plan, z - 1, y)
                            || Math.abs(x) > steppedHalfBeam(plan, z + 1, y);
                    if (face) put(blocks, x, y, z, y == 0 && x == 0 ? Part.KEEL : Part.HULL);
                }
            }
        }
        // Rebuild the full walking deck and surrounding rails against the
        // new longitudinal outline, preserving ladder shafts, doors and helm.
        for (int z = -halfLength; z <= halfLength; z++) {
            int beam = steppedDeckBeam(plan, z);
            for (int x = -beam; x <= beam; x++) {
                Cell deck = new Cell(x, 4, z);
                if (!mainHatch(x, z) && !blocks.containsKey(deck))
                    put(blocks, x, 4, z, Part.DECK);
                boolean edge = Math.abs(x) == beam
                        || Math.abs(x) > steppedDeckBeam(plan, z - 1)
                        || Math.abs(x) > steppedDeckBeam(plan, z + 1);
                Cell rail = new Cell(x, 5, z);
                if (edge && !blocks.containsKey(rail))
                    put(blocks, x, 5, z, Part.RAIL);
            }
        }
        // Back the open hatch shaft and ladder with real wood at the waterline.
        // Paired backing cells keep the exterior silhouette port/starboard.
        for (int z = 0; z <= 1; z++)
            for (int x : new int[]{-2, 2})
                put(blocks, x, 0, z, Part.HULL);
        return Collections.unmodifiableMap(blocks);
    }

    /**
     * MEDIUM V3: retain the complete stepped V2 hull/superstructure, but build
     * fresh rigging around the actual deck heights. V1/V2 are never mutated.
     */
    private static Map<Cell, Part> generateMediumClearance(ShipSuperstructurePlan plan) {
        ShipSuperstructurePlan bare = new ShipSuperstructurePlan(
                plan.shipClass(), plan.hullWidth(), plan.hullLength(),
                List.of(), plan.decks(), plan.bridge(), plan.cabin());
        Map<Cell, Part> blocks = new LinkedHashMap<>(generateStepped(bare));
        for (FirstShipMediumSailPlan.Sail sail : FirstShipMediumSailPlan.forPlan(plan).sails()) {
            int z = sail.mastZ();
            for (int y = 5; y <= sail.mastTopY(); y++) {
                Cell cell = new Cell(0, y, z);
                Part previous = blocks.get(cell);
                if (previous != null && previous != Part.DECK && previous != Part.MAST)
                    throw new IllegalArgumentException("MEDIUM mast intersects " + previous);
                put(blocks, 0, y, z, Part.MAST);
            }
            for (int y : new int[]{sail.lowerYardY(), sail.upperYardY()})
                for (int x = -sail.halfSpan(); x <= sail.halfSpan(); x++) {
                    if (x == 0) continue; // The mast remains continuous at the cross-tree.
                    Cell yard = new Cell(x, y, z);
                    if (blocks.containsKey(yard))
                        throw new IllegalArgumentException("MEDIUM yard intersects deck or superstructure");
                    put(blocks, x, y, z, Part.YARD);
                }
            for (int y = sail.bottom(); y <= sail.top(); y++)
                for (int x = -sail.rowHalfSpan(y); x <= sail.rowHalfSpan(y); x++) {
                    if (x == 0) continue;
                    Cell fabric = new Cell(x, y, z + 1);
                    if (blocks.containsKey(fabric))
                        throw new IllegalArgumentException("MEDIUM cloth intersects superstructure");
                    // Logical cloth only: ShipModularBlockHull deliberately omits SAIL.
                    put(blocks, x, y, z + 1, Part.SAIL);
                }
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
