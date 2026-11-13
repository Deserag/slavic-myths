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
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() >= 2) {
            // A fine bow and a wide, comparatively full working stern.
            int halfLength = plan.hullLength() / 2;
            if (Math.abs(z) > halfLength) return -1;
            int halfWidth = plan.hullWidth() / 2;
            // Scale the bow and quarter taper with hull length. A fixed 12/7
            // block curve left stretched LARGE variants with a very long,
            // iron-shaped rectangular midbody. Keep the 33-block baseline
            // unchanged, then devote the extra length to curved shoulders.
            int run = z >= 0 ? Math.min(halfLength, Math.max(12, halfLength - 4))
                    : Math.min(halfLength, Math.max(7, halfLength - 9));
            int distance = z >= 0 ? halfLength - z : halfLength + z;
            if (distance >= run) return halfWidth;
            double t = Math.max(0D, distance / (double)run);
            double curve = t * t * (3D - 2D * t);
            int tip = z >= 0 ? 0 : Math.max(2, halfWidth / 3);
            return Math.max(tip, (int)Math.round(tip + (halfWidth - tip) * curve));
        }
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

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;

        // Full-block keel, dry hold and weather deck with shaped ends.
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeam(plan, z);
            for (int x = -half; x <= half; x++) {
                // Revision 2 LARGE hulls have an actual stepped bilge in
                // transverse section. The center remains a closed dry floor,
                // while the outer planking climbs outward toward the sheer.
                // This branch is isolated from every persisted revision 1.
                if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                        && plan.hullRevision() >= 2) {
                    int absX = Math.abs(x);
                    int bottom = Math.max(0, half - 2);
                    int chine = Math.max(0, half - 1);
                    if (absX <= bottom)
                        put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                    if (absX >= bottom && absX <= chine)
                        put(blocks, x, 1, z, absX == bottom ? Part.FRAME : Part.HULL);
                    if (absX >= chine && absX <= half)
                        put(blocks, x, 2, z, absX == chine ? Part.KEEL : Part.HULL);
                    boolean outerEdge = absX == half
                            || absX > halfBeam(plan, z - 1)
                            || absX > halfBeam(plan, z + 1);
                    if (outerEdge)
                        put(blocks, x, 3, z, Math.floorMod(z, 6) == 0
                                ? Part.FRAME : Part.HULL);
                    // The square-ended working stern is a watertight transom,
                    // not an open cavity between the stepped chine bands.
                    // The fine bow is capped as well for every hull width.
                    if (Math.abs(z) == halfLength)
                        for (int bulkheadY = 1; bulkheadY <= 3; bulkheadY++)
                            put(blocks, x, bulkheadY, z, Part.HULL);
                    else if (Math.floorMod(z, 4) == 0 && absX < half)
                        put(blocks, x, 3, z, Part.FRAME);
                    if (!mainHatch(x, z)) put(blocks, x, 4, z, Part.DECK);
                    if (outerEdge) {
                        put(blocks, x, 5, z, Part.RAIL);
                        if (z >= halfLength - 3 || z <= -halfLength + 2)
                            put(blocks, x, 6, z, Part.RAIL);
                    }
                    continue;
                }
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
                if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                        && plan.hullRevision() >= 2 && edge
                        && Math.abs(x) == half && Math.abs(z) < halfLength - 2)
                    put(blocks, x, 2, z, Math.floorMod(z, 6) == 0
                            ? Part.FRAME : Part.KEEL);
            }
        }
        // The hold shaft stops at the main deck on medium ships. Their
        // quarterdeck has its own exterior ladder near the stern, so a
        // second mast-height freestanding ladder is not generated amidships.
        boolean largeRevision2 = plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() >= 2;
        int mainShaftTop = plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                ? 4 : largeRevision2 ? (plan.decks() >= 2 ? 8 : 4)
                : 4 + (plan.decks() - 1) * 4;
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
            boolean largeAftDeck = plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                    && plan.hullRevision() >= 2;
            int front = mediumQuarterdeck ? rear + (plan.cabin() ? 6 : 5)
                    : largeAftDeck ? Math.min(halfLength - 5,
                            floor == 2 ? rear + (plan.hullLength() * 3 / 5)
                                    // A central third-deck walking platform is
                                    // required even on the longest LARGE hull.
                                    // The forward part stays open to the sky.
                                    : Math.max(0, rear + (plan.hullLength() * 2 / 5)))
                    : halfLength - 3 * (floor - 1);
            for (int z = rear; z <= front; z++) {
                // Progressive narrowing makes the upper works visibly
                // subordinate to the curved hull, not a full-width timber box.
                int inset = largeAftDeck ? (floor == 2 ? 2 : 3) : 1;
                int half = Math.max(0, halfBeam(plan, z) - inset);
                for (int x = -half; x <= half; x++) {
                    // LARGE third deck uses a separate aft ladder. Do not
                    // punch a floating midships hatch through its bow edge.
                    boolean hatch = !mediumQuarterdeck
                            && !(largeAftDeck && floor == 3)
                            && x == -2 && (z == 0 || z == 1);
                    if (!hatch) put(blocks, x, y, z, Part.DECK);
                    if ((Math.abs(x) == half
                            || largeAftDeck && (z == rear || z == front)) && !hatch)
                        put(blocks, x, y + 1, z, Part.RAIL);
                    if ((z - rear) % 4 == 0 && Math.abs(x) == half)
                        for (int sy = y - 3; sy < y; sy++) {
                            // Narrow third-deck supports must never replace
                            // the second-deck rail, bridge or its ladder.
                            if (largeAftDeck && floor == 3)
                                blocks.putIfAbsent(new Cell(x, sy, z), Part.FRAME);
                            else put(blocks, x, sy, z, Part.FRAME);
                        }
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

        // The compact third aft deck does not reach the midships ladder.
        // Add a separate supported stair shaft from deck two to deck three,
        // forward of the cabin but inside the upper stern-deck footprint.
        if (largeRevision2 && plan.decks() == 3) {
            int accessZ = -halfLength + 11;
            for (int y = 9; y <= 12; y++) {
                put(blocks, -3, y, accessZ, Part.FRAME);
                put(blocks, -2, y, accessZ, Part.LADDER);
            }
        }

        // Real per-mast stations. A mast at one coordinate never overwrites
        // another because spacing is checked by ShipSuperstructurePlan.
        List<Integer> masts = plan.mastPositions();
        for (int i = 0; i < masts.size(); i++) {
            int z = masts.get(i), tier = plan.sailTiers().get(i);
            int top = tier == 1 ? 12 : tier == 2 ? 15 : 19;
            int span = tier == 1 ? 2 : tier == 2 ? 3 : 4;
            boolean raisedLargeRig = plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                    && plan.hullRevision() >= 2;
            int highestDeck = 4 + (plan.decks() - 1) * 4;
            // An upper deck is walkable: neither sail cloth nor the low yard
            // may intersect the player's two-block-high clearance envelope.
            // Rig tier still raises height above the active deck elevation.
            if (raisedLargeRig)
                top = Math.max(top, highestDeck + 7 + (tier - 1) * 2);
            int lowerYard = raisedLargeRig ? highestDeck + 3 : 7;
            for (int y = 5; y <= top + 1; y++) put(blocks, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) {
                // Keep the mast continuous at its centerline. Cross yards
                // join the mast from either side, rather than replacing it.
                if (raisedLargeRig && x == 0) continue;
                put(blocks, x, top + 1, z, Part.YARD);
                put(blocks, x, lowerYard, z, Part.YARD);
            }
            int sailBottom = raisedLargeRig ? highestDeck + 4
                    : plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                    && plan.decks() >= 2 ? 10 : 8;
            for (int y = sailBottom; y <= top; y++)
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

        // LARGE revision 2 has one helm on the highest usable aft deck,
        // never under the second or third floor. Cabin roof access must not
        // depend on purchasing a separate bridge.
        boolean modernLarge = plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() >= 2;
        if (modernLarge && plan.cabin() && !plan.bridge()) {
            int rear = -halfLength + 2, front = rear + 6;
            for (int y = 5; y <= 9; y++) {
                put(blocks, -3, y, front + 1, Part.FRAME);
                put(blocks, -2, y, front + 1, Part.LADDER);
            }
            // A rail-lined roof is a working upper captain's deck.
            for (int z = rear; z <= front; z++)
                for (int x = -3; x <= 3; x++)
                    if (Math.abs(x) == 3 || z == rear || z == front)
                        // With a second deck, the cabin roof and the aft
                        // walking deck share the same elevation. A full rail
                        // across their junction creates an artificial wall.
                        // Keep the enclosure on single-deck hulls.
                        if (!(z == front && (plan.decks() >= 2 || x == -2)))
                            put(blocks, x, 9, z, Part.RAIL);
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
                    if ((Math.abs(x) == side || z == rear || z == front)
                            && !((plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                                    || modernLarge)
                                    && z == front && x == -side + 1))
                        put(blocks, x, 10, z, Part.RAIL);
                }
            // A stand-alone LARGE bridge must have real support at all four
            // corners. Previously only its ladder-side corner reached the
            // main deck, leaving a visibly floating timber platform.
            if (modernLarge && !plan.cabin())
                for (int postZ : new int[]{rear, front})
                    for (int postX : new int[]{-side, side})
                        for (int postY = 5; postY <= 8; postY++) {
                            Cell support = new Cell(postX, postY, postZ);
                            // An existing second-deck plank already carries
                            // the bridge. Never punch a hole in that walkway.
                            if (blocks.get(support) != Part.DECK)
                                put(blocks, postX, postY, postZ, Part.FRAME);
                        }
            for (int y = 5; y <= 9; y++) {
                if ((plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                        || modernLarge) && !plan.cabin() && y < 9)
                    put(blocks, -side, y, front, Part.FRAME);
                put(blocks, -side + 1, y, front, Part.LADDER);
            }
            if (!modernLarge)
                put(blocks, 0, 10, rear + 2, Part.HELM);
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
        } else if (!modernLarge) {
            put(blocks, 0, 5, -halfLength + (plan.cabin() ? 9 : 3), Part.HELM);
        }
        if (modernLarge) {
            int y = 4 + (plan.decks() - 1) * 4 + 1;
            if (plan.bridge() && plan.decks() <= 2) y = 10;
            else if (plan.cabin() && plan.decks() == 1) y = 9;
            int z = -halfLength + (plan.bridge() || plan.cabin() ? 4 : 3);
            put(blocks, 0, y, z, Part.HELM);
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
