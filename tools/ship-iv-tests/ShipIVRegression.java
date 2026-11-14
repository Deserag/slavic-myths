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
        // The fixed access routes are part of every valid IV blueprint.
        int rear = -p.hullLength() / 2 + 3;
        int[][] routes = {
                {-3, -12, 1}, {3, 6, 1},
                {-4, -8, 6}, {4, 2, 6},
                {-5, rear + 8, 11}, {5, rear + 8, 11},
                {-3, rear + 2, 16}, {3, rear + 2, 16}
        };
        for (int[] r : routes) {
            if (r[2] == 16 && !(p.decks() >= 2 || p.bridge()))
                continue;
            for (int i = 1; i <= 5; i++) {
                int y = r[2] + i, z = r[1] + i;
                require(hull.get(new ShipHullVoxelLayout.Cell(r[0], y, z))
                        == ShipHullVoxelLayout.Part.STAIR,
                        "Missing stair tread at " + r[0] + "," + y + "," + z);
                for (int head = y + 1; head <= y + 2; head++)
                    require(!hull.containsKey(
                            new ShipHullVoxelLayout.Cell(r[0], head, z)),
                            "Blocked stair headroom: " + r[0] + "," + head + "," + z);
            }
        }
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
