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
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM, LANTERN, HANGING_LANTERN, TRIM, BOWSPRIT, GUNPORT_SHUTTER
    }

    // Bounded immutable IV blueprint cache: shipyard cost previews otherwise
    // rebuild thousands of blocks repeatedly, often every rendered GUI frame.
    private static final Map<ShipSuperstructurePlan, Map<Cell, Part>> FRIGATE_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(24, .75F, true) {
                @Override protected boolean removeEldestEntry(
                        Map.Entry<ShipSuperstructurePlan, Map<Cell, Part>> eldest) {
                    return size() > 16;
                }
            });

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
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4) return frigateBeam(plan, z);
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
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4) return 21;
        return plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3 ? brigantineTopDeck(plan) + 4 : 7;
    }

    public static int sailTop(ShipSuperstructurePlan plan, int tier) {
        if (tier < 1 || tier > 3) throw new IllegalArgumentException("Unknown sail tier");
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4) return tier == 1 ? 32 : tier == 2 ? 36 : 40;
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3)
            return sailLowerYard(plan) + (tier == 1 ? 5 : tier == 2 ? 8 : 12);
        return tier == 1 ? 12 : tier == 2 ? 15 : 19;
    }

    public static int sailBottom(ShipSuperstructurePlan plan) {
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4) return 22;
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
        int bowRun = Math.min(8, end);
        int aftRun = Math.min(8, end);
        if (bow < bowRun) {
            double t = Math.max(0D, bow / (double)bowRun);
            return Math.max(1, (int)Math.round(1D + (half - 1D) * t));
        }
        if (aft < aftRun) {
            double t = Math.max(0D, aft / (double)aftRun);
            return Math.max(3, (int)Math.round(3D + (half - 3D) * Math.sin(t * Math.PI / 2D)));
        }
        return half;
    }

    /**
     * A real walkable five-block flight, climbing toward +Z.
     * Replace the occupied floor cells above each tread and clear two
     * headroom blocks; the fifth step joins the next existing deck.
     */
    private static void brigantineStairs(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        brigantineStairLane(blocks, x, startZ, lowerFloor, higherFloor);
        brigantineStairLane(blocks, stairPairX(x, lowerFloor), startZ, lowerFloor, higherFloor);
    }

    private static int stairPairX(int x, int lowerFloor) {
        return x - Integer.signum(x);
    }

    private static void brigantineStairLane(Map<Cell, Part> blocks,
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
            blocks.remove(new Cell(x, y + 3, z));
            put(blocks, x, y, z, Part.STAIR);
        }
        // Two-block clear landing; neither level may trap the player.
        int exit = startZ + higherFloor - lowerFloor + 1;
        // Preserve the actual floor at the landing, remove only head blockers.
        blocks.remove(new Cell(x, higherFloor + 1, exit));
        blocks.remove(new Cell(x, higherFloor + 2, exit));
        blocks.remove(new Cell(x, higherFloor + 3, exit));
    }

    private static void clearBrigantineStairHeadroom(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        clearBrigantineStairLane(blocks, x, startZ, lowerFloor, higherFloor);
        clearBrigantineStairLane(blocks, stairPairX(x, lowerFloor), startZ, lowerFloor, higherFloor);
    }

    private static void clearBrigantineStairLane(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        for (int y = lowerFloor + 1; y <= lowerFloor + 2; y++)
            blocks.remove(new Cell(x, y, startZ));
        for (int i = 1; i <= higherFloor - lowerFloor; i++) {
            int y = lowerFloor + i, z = startZ + i;
            for (int head = y + 1; head <= y + 3; head++)
                blocks.remove(new Cell(x, head, z));
            put(blocks, x, y, z, Part.STAIR);
        }
        int landingZ = startZ + higherFloor - lowerFloor + 1;
        blocks.remove(new Cell(x, higherFloor + 1, landingZ));
        blocks.remove(new Cell(x, higherFloor + 2, landingZ));
        blocks.remove(new Cell(x, higherFloor + 3, landingZ));
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

    private static void validateBrigantineStair(Map<Cell, Part> blocks,
            int x, int startZ, int lowerFloor, int higherFloor) {
        validateBrigantineStairLane(blocks, x, startZ, lowerFloor, higherFloor);
        validateBrigantineStairLane(blocks, stairPairX(x, lowerFloor), startZ, lowerFloor, higherFloor);
    }

    private static void validateBrigantineStairLane(Map<Cell, Part> blocks,
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
                    || blocks.containsKey(new Cell(x, y + 2, z))
                    || blocks.containsKey(new Cell(x, y + 3, z)))
                throw new IllegalStateException(
                        "Blocked brigantine stair at " + x + "," + y + "," + z);
        }
        int exit = startZ + higherFloor - lowerFloor + 1;
        Part landing = blocks.get(new Cell(x, higherFloor, exit));
        if (landing != Part.DECK && landing != Part.BRIDGE
                || blocks.containsKey(new Cell(x, higherFloor + 1, exit))
                || blocks.containsKey(new Cell(x, higherFloor + 2, exit))
                || blocks.containsKey(new Cell(x, higherFloor + 3, exit)))
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

    /** Only a deliberately routed stair may cut through a working floor. */
    private static boolean brigantineStairHatch(int x, int floorY, int z, int rear) {
        // One compact stairwell, two adjacent two-block-wide lanes.
        // Alternate port and starboard flights between successive decks.
        int[][] routes = floorY == 5
                ? new int[][]{{-3, rear + 1, 1, 5}}
                : new int[][]{{3, rear + 1, 5, 10}};
        for (int[] r : routes) {
            if (x != r[0] && x != stairPairX(r[0], r[2])) continue;
            for (int step = 1; step <= r[3] - r[2]; step++) {
                int treadY = r[2] + step;
                if (z == r[1] + step && treadY <= floorY
                        && floorY <= treadY + 3) return true;
            }
        }
        return false;
    }

    private static void sealBrigantineWorkingFloors(Map<Cell, Part> blocks,
            ShipSuperstructurePlan plan) {
        int end = plan.hullLength() / 2;
        int rear = -end + 2;
        for (int z = -end; z <= end; z++) {
            int inside = brigantineBeam(plan, z) - 2;
            for (int x = -inside; x <= inside; x++)
                for (int floor : new int[]{5, 10}) {
                    Cell cell = new Cell(x, floor, z);
                    if (!blocks.containsKey(cell)
                            && !brigantineStairHatch(x, floor, z, rear))
                        put(blocks, x, floor, z, Part.DECK);
                }
        }
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
        // Compact, permanently aligned aft stairwell; alternate sides at each deck.
        // Stepped bilge and keel, not a full-width flat underwater slab.
        for (int z = -end; z <= end; z++) {
            int beam = brigantineBeam(p, z);
            for (int x = -beam; x <= beam; x++) {
                int abs = Math.abs(x);
                if (x == 0) put(out, x, 0, z, Part.KEEL);
                // The true bilge narrows toward the keel and flares toward
                // the gunports. Only rings, not a solid full-width box, rise
                // around the three-block-high hold interior.
                // A continuous timber floor covers the entire lower bilge.
                // The previous stepped-only floor left water-visible slots.
                put(out, x, 1, z, Part.HULL);
                if (abs > beam - 3 && abs <= beam - 2)
                    put(out, x, 3, z, Part.HULL);
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
        // A two-block-deep spar grows out of the solid mid-stem, then tapers.
        for (int z = end - 7; z <= end + 5; z++) {
            int radius = z <= end + 3 ? 1 : 0;
            Part timber = z <= end ? Part.FRAME : Part.BOWSPRIT;
            for (int x = -radius; x <= radius; x++)
                for (int y = 8; y <= 9; y++) put(out, x, y, z, timber);
        }
        // Port lane rises to the gun deck; starboard lane rises to the
        // weather deck. Both stay in the same aft 7-block-deep shaft.
        // Alternating lanes prevents successive flights blocking headroom.
        int stairZ = rear + 1;
        brigantineStairs(out, -3, stairZ, 1, 5);
        brigantineStairs(out, 3, stairZ, 5, 10);
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + (top >= 20 ? 15 : 9);
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
                            y == 12 && Math.abs(x) < half
                                    ? Part.WINDOW : Part.CABIN_WALL);
            // Enclose the side of the raised stern deck with real timber
            // and framed windows. Columns alone exposed the entire storey.
            for (int z = rear + 1; z <= front; z++)
                for (int side : new int[]{-half, half})
                    for (int y = 11; y <= 14; y++)
                        put(out, side, y, z,
                                y == 12 && Math.floorMod(z - rear,
                                        p.cabin() ? 2 : 4) == 0
                                        ? Part.WINDOW : Part.CABIN_WALL);
            // Two-block access door in the forward wall.
            for (int x = -half + 1; x < half; x++)
                for (int y = 11; y <= 14; y++) {
                    if (x == 0 && (y == 11 || y == 12))
                        put(out, x, y, front, Part.DOOR);
                    else put(out, x, y, front, Part.CABIN_WALL);
                }
        }
        if (top >= 20) {
            int front = rear + 16;
            // The top stair uses x=+/-3 and its paired lane at +/-2.
            // Keep the full landing and an outer one-block cabin wall.
            int half = Math.min(4, p.hullWidth() / 2 - 3);
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
                        // An open hole on the third deck was never fitted
                        // with a usable door: give the forward cabin a
                        // complete paired door facing the ship's bow.
                        put(out, x, y, front, Part.DOOR);
                    else put(out, x, y, front, Part.CABIN_WALL);
                }
            // The upper wall is one station ahead of the y=15 quarterdeck.
            // Without these planks its newly installed lower door is
            // suspended over empty air and cannot be approached on foot.
            for (int z = front; z <= front + 1; z++)
                for (int x = -1; x <= 1; x++)
                    put(out, x, 15, z, Part.DECK);
        }
        // A bridge upgrade must have visible geometry even when a raised
        // deck already exists. Keep the captain's wheel accessible.
        if (p.bridge()) {
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
        }
        // Continue the SAME two-wide stairwell through the raised decks.
        if (top >= 15) brigantineStairs(out, -3, stairZ, 10, 15);
        if (top >= 20) brigantineStairs(out, 3, stairZ, 15, 20);
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
        clearBrigantineStairHeadroom(out, -3, stairZ, 1, 5);
        clearBrigantineStairHeadroom(out, 3, stairZ, 5, 10);
        if (top >= 15) clearBrigantineStairHeadroom(out, -3, stairZ, 10, 15);
        if (top >= 20) clearBrigantineStairHeadroom(out, 3, stairZ, 15, 20);

        sealBrigantineWorkingFloors(out, p);
        placeShipLighting(out, p, false);
        validateBrigantineBilge(out, p);
        validateBrigantineStair(out, -3, stairZ, 1, 5);
        validateBrigantineStair(out, 3, stairZ, 5, 10);
        if (top >= 15) validateBrigantineStair(out, -3, stairZ, 10, 15);
        if (top >= 20) validateBrigantineStair(out, 3, stairZ, 15, 20);
        // One physical wheel on the highest accessible captain's platform.
        int wheelZ = rear + 3;
        put(out, 0, top + 1, wheelZ, Part.HELM);
        return Collections.unmodifiableMap(out);
    }


    // IV is a separate four-masted frigate, never a scaled LARGE brigantine.
    private static int frigateBeam(ShipSuperstructurePlan p, int z) {
        int end = p.hullLength() / 2;
        if (Math.abs(z) > end) return -1;
        int half = p.hullWidth() / 2;
        // +Z is the actual bow. Retain a substantial five-block-wide stem
        // at the foremost station and ease the beam inward over twelve
        // stations rather than ending in a misplaced one-block needle.
        if (z >= end - 12) {
            double t = Math.max(0D, (end - z) / 12D);
            return Math.max(2, (int)Math.round(2D + (half - 2D)
                    * Math.sin(t * Math.PI / 2D)));
        }
        if (z <= -end + 8) {
            double t = Math.max(0D, (end + z) / 8D);
            return Math.max(3, (int)Math.round(3D + (half - 3D)
                    * Math.sin(t * Math.PI / 2D)));
        }
        return half;
    }

    private static void frigateStairs(Map<Cell, Part> blocks,
            int x, int startZ, int lower, int upper) {
        frigateStairLane(blocks, x, startZ, lower, upper);
        frigateStairLane(blocks, stairPairX(x, lower), startZ, lower, upper);
    }

    private static void frigateStairLane(Map<Cell, Part> blocks,
            int x, int startZ, int lower, int upper) {
        if (upper - lower != 5)
            throw new IllegalArgumentException("IV stairs must connect five-block decks");
        Part approach = blocks.get(new Cell(x, lower, startZ));
        Part landing = blocks.get(new Cell(x, upper, startZ + 6));
        boolean safeApproach = approach == Part.DECK || approach == Part.BRIDGE
                || lower == 1 && approach == Part.HULL;
        boolean safeLanding = landing == Part.DECK || landing == Part.BRIDGE;
        if (!safeApproach || !safeLanding)
            throw new IllegalStateException("IV staircase requires actual walking floors at "
                    + x + "," + startZ + " -> " + (startZ + 6));
        for (int i = 0; i <= 6; i++) {
            int z = startZ + i;
            int floor = i == 0 ? lower : i == 6 ? upper : lower + i;
            for (int y = floor + 1; y <= floor + 3; y++)
                blocks.remove(new Cell(x, y, z));
            if (i >= 1 && i <= 5) {
                blocks.remove(new Cell(x, floor, z));
                put(blocks, x, floor, z, Part.STAIR);
                for (int y = lower + 1; y < floor; y++)
                    if (!blocks.containsKey(new Cell(x, y, z)))
                        put(blocks, x, y, z, Part.FRAME);
            }
        }
        for (int i = 0; i <= 6; i++) {
            int floor = i == 0 ? lower : i == 6 ? upper : lower + i;
            if (blocks.containsKey(new Cell(x, floor + 1, startZ + i))
                    || blocks.containsKey(new Cell(x, floor + 2, startZ + i))
                    || blocks.containsKey(new Cell(x, floor + 3, startZ + i)))
                throw new IllegalStateException("IV blocked staircase at "
                        + x + "," + floor + "," + (startZ + i));
        }
    }

    private static boolean lightSupport(Part part) {
        return part == Part.HULL || part == Part.DECK
                || part == Part.BRIDGE || part == Part.FRAME || part == Part.TRIM;
    }

    private static void placeSpacedLight(Map<Cell, Part> blocks,
            int x, int y, int z, boolean hanging) {
        Cell cell = new Cell(x, y, z);
        if (blocks.containsKey(cell) || !lightSupport(blocks.get(new Cell(
                x, y + (hanging ? 1 : -1), z)))) return;
        for (int clearance = 0; clearance <= 3; clearance++)
            if (blocks.get(new Cell(x, y - clearance, z)) == Part.STAIR) return;
        for (var entry : blocks.entrySet()) {
            if (entry.getValue() != Part.LANTERN
                    && entry.getValue() != Part.HANGING_LANTERN) continue;
            Cell other = entry.getKey();
            int dx = x - other.x(), dy = y - other.y(), dz = z - other.z();
            if (dx * dx + dy * dy + dz * dz < 25) return;
        }
        blocks.put(cell, hanging ? Part.HANGING_LANTERN : Part.LANTERN);
    }

    private static void placeShipLighting(Map<Cell, Part> blocks,
            ShipSuperstructurePlan plan, boolean frigate) {
        int end = plan.hullLength() / 2;
        int[] floors = frigate ? new int[]{1, 6, 11, 16, 21}
                : new int[]{1, 5, 10, 15, 20};
        for (int floor : floors)
            for (int z = -end + 4; z <= end - 4; z += 7) {
                int beam = frigate ? frigateBeam(plan, z) : brigantineBeam(plan, z);
                if (beam < 4) continue;
                int side = Math.min(beam - 2, frigate ? 6 : 4);
                int ceiling = floor == 1 && !frigate ? 5 : floor + 5;
                // A deck is a walking surface, never a lantern stand.
                // Only secure lights to the underside of a ceiling beam.
                for (int x : new int[]{-side, side})
                    if (lightSupport(blocks.get(new Cell(x, ceiling, z))))
                        placeSpacedLight(blocks, x, ceiling - 1, z, true);
                int centerZ = z + (frigate ? 0 : 3);
                if (centerZ <= end - 4
                        && lightSupport(blocks.get(new Cell(0, ceiling, centerZ))))
                    placeSpacedLight(blocks, 0, ceiling - 1, centerZ, true);
            }
    }

    private static boolean frigateStairHole(int x, int floor, int z, int rear) {
        int[][] routes = floor == 6
                ? new int[][]{{-3, rear + 2}}
                : floor == 11
                ? new int[][]{{3, rear + 2}}
                : floor == 16
                ? new int[][]{{-3, rear + 2}}
                : floor == 21
                ? new int[][]{{3, rear + 2}}
                : new int[0][];
        for (int[] r : routes) {
            int lower = floor == 6 ? 1 : floor == 11 ? 6 : floor == 16 ? 11 : 16;
            if ((x == r[0] || x == stairPairX(r[0], lower))
                    && z >= r[1] + 2 && z <= r[1] + 5)
                return true;
        }
        return false;
    }

    private static void validateFrigateStairLandings(Map<Cell, Part> blocks,
            int x, int z, int lower, int upper) {
        validateFrigateStairLane(blocks, x, z, lower, upper);
        validateFrigateStairLane(blocks, stairPairX(x, lower), z, lower, upper);
    }

    private static void validateFrigateStairLane(Map<Cell, Part> blocks,
            int x, int z, int lower, int upper) {
        Part start = blocks.get(new Cell(x, lower, z));
        Part finish = blocks.get(new Cell(x, upper, z + 6));
        if (!(start == Part.DECK || start == Part.BRIDGE
                || lower == 1 && start == Part.HULL)
                || finish != Part.DECK && finish != Part.BRIDGE)
            throw new IllegalStateException("IV stair is not connected to two walkable decks");
        for (int i : new int[]{0, 6}) {
            int floor = i == 0 ? lower : upper;
            int atZ = z + i;
            for (int head = 1; head <= 3; head++)
                if (blocks.containsKey(new Cell(x, floor + head, atZ)))
                    throw new IllegalStateException("IV stair landing head obstruction at "
                            + x + "," + (floor + head) + "," + atZ);
        }
        // Recheck the complete flight AFTER all railings, lanterns,
        // wheel and decorative pieces have been added.
        for (int i = 1; i <= 5; i++) {
            int y = lower + i, atZ = z + i;
            if (blocks.get(new Cell(x, y, atZ)) != Part.STAIR)
                throw new IllegalStateException("IV missing stair at " + x + "," + y + "," + atZ);
            for (int head = 1; head <= 3; head++)
                if (blocks.containsKey(new Cell(x, y + head, atZ)))
                    throw new IllegalStateException("IV stair blocks player head at "
                            + x + "," + (y + head) + "," + atZ);
        }
    }

    private static void validateFrigateIV(Map<Cell, Part> blocks,
            ShipSuperstructurePlan plan, int rear) {
        int end = plan.hullLength() / 2;
        for (int z = rear; z <= rear + 15; z++) {
            int half = Math.min(7, Math.max(2, frigateBeam(plan, z) - 2));
            for (int x : new int[]{-half, half})
                for (int y = 12; y <= 15; y++) {
                    Part wall = blocks.get(new Cell(x, y, z));
                    if (wall != Part.CABIN_WALL && wall != Part.WINDOW)
                        throw new IllegalStateException("IV open quarterdeck side at "
                                + x + "," + y + "," + z);
                }
        }
        for (int z = -end; z <= end; z++) {
            int width = frigateBeam(plan, z);
            int prev = frigateBeam(plan, z - 1), next = frigateBeam(plan, z + 1);
            for (int x = -width; x <= width; x++) {
                if (blocks.get(new Cell(x, 1, z)) != Part.HULL)
                    throw new IllegalStateException("IV open bottom at "
                            + x + ",1," + z);
                int d = Math.abs(x);
                boolean exterior = d >= width - 1 || prev < 0 || next < 0
                        || d > prev || d > next;
                if (exterior) {
                    for (int y = 2; y <= 5; y++)
                        if (blocks.get(new Cell(x, y, z)) != Part.HULL)
                            throw new IllegalStateException("IV bilge leak at "
                                    + x + "," + y + "," + z);
                } else {
                    for (int y : new int[]{6, 11}) {
                        Part actual = blocks.get(new Cell(x, y, z));
                        if (actual != Part.DECK && actual != Part.STAIR
                                && !frigateStairHole(x, y, z, rear))
                            throw new IllegalStateException("IV unexpected deck hole at "
                                    + x + "," + y + "," + z);
                    }
                }
            }
        }
        for (int[] route : new int[][] {
                {-3, rear + 2, 1, 6}, {3, rear + 2, 6, 11},
                {-3, rear + 2, 11, 16}
        })
            validateFrigateStairLandings(blocks, route[0], route[1], route[2], route[3]);
        if (plan.decks() >= 2 || plan.bridge())
            validateFrigateStairLandings(blocks, 3, rear + 2, 16, 21);
        for (int z : plan.mastPositions())
            if (blocks.get(new Cell(0, 12, z)) != Part.MAST)
                throw new IllegalStateException("IV mast base missing at " + z);
        for (int[] flight : new int[][]{{-3, rear + 2, 1},
                {3, rear + 2, 6}, {-3, rear + 2, 11},
                {3, rear + 2, 16}}) {
            if (flight[2] == 11 && !(plan.decks() >= 1)
                    || flight[2] == 16 && !(plan.decks() >= 2 || plan.bridge()))
                continue;
            for (int i = 1; i <= 5; i++) {
                int y = flight[2] + i, z = flight[1] + i;
                for (int x : new int[]{flight[0], stairPairX(flight[0], flight[2])})
                    if (blocks.get(new Cell(x, y, z)) != Part.STAIR
                            || blocks.containsKey(new Cell(x, y + 1, z))
                            || blocks.containsKey(new Cell(x, y + 2, z))
                    || blocks.containsKey(new Cell(x, y + 3, z)))
                        throw new IllegalStateException("IV blocked step at "
                                + x + "," + y + "," + z);
            }
        }
    }

    private static Map<Cell, Part> generateFrigateIV(ShipSuperstructurePlan p) {
        Map<Cell, Part> out = new LinkedHashMap<>();
        int end = p.hullLength() / 2;
        int rear = -end + 4;
        // The entire waterline bottom is face-connected and never an open
        // diagonal lattice. The stern and bow have independent station curves.
        for (int z = -end; z <= end; z++) {
            int beam = frigateBeam(p, z);
            int adjacentAft = frigateBeam(p, z - 1);
            int adjacentBow = frigateBeam(p, z + 1);
            for (int x = -beam; x <= beam; x++) {
                int d = Math.abs(x);
                put(out, x, 1, z, Part.HULL);
                if (x == 0) put(out, x, 0, z, Part.KEEL);
                boolean exterior = d >= beam - 1 || adjacentAft < 0
                        || adjacentBow < 0 || d > adjacentAft
                        || d > adjacentBow;
                if (exterior) {
                    for (int y = 2; y <= 11; y++)
                        put(out, x, y, z, y == 8 && d >= beam - 1
                                && Math.floorMod(z + end, 5) == 0
                                && z > -end + 7 && z < end - 9
                                ? Part.GUNPORT_SHUTTER : Part.HULL);
                } else {
                    if (d >= beam - 3)
                        for (int y = 2; y <= 5; y++) put(out, x, y, z, Part.HULL);
                    put(out, x, 6, z, Part.DECK);
                    put(out, x, 11, z, Part.DECK);
                }
            }
        }
        // The forecastle's forward sheer rises above the weather deck:
        // port and starboard edges climb together toward the +Z stem.
        // Keep the interior clear for deck walkers, and leave the established
        // horizontal bowsprit, its attachment and collision reach intact.
        for (int z = end - 11; z <= end; z++) {
            int beam = frigateBeam(p, z);
            int nextBeam = frigateBeam(p, z + 1);
            int rise = Math.min(4, (z - (end - 12) + 1) / 3);
            for (int x = -beam; x <= beam; x++) {
                int d = Math.abs(x);
                boolean outerSkin = d >= beam - 1 || nextBeam < 0 || d > nextBeam;
                if (!outerSkin) continue;
                for (int y = 12; y <= 11 + rise; y++)
                    put(out, x, y, z, y == 11 + rise ? Part.TRIM : Part.HULL);
            }
        }
        // The knyavdiged must follow the owner's wedge-shaped bow profile:
        // a nearly level upper edge extending forward from the existing
        // raised prow, and a curved underside rising TOWARD that edge.
        // Do not make a descending keel extension or a vertical appendage.
        // The thick heel joins the real bow plating at +Z; its underside
        // keeps rising until the final, still solid, three-block-wide tip.
        for (int z = end + 1; z <= end + 18; z++) {
            double t = (z - end) / 18D;
            double eased = t * t * (3D - 2D * t);
            int underside = 5 + (int)Math.round(8D * eased);
            int upper = z >= end + 13 ? 16 : 15;
            int halfStem = z <= end + 7 ? 2 : 1;
            for (int x = -halfStem; x <= halfStem; x++)
                for (int y = underside; y <= upper; y++) {
                    Part part = x == 0 && y <= underside + 1 ? Part.KEEL
                            : y == upper ? Part.TRIM
                            : Math.abs(x) == halfStem ? Part.FRAME : Part.HULL;
                    put(out, x, y, z, part);
                }
        }
        boolean raised = true; // IV always includes a functional stern quarterdeck.
        boolean captainDeck = p.decks() >= 2 || p.bridge();
        if (raised) {
            for (int z = rear; z <= rear + 15; z++) {
                int half = Math.min(7, Math.max(2, frigateBeam(p, z) - 2));
                for (int x = -half; x <= half; x++) {
                    put(out, x, 16, z, Part.DECK);
                    if (z == rear && Math.abs(x) < half)
                        put(out, x, 13, z, Part.WINDOW);
                    if (Math.abs(x) == half)
                        for (int y = 12; y <= 15; y++)
                            put(out, x, y, z,
                                    y == 13 && Math.floorMod(z - rear, p.cabin() ? 2 : 4) == 0
                                            ? Part.WINDOW : Part.CABIN_WALL);
                }
            }
            int sternHalf = Math.min(7, Math.max(2, frigateBeam(p, rear) - 2));
            for (int x = -sternHalf; x <= sternHalf; x++)
                for (int y = 12; y <= 15; y++)
                    put(out, x, y, rear,
                            y == 13 && Math.abs(x) < sternHalf
                                    ? Part.WINDOW : Part.CABIN_WALL);
            int frontHalf = Math.min(7, Math.max(2, frigateBeam(p, rear + 15) - 2));
            for (int x = -frontHalf; x <= frontHalf; x++)
                for (int y = 12; y <= 15; y++)
                    if (x == 0 && (y == 12 || y == 13))
                        put(out, x, y, rear + 15, Part.DOOR);
                    else put(out, x, y, rear + 15, Part.CABIN_WALL);
        }
        if (captainDeck) {
            for (int z = rear; z <= rear + 9; z++) {
                int half = Math.min(5, Math.max(2, frigateBeam(p, z) - 3));
                for (int x = -half; x <= half; x++) {
                    put(out, x, 21, z, Part.BRIDGE);
                }
            }
        }
        if (p.decks() >= 3) {
            // Third-deck upgrade: a real wheelhouse canopy on the captain platform.
            for (int z = rear + 1; z <= rear + 4; z++) {
                for (int x = -3; x <= 3; x++)
                    put(out, x, 26, z, Part.BRIDGE);
                for (int x : new int[]{-3, 3})
                    for (int y = 22; y <= 25; y++)
                        put(out, x, y, z, Part.FRAME);
            }
        }
        if (p.bridge()) {
            // The captain bridge upgrade is a physical change even when the
            // third deck was already installed; it is not a no-op flag.
            for (int z = rear + 5; z <= rear + 8; z++)
                for (int x : new int[]{-4, 4})
                    if (z == rear + 5 || z == rear + 8)
                        for (int y = 23; y <= 25; y++)
                            put(out, x, y, z, Part.FRAME);
            for (int x = -4; x <= 4; x++)
                put(out, x, 26, rear + 5, Part.BRIDGE);
        }
        // Four independent mast stations; wooden geometry remains server side,
        // but all sail fabric is drawn only by the client renderer.
        for (int i = 0; i < p.mastPositions().size(); i++) {
            int z = p.mastPositions().get(i);
            int tier = p.sailTiers().get(i);
            int height = Math.min(46, (i == 1 || i == 2 ? 43 : 40)
                    + (tier - 1) * 2);
            for (int y = 12; y <= height; y++) {
                // Structural 3x3 lower trunk; the upper mast tapers to 1x1.
                // Reinforced rigging extends the thick lower section upward.
                int radius = y < (tier >= 2 ? 27 : 23) ? 1 : 0;
                for (int dx = -radius; dx <= radius; dx++)
                    for (int dz = -radius; dz <= radius; dz++)
                        put(out, dx, y, z + dz, Part.MAST);
            }
            int span = (i == 1 || i == 2 ? 8 : 6) + tier - 1;
            for (int yy : new int[]{21, 30, 39}) {
                for (int x = -span; x <= span; x++)
                    if (x != 0) put(out, x, yy, z, Part.YARD);
            }
            if (tier >= 3)
                for (int x = -Math.max(3, span - 3); x <= Math.max(3, span - 3); x++)
                    if (x != 0) put(out, x, Math.min(45, height - 1), z, Part.YARD);
        }
        // One fixed port/starboard switchback shaft throughout the ship.
        // Every flight remains 2 blocks wide, with 3 empty headroom blocks.
        int stairZ = rear + 2;
        frigateStairs(out, -3, stairZ, 1, 6);
        frigateStairs(out, 3, stairZ, 6, 11);
        frigateStairs(out, -3, stairZ, 11, 16);
        if (captainDeck) frigateStairs(out, 3, stairZ, 16, 21);
        int wheelY = captainDeck ? 22 : raised ? 17 : 12;
        put(out, 0, wheelY, rear + 4, Part.HELM);
        placeShipLighting(out, p, true);
        validateFrigateIV(out, p, rear);
        return Collections.unmodifiableMap(out);
    }

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4) {
            Map<Cell, Part> cached = FRIGATE_CACHE.get(plan);
            if (cached != null) return cached;
            Map<Cell, Part> generated = generateFrigateIV(plan);
            FRIGATE_CACHE.put(plan, generated);
            return generated;
        }
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
