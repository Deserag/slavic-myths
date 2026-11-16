import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Dependency-free acceptance checks for the actual revision-3/4 hull generator. */
public final class ShipHullVoxelLayoutCheck {
    private static int checks;
    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static ShipHullVoxelLayout.Part at(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> model,
            int x, int y, int z) {
        return model.get(new ShipHullVoxelLayout.Cell(x, y, z));
    }

    private static void flight(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> model,
            int startZ, int lower, int upper, String name) {
        for (int lane : new int[]{-3, -2}) {
            var approach = at(model, lane, lower, startZ);
            require(approach == ShipHullVoxelLayout.Part.DECK
                    || approach == ShipHullVoxelLayout.Part.BRIDGE
                    || lower == 1 && approach == ShipHullVoxelLayout.Part.HULL,
                    name + " missing lower stair approach at " + lane + "," + lower + "," + startZ);
            for (int head = 1; head <= 2; head++)
                require(at(model, lane, lower + head, startZ) == null,
                        name + " blocked lower approach " + lane + "," + startZ);
            for (int step = 1; step <= upper - lower; step++) {
                int y = lower + step, z = startZ - step;
                require(at(model, lane, y, z) == ShipHullVoxelLayout.Part.STAIR,
                        name + " missing paired tread at " + lane + "," + y + "," + z);
                for (int head = 1; head <= 3; head++)
                    require(at(model, lane, y + head, z) == null,
                            name + " blocked three-block headroom at " + lane + "," + y + "," + z);
                require(at(model, 3, y, z) != ShipHullVoxelLayout.Part.STAIR,
                        name + " staircase jumps to starboard at Y=" + y + " Z=" + z);
            }
            int exit = startZ - (upper - lower + 1);
            var landing = at(model, lane, upper, exit);
            var onward = at(model, lane, upper, exit - 1);
            require(landing == ShipHullVoxelLayout.Part.DECK
                    || landing == ShipHullVoxelLayout.Part.BRIDGE,
                    name + " missing upper landing at " + lane + "," + upper + "," + exit);
            require(onward == ShipHullVoxelLayout.Part.DECK
                    || onward == ShipHullVoxelLayout.Part.BRIDGE,
                    name + " upper landing drops into a hole at " + lane + "," + upper + "," + (exit - 1));
            for (int head = 1; head <= 3; head++)
                require(at(model, lane, upper + head, exit) == null,
                        name + " landing headroom blocked at " + lane + "," + upper + "," + exit);
        }
    }

    private static void stairRoute(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> model,
            int rear, int[] floors, String name) {
        int start = rear + 6;
        int expectedTreads = 0;
        for (int i = 0; i + 1 < floors.length; i++) {
            int rise = floors[i + 1] - floors[i];
            start += rise + 1;
            expectedTreads += rise * 2;
        }
        long actualTreads = model.values().stream()
                .filter(part -> part == ShipHullVoxelLayout.Part.STAIR).count();
        require(actualTreads == expectedTreads,
                name + " has missing, duplicated or disconnected stairs: "
                        + actualTreads + " expected " + expectedTreads);
        for (int i = 0; i + 1 < floors.length; i++) {
            flight(model, start, floors[i], floors[i + 1], name);
            // Each upper landing is the next flight's lower approach.
            start -= floors[i + 1] - floors[i] + 1;
        }
        require(start == rear + 6, name + " disconnected final landing");
    }

