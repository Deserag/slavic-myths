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
            int x, int z, int lower, int rise, String name) {
        for (int lane : new int[] {x, x - Integer.signum(x)})
            for (int i = 1; i <= rise; i++) {
                int yy = lower + i, zz = z + i;
                require(at(model, lane, yy, zz) == ShipHullVoxelLayout.Part.STAIR,
                        name + " missing paired step " + lane + "," + yy + "," + zz);
                for (int head = 1; head <= 3; head++)
                    require(at(model, lane, yy + head, zz) == null,
                            name + " blocked headroom " + lane + "," + yy + "," + zz);
            }
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
        for (int z = rear + 1; z < rear + 15; z++)
            for (int y = 12; y <= 15; y++)
                require(at(model, 0, y, z) != ShipHullVoxelLayout.Part.CABIN_WALL,
                        "IV has nested inner cabin bulkhead " + p);
        for (int z = end - 8; z <= end + 5; z++)
            for (int x = -1; x <= 1; x++)
                for (int y = 9; y <= 10; y++)
                    require(at(model, x, y, z) == (z <= end
                            ? ShipHullVoxelLayout.Part.FRAME : ShipHullVoxelLayout.Part.BOWSPRIT),
                            "IV bowsprit is detached or too thin " + p);
        for (int[] r : new int[][] {
                {-3, rear + 2, 1}, {3, rear + 2, 6},
                {-3, rear + 2, 11}
        }) flight(model, r[0], r[1], r[2], 5, "IV " + p);
        if (p.decks() >= 2 || p.bridge())
            flight(model, 3, rear + 2, 16, 5, "IV captain " + p);
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
        flight(model, -3, rear + 1, 1, 4, "III " + p);
        flight(model, 3, rear + 1, 5, 5, "III " + p);
        int top = ShipHullVoxelLayout.brigantineTopDeck(p);
        if (top >= 15) {
            flight(model, -3, rear + 1, 10, 5, "III quarterdeck " + p);
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
        if (top >= 20)
            flight(model, 3, rear + 1, 15, 5, "III upper " + p);
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
                        for (boolean cabin : new boolean[] {false, true}) {
                            int width = baseWidth + extraWidth * 2;
                            int length = baseLength + extraLength * 4;
                            int minimum = (int)Math.ceil(width * 3.6D);
                            if ((minimum & 1) == 0) minimum++;
                            length = Math.max(length, minimum);
                            var p = new ShipSuperstructurePlan(type, width, length,
                                    masts, deck, false, cabin, revision);
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
