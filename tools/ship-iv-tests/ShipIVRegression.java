import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/**
 * Standalone Java-21 regression test (no Minecraft or NeoForge dependency).
 * Compile the four pure planning classes and this file with javac.
 */
public final class ShipIVRegression {
    private static final ShipSectionPlan.ShipClass IV =
            ShipSectionPlan.ShipClass.LARGEST;
    private static int assertions;

    private static void require(boolean condition, String reason) {
        assertions++;
        if (!condition) throw new AssertionError(reason);
    }

    private static ShipSuperstructurePlan config(int length, int beam,
            int decks, boolean cabin, boolean bridge, int tier) {
        return new ShipSuperstructurePlan(IV, beam, length,
                List.of(tier, tier, tier, tier), decks,
                bridge, cabin, 4);
    }

    private static void assertFlight(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull,
            int x, int start, int lower) {
        require(hull.get(new ShipHullVoxelLayout.Cell(x, lower, start))
                == ShipHullVoxelLayout.Part.HULL && lower == 1
                || hull.get(new ShipHullVoxelLayout.Cell(x, lower, start))
                    == ShipHullVoxelLayout.Part.DECK
                || hull.get(new ShipHullVoxelLayout.Cell(x, lower, start))
                    == ShipHullVoxelLayout.Part.BRIDGE,
                "Missing stair approach at " + x + "," + lower + "," + start);
        require(hull.get(new ShipHullVoxelLayout.Cell(x, lower + 5, start - 6))
                == ShipHullVoxelLayout.Part.DECK
                || hull.get(new ShipHullVoxelLayout.Cell(x, lower + 5, start - 6))
                    == ShipHullVoxelLayout.Part.BRIDGE,
                "Missing stair exit at " + x + "," + lower + "," + start);
        for (int i = 1; i <= 5; i++) {
            int y = lower + i, z = start - i;
            require(hull.get(new ShipHullVoxelLayout.Cell(x, y, z))
                    == ShipHullVoxelLayout.Part.STAIR,
                    "Missing stair tread at " + x + "," + y + "," + z);
            for (int head = 1; head <= 3; head++)
                require(!hull.containsKey(new ShipHullVoxelLayout.Cell(x, y + head, z)),
                        "Blocked stair headroom: " + x + "," + (y + head) + "," + z);
        }
    }

    private static void testHull(ShipSuperstructurePlan p) {
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull =
                ShipHullVoxelLayout.generate(p);
        require(!hull.isEmpty() && hull.size() < 50000,
                "Empty or excessively large IV hull");
        require(p.mastPositions().size() == 4
                && new HashSet<>(p.mastPositions()).size() == 4,
                "IV must contain four independent mast stations");
        for (int z = -p.hullLength() / 2; z <= p.hullLength() / 2; z++) {
            int radius = ShipHullVoxelLayout.halfBeam(p, z);
            for (int x = -radius; x <= radius; x++)
                require(hull.get(new ShipHullVoxelLayout.Cell(x, 1, z))
                        == ShipHullVoxelLayout.Part.HULL,
                        "Underwater bottom hole: " + x + ",1," + z);
        }
        for (int z : p.mastPositions()) {
            require(hull.get(new ShipHullVoxelLayout.Cell(0, 12, z))
                    == ShipHullVoxelLayout.Part.MAST,
                    "Mast missing: z=" + z);
            require(z < p.hullLength() / 2 - 12,
                    "Mast collides with IV bow stem");
        }
        // Two interior flights finish on the open weather deck ahead of
        // the cabin, without a floor opening inside the captain's room.
        int rear = -p.hullLength() / 2 + 4;
        int startZ = rear + 30;
        for (int flight = 0; flight < 2; flight++) {
            int lower = 1 + flight * 5;
            for (int x : new int[]{-3, -2})
                assertFlight(hull, x, startZ - 6 * flight, lower);
        }
        // The captain's station is reached from the quarterdeck via its
        // own port-side exterior access; it cannot hijack the main route.
        for (int x : new int[]{6, 7})
            assertFlight(hull, x, rear + 21, 11);
        if (p.decks() >= 2 || p.bridge())
            for (int x : new int[]{-5, -4})
                assertFlight(hull, x, rear + 10, 16);
        require(hull.get(new ShipHullVoxelLayout.Cell(0,
                p.decks() >= 2 || p.bridge() ? 22 : 17, rear + 4))
                == ShipHullVoxelLayout.Part.HELM, "Missing wheel at captain station");
        int rim = ShipHullVoxelLayout.halfBeam(p, rear + 8);
        require(hull.get(new ShipHullVoxelLayout.Cell(rim, 16, rear + 8))
                    == ShipHullVoxelLayout.Part.DECK
                && hull.get(new ShipHullVoxelLayout.Cell(rim, 17, rear + 8))
                    == ShipHullVoxelLayout.Part.RAIL,
                "Quarterdeck rail does not follow the outboard rim");
        int station = rear + 20;
        int beam = ShipHullVoxelLayout.halfBeam(p, station);
        for (int edge : new int[]{-beam, beam}) {
            require(hull.get(new ShipHullVoxelLayout.Cell(edge, 12, station))
                    == ShipHullVoxelLayout.Part.RAIL,
                    "Weather rail is not at the physical edge of the IV hull");
            require(hull.get(new ShipHullVoxelLayout.Cell(
                    edge > 0 ? edge - 2 : edge + 2, 12, station))
                    != ShipHullVoxelLayout.Part.RAIL,
                    "Old internal IV guardrail still cuts across the deck");
        }
        require(hull.get(new ShipHullVoxelLayout.Cell(-3, 11, rear + 18))
                    == ShipHullVoxelLayout.Part.DECK,
                "Internal stairs fail to arrive on open deck");
        int helmFloor = p.decks() >= 2 || p.bridge() ? 21 : 16;
        require(hull.get(new ShipHullVoxelLayout.Cell(0,
                        helmFloor + 1, rear + 1))
                    == ShipHullVoxelLayout.Part.TRIM,
                "Captain stern screen missing behind helm");
        require(hull.get(new ShipHullVoxelLayout.Cell(0,
                        helmFloor + 1, rear + 5)) == null,
                "Captain helm's forward approach is blocked");
        require(hull.values().stream().filter(part ->
                part == ShipHullVoxelLayout.Part.RAIL).count() >= 30,
                "Open-deck safety rails missing");
        require(hull.get(new ShipHullVoxelLayout.Cell(0, 12, rear + 15))
                == ShipHullVoxelLayout.Part.DOOR
                && hull.get(new ShipHullVoxelLayout.Cell(0, 13, rear + 15))
                == ShipHullVoxelLayout.Part.DOOR, "Cabin double-height door missing");

    }

