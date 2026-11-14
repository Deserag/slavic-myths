package org.slavicmyths.ships;

import java.util.List;
import java.util.Map;

/**
 * Pure-Java IV regression check. No Minecraft, loader, JUnit or world required.
 *
 * Run from repository root (JDK 21):
 * javac -encoding UTF-8 -d .ship-iv-checks src/main/java/org/slavicmyths/ships/ShipSectionPlan.java src/main/java/org/slavicmyths/ships/ShipSuperstructurePlan.java src/main/java/org/slavicmyths/ships/ShipHullVoxelLayout.java src/main/java/org/slavicmyths/ships/ShipRigidMotion.java tools/ship-iv-checks/ShipIVHeadless.java
 * java -cp .ship-iv-checks org.slavicmyths.ships.ShipIVHeadless
 */
public final class ShipIVHeadless {
    private static final ShipSectionPlan.ShipClass TYPE =
            ShipSectionPlan.ShipClass.LARGEST;
    private static int configurations;
    private static int checkedSteps;
    private static int checkedHullCells;

    private static void require(boolean pass, String message) {
        if (!pass) throw new AssertionError(message);
    }

    private static ShipHullVoxelLayout.Part at(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            int x, int y, int z) {
        return blocks.get(new ShipHullVoxelLayout.Cell(x, y, z));
    }

    private static void stair(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            int x, int startZ, int lower, int upper, String label) {
        require(upper - lower == 5, "Unexpected IV stair height");
        require(at(blocks, x, lower, startZ) != null,
                label + " has no lower support");
        require(at(blocks, x, upper, startZ + 6) != null,
                label + " has no upper landing");
        for (int i = 0; i <= 6; i++) {
            int z = startZ + i;
            int y = i == 0 ? lower : i == 6 ? upper : lower + i;
            if (i > 0 && i < 6)
                require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.STAIR,
                        label + " missing step " + i);
            require(at(blocks, x, y + 1, z) == null,
                    label + " head collision at " + x + "," + (y + 1) + "," + z);
            require(at(blocks, x, y + 2, z) == null,
                    label + " upper head collision at " + x + "," + (y + 2) + "," + z);
            checkedSteps++;
        }
    }

    private static void geometry(int length, int width, int decks,
            boolean bridge, boolean cabin, int tier) {
        List<Integer> rig = List.of(tier, tier, tier, tier);
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(
                TYPE, width, length, rig, decks, bridge, cabin, 4);
        var blocks = ShipHullVoxelLayout.generate(plan);
        String key = length + "x" + width + " decks=" + decks
                + " bridge=" + bridge + " cabin=" + cabin + " tier=" + tier;
        require(plan.mastPositions().size() == 4, key + ": mast count");
        int end = length / 2;
        int cabinFront = -end + 10;
        for (int z : plan.mastPositions()) {
            require(z > cabinFront, key + ": mast intersects stern cabin z=" + z);
            require(z < end - 2, key + ": mast too near bow z=" + z);
            require(at(blocks, 0, 12, z) == ShipHullVoxelLayout.Part.MAST,
                    key + ": mast missing near deck z=" + z);
        }
        // A real plank must cover every station of the under-waterline keel.
        // No diagonal-only seam may connect the sea to the hold.
        for (int z = -end; z <= end; z++)
            for (int x = -width / 2; x <= width / 2; x++) {
                var base = at(blocks, x, 1, z);
                if (base != null)
                    require(base == ShipHullVoxelLayout.Part.HULL,
                            key + ": bottom replaced at x=" + x + " z=" + z);
                checkedHullCells++;
            }
        for (int[] route : new int[][] {
                {-3, -12, 1, 6}, {3, 6, 1, 6},
                {-4, -8, 6, 11}, {4, 2, 6, 11},
                {-5, -end + 11, 11, 16}, {5, -end + 11, 11, 16}
        }) stair(blocks, route[0], route[1], route[2], route[3], key);
        if (decks >= 2 || bridge)
            for (int x : new int[] {-3, 3})
                stair(blocks, x, -end + 5, 16, 21, key);
        for (var entry : blocks.entrySet()) {
            if (entry.getValue() != ShipHullVoxelLayout.Part.LANTERN) continue;
            var cell = entry.getKey();
            var support = at(blocks, cell.x(), cell.y() - 1, cell.z());
            require(support == ShipHullVoxelLayout.Part.HULL
                    || support == ShipHullVoxelLayout.Part.FRAME
                    || support == ShipHullVoxelLayout.Part.DECK
                    || support == ShipHullVoxelLayout.Part.BRIDGE,
                    key + ": floating lantern " + cell);
        }
        configurations++;
    }

    private static void motion() {
        var fast = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D),
                35D, ShipRigidMotion.MAX_IV_FORWARD);
        var normal = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D), 20D);
        for (int i = 0; i < 200; i++) {
            fast.advance(new ShipRigidMotion.Control(1D, 0D), pose -> true);
            normal.advance(new ShipRigidMotion.Control(1D, 0D), pose -> true);
        }
        require(fast.speed() > normal.speed() * 2D,
                "IV ship has no meaningful speed advantage");
        require(fast.speed() <= ShipRigidMotion.MAX_IV_FORWARD,
                "IV speed exceeds its bound");
        require(normal.speed() <= ShipRigidMotion.MAX_FORWARD,
                "Legacy ship speed exceeds its bound");
        require(fast.pose().z() > normal.pose().z(),
                "IV hull does not travel farther under identical input");
    }

    public static void main(String[] args) {
        for (int length : new int[]{51, 55, 59, 63})
            for (int width : new int[]{21, 23, 25})
                for (int decks = 1; decks <= 3; decks++)
                    for (boolean bridge : new boolean[]{false, true})
                        for (boolean cabin : new boolean[]{false, true})
                            for (int tier : new int[]{1, 2, 3})
                                geometry(length, width, decks, bridge, cabin, tier);
        motion();
        System.out.println("PASS: " + configurations + " IV configurations; "
                + checkedSteps + " stair stations; "
                + checkedHullCells + " bottom station probes; IV vs legacy motion");
    }

    private ShipIVHeadless() {}
}
