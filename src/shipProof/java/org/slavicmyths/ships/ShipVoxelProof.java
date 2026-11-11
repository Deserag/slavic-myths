package org.slavicmyths.ships;

import java.util.List;
import java.util.Map;

public final class ShipVoxelProof {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static ShipSuperstructurePlan plan(ShipSectionPlan.ShipClass type,
            int width, int length, List<Integer> tiers, int decks,
            boolean bridge, boolean cabin) {
        return new ShipSuperstructurePlan(type, width, length, tiers, decks, bridge, cabin);
    }

    private static void inspect(ShipSuperstructurePlan plan) {
        var blocks = ShipHullVoxelLayout.generate(plan);
        int half = plan.hullLength() / 2;
        check(ShipHullVoxelLayout.halfBeam(plan, half) == 0, "Bow is not sharp");
        check(ShipHullVoxelLayout.halfBeam(plan, -half) <= 1, "Stern is not narrowed");
        check(ShipHullVoxelLayout.halfBeam(plan, 0) == plan.hullWidth() / 2, "Middle beam");
        check(ShipHullVoxelLayout.halfBeam(plan, half - 1) <
                ShipHullVoxelLayout.halfBeam(plan, 0), "Forward shaping absent");
        for (int z = -half; z <= half; z++) {
            int beam = ShipHullVoxelLayout.halfBeam(plan, z);
            for (int x = -beam; x <= beam; x++) {
                check(blocks.containsKey(new ShipHullVoxelLayout.Cell(x, 1, z)),
                        "Leaky dry floor at " + x + "," + z);
                check(blocks.get(new ShipHullVoxelLayout.Cell(-x, 1, z)) ==
                        blocks.get(new ShipHullVoxelLayout.Cell(x, 1, z)),
                        "Asymmetric dry floor at " + x + "," + z);
                if (Math.abs(x) > Math.max(0, beam / 3))
                    check(!blocks.containsKey(new ShipHullVoxelLayout.Cell(x, 0, z)),
                            "Keel is too wide at " + x + "," + z);
            }
            for (int y = 1; y <= 3; y++)
                for (int x : new int[] {-beam, beam})
                    check(blocks.containsKey(new ShipHullVoxelLayout.Cell(x, y, z)),
                            "Leaky broadside at " + x + "," + y + "," + z);
        }
        check(blocks.get(new ShipHullVoxelLayout.Cell(0, 3, -2)) ==
                ShipHullVoxelLayout.Part.LIGHT, "Aft hold light missing");
        check(blocks.get(new ShipHullVoxelLayout.Cell(0, 3, 2)) ==
                ShipHullVoxelLayout.Part.LIGHT, "Fore hold light missing");
        if (plan.cabin() && plan.hullWidth() >= 9)
            check(blocks.get(new ShipHullVoxelLayout.Cell(0, 7, -half + 5)) ==
                    ShipHullVoxelLayout.Part.LIGHT, "Cabin light missing");
        if (plan.hullWidth() >= 9) {
            long windows = blocks.values().stream()
                    .filter(p -> p == ShipHullVoxelLayout.Part.HULL_WINDOW).count();
            check(windows >= 2, "No watertight illuminated hull windows");
        }
        for (int floor = 2; floor <= plan.decks(); floor++) {
            int lampY = 3 + (floor - 1) * 4;
            check(blocks.entrySet().stream().anyMatch(e -> e.getKey().y() == lampY
                    && e.getValue() == ShipHullVoxelLayout.Part.LIGHT),
                    "Missing light beneath elevated deck " + floor);
        }
        for (int z : plan.mastPositions()) {
            check(blocks.get(new ShipHullVoxelLayout.Cell(0, 7, z)) ==
                    ShipHullVoxelLayout.Part.MAST, "Mast core was overwritten");
            for (int y = 8; y <= 12; y++)
                check(blocks.get(new ShipHullVoxelLayout.Cell(0, y, z)) ==
                        ShipHullVoxelLayout.Part.MAST, "Interrupted mast");
        }
        long sails = blocks.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.SAIL).count();
        check(sails > 0, "No visible sails");
        check(sails >= plan.sailTiers().size() * 12, "Missing rigging panels");
    }

    public static void main(String[] args) {
        inspect(plan(ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, false));
        var smallCabin = plan(ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, true);
        var cabinBlocks = ShipHullVoxelLayout.generate(smallCabin);
        check(cabinBlocks.get(new ShipHullVoxelLayout.Cell(0, 5, -1)) ==
                ShipHullVoxelLayout.Part.DOOR, "Small cabin door missing");
        check(cabinBlocks.get(new ShipHullVoxelLayout.Cell(0, 7, -2)) ==
                ShipHullVoxelLayout.Part.LIGHT, "Small cabin lighting missing");
        check(cabinBlocks.get(new ShipHullVoxelLayout.Cell(0, 6, 0)) ==
                ShipHullVoxelLayout.Part.MAST, "Small cabin intersects mast");
        inspect(plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21, List.of(2, 2), 2, true, true));
        inspect(plan(ShipSectionPlan.ShipClass.LARGE, 21, 33, List.of(3, 3, 3), 3, true, true));
        inspect(plan(ShipSectionPlan.ShipClass.LARGEST, 21, 33, List.of(3, 3, 3, 3), 3, true, true));
        inspect(plan(ShipSectionPlan.ShipClass.SMALL, 7, 15, List.of(1), 1, false, false));
        inspect(plan(ShipSectionPlan.ShipClass.LARGE, 25, 41, List.of(3, 3, 3), 3, true, true));

        ShipSuperstructurePlan small = plan(ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, false);
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> old =
                ShipHullVoxelLayout.generateLegacy(small);
        check(old.get(new ShipHullVoxelLayout.Cell(0, 3, 2)) != ShipHullVoxelLayout.Part.LIGHT,
                "Legacy ship accidentally changed");
        check(old.get(new ShipHullVoxelLayout.Cell(0, 0, 5)) == ShipHullVoxelLayout.Part.KEEL,
                "Legacy bow changed");
        check(old.get(new ShipHullVoxelLayout.Cell(0, 5, -2)) ==
                ShipHullVoxelLayout.Part.HELM, "Legacy helm changed");
        System.out.println("PASS: 6 hull variants, shape/closure/rigging/lanterns and legacy palette");
    }
    private ShipVoxelProof() {}
}
