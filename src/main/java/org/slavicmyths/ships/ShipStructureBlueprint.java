package org.slavicmyths.ships;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic, block-resolution structural blueprint.
 *
 * Coordinates are local full Minecraft blocks: +Z bow, -Z stern, waterline Y=0.
 * No world mutation occurs here. Actual ship placement must validate clearance,
 * cargo ownership and cost on the SERVER before building this blueprint.
 */
public final class ShipStructureBlueprint {
    public enum Part {
        KEEL, BOTTOM, RIB, SIDE, MAIN_DECK, UPPER_DECK, RAIL,
        BEAM, LADDER, MAST, YARD, SAIL, ROOM_WALL, WINDOW,
        DOOR, ROOF, HELM
    }
    public record Cell(int x, int y, int z) {}
    public record Change(Cell location, Part oldPart, Part newPart) {}

    private final ShipSuperstructurePlan design;
    private final Map<Cell, Part> blocks;

    private ShipStructureBlueprint(ShipSuperstructurePlan design, Map<Cell, Part> blocks) {
        this.design = design;
        this.blocks = Collections.unmodifiableMap(new LinkedHashMap<>(blocks));
    }

    public ShipSuperstructurePlan design() { return design; }
    public Map<Cell, Part> blocks() { return blocks; }

    private static void put(Map<Cell, Part> map, int x, int y, int z, Part part) {
        Part existing = map.put(new Cell(x, y, z), part);
        if (existing == part) return;
    }

    private static int beam(int halfWidth, int halfLength, int z) {
        // Preserve narrow, stepped bow/stern; never generate a rectangular box.
        int distanceToEnd = halfLength - Math.abs(z);
        return Math.min(halfWidth, Math.max(0, (distanceToEnd + 1) / 2));
    }

