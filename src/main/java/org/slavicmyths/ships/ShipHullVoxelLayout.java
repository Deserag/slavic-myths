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
        KEEL, HULL, FRAME, DECK, RAIL, LADDER, MAST, YARD, RIGGING, SAIL, SAIL_TRIM,
        CABIN_WALL, WINDOW, HULL_WINDOW, DOOR, BRIDGE, HELM, LIGHT
    }

    private static int beam(int width, int length, int z) {
        int halfWidth = width / 2, halfLength = length / 2;
        if (Math.abs(z) > halfLength) return -1;
        int endDistance = halfLength - Math.abs(z);
        int bowRun = Math.min(halfLength, Math.max(4, width / 2 + 1));
        int sternRun = Math.min(halfLength, Math.max(3, width / 2));
        int run = z >= 0 ? bowRun : sternRun;
        if (endDistance >= run) return halfWidth;
        int tipBeam = z >= 0 ? 0 : Math.min(1, halfWidth);
        double fraction = (double)endDistance / run;
        return Math.min(halfWidth, tipBeam + (int)Math.floor((halfWidth - tipBeam) * Math.pow(fraction, .78D)));
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
                // Narrow submerged keel, wider dry hold floor one block above it.
                // The full floor at y=1 leaves two clear interior blocks at y=2,3.
                int keelBeam = Math.max(0, half / 3);
                if (Math.abs(x) <= keelBeam)
                    put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                put(blocks, x, 1, z, Part.HULL);
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
        // The main hatch reaches the dry hold. Upper floors have a separate
        // protected aft ladder so they never require an exposed floating shaft.
        for (int y = 2; y <= 4; y++) {
            put(blocks, -3, y, 1, Part.FRAME);
            put(blocks, -2, y, 1, Part.LADDER);
        }
        for (int y = 2; y <= 4; y++)
            blocks.remove(new Cell(-2, y, 0));

        // Elevated deck floors are stepped, not full-height rectangular towers.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int y = 4 + (floor - 1) * 4;
            int rear = -halfLength + 2;
            int front = Math.min(halfLength - 3,
                    rear + Math.max(4, plan.hullLength() / (floor + 1)));
            for (int z = rear; z <= front; z++) {
                int half = Math.max(0, halfBeam(plan, z) - (floor - 1));
                for (int x = -half; x <= half; x++) {
                    boolean hatch = x == -2 && z == rear + 5;
                    if (!hatch) put(blocks, x, y, z, Part.DECK);
                    if (Math.abs(x) == half && !hatch)
                        put(blocks, x, y + 1, z, Part.RAIL);
                    if ((z - rear) % 4 == 0 && Math.abs(x) == half)
                        for (int sy = y - 3; sy < y; sy++)
                            put(blocks, x, sy, z, Part.FRAME);
                }
            }
        }

        // Raised, tapered forecastle. A real enclosed two-block-high space
        // remains below its deck and a side ladder reaches the upper walk.
        if (plan.hullLength() >= 21) {
            int foreRear = halfLength - 7, foreFront = halfLength - 2;
            for (int z = foreRear; z <= foreFront; z++) {
                int side = Math.max(1, halfBeam(plan, z) - 1);
                for (int x = -side; x <= side; x++) {
                    boolean edge = Math.abs(x) == side || z == foreFront || z == foreRear;
                    if (edge && !(z == foreRear && x == 0)) {
                        for (int y = 5; y <= 6; y++)
                            put(blocks, x, y, z, Part.CABIN_WALL);
                        if (Math.abs(x) == side && z == foreRear + 2)
                            put(blocks, x, 6, z, Part.HULL_WINDOW);
                    }
                    put(blocks, x, 7, z, Part.DECK);
                    if (edge) put(blocks, x, 8, z, Part.RAIL);
                }
            }
            for (int y = 5; y <= 7; y++) {
                put(blocks, -3, y, foreRear - 1, Part.FRAME);
                put(blocks, -2, y, foreRear - 1, Part.LADDER);
            }
            put(blocks, 0, 6, foreRear + 2, Part.LIGHT);
        }

        // Separate square-rigged panels, stepped edges and staggered mast heights.
        // The sails remain a blueprint preview until the sail palette is enabled
        // in the moving-ship renderer.
        List<Integer> masts = plan.mastPositions();
        for (int i = 0; i < masts.size(); i++) {
            int z = masts.get(i), tier = plan.sailTiers().get(i);
            int mastTop = (tier == 1 ? 12 : tier == 2 ? 16 : 20)
                    + (masts.size() > 1 && i == masts.size() / 2 ? 2 : 0);
            int span = Math.min(plan.hullWidth() / 2 - 2,
                    tier == 1 ? 3 : tier == 2 ? Math.max(4, plan.hullWidth() / 3)
                            : Math.max(5, plan.hullWidth() / 3));
            for (int y = 5; y <= mastTop + 1; y++)
                put(blocks, 0, y, z, Part.MAST);
            if (plan.shipClass() == ShipSectionPlan.ShipClass.SMALL) {
                // A single fore-and-aft lateen-style Minecraft sail on a
                // diagonal wooden spar, rather than a miniature square wall.
                int reach = Math.min(4, halfLength - z - 1);
                for (int dz = 1; dz <= reach; dz++) {
                    int sparY = mastTop + 1 - dz;
                    put(blocks, 0, sparY, z + dz, Part.YARD);
                    for (int y = 7; y < sparY; y++)
                        blocks.putIfAbsent(new Cell(0, y, z + dz), Part.SAIL);
                }
            } else {
                int lowerTop = tier == 1 ? mastTop + 1 : 13;
                for (int x = -span; x <= span; x++) {
                    if (x == 0) continue;
                    put(blocks, x, 7, z, Part.YARD);
                    put(blocks, x, lowerTop, z, Part.YARD);
                }
                sailPanel(blocks, z, 8, lowerTop - 1, span);
                if (tier >= 2) {
                    int midSpan = Math.max(1, span - 1);
                    int middleTop = tier == 3 ? 18 : mastTop + 1;
                    for (int x = -midSpan; x <= midSpan; x++)
                        if (x != 0) put(blocks, x, middleTop, z, Part.YARD);
                    sailPanel(blocks, z, 14, middleTop - 1, midSpan);
                    if (tier == 3) {
                        int upperSpan = Math.max(1, span - 2);
                        for (int x = -upperSpan; x <= upperSpan; x++)
                            if (x != 0) put(blocks, x, mastTop + 1, z, Part.YARD);
                        sailPanel(blocks, z, 19, mastTop, upperSpan);
                    }
                }
            }
            if (plan.hullWidth() >= 9) {
                // Native vertical chain segments trace sloped forestays, one
                // on each flank behind the linen panels, without crossing them.
                int outer = Math.min(halfBeam(plan, z - 1), plan.hullWidth() / 2 - 1);
                for (int y = 6; y < mastTop; y++) {
                    int spread = Math.max(1,
                            (int)Math.round((double)outer * (mastTop - y) / (mastTop - 5)));
                    for (int side : new int[] {-1, 1})
                        blocks.putIfAbsent(new Cell(side * spread, y, z - 1), Part.RIGGING);
                }
            }
        }

        // Closed stern cabin, with a two-block door aperture and real windows.
        if (plan.cabin()) {
            int rear = -halfLength + 2, front = rear + (plan.hullWidth() == 7 ? 2 : 6);
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++) {
                    boolean wall = Math.abs(x) == side || z == rear || z == front;
                    for (int y = 5; y <= 7; y++) {
                        if (z == front && x == 0 && y <= 6) {
                            blocks.remove(new Cell(x, y, z));
                        } else if (wall) {
                            Part part = (Math.abs(x) == side && z == rear + (plan.hullWidth() == 7 ? 1 : 3) && y == 6)
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
            put(blocks, 0, 5, plan.cabin() && plan.hullWidth() == 7
                    ? 2 : -halfLength + (plan.cabin() ? 9 : 3), Part.HELM);
        }

        // Vertical access to the shorter quarterdeck / upper cabin decks.
        // Keep a solid backing post and a matching hatch in every floor.
        if (plan.decks() >= 2) {
            int accessZ = -halfLength + 7;
            for (int y = 5; y <= 4 + (plan.decks() - 1) * 4; y++) {
                put(blocks, -3, y, accessZ, Part.FRAME);
                put(blocks, -2, y, accessZ, Part.LADDER);
            }
        }

        // Full glass blocks in the above-water hull keep the dry hold sealed.
        // Internal hanging lanterns make these regularly spaced windows warm
        // instead of painting fictitious yellow pixels on a solid wall.
        if (plan.hullWidth() >= 9)
            for (int z = -halfLength + 4; z <= halfLength - 4; z++) {
                if (Math.floorMod(z, 5) != 0) continue;
                int half = halfBeam(plan, z);
                if (half < 3) continue;
                for (int side : new int[] {-1, 1}) {
                    Cell glass = new Cell(side * half, 2, z);
                    Cell lamp = new Cell(side * (half - 1), 3, z);
                    if (blocks.get(glass) == Part.HULL)
                        put(blocks, glass.x(), glass.y(), glass.z(), Part.HULL_WINDOW);
                    if (!blocks.containsKey(lamp) &&
                            blocks.get(new Cell(lamp.x(), 4, z)) == Part.DECK)
                        put(blocks, lamp.x(), lamp.y(), lamp.z(), Part.LIGHT);
                }
            }

        // Real vanilla light sources: the hold, cabin and exterior approach.
        // Dynamic illumination while the palette is carried as an entity
        // requires a separate client lighting implementation.
        for (int z : new int[] {-2, 2})
            if (blocks.get(new Cell(0, 4, z)) == Part.DECK)
                put(blocks, 0, 3, z, Part.LIGHT);
        if (plan.cabin()) {
            int rear = -halfLength + 2;
            put(blocks, 0, 7, rear + (plan.hullWidth() == 7 ? 1 : 3), Part.LIGHT);
        }
        // Two warm hanging lights under each elevated deck; only install
        // where a complete overhead block supports the vanilla lantern.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int ceiling = 4 + (floor - 1) * 4;
            int rear = -halfLength + 2;
            int front = Math.min(halfLength - 3,
                    rear + Math.max(4, plan.hullLength() / (floor + 1)));
            for (int z : new int[] {rear + 2, front - 2}) {
                Cell roof = new Cell(1, ceiling, z);
                Cell lamp = new Cell(1, ceiling - 1, z);
                if (blocks.get(roof) == Part.DECK && !blocks.containsKey(lamp))
                    blocks.put(lamp, Part.LIGHT);
            }
        }
        for (int z : new int[] {-halfLength + 4, halfLength - 3}) {
            int half = halfBeam(plan, z);
            if (half >= 2 && blocks.get(new Cell(half, 5, z)) == Part.RAIL)
                put(blocks, half, 6, z, Part.LIGHT);
        }

        return Collections.unmodifiableMap(blocks);
    }


    // Persisted V1 voxel palette. Never change this method: saved ships must
    // keep exactly the blocks originally generated before the V2 redesign.
    private static int beamLegacy(int width, int length, int z) {
        int halfWidth = width / 2, halfLength = length / 2;
        if (Math.abs(z) > halfLength) return -1;
        int taper = Math.max(0, Math.abs(z) - (halfLength - 4));
        int reduction = (int)Math.ceil(taper * halfWidth / 4D);
        int tip = z == halfLength ? 0 : z == -halfLength ? 1 : 0;
        return Math.max(tip, halfWidth - reduction);
    }


    private static int halfBeamLegacy(ShipSuperstructurePlan plan, int z) {
        return beamLegacy(plan.hullWidth(), plan.hullLength(), z);
    }

    public static Map<Cell, Part> generateLegacy(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;

        // Full-block keel, dry hold and weather deck with shaped ends.
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeamLegacy(plan, z);
            for (int x = -half; x <= half; x++) {
                put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                boolean edge = Math.abs(x) == half
                        || Math.abs(x) > halfBeamLegacy(plan, z - 1)
                        || Math.abs(x) > halfBeamLegacy(plan, z + 1);
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
                int half = Math.max(0, halfBeamLegacy(plan, z) - 1);
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


    private static void sailPanel(Map<Cell, Part> blocks, int mastZ,
                                  int bottom, int top, int halfSpan) {
        for (int y = bottom; y <= top; y++) {
            int endInset = y == bottom || y == top ? 1 : 0;
            int width = Math.max(1, halfSpan - endInset);
            for (int x = -width; x <= width; x++) {
                if (x == 0) continue;
                int bulge = Math.abs(x) < width && y > bottom && y < top ? 2 : 1;
                Cell cell = new Cell(x, y, mastZ + bulge);
                // Restrained red Slavic-style border, still genuine wool blocks.
                Part cloth = y == bottom + 1 && Math.abs(x) % 2 == 0
                        ? Part.SAIL_TRIM : Part.SAIL;
                blocks.putIfAbsent(cell, cloth);
            }
        }
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
