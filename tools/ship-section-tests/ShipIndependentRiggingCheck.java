import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipRiggingPlan;
import org.slavicmyths.ships.ShipRiggingState;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Zero-Minecraft Java21 integration checks of independent rigging and lighting. */
public final class ShipIndependentRiggingCheck {
    private static int checks;
    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }

    private static ShipSuperstructurePlan large(ShipRiggingState rig, int decks) {
        return new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                21, 33, List.of(1), decks, false, false, List.of(rig));
    }

    private static int count(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> cells,
            ShipHullVoxelLayout.Part part) {
        return (int)cells.values().stream().filter(p -> p == part).count();
    }

    public static void main(String[] args) {
        ShipRiggingState basic = ShipRiggingState.defaults(List.of(1)).get(0);
        ShipSuperstructurePlan baseline = large(basic, 2);
        var unchanged = ShipHullVoxelLayout.generate(baseline, 6);
        check(baseline.customPhysicalRigging(), "Single-yard starter must generate physical custom rigging");
        var legacyTwoYards = large(new ShipRiggingState(0, 1, 0, 2, 0, 1, 1), 2);
        check(!legacyTwoYards.customPhysicalRigging(), "Existing two-yard ships must keep historical geometry");
        check(ShipHullVoxelLayout.generate(legacyTwoYards, 6).containsKey(
                new ShipHullVoxelLayout.Cell(0, 5, ShipRiggingPlan.forPlan(legacyTwoYards, 6).masts().get(0).z())),
                "Existing two-yard physical rig disappeared");
        var height = large(basic.increase(0), 2);
        var heightPlan = ShipRiggingPlan.forPlan(height, 6).masts().get(0);
        check(heightPlan.topY() > ShipRiggingPlan.forPlan(baseline, 6).masts().get(0).topY(),
                "Mast height must alter real mast top");
        check(ShipHullVoxelLayout.generate(height, 6).containsKey(
                new ShipHullVoxelLayout.Cell(0, heightPlan.topY(), heightPlan.z())),
                "Extended mast lacks physical voxels");
        check(height.propulsionFactor() == baseline.propulsionFactor(),
                "Mast height must not change established propulsion");
        var strength = large(basic.increase(1), 2);
        check(count(ShipHullVoxelLayout.generate(strength, 6), ShipHullVoxelLayout.Part.MAST)
                > count(ShipHullVoxelLayout.generate(height, 6), ShipHullVoxelLayout.Part.MAST) - 2,
                "Mast strengthening must add braces");
        var span = large(basic.increase(2), 2);
        check(ShipRiggingPlan.forPlan(span, 6).masts().get(0).yards().get(0).span()
                > ShipRiggingPlan.forPlan(height, 6).masts().get(0).yards().get(0).span(),
                "Yard width must change physical span");
        check(ShipRiggingPlan.forPlan(baseline, 6).masts().get(0).yards().size() == 1,
                "Starter mast must carry only one physical yard");
        var yardCount = large(basic.increase(3), 2);
        check(ShipRiggingPlan.forPlan(yardCount, 6).masts().get(0).yards().size() == 2,
                "New upper yard must be independently generated");
        var largerSail = large(basic.increase(4), 2);
        check(count(ShipHullVoxelLayout.generate(largerSail, 6), ShipHullVoxelLayout.Part.SAIL)
                > count(ShipHullVoxelLayout.generate(span, 6), ShipHullVoxelLayout.Part.SAIL) - 20,
                "Larger fabric must occupy more cloth voxels");
        var layers = large(basic.increase(3).increase(5), 2);
        check(ShipRiggingPlan.forPlan(layers, 6).masts().get(0).panels().size() == 2,
                "Two-layer sail must render as two separate panels");
        var fabric = large(basic.increase(6), 2);
        check(fabric.customPhysicalRigging(), "Fabric upgrade must retain the physical single-yard rig");
        check(ShipHullVoxelLayout.generate(fabric, 6).equals(unchanged),
                "Fabric-only upgrade altered physical hull geometry");
        check(baseline.propulsionFactor() == fabric.propulsionFactor(),
                "Fabric quality unexpectedly changed ship speed");
        var withUpperDeck = large(basic.increase(2), 3);
        check(ShipRiggingPlan.forPlan(withUpperDeck, 6).masts().get(0).yards().get(0).y()
                >= ShipRiggingPlan.forPlan(span, 6).masts().get(0).yards().get(0).y(),
                "New upper deck pushed a yard into the floor");
        check(ShipHullVoxelLayout.generate(withUpperDeck, 6).equals(
                ShipHullVoxelLayout.generate(withUpperDeck, 6)), "Non-deterministic rigging geometry");

        var v6 = ShipHullVoxelLayout.generate(baseline, 6);
        var v7 = ShipHullVoxelLayout.generate(baseline, 7);
        var v8 = ShipHullVoxelLayout.generate(baseline, 8);
        var v9 = ShipHullVoxelLayout.generate(baseline, 9);
        int light7 = count(v7, ShipHullVoxelLayout.Part.LANTERN);
        int light8 = count(v8, ShipHullVoxelLayout.Part.LANTERN);
        int light9 = count(v9, ShipHullVoxelLayout.Part.LANTERN);
        check(count(v6, ShipHullVoxelLayout.Part.LANTERN) == 0, "V6 legacy lighting changed");
        check(light7 > 0 && light8 >= light7 && light9 >= light8,
                "Lighting levels did not install real lantern cells");
        for (var entry : v9.entrySet()) if (entry.getValue() == ShipHullVoxelLayout.Part.LANTERN) {
            var pos = entry.getKey();
            var below = v9.get(new ShipHullVoxelLayout.Cell(pos.x(), pos.y() - 1, pos.z()));
            check(below == ShipHullVoxelLayout.Part.DECK
                    || below == ShipHullVoxelLayout.Part.BRIDGE
                    || below == ShipHullVoxelLayout.Part.HULL
                    || below == ShipHullVoxelLayout.Part.KEEL,
                    "Lantern must have real solid floor support");
        }
        check(v9.equals(ShipHullVoxelLayout.generate(baseline, 9)),
                "Light generation must be deterministic");
        System.out.println("SHIP_INDEPENDENT_RIGGING_LIGHT_PASS checks=" + checks);
    }
}
