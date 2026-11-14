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
        KEEL, HULL, FRAME, DECK, RAIL, LADDER, STAIR, MAST, YARD, SAIL,
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM, LANTERN, TRIM, BOWSPRIT, GUNPORT_SHUTTER
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
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3) return brigantineBeam(plan, z);
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                || plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST)
            throw new UnsupportedOperationException("Withdrawn hulls have no station profile");
        if (plan.shipClass() != ShipSectionPlan.ShipClass.MEDIUM)
            return beam(plan.hullWidth(), plan.hullLength(), z);
        // The trading strug has a long, narrow bow and a fuller working
        // midship, not the same four-row taper as every other ship class.
        int halfLength = plan.hullLength() / 2;
        if (Math.abs(z) > halfLength) return -1;
        int halfWidth = plan.hullWidth() / 2;
        int taperLength = z >= 0 ? 7 : 5;
        int taper = Math.max(0, Math.abs(z) - (halfLength - taperLength));
        int reduction = (int)Math.ceil(taper * halfWidth / (double)taperLength);
        return Math.max(z == halfLength ? 0 : z == -halfLength ? 1 : 0,
                halfWidth - reduction);
    }

    /** Cabin footprint for the retained SMALL/MEDIUM ships only. */
    public static int cabinHalfWidth(ShipSuperstructurePlan plan) {
        return Math.min(3, plan.hullWidth() / 2 - 2);
    }

    public static int sailLowerYard(ShipSuperstructurePlan plan) {
        return plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3 ? brigantineTopDeck(plan) + 4 : 7;
    }

    public static int sailTop(ShipSuperstructurePlan plan, int tier) {
        if (tier < 1 || tier > 3) throw new IllegalArgumentException("Unknown sail tier");
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3)
            return sailLowerYard(plan) + (tier == 1 ? 5 : tier == 2 ? 8 : 12);
        return tier == 1 ? 12 : tier == 2 ? 15 : 19;
    }

    public static int sailBottom(ShipSuperstructurePlan plan) {
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3) return sailLowerYard(plan) + 1;
        return plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                && plan.decks() >= 2 ? 10 : 8;
    }


    /** Revision 3: every vertical element uses one actual top working deck.
     * Lower hold y=1, gun deck y=5, weather deck y=10, raised decks y=15/20.
     * The mast, yards and rendered cloth all rise with each deck modification. */
    public static int brigantineTopDeck(ShipSuperstructurePlan plan) {
        return plan.decks() >= 3 ? 20
                : plan.decks() >= 2 || plan.bridge() || plan.cabin() ? 15 : 10;
    }

    private static int brigantineBeam(ShipSuperstructurePlan plan, int z) {
        int end = plan.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = plan.hullWidth() / 2;
        int bow = end - z, aft = end + z;
        int bowRun = Math.min(12, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * Math.sin(t * Math.PI / 2D)));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        int waist = Math.abs(z) <= 3 ? 1 : 0;
        return half - waist;
    }

    /**
     * A real walkable five-block flight, climbing toward +Z.
     * Replace the occupied floor cells above each tread and clear two
     * headroom blocks; the fifth step joins the next existing deck.
     */
    private static void brigantineStairs(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        // Fixed stairwell: a clear lower approach, a complete flight and
        // a clear upper landing. Never scatter single steps at random.
        for (int y = lowerFloor + 1; y <= lowerFloor + 2; y++)
            blocks.remove(new Cell(x, y, startZ));
        for (int i = 1; i <= higherFloor - lowerFloor; i++) {
            int y = lowerFloor + i, z = startZ + i;
            blocks.remove(new Cell(x, y, z));
            for (int supportY = lowerFloor + 1; supportY < y; supportY++)
                put(blocks, x, supportY, z, Part.FRAME);
            blocks.remove(new Cell(x, y + 1, z));
            blocks.remove(new Cell(x, y + 2, z));
            put(blocks, x, y, z, Part.STAIR);
        }
        // Two-block clear landing; neither level may trap the player.
        int exit = startZ + higherFloor - lowerFloor + 1;
        // Preserve the actual floor at the landing, remove only head blockers.
        blocks.remove(new Cell(x, higherFloor + 1, exit));
        blocks.remove(new Cell(x, higherFloor + 2, exit));
    }

    private static void clearBrigantineStairHeadroom(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        for (int y = lowerFloor + 1; y <= lowerFloor + 2; y++)
            blocks.remove(new Cell(x, y, startZ));
        for (int i = 1; i <= higherFloor - lowerFloor; i++) {
            int y = lowerFloor + i, z = startZ + i;
            for (int head = y + 1; head <= y + 2; head++)
                blocks.remove(new Cell(x, head, z));
            put(blocks, x, y, z, Part.STAIR);
        }
        int landingZ = startZ + higherFloor - lowerFloor + 1;
        blocks.remove(new Cell(x, higherFloor + 1, landingZ));
        blocks.remove(new Cell(x, higherFloor + 2, landingZ));
    }

    /**
     * A stepped bilge must be FACE-CLOSED, not merely diagonally touching.
     * Complete the solid floor and the side/shoulder seams before creating
     * the intentional stair openings on the higher decks.
     */
    private static void sealBrigantineBilge(Map<Cell, Part> blocks,
            ShipSuperstructurePlan plan) {
        int end = plan.hullLength() / 2;
        for (int z = -end; z <= end; z++) {
            int width = brigantineBeam(plan, z);
            int previous = z == -end ? -1 : brigantineBeam(plan, z - 1);
            int next = z == end ? -1 : brigantineBeam(plan, z + 1);
            for (int x = -width; x <= width; x++) {
                int distance = Math.abs(x);
                if (distance <= width - 1)
                    put(blocks, x, 1, z, Part.HULL);
                boolean rim = distance >= width - 1
                        || previous < 0 || next < 0
                        || distance >= previous - 1 || distance >= next - 1;
                if (rim)
                    for (int y = 2; y <= 4; y++)
                        put(blocks, x, y, z, Part.HULL);
            }
        }
    }

    /**
     * Lamps never replace floors, hull planking, stairs or supports.
     * A fixture must rest on a solid block and cannot obstruct a stairwell.
     */
    private static void sanitizeBrigantineLanterns(Map<Cell, Part> blocks) {
        java.util.List<Cell> lamps = new java.util.ArrayList<>();
        for (var entry : blocks.entrySet())
            if (entry.getValue() == Part.LANTERN) lamps.add(entry.getKey());
        for (Cell lamp : lamps) {
            Part below = blocks.get(new Cell(lamp.x(), lamp.y() - 1, lamp.z()));
            boolean supported = below == Part.DECK || below == Part.BRIDGE
                    || below == Part.HULL || below == Part.TRIM
                    || below == Part.FRAME;
            boolean closeToStair = false;
            for (int dx = -1; dx <= 1 && !closeToStair; dx++)
                for (int dz = -1; dz <= 1 && !closeToStair; dz++) {
                    Part at = blocks.get(new Cell(lamp.x() + dx, lamp.y(),
                            lamp.z() + dz));
                    closeToStair = at == Part.STAIR;
                }
            if (!supported || closeToStair)
                blocks.remove(lamp);
        }
    }

    private static void validateBrigantineStair(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        Part approach = blocks.get(new Cell(x, lowerFloor, startZ));
        if (approach != Part.DECK && approach != Part.BRIDGE
                && approach != Part.HULL)
            throw new IllegalStateException("Missing supported stair approach at "
                    + x + "," + lowerFloor + "," + startZ);
        for (int i = 1; i <= higherFloor - lowerFloor; i++) {
            int y = lowerFloor + i, z = startZ + i;
            if (blocks.get(new Cell(x, y, z)) != Part.STAIR
                    || blocks.containsKey(new Cell(x, y + 1, z))
                    || blocks.containsKey(new Cell(x, y + 2, z)))
                throw new IllegalStateException(
                        "Blocked brigantine stair at " + x + "," + y + "," + z);
        }
        int exit = startZ + higherFloor - lowerFloor + 1;
        Part landing = blocks.get(new Cell(x, higherFloor, exit));
        if (landing != Part.DECK && landing != Part.BRIDGE
                || blocks.containsKey(new Cell(x, higherFloor + 1, exit))
                || blocks.containsKey(new Cell(x, higherFloor + 2, exit)))
            throw new IllegalStateException("Missing safe stair landing at "
                    + x + "," + higherFloor + "," + exit);
        if (blocks.containsKey(new Cell(x, lowerFloor + 1, startZ))
                || blocks.containsKey(new Cell(x, lowerFloor + 2, startZ)))
            throw new IllegalStateException("Blocked stair approach at "
                    + x + "," + lowerFloor + "," + startZ);
        // The landing must join a real walking surface one block beyond it,
        // rather than ending at the railing and dropping into the sea.
        Part onward = blocks.get(new Cell(x, higherFloor, exit + 1));
        if (onward != Part.DECK && onward != Part.BRIDGE)
            throw new IllegalStateException("Stair landing leads to a hole at "
                    + x + "," + higherFloor + "," + (exit + 1));
    }

    private static void validateBrigantineBilge(Map<Cell, Part> blocks,
            ShipSuperstructurePlan plan) {
        int end = plan.hullLength() / 2;
        for (int z = -end; z <= end; z++) {
            int beam = brigantineBeam(plan, z);
            int previous = z == -end ? -1 : brigantineBeam(plan, z - 1);
            int next = z == end ? -1 : brigantineBeam(plan, z + 1);
            for (int x = -beam; x <= beam; x++) {
                int distance = Math.abs(x);
                if (distance <= beam - 1
                        && blocks.get(new Cell(x, 1, z)) != Part.HULL)
                    throw new IllegalStateException("Open brigantine bottom at "
                            + x + ",1," + z);
                boolean exterior = distance >= beam - 1
                        || previous < 0 || next < 0
                        || distance >= previous - 1 || distance >= next - 1;
                if (exterior)
                    for (int y = 2; y <= 4; y++)
                        if (blocks.get(new Cell(x, y, z)) != Part.HULL)
                            throw new IllegalStateException("Open brigantine bilge at "
                                    + x + "," + y + "," + z);
            }
        }
    }

    private static Map<Cell, Part> generateBrigantine(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 2;
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = brigantineBeam(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0 && Math.abs(z) < end - 1) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                // A continuous timber floor covers the entire lower bilge.
                // The previous stepped-only floor left water-visible slots.
                put(out, x, 1, z, Part.HULL);
                if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // The lower hull must have a continuous shell at each Y;
                // a single diagonal staircase of blocks leaves huge slits.
                if (abs == beam - 1)
                    for (int y = 2; y <= 4; y++) put(out, x, y, z, Part.HULL);
                if (abs == beam - 2)
                    for (int y = 2; y <= 3; y++) put(out, x, y, z, Part.HULL);
                // Gun deck spans a full enclosed storey. Closed timber
                // shutters mark future artillery openings (no open holes).
                if (abs <= beam - 1)
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1)
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    if (abs != beam - 1) continue;
                    boolean gunport = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    put(out, x, y, z, gunport ? Part.GUNPORT_SHUTTER
                            : y == 6 || y == 9 ? Part.TRIM
                            : Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                }
                // Tumblehome: gunport shoulders are wider than the upper rail.
                if (abs == beam - 1) {
                    put(out, x, 11, z,
                            Math.floorMod(z, 5) == 0 ? Part.FRAME : Part.HULL);
                    int bow = end - z, stern = end + z;
                    int lift = bow <= 2 ? 2 : bow <= 6 ? 1
                            : stern <= 3 ? 2 : stern <= 6 ? 1 : 0;
                    for (int y = 12; y <= 11 + lift; y++)
                        put(out, x, y, z,
                                y == 11 + lift ? Part.TRIM : Part.FRAME);
                }
            }
        }
        // Seal the transverse end faces. The original blueprint only
        // generated the left/right skin at each station: looking along Z
        // exposed a full-height open mouth into the gun deck and hold.
        // Fill the end caps and the exposed shoulders at every abrupt
        // change in beam. Keep deliberate side gunports untouched.
        for (int z = -end; z <= end; z++) {
            int beam = brigantineBeam(p, z);
            int outwardBeam = z >= 0 ? brigantineBeam(p, z + 1)
                    : brigantineBeam(p, z - 1);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                boolean endFace = Math.abs(z) == end;
                boolean exposedShoulder = outwardBeam >= 0 && abs > outwardBeam;
                if (!endFace && !exposedShoulder) continue;
                for (int y = 5; y <= 9; y++) {
                    // Side ports are exactly two blocks high and only at
                    // stations far from either extremity.
                    put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM : Part.HULL);
                }
                // A narrowing section exposes its side-facing lower bilge.
                // Seal that face too, rather than only the top gun-deck wall.
                if (exposedShoulder)
                    for (int y = 2; y <= 4; y++)
                        put(out, x, y, z, Part.HULL);
                // Close the hold and deck seams at the very tips too:
                // otherwise the end still exposes a two-to-four-block slot
                // when viewed straight down the ship axis.
                if (endFace) {
                    for (int y = 2; y <= 4; y++)
                        put(out, x, y, z, y == 4 ? Part.TRIM : Part.HULL);
                    put(out, x, 10, z, Part.DECK);
                    put(out, x, 11, z, Part.HULL);
                    if (abs <= Math.max(0, beam - 1)) put(out, x, 12, z, Part.TRIM);
                }
            }
        }
        sealBrigantineBilge(out, p);
        // Short true bowsprit: part of the moving block palette, not a fake
        // mast or a detached decorative entity.
        for (int z = end; z <= end + 3; z++)
            put(out, 0, 12, z, Part.BOWSPRIT);
        // Permanent functional lighting, not glowing cosmetic textures.
        // Posts stand inside the bulwark without blocking the central aisle.
        for (int z : new int[]{-end + 8, end - 8}) {
            int beam = brigantineBeam(p, z);
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 11, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Actual light sources at both low interior levels, on five-block
        // centers, not only two widely separated lantern pairs.
        for (int z = -end + 7; z <= end - 7; z += 5) {
            int beam = brigantineBeam(p, z);
            if (beam < 6) continue;
            // Keep the central corridor free: attach light blocks near the
            // inner side walls instead of placing them in walking lanes.
            for (int x : new int[]{-beam + 2, beam - 2}) {
                put(out, x, 2, z, Part.LANTERN);
                put(out, x, 6, z, Part.LANTERN);
            }
        }
        // Two separated stairwells: aft / port and bow / starboard.
        // The two lower floors share safe routes without vertical ladders.
        for (int[] route : new int[][]{{-2, -10}, {2, 4}})
            brigantineStairs(out, route[0], route[1], 1, 5);
        // Stagger the upper flights to the OPPOSITE side. Using the same
        // X/Z column for stacked flights creates support beams directly
        // above lower stair treads, so players cannot finish climbing.
        for (int[] route : new int[][]{{3, -8}, {-3, 3}})
            brigantineStairs(out, route[0], route[1], 5, 10);
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            // Two accessible stairwells will join the weather deck to this
            // raised deck after the cabin walls have been constructed.
            // A raised deck needs a real aft bulkhead. Side pillars alone
            // leave a giant through-hole when viewed from the stern.
            for (int x = -half; x <= half; x++)
                for (int y = 11; y <= 14; y++)
                    put(out, x, y, rear,
                            y == 12 && (x == -half + 2 || x == half - 2)
                                    ? Part.WINDOW : Part.CABIN_WALL);
            // Enclose the side of the raised stern deck with real timber
            // and framed windows. Columns alone exposed the entire storey.
            for (int z = rear + 1; z <= front; z++)
                for (int side : new int[]{-half, half})
                    for (int y = 11; y <= 14; y++)
                        put(out, side, y, z,
                                y == 12 && Math.floorMod(z - rear, 4) == 2
                                        ? Part.WINDOW : Part.CABIN_WALL);
            // Two-block access door in the forward wall.
            for (int x = -half + 1; x < half; x++)
                for (int y = 11; y <= 14; y++) {
                    if (x == 0 && (y == 11 || y == 12))
                        out.remove(new Cell(x, y, front));
                    else put(out, x, y, front, Part.CABIN_WALL);
                }
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 8;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            // Twin stairwells above this level are carved after all walls.
            // Third-level quarterdeck also closes across its entire aft face.
            for (int x = -half; x <= half; x++)
                for (int y = 16; y <= 19; y++)
                    put(out, x, y, rear,
                            y == 17 && Math.abs(x) == Math.max(1, half - 1)
                                    ? Part.WINDOW : Part.CABIN_WALL);
            for (int z = rear + 1; z <= front; z++)
                for (int side : new int[]{-half, half})
                    for (int y = 16; y <= 19; y++)
                        put(out, side, y, z,
                                y == 17 && Math.floorMod(z - rear, 3) == 1
                                        ? Part.WINDOW : Part.CABIN_WALL);
            for (int x = -half + 1; x < half; x++)
                for (int y = 16; y <= 19; y++) {
                    if (x == 0 && (y == 16 || y == 17))
                        out.remove(new Cell(x, y, front));
                    else put(out, x, y, front, Part.CABIN_WALL);
                }
            put(out, half - 1, 21, rear + 2, Part.LANTERN);
            put(out, -half + 1, 21, rear + 2, Part.LANTERN);
        }
        if (p.cabin()) {
            int half = Math.min(4, p.hullWidth() / 2 - 4);
            // Cabin is on the weather deck, beneath the actual stern deck.
            for (int z = rear + 1; z <= rear + 6; z++)
                for (int x = -half; x <= half; x++) {
                    boolean wall = Math.abs(x) == half || z == rear + 1 || z == rear + 6;
                    if (!wall) continue;
                    for (int y = 11; y <= 14; y++) {
                        if (z == rear + 6 && x == 0 && (y == 11 || y == 12))
                            put(out, x, y, z, Part.DOOR);
                        else put(out, x, y, z,
                                Math.abs(x) == half && y == 12 && z == rear + 3
                                        ? Part.WINDOW : Part.CABIN_WALL);
                    }
                }
        }
        // A bridge upgrade must have visible geometry even when a raised
        // deck already exists. Keep the captain's wheel accessible.
        if (p.bridge()) {
            for (int z = rear + 1; z <= rear + 5; z++)
                for (int x : new int[]{-3, 3})
                    put(out, x, top + 1, z, Part.RAIL);
            // A small sheltered command canopy distinguishes the actual
            // captain bridge from a basic exposed steering position. It sits
            // BEHIND the wheel, so the player and wheel remain accessible.
            for (int z = rear + 1; z <= rear + 2; z++) {
                for (int x = -2; x <= 2; x++)
                    put(out, x, top + 4, z, Part.BRIDGE);
                for (int x : new int[]{-2, 2})
                    for (int y = top + 1; y <= top + 3; y++)
                        put(out, x, y, z, Part.FRAME);
            }
            for (int x : new int[]{-2, 2})
                put(out, x, top + 2, rear + 4, Part.LANTERN);
        }
        // Rebuild both raised access routes AFTER all cabin and stern walls.
        // This prevents a later upgrade from sealing an earlier passage.
        if (top >= 15) {
            for (int x : new int[]{-2, 2})
                brigantineStairs(out, x, rear + 1, 10, 15);
            // Light the elevated deck from supported side positions, not the
            // middle of the stair approach or inside the captain's cabin.
            for (int z : new int[]{rear + 2, rear + 5})
                for (int x : new int[]{-3, 3})
                    put(out, x, 16, z, Part.LANTERN);
        }
        if (top >= 20) {
            for (int x : new int[]{-1, 1})
                brigantineStairs(out, x, rear, 15, 20);
            for (int z : new int[]{rear + 2, rear + 5})
                put(out, 0, 21, z, Part.LANTERN);
        }
        // Stair approaches must remain walkable; deck lights are placed away
        // from the centerline on already-supported side posts.
        for (int z : new int[]{-9, -5, 5, 10}) {
            int side = brigantineBeam(p, z) - 2;
            if (side > 3)
                for (int y : new int[]{6, 11})
                    put(out, side, y, z, Part.LANTERN);
        }

        // Every installed mast is rooted in the main deck; the complete
        // visible rig begins above the highest raised floor, never below it.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i), tier = p.sailTiers().get(i);
            int upper = sailTop(p, tier), yard = sailLowerYard(p);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 11; y <= upper + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(out, x, yard, z, Part.YARD);
                put(out, x, upper + 1, z, Part.YARD);
            }
            for (int y = sailBottom(p); y <= upper; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !out.containsKey(new Cell(x, y, z + 1)))
                        put(out, x, y, z + 1, Part.SAIL);
        }
        // Run this after every wall, mast, lantern and cabin placement: no
        // later decorative block may obstruct either flight or its landing.
        for (int[] route : new int[][]{{-2, -10, 1, 5}, {2, 4, 1, 5},
                {3, -8, 5, 10}, {-3, 3, 5, 10}})
            clearBrigantineStairHeadroom(out, route[0], route[1], route[2], route[3]);
        if (top >= 15)
            for (int x : new int[]{-2, 2})
                clearBrigantineStairHeadroom(out, x, rear + 1, 10, 15);
        if (top >= 20)
            for (int x : new int[]{-1, 1})
                clearBrigantineStairHeadroom(out, x, rear, 15, 20);

        sanitizeBrigantineLanterns(out);
        validateBrigantineBilge(out, p);
        for (int[] route : new int[][]{{-2, -10, 1, 5}, {2, 4, 1, 5},
                {3, -8, 5, 10}, {-3, 3, 5, 10}})
            validateBrigantineStair(out, route[0], route[1], route[2], route[3]);
        if (top >= 15)
            for (int x : new int[]{-2, 2})
                validateBrigantineStair(out, x, rear + 1, 10, 15);
        if (top >= 20)
            for (int x : new int[]{-1, 1})
                validateBrigantineStair(out, x, rear, 15, 20);
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        // Tier III/IV designs were rejected and must not silently regenerate
        // the old barge-shaped hull. Re-enable only after a new blueprint is
        // explicitly approved and implemented; tiers I/II remain unchanged.
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3) return generateBrigantine(plan);
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                || plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST)
            throw new UnsupportedOperationException(
                    "Old LARGE/LARGEST hull designs withdrawn");
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
                if (edge) for (int y = 1; y <= 3; y++) {
                    // Timber sheer band and spaced upright ribs break up the
                    // untextured full-height slab without opening the dry hold.
                    // All replacement cells remain solid structural blocks.
                    boolean trading = plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM;
                    boolean side = Math.abs(x) == half && Math.abs(z) <= halfLength - 3;
                    Part board = trading && side && y == 2 ? Part.KEEL
                            : trading && side && Math.floorMod(z, 5) == 0
                                    ? Part.FRAME : Part.HULL;
                    put(blocks, x, y, z, board);
                } else if (Math.floorMod(z, 3) == 0)
                    put(blocks, x, 3, z, Part.FRAME);
                if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
                if (edge) put(blocks, x, 5, z, Part.RAIL);
            }
        }
        // The hold shaft stops at the main deck on medium ships. Their
        // quarterdeck has its own exterior ladder near the stern, so a
        // second mast-height freestanding ladder is not generated amidships.
        int mainShaftTop = plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                ? 4 : 4 + (plan.decks() - 1) * 4;
        for (int y = 1; y <= mainShaftTop; y++) {
            put(blocks, -3, y, 1, Part.FRAME);
            put(blocks, -2, y, 1, Part.LADDER);
        }
        for (int y = 1; y <= 4; y++)
            blocks.remove(new Cell(-2, y, 0));

        // Elevated deck floors are stepped, not full-height rectangular towers.
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int y = 4 + (floor - 1) * 4;
            int rear = -halfLength + 2;
            // The medium ship gets a short, raised quarterdeck. A long
            // full-width second floor made the vessel look like a two-storey
            // rectangular building and interfered with the mast.
            boolean mediumQuarterdeck = plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                    && floor == 2;
            int front = mediumQuarterdeck ? rear + (plan.cabin() ? 6 : 5)
                    : halfLength - 3 * (floor - 1);
            for (int z = rear; z <= front; z++) {
                int half = Math.max(0, halfBeam(plan, z) - 1);
                for (int x = -half; x <= half; x++) {
                    boolean hatch = !mediumQuarterdeck && x == -2 && (z == 0 || z == 1);
                    if (!hatch) put(blocks, x, y, z, Part.DECK);
                    if (Math.abs(x) == half && !hatch)
                        put(blocks, x, y + 1, z, Part.RAIL);
                    if ((z - rear) % 4 == 0 && Math.abs(x) == half)
                        for (int sy = y - 3; sy < y; sy++)
                            put(blocks, x, sy, z, Part.FRAME);
                }
            }
            if (mediumQuarterdeck) {
                // External access is separate from the hold hatch. Put the
                // ladder immediately forward of the quarterdeck, never inside
                // the captain's cabin or through an occupied mast station.
                int accessZ = front + 1;
                for (int ladderY = 5; ladderY <= 8; ladderY++) {
                    put(blocks, -3, ladderY, accessZ, Part.FRAME);
                    put(blocks, -2, ladderY, accessZ, Part.LADDER);
                }
            }
        }

        // Real per-mast stations. A mast at one coordinate never overwrites
        // another because spacing is checked by ShipSuperstructurePlan.
        List<Integer> masts = plan.mastPositions();
        for (int i = 0; i < masts.size(); i++) {
            int z = masts.get(i), tier = plan.sailTiers().get(i);
            int top = sailTop(plan, tier);
            int lowerYard = sailLowerYard(plan);
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            for (int y = 5; y <= top + 1; y++) put(blocks, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                put(blocks, x, top + 1, z, Part.YARD);
                put(blocks, x, lowerYard, z, Part.YARD);
            }
            int sailBottom = sailBottom(plan);
            for (int y = sailBottom; y <= top; y++)
                for (int x = -span; x <= span; x++)
                    if (x != 0 && !blocks.containsKey(new Cell(x, y, z + 1)))
                        put(blocks, x, y, z + 1, Part.SAIL);
        }

        // Closed stern cabin, with a two-block door aperture and real windows.
        if (plan.cabin()) {
            int rear = -halfLength + 2, front = rear + 6;
            int side = cabinHalfWidth(plan);
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
            int bridgeFloor = 9;
            int rear = -halfLength + 2, front = rear + 4;
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            int ladderX = -side + 1;
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++) {
                    put(blocks, x, bridgeFloor, z, Part.BRIDGE);
                    boolean entrance = z == front && x == ladderX
                            && plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM;
                    if ((Math.abs(x) == side || z == rear || z == front) && !entrance)
                        put(blocks, x, bridgeFloor + 1, z, Part.RAIL);
                }
            for (int y = 5; y <= bridgeFloor; y++) {
                if (plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                        && !plan.cabin() && y < bridgeFloor)
                    put(blocks, -side, y, front, Part.FRAME);
                put(blocks, ladderX, y, front, Part.LADDER);
            }
            put(blocks, 0, bridgeFloor + 1, rear + 2, Part.HELM);
        } else if (plan.cabin() && plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM) {
            // The cabin roof IS the captain's platform when no separate
            // bridge was purchased. Keep the only helm above the room.
            int rear = -halfLength + 2, front = rear + 6;
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++)
                    if ((Math.abs(x) == side || z == rear || z == front)
                            && !(z == front && x == -side + 1))
                        put(blocks, x, 9, z, Part.RAIL);
            // Support the external ladder against a real post. Leave its
            // opening clear through an optional second-deck floor.
            for (int y = 5; y <= 9; y++) {
                put(blocks, -side, y, front + 1, Part.FRAME);
                put(blocks, -side + 1, y, front + 1, Part.LADDER);
            }
            put(blocks, 0, 9, rear + 2, Part.HELM);
        } else if (plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                && plan.decks() >= 2) {
            // With a raised quarterdeck the wheel must be above it, not
            // hidden underneath the floor at main-deck height.
            put(blocks, 0, 9, -halfLength + 4, Part.HELM);
        } else {
            put(blocks, 0, 5, -halfLength + (plan.cabin() ? 9 : 3), Part.HELM);
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static boolean mainHatch(int x, int z) {
        return x == -2 && (z == 0 || z == 1);
    }

    private static void put(Map<Cell, Part> blocks, int x, int y, int z, Part part) {
        if (y < 0 || y > 48) throw new IllegalArgumentException("Ship part outside vertical bounds");
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