    public static ShipStructureBlueprint generate(ShipSuperstructurePlan plan) {
        Objects.requireNonNull(plan, "plan");
        Map<Cell, Part> out = new LinkedHashMap<>();
        int hw = plan.hullWidth() / 2, hl = plan.hullLength() / 2;
        for (int z = -hl; z <= hl; z++) {
            int b = beam(hw, hl, z);
            put(out, 0, 0, z, Part.KEEL);
            for (int x = -b; x <= b; x++) {
                put(out, x, 1, z, Part.BOTTOM);
                if (Math.abs(x) == b) {
                    for (int y = 2; y <= 4; y++) put(out, x, y, z, Part.SIDE);
                    if (b > 0) put(out, x, 6, z, Part.RAIL);
                } else if (x % 3 == 0) {
                    put(out, x, 2, z, Part.RIB);
                }
                put(out, x, 5, z, Part.MAIN_DECK);
            }
        }
        // Hatch and ladder are aligned to the same clear vertical shaft.
        for (int y = 2; y <= 5; y++) put(out, -1, y, 1, Part.LADDER);

        if (plan.decks() >= 2) {
            int end = Math.min(-1, -hl + Math.max(7, plan.hullLength() / 2));
            for (int z = -hl + 2; z <= end; z++) {
                int b = Math.max(1, beam(hw, hl, z) - 1);
                for (int x = -b; x <= b; x++) put(out, x, 9, z, Part.UPPER_DECK);
                for (int x : new int[]{-b, b}) put(out, x, 10, z, Part.RAIL);
                if (z % 3 == 0) for (int x : new int[]{-b, b})
                    for (int y = 6; y <= 8; y++) put(out, x, y, z, Part.BEAM);
            }
            for (int y = 6; y <= 9; y++) put(out, -1, y, end, Part.LADDER);
        }
        if (plan.decks() >= 3) {
            int start = -hl + 3, end = Math.min(-2, -hl + 9);
            for (int z = start; z <= end; z++) {
                int b = Math.max(1, beam(hw, hl, z) - 2);
                for (int x = -b; x <= b; x++) put(out, x, 13, z, Part.UPPER_DECK);
                for (int x : new int[]{-b, b}) put(out, x, 14, z, Part.RAIL);
            }
            for (int y = 10; y <= 13; y++) put(out, -1, y, end, Part.LADDER);
        }

        // A 7-block-deep functional stern room, door on the forward wall.
        if (plan.cabin()) {
            int aft = -hl + 2, fore = aft + 6;
            int roomHalf = Math.min(3, hw - 2);
            for (int z = aft; z <= fore; z++) {
                for (int x = -roomHalf; x <= roomHalf; x++) {
                    put(out, x, 9, z, Part.ROOF);
                    if (z == aft || z == fore || Math.abs(x) == roomHalf) {
                        for (int y = 6; y <= 8; y++) {
                            if (z == fore && x == 0 && y <= 7) continue;
                            Part part = y == 7 && Math.abs(x) == roomHalf
                                    && z > aft && z < fore ? Part.WINDOW : Part.ROOM_WALL;
                            put(out, x, y, z, part);
                        }
                    }
                }
            }
            put(out, 0, 8, fore, Part.DOOR);
        }
        if (plan.bridge()) {
            int station = -hl + 3;
            for (int z = station - 1; z <= station + 2; z++)
                for (int x = -2; x <= 2; x++) put(out, x, 10, z, Part.UPPER_DECK);
            for (int z = station - 1; z <= station + 2; z++)
                for (int x : new int[]{-2, 2}) put(out, x, 11, z, Part.RAIL);
            for (int y = 6; y <= 10; y++) put(out, 2, y, station, Part.BEAM);
            for (int y = 6; y <= 10; y++) put(out, -1, y, station + 2, Part.LADDER);
            put(out, 0, 11, station, Part.HELM);
        } else {
            put(out, 0, 7, -hl + 2, Part.HELM);
        }

        // Per-mast tiers change real height, yard width, and cloth area.
        // Each mast has independent tier, as required by the shipyard design.
        for (int n = 0; n < plan.sailTiers().size(); n++) {
            int z = plan.mastPositions().get(n);
            int tier = plan.sailTiers().get(n);
            int top = 12 + (tier - 1) * 4;
            int span = 2 + tier;
            for (int y = 5; y <= top + 1; y++) put(out, 0, y, z, Part.MAST);
            for (int x = -span; x <= span; x++) put(out, x, top + 1, z, Part.YARD);
            for (int y = 8; y <= top; y++) {
                int rowSpan = span - (y == top || y == 8 ? 1 : 0);
                for (int x = -rowSpan; x <= rowSpan; x++)
                    put(out, x, y, z + 1, Part.SAIL);
            }
            if (tier == 3) {
                for (int x = -span + 1; x <= span - 1; x++) put(out, x, top + 3, z, Part.YARD);
                for (int y = top + 2; y <= top + 5; y++)
                    for (int x = -(span - 2); x <= span - 2; x++)
                        put(out, x, y, z + 1, Part.SAIL);
                for (int y = top + 2; y <= top + 5; y++) put(out, 0, y, z, Part.MAST);
            }
        }
        return new ShipStructureBlueprint(plan, out);
    }

    /** Only payable construction changes; retained blocks are not charged again. */
    public Map<Cell, Change> changesFrom(ShipStructureBlueprint old) {
        Objects.requireNonNull(old, "old");
        Map<Cell, Change> difference = new LinkedHashMap<>();
        for (var entry : blocks.entrySet()) {
            Part before = old.blocks.get(entry.getKey());
            if (before != entry.getValue())
                difference.put(entry.getKey(), new Change(entry.getKey(), before, entry.getValue()));
        }
        return Collections.unmodifiableMap(difference);
    }

    public Map<Part, Integer> materialsFrom(ShipStructureBlueprint old) {
        Map<Part, Integer> counts = new java.util.EnumMap<>(Part.class);
        for (Change change : changesFrom(old).values())
            counts.merge(change.newPart(), 1, Integer::sum);
        return Collections.unmodifiableMap(counts);
    }
    private ShipStructureBlueprint() { throw new AssertionError(); }
}
