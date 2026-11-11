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
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM, LIGHT, SAIL_TRIM, RIGGING, BOWSPRIT, STANDING_LANTERN
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


    /**
     * New 2.6 cross-section profile. Unlike the legacy four-row taper, the
     * narrowing occupies a substantial part of the length on every class.
     * Existing saved version-1 blueprints keep the original generator.
     */
    public static int halfBeamV2(ShipSuperstructurePlan plan, int z) {
        int halfLength = plan.hullLength() / 2;
        if (Math.abs(z) > halfLength) return -1;
        int full = plan.hullWidth() / 2;
        double longitudinal = (double)Math.abs(z) / halfLength;
        double start = z >= 0 ? 0.32D : 0.47D;
        double progress = Math.max(0D, (longitudinal - start) / (1D - start));
        double remaining = 1D - Math.pow(progress, z >= 0 ? 1.15D : 1.35D);
        int minimum = z >= 0 ? 0 : 1;
        return Math.max(minimum, (int)Math.round(full * remaining));
    }

    /**
     * Versioned geometry entrypoint. Old saves MUST use the exact previous
     * palette or their world block snapshots can no longer be inspected.
     */
    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan, int layoutVersion) {
        if (layoutVersion == 1) return generate(plan);
        if (layoutVersion != 2) throw new IllegalArgumentException("Unsupported hull layout version");
        Map<Cell, Part> blocks = new LinkedHashMap<>(generate(plan));
        int halfLength = plan.hullLength() / 2;
        for (int z = -halfLength; z <= halfLength; z++)
            for (int x = -plan.hullWidth() / 2; x <= plan.hullWidth() / 2; x++)
                for (int y = 0; y <= 5; y++) {
                    Cell cell = new Cell(x, y, z);
                    Part old = blocks.get(cell);
                    if (old == Part.KEEL || old == Part.HULL || old == Part.FRAME
                            || old == Part.DECK || old == Part.RAIL)
                        blocks.remove(cell);
                }

        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeamV2(plan, z);
            for (int x = -half; x <= half; x++) {
                // Underwater keel is narrower than the sealed, dry hold floor.
                // Solid y=1 across the footprint prevents water entering the hold.
                if (Math.abs(x) <= Math.max(0, half - 2))
                    put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                put(blocks, x, 1, z, Part.HULL);
                boolean side = Math.abs(x) == half
                        || Math.abs(x) > halfBeamV2(plan, z - 1)
                        || Math.abs(x) > halfBeamV2(plan, z + 1);
                if (side) {
                    for (int y = 2; y <= 3; y++) put(blocks, x, y, z, Part.HULL);
                    put(blocks, x, 5, z, Part.RAIL);
                } else {
                    if (Math.floorMod(z, 4) == 0) put(blocks, x, 3, z, Part.FRAME);
                }
                if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
            }
        }

        // Restore the through-deck stairwell and its backing after remodeling.
        for (int y = 1; y <= 4; y++) {
            put(blocks, -3, y, 1, Part.FRAME);
            put(blocks, -2, y, 1, Part.LADDER);
            if (y >= 2) blocks.remove(new Cell(-2, y, 0));
        }
        // The ladder shaft must have a waterproof support under its first rung.
        put(blocks, -2, 0, 1, Part.HULL);

        // An unobstructed, genuinely emitting lantern hangs below the deck.
        // Placing a timber ceiling above it works both with world blocks and
        // the palette-based moving renderer; moving dynamic block light still
        // requires a separate in-game verification.
        for (int z = -halfLength + 3; z <= halfLength - 3; z += 5) {
            if (halfBeamV2(plan, z) < 2 || Math.abs(z) <= 2) continue;
            Cell lamp = new Cell(0, 2, z);
            if (!blocks.containsKey(lamp)) {
                put(blocks, 0, 3, z, Part.DECK);
                put(blocks, 0, 2, z, Part.LIGHT);
            }
        }
        if (plan.cabin()) {
            int rear = -halfLength + 2;
            for (int z = rear + 2; z <= rear + 4; z += 2)
                if (!blocks.containsKey(new Cell(0, 7, z)))
                    put(blocks, 0, 7, z, Part.LIGHT);
        }

        // Older versions used rectangular roofs across nearly the WHOLE
        // ship. Retain their exact geometry in V1 but build shorter stepped,
        // enclosed, windowed decks on V2 instead of a timber bath-tub.
        if (plan.decks() >= 2) {
            blocks.entrySet().removeIf(entry -> entry.getKey().y() >= 8
                    && (entry.getValue() == Part.DECK
                    || entry.getValue() == Part.RAIL
                    || entry.getValue() == Part.FRAME));
            for (int floor = 2; floor <= plan.decks(); floor++) {
                int top = 4 + (floor - 1) * 4;
                int rear = -halfLength + (floor == 2 ? 2 : 7);
                int front = floor == 2 ? halfLength - Math.max(6, halfLength / 2)
                        : Math.min(4, halfLength / 4);
                for (int z = rear; z <= front; z++) {
                    int span = Math.max(1, Math.min(
                            plan.hullWidth() / 2 - floor,
                            halfBeamV2(plan, z) - 1));
                    // Captain's cabin roof remains connected to its walls.
                    if (floor == 2 && plan.cabin() && z <= -halfLength + 8)
                        span = Math.max(span, Math.min(3, plan.hullWidth() / 2 - 2));
                    for (int x = -span; x <= span; x++) {
                        boolean shaft = x == -2 && (z == 0 || z == 1);
                        if (!shaft) put(blocks, x, top, z, Part.DECK);
                        if (Math.abs(x) != span) continue;
                        put(blocks, x, top + 1, z, Part.RAIL);
                        for (int sy = top - 3; sy < top; sy++) {
                            Part material = sy == top - 2
                                    && Math.floorMod(z - rear, 5) == 2
                                    ? Part.WINDOW : Part.CABIN_WALL;
                            put(blocks, x, sy, z, material);
                        }
                    }
                }
            }
            // Vertical access remains clear through the redesigned roofs.
            for (int y = 5; y <= 4 + (plan.decks() - 1) * 4; y++) {
                put(blocks, -3, y, 1, Part.FRAME);
                put(blocks, -2, y, 1, Part.LADDER);
            }
        }

        // Guaranteed lighting in the little single-deck vessel's hold.
        if (plan.shipClass() == ShipSectionPlan.ShipClass.SMALL) {
            put(blocks, 0, 3, -2, Part.DECK);
            put(blocks, 0, 2, -2, Part.LIGHT);
        }
        // Standing warm lamps on weather decks and the raised captain's bridge.
        for (int z = -halfLength + 3; z <= halfLength - 3; z += 8) {
            int edge = halfBeamV2(plan, z);
            Cell p = new Cell(edge - 1, 5, z);
            if (edge >= 3 && !blocks.containsKey(p))
                put(blocks, p.x(), p.y(), p.z(), Part.STANDING_LANTERN);
        }
        if (plan.bridge()) {
            int rear = -halfLength + 2;
            Cell bridgeLamp = new Cell(1, 10, rear + 2);
            if (!blocks.containsKey(bridgeLamp))
                put(blocks, bridgeLamp.x(), bridgeLamp.y(), bridgeLamp.z(), Part.STANDING_LANTERN);
        }

        // V2 rigging is rebuilt AFTER the cabin and the shaped hull. Delete
        // only old cloth/spars so the saved V1 palette remains byte-for-byte
        // identical and the new version has no duplicate rectangular sails.
        blocks.entrySet().removeIf(entry -> entry.getValue() == Part.SAIL
                || entry.getValue() == Part.MAST || entry.getValue() == Part.YARD);
        for (int mast = 0; mast < plan.mastPositionsV2().size(); mast++) {
            int z = plan.mastPositionsV2().get(mast);
            int tier = plan.sailTiers().get(mast);
            int broad = Math.min(plan.hullWidth() / 2 - 1,
                    1 + tier + (plan.hullWidth() >= 13 ? 1 : 0));
            int mastTop = 13 + (tier - 1) * 7;
            for (int y = 2; y <= mastTop + 2; y++)
                put(blocks, 0, y, z, Part.MAST);
            for (int layer = 0; layer < tier; layer++) {
                int low = 8 + layer * 7, high = low + 4;
                int span = Math.max(1, broad - layer);
                for (int x = -span; x <= span; x++) {
                    put(blocks, x, low - 1, z, Part.YARD);
                    put(blocks, x, high + 1, z, Part.YARD);
                }
                for (int y = low; y <= high; y++) {
                    int edge = Math.max(1, span - (y == high ? 1 : 0));
                    int belly = (y == low || y == high) ? 1 : 2;
                    for (int x = -edge; x <= edge; x++) {
                        Part cloth = (Math.abs(x) == 1 && y == low + 2)
                                || (x == 0 && y == low + 3)
                                ? Part.SAIL_TRIM : Part.SAIL;
                        put(blocks, x, y, z + belly, cloth);
                    }
                }
            }
            // Vertical stays alongside the lower sail; do not run rigging
            // through walkable hatch, helm, or the protected aft cabin.
            if (broad + 1 < plan.hullWidth() / 2)
                for (int x : new int[]{-broad - 1, broad + 1})
                    for (int y = 9; y <= 12; y++)
                        put(blocks, x, y, z, Part.RIGGING);
        }

        // A narrow, forward-pointing block-built bowsprit, including its
        // overhang in the moving collision palette.
        for (int z = halfLength; z <= halfLength + 2; z++)
            put(blocks, 0, 6, z, Part.BOWSPRIT);

        // Each covered upper level receives actual hanging lantern blocks,
        // attached to the deck above, rather than painted yellow windows.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int ceiling = 4 + (floor - 1) * 4;
            for (int z = -halfLength + 5; z <= halfLength - 5; z += 6) {
                Cell support = new Cell(1, ceiling, z);
                Cell light = new Cell(1, ceiling - 1, z);
                if (blocks.get(support) == Part.DECK && !blocks.containsKey(light))
                    put(blocks, 1, ceiling - 1, z, Part.LIGHT);
            }
        }

        // Later tiered sails can consume a preselected ceiling position.
        // Enforce one real unobstructed lantern for EVERY accessible upper level.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int ceiling = 4 + (floor - 1) * 4;
            boolean hasLight = false;
            for (var e : blocks.entrySet())
                if (e.getKey().y() == ceiling - 1
                        && e.getValue() == Part.LIGHT) {
                    hasLight = true;
                    break;
                }
            if (hasLight) continue;
            boolean placed = false;
            for (int z = -halfLength + 5; z <= halfLength - 5 && !placed; z++)
                for (int x = 1; x < plan.hullWidth() / 2 - 1 && !placed; x++) {
                    Cell support = new Cell(x, ceiling, z);
                    Cell lamp = new Cell(x, ceiling - 1, z);
                    if (blocks.get(support) == Part.DECK && !blocks.containsKey(lamp)) {
                        put(blocks, x, ceiling - 1, z, Part.LIGHT);
                        placed = true;
                    }
                }
            if (!placed)
                throw new IllegalArgumentException("Missing lantern mounting point for deck " + floor);
        }
        return Collections.unmodifiableMap(blocks);
    }

    public static int halfBeam(ShipSuperstructurePlan plan, int z, int layoutVersion) {
        return layoutVersion == 1 ? halfBeam(plan, z)
                : layoutVersion == 2 ? halfBeamV2(plan, z)
                : -1;
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