    private static void lights(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> model,
            String name) {
        int total = 0;
        for (var a : model.entrySet()) {
            if (a.getValue() != ShipHullVoxelLayout.Part.LANTERN
                    && a.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN) continue;
            total++;
            require(a.getValue() == ShipHullVoxelLayout.Part.HANGING_LANTERN,
                    name + " lantern is standing on the walking deck");
            var c = a.getKey();
            var overhead = at(model, c.x(), c.y() + 1, c.z());
            require(overhead == ShipHullVoxelLayout.Part.HULL
                    || overhead == ShipHullVoxelLayout.Part.DECK
                    || overhead == ShipHullVoxelLayout.Part.BRIDGE
                    || overhead == ShipHullVoxelLayout.Part.FRAME
                    || overhead == ShipHullVoxelLayout.Part.TRIM,
                    name + " lantern not supported by ceiling");
            for (var b : model.entrySet()) {
                if (b.getKey().equals(a.getKey()) || b.getValue() != ShipHullVoxelLayout.Part.LANTERN
                        && b.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN) continue;
                var d = b.getKey();
                int dx = c.x() - d.x(), dy = c.y() - d.y(), dz = c.z() - d.z();
                require(dx * dx + dy * dy + dz * dz >= 25,
                        name + " lantern spacing below 5 blocks");
            }
        }
        require(total > 0, name + " lacks interior lighting");
    }

    private static void frigate(ShipSuperstructurePlan p) {
        var model = ShipHullVoxelLayout.generate(p);
        int end = p.hullLength() / 2, rear = -end + 4;
        require(at(model, 0, 12, rear + 15) == ShipHullVoxelLayout.Part.DOOR
                && at(model, 0, 13, rear + 15) == ShipHullVoxelLayout.Part.DOOR,
                "IV captain door must be a single paired opening " + p);
        require(model.values().stream().filter(v -> v == ShipHullVoxelLayout.Part.DOOR).count() == 2,
                "IV contains duplicated cabin entrances " + p);
        int half = Math.min(7, Math.max(2, ShipHullVoxelLayout.halfBeam(p, rear) - 2));
        for (int x = -half + 1; x <= half - 1; x++)
            require(at(model, x, 13, rear) == ShipHullVoxelLayout.Part.WINDOW,
                    "IV stern window row has a gap " + p + " x=" + x);
        // Exactly one room boundary: no second, nested inner wall or door.
        // The interior may contain masts, but no extra cabin bulkheads.
        for (int z = rear + 1; z < rear + 15; z++) {
            int side = Math.min(7, Math.max(2, ShipHullVoxelLayout.halfBeam(p, z) - 2));
            for (int x = -side + 1; x < side; x++)
                for (int y = 12; y <= 15; y++)
                    require(at(model, x, y, z) != ShipHullVoxelLayout.Part.CABIN_WALL
                                    && at(model, x, y, z) != ShipHullVoxelLayout.Part.DOOR,
                            "IV has nested inner cabin at " + x + "," + y + "," + z + " " + p);
            for (int x : new int[] {-side, side})
                for (int y = 12; y <= 15; y++)
                    require(at(model, x, y, z) == ShipHullVoxelLayout.Part.CABIN_WALL
                                    || at(model, x, y, z) == ShipHullVoxelLayout.Part.WINDOW,
                            "IV outer cabin wall is missing at " + x + "," + y + "," + z);
        }
        // The approved IV bow is an eighteen-station wedge whose underside
        // rises to a nearly level upper edge; do not revive the old bowsprit.
        int previousUnderside = 5;
        for (int z = end + 1; z <= end + 18; z++) {
            double t = (z - end) / 18D;
            int underside = 5 + (int)Math.round(8D * t * t * (3D - 2D * t));
            int upper = z >= end + 13 ? 16 : 15;
            int halfStem = z <= end + 7 ? 2 : 1;
            require(underside >= previousUnderside
                            && at(model, 0, underside - 1, z) == null
                            && at(model, 0, upper + 1, z) == null,
                    "IV bow deviated from the approved rising wedge " + p + " z=" + z);
            require(at(model, 0, underside, z) == ShipHullVoxelLayout.Part.KEEL
                            && at(model, 0, upper, z) == ShipHullVoxelLayout.Part.TRIM,
                    "IV bow spine disconnected " + p + " z=" + z);
            for (int side : new int[]{-halfStem, halfStem})
                for (int y = underside; y < upper; y++)
                    require(at(model, side, y, z) == ShipHullVoxelLayout.Part.FRAME,
                            "IV bow skin disconnected " + p + " x=" + side + " z=" + z);
            previousUnderside = underside;
        }
        stairRoute(model, rear, p.decks() >= 2 || p.bridge()
                        ? new int[]{1, 6, 11, 16, 21}
                        : new int[]{1, 6, 11, 16}, "IV " + p);
        lights(model, "IV " + p);
    }

