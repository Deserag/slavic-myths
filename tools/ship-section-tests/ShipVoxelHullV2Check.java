import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Pure Java geometry regression checks. No Minecraft runtime required. */
public final class ShipVoxelHullV2Check {
    private static int assertions;
    private static void check(boolean success, String message) {
        assertions++;
        if (!success) throw new AssertionError(message);
    }

    private static void verify(ShipSectionPlan.ShipClass type, int width, int length,
            List<Integer> sails, int decks, boolean bridge, boolean cabin) {
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(type, width, length,
                sails, decks, bridge, cabin);
        var v1 = ShipHullVoxelLayout.generate(plan);
        var legacy = ShipHullVoxelLayout.generate(plan, 1);
        check(v1.equals(legacy), type + " must preserve V1");
        var voxels = ShipHullVoxelLayout.generate(plan, 2);
        check(voxels.equals(ShipHullVoxelLayout.generate(plan, 2)), "deterministic V2");
        check(!voxels.isEmpty(), "nonempty " + type);
        int half = length / 2;
        check(ShipHullVoxelLayout.halfBeamV2(plan, half) == 0, "sharp bow");
        check(ShipHullVoxelLayout.halfBeamV2(plan, -half) == 1, "shaped stern");
        check(ShipHullVoxelLayout.halfBeamV2(plan, 0) == width / 2, "full midship");
        for (int z = -half; z <= half; z++) {
            int beam = ShipHullVoxelLayout.halfBeamV2(plan, z);
            check(beam <= width / 2, "beam bound");
            check(beam >= 0, "beam nonnegative");
            for (int x = -beam; x <= beam; x++) {
                var floor = voxels.get(new ShipHullVoxelLayout.Cell(x, 0, z));
                check(floor == ShipHullVoxelLayout.Part.HULL
                        || floor == ShipHullVoxelLayout.Part.KEEL, "watertight hold floor");
            }
            check(beam == ShipHullVoxelLayout.halfBeam(plan, z, 2), "versioned beam");
        }
        check(voxels.values().contains(ShipHullVoxelLayout.Part.LIGHT),
                "real lantern placement required " + type);
        check(voxels.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.HELM)
                .count() == 1, "single helm");
        check(voxels.values().contains(ShipHullVoxelLayout.Part.MAST), "mast placed");
        check(voxels.values().contains(ShipHullVoxelLayout.Part.SAIL), "canvas placed");
        check(voxels.values().contains(ShipHullVoxelLayout.Part.YARD), "yards placed");
        check(voxels.values().contains(ShipHullVoxelLayout.Part.RIGGING), "stays placed");
        if (sails.stream().anyMatch(t -> t >= 2))
            check(voxels.values().contains(ShipHullVoxelLayout.Part.SAIL_TRIM), "trim present");
        for (int z : plan.mastPositions())
            check(voxels.get(new ShipHullVoxelLayout.Cell(0, 6, z))
                    == ShipHullVoxelLayout.Part.MAST, "mast roots intact");
        int outer = ShipHullVoxelLayout.halfBeamV2(plan, 0);
        check(ShipHullVoxelLayout.halfBeamV2(plan, half - 2) < outer,
                "bow narrows well before the tip");
        for (int floor = 2; floor <= decks; floor++) {
            int underside = 4 + (floor - 1) * 4 - 1;
            long lamps = voxels.entrySet().stream()
                    .filter(e -> e.getKey().y() == underside
                            && e.getValue() == ShipHullVoxelLayout.Part.LIGHT)
                    .count();
            check(lamps >= 2, "at least two lower ceiling lights " + type + " floor=" + floor);
            for (var entry : voxels.entrySet())
                if (entry.getKey().y() == underside
                        && entry.getValue() == ShipHullVoxelLayout.Part.LIGHT)
                    check(voxels.get(new ShipHullVoxelLayout.Cell(
                            entry.getKey().x(), underside + 1, entry.getKey().z()))
                            == ShipHullVoxelLayout.Part.DECK,
                            "hanging lantern supported by real deck");
        }
        check(!ShipHullVoxelLayout.count(voxels).isEmpty(), "count materials");
    }

    public static void main(String[] args) {
        verify(ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, false);
        verify(ShipSectionPlan.ShipClass.MEDIUM, 13, 21, List.of(1, 2), 2, true, true);
        verify(ShipSectionPlan.ShipClass.LARGE, 21, 33, List.of(1, 2, 3), 3, true, true);
        verify(ShipSectionPlan.ShipClass.LARGEST, 21, 33, List.of(3, 3, 2, 1), 3, true, true);
        verify(ShipSectionPlan.ShipClass.MEDIUM, 15, 25, List.of(2, 2), 2, true, true);
        verify(ShipSectionPlan.ShipClass.LARGE, 25, 37, List.of(3, 3, 3), 3, true, true);
        System.out.println("PASS ShipVoxelHullV2Check assertions=" + assertions);
    }
}
