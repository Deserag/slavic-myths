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
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2) {
            // The flagship has a deep, broad merchant stern and a long
            // asymmetric clipper stem: never copy LARGE's hull stations.
            int halfLength = plan.hullLength() / 2;
            if (Math.abs(z) > halfLength) return -1;
            int halfWidth = plan.hullWidth() / 2;
            int run = z >= 0 ? Math.min(15, halfLength) : Math.min(9, halfLength);
            int distance = z >= 0 ? halfLength - z : halfLength + z;
            if (distance >= run) return halfWidth;
            double t = distance / (double)run;
            double curve = z >= 0 ? Math.pow(t, 1.35D)
                    : t * t * (3D - 2D * t);
            int tip = z >= 0 ? 1 : Math.max(4, halfWidth / 2);
            return Math.max(tip, (int)Math.round(tip + (halfWidth - tip) * curve));
        }
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() >= 2) {
            // A fine bow and a wide, comparatively full working stern.
            int halfLength = plan.hullLength() / 2;
            if (Math.abs(z) > halfLength) return -1;
            int halfWidth = plan.hullWidth() / 2;
            int run = z >= 0 ? Math.min(12, halfLength) : Math.min(7, halfLength);
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

    /** Shared server/client rig heights: never let the renderer invent another sail profile. */
    public static int sailLowerYard(ShipSuperstructurePlan plan) {
        return plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2
                ? Math.max(7, 4 + (plan.decks() - 1) * 4 + 3) : 7;
    }

    public static int sailTop(ShipSuperstructurePlan plan, int tier) {
        if (tier < 1 || tier > 3) throw new IllegalArgumentException("Unknown sail tier");
        int base = tier == 1 ? 12 : tier == 2 ? 15 : 19;
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2)
            return Math.max(base, sailLowerYard(plan)
                    + (tier == 1 ? 5 : tier == 2 ? 8 : 12));
        return base;
    }

    public static int sailBottom(ShipSuperstructurePlan plan) {
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2) return sailLowerYard(plan) + 1;
        return plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                && plan.decks() >= 2 ? 10 : 8;
    }

    public static Map<Cell, Part> generate(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        Map<Cell, Part> blocks = new LinkedHashMap<>();
        int halfLength = plan.hullLength() / 2;

        // Full-block keel, dry hold and weather deck with shaped ends.
        for (int z = -halfLength; z <= halfLength; z++) {
            int half = halfBeam(plan, z);
            for (int x = -half; x <= half; x++) {
                // The flagship bilge must NOT be a full-beam flat slab.
                // A narrower solid keel steps out into a watertight floor at
                // y=1, then rises into the wider chines at y=2.
                boolean flagshipBilge = plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                        && plan.hullRevision() >= 2;
                int keelHalf = Math.max(1, half - 3);
                int floorHalf = Math.max(keelHalf, half - 2);
                // Rise the keel above the waterline at both ends while the
                // sealed hold-floor at y=1 stays intact. This removes the
                // ruler-straight underwater rectangle in side elevation.
                boolean liftedKeel = flagshipBilge
                        && (halfLength - z <= 4 || halfLength + z <= 3);
                if (!liftedKeel && (!flagshipBilge || Math.abs(x) <= keelHalf))
                    put(blocks, x, 0, z, x == 0 ? Part.KEEL : Part.HULL);
                if (flagshipBilge) {
                    if (Math.abs(x) <= floorHalf) put(blocks, x, 1, z, Part.HULL);
                    else put(blocks, x, 2, z, Part.HULL);
                }
                boolean edge = Math.abs(x) == half
                        || Math.abs(x) > halfBeam(plan, z - 1)
                        || Math.abs(x) > halfBeam(plan, z + 1);
                if (edge) for (int y = flagshipBilge ? 2 : 1; y <= 3; y++) {
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
                if ((plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                        || plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST)
                        && plan.hullRevision() >= 2 && edge
                        && Math.abs(x) == half && Math.abs(z) < halfLength - 2)
                    put(blocks, x, 2, z, Math.floorMod(z, 6) == 0
                            ? Part.FRAME : Part.KEEL);
            }
        }
        // Revision-2 flagship sheer: the long bow rises three steps and the
        // broad transom two. These perimeter bulwarks leave the central deck
        // and its two-block walking headroom untouched, but break the old
        // flat side elevation that made the vessel resemble an iron.
        if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2) {
            for (int z = -halfLength; z <= halfLength; z++) {
                int bowRun = halfLength - z, sternRun = halfLength + z;
                int lift = bowRun <= 2 ? 3 : bowRun <= 5 ? 2
                        : bowRun <= 8 ? 1
                        : sternRun <= 2 ? 2 : sternRun <= 5 ? 1 : 0;
                if (lift == 0) continue;
                int half = halfBeam(plan, z);
                for (int x : new int[]{-half, half})
                    for (int y = 6; y <= 5 + lift; y++)
                        put(blocks, x, y, z,
                                y == 5 + lift ? Part.RAIL : Part.FRAME);
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
            boolean modernAftDeck = plan.hullRevision() >= 2
                    && (plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                            || plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST);
            int front = mediumQuarterdeck ? rear + (plan.cabin() ? 6 : 5)
                    : modernAftDeck ? Math.min(halfLength - 5,
                            rear + (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                                    ? (floor == 2 ? plan.hullLength() * 2 / 3
                                            : plan.hullLength() / 2)
                                    : (floor == 2 ? plan.hullLength() * 3 / 5
                                            : plan.hullLength() * 2 / 5)))
                    : halfLength - 3 * (floor - 1);
            for (int z = rear; z <= front; z++) {
                // LARGEST's third deck is a narrower raised sterncastle, not
                // another full-beam rectangular storey above the second deck.
                int inset = plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                        && plan.hullRevision() >= 2 && floor >= 3 ? 3 : 1;
                int half = Math.max(0, halfBeam(plan, z) - inset);
                for (int x = -half; x <= half; x++) {
                    boolean hatch = !mediumQuarterdeck && x == -2 && (z == 0 || z == 1);
                    if (!hatch) put(blocks, x, y, z, Part.DECK);
                    // Aft and forward ends of the raised flagship decks need
                    // actual cross-rails. Side rails alone leave full-width
                    // unguarded drops on the short upper-deck footprints.
                    boolean flagshipEnd = plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                            && plan.hullRevision() >= 2 && (z == rear || z == front);
                    if ((Math.abs(x) == half || flagshipEnd) && !hatch)
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
            // LARGEST's bridge belongs on the highest aft deck: a fixed
            // y=9 wheel is buried under a three-deck sterncastle.
            boolean flagshipBridge = plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                    && plan.hullRevision() >= 2;
            int bridgeFloor = flagshipBridge && plan.decks() >= 3 ? 12 : 9;
            int rear = -halfLength + 2, front = rear + 4;
            int side = Math.min(3, plan.hullWidth() / 2 - 2);
            int ladderX = -side + 1;
            for (int z = rear; z <= front; z++)
                for (int x = -side; x <= side; x++) {
                    put(blocks, x, bridgeFloor, z, Part.BRIDGE);
                    boolean entrance = z == front && x == ladderX
                            && (flagshipBridge || plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM);
                    if ((Math.abs(x) == side || z == rear || z == front) && !entrance)
                        put(blocks, x, bridgeFloor + 1, z, Part.RAIL);
                }
            for (int y = 5; y <= bridgeFloor; y++) {
                if (plan.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                        && !plan.cabin() && y < bridgeFloor)
                    put(blocks, -side, y, front, Part.FRAME);
                if (flagshipBridge && y < bridgeFloor)
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
        } else if (plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() >= 2) {
            // Without a purchased bridge, use the highest accessible stern
            // deck, or the cabin roof on an otherwise single-deck flagship.
            int rear = -halfLength + 2;
            int helmFloor = plan.decks() >= 3 ? 12
                    : plan.decks() >= 2 || plan.cabin() ? 8 : 4;
            if (plan.cabin() && plan.decks() == 1) {
                int roofFront = rear + 6;
                int side = Math.min(3, plan.hullWidth() / 2 - 2);
                for (int y = 5; y <= 9; y++) {
                    put(blocks, -side, y, roofFront + 1, Part.FRAME);
                    put(blocks, -side + 1, y, roofFront + 1, Part.LADDER);
                }
            }
            put(blocks, 0, helmFloor + 1, rear + (plan.cabin() ? 3 : 4), Part.HELM);
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