    private static void brigantine(ShipSuperstructurePlan p) {
        var model = ShipHullVoxelLayout.generate(p);
        int end = p.hullLength() / 2, rear = -end + 2;
        for (int z = end - 7; z <= end + 3; z++)
            for (int x = -1; x <= 1; x++)
                for (int y = 8; y <= 9; y++)
                    require(at(model, x, y, z) == (z <= end
                            ? ShipHullVoxelLayout.Part.FRAME : ShipHullVoxelLayout.Part.BOWSPRIT),
                            "III bowsprit is floating or too thin " + p);
        int top = ShipHullVoxelLayout.brigantineTopDeck(p);
        stairRoute(model, rear, top >= 20
                        ? new int[]{1, 5, 10, 15, 20}
                        : top >= 15 ? new int[]{1, 5, 10, 15}
                        : new int[]{1, 5, 10}, "III " + p);
        if (top >= 15) {
            int half = Math.min(5, p.hullWidth() / 2 - 3);
            for (int x = -half + 1; x <= half - 1; x++)
                require(at(model, x, 12, rear) == ShipHullVoxelLayout.Part.WINDOW,
                        "III stern window row has a gap " + p + " x=" + x);
            require(at(model, 0, 11, rear + (top >= 20 ? 15 : 9))
                            == ShipHullVoxelLayout.Part.DOOR
                    && at(model, 0, 12, rear + (top >= 20 ? 15 : 9))
                            == ShipHullVoxelLayout.Part.DOOR,
                    "III cabin door missing " + p);
        }
        lights(model, "III " + p);
    }

    public static void main(String[] args) {
        int scenarios = 0;
        for (var type : List.of(ShipSectionPlan.ShipClass.LARGE,
                ShipSectionPlan.ShipClass.LARGEST)) {
            int revision = type == ShipSectionPlan.ShipClass.LARGE ? 3 : 4;
            int baseWidth = revision == 3 ? 15 : 21;
            int baseLength = revision == 3 ? 33 : 51;
            List<Integer> masts = revision == 3 ? List.of(1, 1) : List.of(1, 1, 1, 1);
            for (int extraWidth = 0; extraWidth <= 2; extraWidth++)
                for (int extraLength = 0; extraLength <= 3; extraLength++)
                    for (int deck : new int[] {1, 2, 3})
                        for (boolean cabin : new boolean[] {false, true})
                            for (boolean bridge : new boolean[] {false, true}) {
                                int width = baseWidth + extraWidth * 2;
                                int length = baseLength + extraLength * 4;
                                int minimum = (int)Math.ceil(width * 3.6D);
                                if ((minimum & 1) == 0) minimum++;
                                length = Math.max(length, minimum);
                                var p = new ShipSuperstructurePlan(type, width, length,
                                        masts, deck, bridge, cabin, revision);
                                double ratio = length / (double)width;
                                require(ratio >= 3.5D && ratio <= 4.5D,
                                        "III/IV disproportionate hull " + p);
                                if (revision == 3) brigantine(p);
                                else frigate(p);
                                scenarios++;
                            }
        }
        var medium = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.MEDIUM,
                13, 21, List.of(1, 1), 2, false, true);
        require(ShipHullVoxelLayout.generate(medium).get(
                new ShipHullVoxelLayout.Cell(0, 5, -21 / 2 + 2 + 6))
                == ShipHullVoxelLayout.Part.DOOR,
                "Retained MEDIUM cabin must continue working");
        System.out.println("PASS ShipHullVoxelLayoutCheck scenarios=" + scenarios
                + " assertions=" + checks);
    }
}