    private static void testModifications() {
        ShipSuperstructurePlan[] stages = {
                config(51, 21, 1, false, false, 1),
                config(63, 21, 1, false, false, 1),
                config(63, 25, 1, false, false, 1),
                config(63, 25, 1, false, false, 2),
                config(63, 25, 1, false, false, 3),
                config(63, 25, 2, false, false, 3),
                config(63, 25, 2, true, false, 3),
                config(63, 25, 2, true, true, 3),
                config(63, 25, 3, true, true, 3)
        };
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> previous = null;
        for (int i = 0; i < stages.length; i++) {
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> current =
                    ShipHullVoxelLayout.generate(stages[i]);
            testHull(stages[i]);
            if (previous != null)
                require(!previous.equals(current),
                        "Upgrade stage " + (i + 1) + " changed no physical blocks");
            previous = current;
        }
        require(stages[8].propulsionFactor() > stages[0].propulsionFactor(),
                "Upgraded sails must provide higher thrust");
    }

    private static void testCruiseSpeed() {
        ShipRigidMotion legacy = new ShipRigidMotion(
                new ShipRigidMotion.Pose(0, 0, 0), 7);
        ShipRigidMotion frigate = new ShipRigidMotion(
                new ShipRigidMotion.Pose(0, 0, 0), 30,
                ShipRigidMotion.MAX_IV_FORWARD);
        for (int i = 0; i < 120; i++) {
            legacy.advance(new ShipRigidMotion.Control(1, 0), pose -> true);
            frigate.advance(new ShipRigidMotion.Control(1, 0), pose -> true);
        }
        require(frigate.speed() > legacy.speed() * 2,
                "IV speed must be significantly above I-III");
        require(frigate.speed() <= ShipRigidMotion.MAX_IV_FORWARD,
                "IV exceeded its speed cap");
    }

    public static void main(String[] args) {
        int configs = 0;
        for (int length : new int[]{51, 55, 59, 63})
            for (int width : new int[]{21, 23, 25})
                for (int decks = 1; decks <= 3; decks++)
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true}) {
                            testHull(config(length, width, decks,
                                    cabin, bridge, 1));
                            configs++;
                        }
        testModifications();
        testCruiseSpeed();
        System.out.println("SHIP_IV_OK: " + configs + " configurations, "
                + assertions + " assertions, 9 upgrade stages, 2 motion profiles");
    }

    private ShipIVRegression() {}
}
