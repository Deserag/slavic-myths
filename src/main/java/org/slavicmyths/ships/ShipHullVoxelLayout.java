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
        CABIN_WALL, WINDOW, DOOR, BRIDGE, HELM, LANTERN, TRIM, BOWSPRIT
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
                if (abs <= Math.max(1, beam - 4)) put(out, x, 1, z, Part.HULL);
                else if (abs <= beam - 3) put(out, x, 2, z, Part.HULL);
                else if (abs <= beam - 2) put(out, x, 3, z, Part.HULL);
                else if (abs == beam - 1) put(out, x, 4, z, Part.HULL);
                // Wide gun floor below a narrower weather deck.
                if (abs < beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 5, z, Part.DECK);
                if (abs <= beam - 1 && !(x == -2 && z == 1))
                    put(out, x, 10, z, Part.DECK);
                for (int y = 5; y <= 9; y++) {
                    int skin = y == 7 || y == 8 ? beam : beam - 1;
                    if (abs != skin) continue;
                    boolean port = Math.abs(z) <= end - 5
                            && Math.abs(z) >= 3 && Math.floorMod(z + end, 4) == 0
                            && (y == 7 || y == 8);
                    if (!port) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM
                                    : Math.floorMod(z, 5) == 0
                                            ? Part.FRAME : Part.HULL);
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
                    boolean gunport = abs == beam && (y == 7 || y == 8)
                            && Math.abs(z) <= end - 5 && Math.abs(z) >= 3
                            && Math.floorMod(z + end, 4) == 0;
                    if (!gunport) put(out, x, y, z,
                            y == 6 || y == 9 ? Part.TRIM : Part.HULL);
                }
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
        // Real hatch between hold, gun deck and weather deck.
        for (int y = 2; y <= 10; y++) {
            put(out, -3, y, 1, Part.FRAME);
            put(out, -2, y, 1, Part.LADDER);
        }
        // Upper deck only covers the stern, leaving the middle completely open.
        int top = brigantineTopDeck(p);
        if (top >= 15) {
            int front = rear + 9;
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 15, z, Part.DECK);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 16, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 11; y <= 14; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 11; y <= 15; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 16, front));
            put(out, half - 1, 16, rear + 3, Part.LANTERN);
            put(out, -half + 1, 16, rear + 3, Part.LANTERN);
        }
        if (top >= 20) {
            int front = rear + 6;
            int half = Math.min(3, p.hullWidth() / 2 - 5);
            for (int z = rear; z <= front; z++)
                for (int x = -half; x <= half; x++) {
                    if (x == -half + 1 && z == front) continue;
                    put(out, x, 20, z, Part.BRIDGE);
                    if (Math.abs(x) == half || z == rear || z == front)
                        put(out, x, 21, z, Part.HULL);
                    if (Math.abs(x) == half && Math.floorMod(z, 3) == 0)
                        for (int y = 17; y <= 19; y++)
                            put(out, x, y, z, Part.FRAME);
                }
            for (int y = 16; y <= 20; y++) {
                put(out, -half, y, front + 1, Part.FRAME);
                put(out, -half + 1, y, front + 1, Part.LADDER);
            }
            out.remove(new Cell(-half + 1, 21, front));
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
