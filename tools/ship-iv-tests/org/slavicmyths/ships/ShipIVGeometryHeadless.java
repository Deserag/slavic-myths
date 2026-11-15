package org.slavicmyths.ships;

import java.util.List;
import java.util.Map;

/**
 * Offline regression harness for the actual Java IV geometry and motion kernel.
 * Run: ./gradlew verifyShipIVGeometry
 * This test has no Minecraft world dependency and is excluded from the mod JAR.
 */
public final class ShipIVGeometryHeadless {
    private static int checked;

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }

    private static ShipSuperstructurePlan plan(int lengthExtra, int widthExtra,
            int decks, boolean bridge, boolean cabin, List<Integer> tiers) {
        return new ShipBuildConfiguration(ShipSectionPlan.ShipClass.LARGEST,
                lengthExtra, widthExtra, tiers, decks, bridge, cabin, 4).structure();
    }

    private static ShipHullVoxelLayout.Part at(Map<ShipHullVoxelLayout.Cell,
            ShipHullVoxelLayout.Part> cells, int x, int y, int z) {
        return cells.get(new ShipHullVoxelLayout.Cell(x, y, z));
    }

    private static boolean isWalkable(ShipHullVoxelLayout.Part p) {
        return p == ShipHullVoxelLayout.Part.HULL
                || p == ShipHullVoxelLayout.Part.DECK
                || p == ShipHullVoxelLayout.Part.BRIDGE;
    }

    private static void flight(Map<ShipHullVoxelLayout.Cell,
            ShipHullVoxelLayout.Part> blocks, int x, int start, int lower) {
        flightLane(blocks, x, start, lower);
        int paired = x - Integer.signum(x);
        flightLane(blocks, paired, start, lower);
    }

    private static void flightLane(Map<ShipHullVoxelLayout.Cell,
            ShipHullVoxelLayout.Part> blocks, int x, int start, int lower) {
        int upper = lower + 5;
        require(isWalkable(at(blocks, x, lower, start)),
                "Missing lower approach " + x + "," + lower + "," + start);
        require(isWalkable(at(blocks, x, upper, start + 6)),
                "Missing upper landing " + x + "," + upper + "," + (start + 6));
        for (int i = 0; i <= 6; i++) {
            int y = i == 0 ? lower : i == 6 ? upper : lower + i;
            int z = start + i;
            require(at(blocks, x, y + 1, z) == null
                    && at(blocks, x, y + 2, z) == null
                    && at(blocks, x, y + 3, z) == null,
                    "Head collision " + x + "," + y + "," + z);
            if (i > 0 && i < 6)
                require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.STAIR,
                        "Missing stair " + x + "," + y + "," + z);
        }
    }

    private static void validate(ShipSuperstructurePlan config) {
        double ratio = config.hullLength() / (double)config.hullWidth();
        require(ratio >= 3.5D && ratio <= 4.5D,
                "IV hull proportions outside ship profile: " + ratio);
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cells =
                ShipHullVoxelLayout.generate(config);
        require(!cells.isEmpty(), "Empty IV layout");
        int end = config.hullLength() / 2;
        int rear = -end + 4;
        for (int z = -end; z <= end; z++) {
            int beam = ShipHullVoxelLayout.halfBeam(config, z);
            for (int x = -beam; x <= beam; x++)
                require(at(cells, x, 1, z) == ShipHullVoxelLayout.Part.HULL,
                        "Unsealed floor " + x + ",1," + z);
        }
        for (int y : new int[]{12, 14, 15})
            require(at(cells, 0, y, rear) == ShipHullVoxelLayout.Part.CABIN_WALL,
                    "Open stern cabin at height " + y);
        int sternHalf = Math.min(7, Math.max(2,
                ShipHullVoxelLayout.halfBeam(config, rear) - 2));
        for (int x = -sternHalf + 1; x < sternHalf; x++)
            require(at(cells, x, 13, rear) == ShipHullVoxelLayout.Part.WINDOW,
                    "Stern window row has a gap at " + x);
        for (var cell : cells.entrySet()) {
            if (cell.getValue() != ShipHullVoxelLayout.Part.LANTERN
                    && cell.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN) continue;
            var pos = cell.getKey();
            int support = cell.getValue() == ShipHullVoxelLayout.Part.HANGING_LANTERN
                    ? pos.y() + 1 : pos.y() - 1;
            require(isWalkable(at(cells, pos.x(), support, pos.z()))
                    || at(cells, pos.x(), support, pos.z())
                            == ShipHullVoxelLayout.Part.FRAME
                    || at(cells, pos.x(), support, pos.z())
                            == ShipHullVoxelLayout.Part.TRIM,
                    "Unsupported lantern at " + pos);
            for (var other : cells.entrySet()) {
                if (other.getKey().equals(cell.getKey()) || other.getValue() != ShipHullVoxelLayout.Part.LANTERN
                        && other.getValue() != ShipHullVoxelLayout.Part.HANGING_LANTERN) continue;
                var p = other.getKey();
                int dx = p.x() - pos.x(), dy = p.y() - pos.y(), dz = p.z() - pos.z();
                require(dx * dx + dy * dy + dz * dz >= 25,
                        "Lanterns are too close: " + pos + " / " + p);
            }
        }
        require(config.mastPositions().size() == 4, "Not four mast stations");
        for (int z : config.mastPositions())
            require(at(cells, 0, 12, z) == ShipHullVoxelLayout.Part.MAST,
                    "Missing physical mast " + z);
        flight(cells, -3, rear + 2, 1);
        flight(cells, 3, rear + 2, 6);
        flight(cells, -3, rear + 2, 11);
        if (config.decks() >= 2 || config.bridge())
            flight(cells, 3, rear + 2, 16);
        checked++;
    }

    private static double simulate(double factor, double cap) {
        ShipRigidMotion motion = new ShipRigidMotion(
                new ShipRigidMotion.Pose(0, 0, 0), 35, cap);
        for (int i = 0; i < 200; i++)
            motion.advance(new ShipRigidMotion.Control(factor, 0), pose -> true);
        return motion.speed();
    }

    public static void main(String[] args) {
        List<List<Integer>> sails = List.of(
                List.of(1, 1, 1, 1),
                List.of(2, 2, 2, 2),
                List.of(3, 3, 3, 3),
                List.of(1, 2, 3, 1));
        for (int length = 0; length <= 3; length++)
            for (int width = 0; width <= 2; width++)
                for (int deck = 1; deck <= 3; deck++)
                    for (boolean bridge : new boolean[]{false, true})
                        for (boolean cabin : new boolean[]{false, true})
                            for (List<Integer> tiers : sails)
                                validate(plan(length, width, deck, bridge, cabin, tiers));

        double base = simulate(plan(0, 0, 1, false, false,
                List.of(1, 1, 1, 1)).propulsionFactor(),
                ShipRigidMotion.MAX_IV_FORWARD);
        double upgraded = simulate(plan(3, 2, 3, true, true,
                List.of(3, 3, 3, 3)).propulsionFactor(),
                ShipRigidMotion.MAX_IV_FORWARD);
        double legacy = simulate(1D, ShipRigidMotion.MAX_FORWARD);
        require(base > .38D, "IV base ship too slow: " + base);
        require(upgraded > base + .15D, "IV upgrades do not increase speed");
        require(upgraded > legacy * 1.7D,
                "IV upgraded speed not significantly above old ships");
        System.out.printf("PASS IV: %d configurations; base %.3f, upgraded %.3f, old %.3f blocks/tick%n",
                checked, base, upgraded, legacy);
    }

    private ShipIVGeometryHeadless() {}
}
