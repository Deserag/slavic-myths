import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;

public final class ShipHullVoxelLayoutCheck {
    private static int checks;
    private static void yes(boolean value, String name) {
        checks++;
        if (!value) throw new AssertionError(name);
    }

    private static ShipSuperstructurePlan plan(ShipSectionPlan.ShipClass type,
            int width, int length, List<Integer> sails, int decks,
            boolean bridge, boolean cabin) {
        return new ShipSuperstructurePlan(type, width, length, sails, decks, bridge, cabin);
    }

    public static void main(String[] args) {
        var small = plan(ShipSectionPlan.ShipClass.SMALL, 7, 11,
                List.of(1), 1, false, false);
        var medium = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1, 2), 2, true, true);
        var large = plan(ShipSectionPlan.ShipClass.LARGE, 21, 33,
                List.of(1, 2, 3), 3, true, true);
        var largest = plan(ShipSectionPlan.ShipClass.LARGEST, 21, 33,
                List.of(3, 3, 2, 1), 3, true, true);

        for (var config : List.of(small, medium, large, largest)) {
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> voxels =
                    ShipHullVoxelLayout.generate(config);
            yes(!voxels.isEmpty(), "nonempty geometry " + config.shipClass());
            yes(voxels.equals(ShipHullVoxelLayout.generate(config)), "deterministic geometry");
            yes(ShipHullVoxelLayout.halfBeam(config, config.hullLength() / 2) == 0,
                    "sharp bow tip");
            for (int z = -config.hullLength() / 2; z <= config.hullLength() / 2; z++) {
                int beam = ShipHullVoxelLayout.halfBeam(config, z);
                for (int x = -beam; x <= beam; x++) {
                    var floor = voxels.get(new ShipHullVoxelLayout.Cell(x, 1, z));
                    yes(floor == ShipHullVoxelLayout.Part.HULL
                            || floor == ShipHullVoxelLayout.Part.FRAME
                            || floor == ShipHullVoxelLayout.Part.LADDER,
                            "solid raised dry hold floor");
                }
            }
            yes(voxels.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.HELM)
                    .count() == 1, "exactly one functional helm");
            yes(voxels.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.MAST)
                    .count() > 0, "mast blocks");
            for (int z : config.mastPositions()) {
                yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 6, z))
                        == ShipHullVoxelLayout.Part.MAST, "mast at planned station");
            }
            if (config.cabin()) {
                int front = -config.hullLength() / 2 + 8;
                yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 5, front))
                        == ShipHullVoxelLayout.Part.DOOR, "cabin doorway lower half");
                yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 6, front))
                        == ShipHullVoxelLayout.Part.DOOR, "cabin doorway upper half");
                yes(voxels.values().contains(ShipHullVoxelLayout.Part.WINDOW),
                        "cabin windows installed");
            }
            if (config.bridge()) yes(voxels.values().contains(ShipHullVoxelLayout.Part.BRIDGE),
                    "raised quarterdeck installed");
            if (config.decks() >= 2)
                yes(voxels.containsKey(new ShipHullVoxelLayout.Cell(0, 8, 3)),
                        "upper deck floor");
            if (config.decks() >= 3)
                yes(voxels.containsKey(new ShipHullVoxelLayout.Cell(1, 12, 3)),
                        "third deck floor");
            yes(!ShipHullVoxelLayout.count(voxels).isEmpty(), "material bill");
        }

        var cabinWithoutBridge = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1), 1, false, true);
        var cabinBlocks = ShipHullVoxelLayout.generate(cabinWithoutBridge);
        int helmZ = -cabinWithoutBridge.hullLength() / 2 + 9;
        yes(cabinBlocks.get(new ShipHullVoxelLayout.Cell(0, 5, helmZ))
                == ShipHullVoxelLayout.Part.HELM,
                "lower helm must remain outside an aft cabin");
        var bare = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(), 1, false, false);
        var withMast = bare.withSail(1);
        var delta = ShipHullVoxelLayout.additions(
                ShipHullVoxelLayout.generate(bare), ShipHullVoxelLayout.generate(withMast));
        yes(delta.getOrDefault(ShipHullVoxelLayout.Part.MAST, 0) > 0,
                "mast costs real wood");
        yes(delta.getOrDefault(ShipHullVoxelLayout.Part.SAIL, 0) > 0,
                "sail costs cloth");
        var decks = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(), 2, false, false);
        var addedDeck = ShipHullVoxelLayout.additions(
                ShipHullVoxelLayout.generate(bare), ShipHullVoxelLayout.generate(decks));
        yes(addedDeck.getOrDefault(ShipHullVoxelLayout.Part.DECK, 0) > 0,
                "deck bills include new flooring");
        System.out.println("PASS ShipHullVoxelLayoutCheck assertions=" + checks);
    }
}
