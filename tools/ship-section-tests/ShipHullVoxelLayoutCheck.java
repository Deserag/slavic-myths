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
                    var keel = voxels.get(new ShipHullVoxelLayout.Cell(x, 0, z));
                    yes(keel == ShipHullVoxelLayout.Part.HULL
                            || keel == ShipHullVoxelLayout.Part.KEEL, "solid dry hull floor");
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
            if (config.decks() >= 2) {
                int upperZ = config.shipClass() == ShipSectionPlan.ShipClass.MEDIUM
                        ? -config.hullLength() / 2 + 5 : 3;
                yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 8, upperZ))
                                == ShipHullVoxelLayout.Part.DECK,
                        "supported upper deck floor");
            }
            if (config.decks() >= 3)
                yes(voxels.containsKey(new ShipHullVoxelLayout.Cell(1, 12, 3)),
                        "third deck floor");
            yes(!ShipHullVoxelLayout.count(voxels).isEmpty(), "material bill");
        }

        var cabinWithoutBridge = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1), 1, false, true);
        var cabinBlocks = ShipHullVoxelLayout.generate(cabinWithoutBridge);
        int helmZ = -cabinWithoutBridge.hullLength() / 2 + 4;
        yes(cabinBlocks.get(new ShipHullVoxelLayout.Cell(0, 9, helmZ))
                == ShipHullVoxelLayout.Part.HELM,
                "cabin moves the only helm above the roof");
        yes(cabinBlocks.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.HELM)
                .count() == 1, "cabin has only one helm");
        int cabinFront = -cabinWithoutBridge.hullLength() / 2 + 8;
        for (int y = 5; y <= 9; y++) {
            yes(cabinBlocks.get(new ShipHullVoxelLayout.Cell(-2, y, cabinFront + 1))
                    == ShipHullVoxelLayout.Part.LADDER, "roof-access ladder " + y);
            yes(cabinBlocks.get(new ShipHullVoxelLayout.Cell(-3, y, cabinFront + 1))
                    == ShipHullVoxelLayout.Part.FRAME, "ladder support " + y);
        }
        // Every supported MEDIUM arrangement must preserve access, the shaped
        // hold and exactly one active helm, including extensions and cabin.
        for (int width : new int[]{13, 15})
            for (int length : new int[]{21, 25, 29})
                for (int decks : new int[]{1, 2})
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true})
                            for (int[] sails : new int[][]{{1}, {2}, {1, 1}, {2, 2}}) {
                                List<Integer> tiers = new java.util.ArrayList<>();
                                for (int tier : sails) tiers.add(tier);
                                var variant = plan(ShipSectionPlan.ShipClass.MEDIUM,
                                        width, length, tiers, decks, bridge, cabin);
                                var model = ShipHullVoxelLayout.generate(variant);
                                yes(model.values().stream().filter(p ->
                                        p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                        "medium exactly one helm: " + variant);
                                yes(model.get(new ShipHullVoxelLayout.Cell(-2, 4, 1))
                                        == ShipHullVoxelLayout.Part.LADDER,
                                        "main hatch keeps ladder: " + variant);
                                yes(!model.containsKey(new ShipHullVoxelLayout.Cell(-2, 4, 0)),
                                        "main hatch open: " + variant);
                                if (decks == 2) {
                                    int accessZ = -length / 2 + 3 + (cabin ? 6 : 5);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(-2, 8, accessZ))
                                            == ShipHullVoxelLayout.Part.LADDER,
                                            "quarterdeck has dedicated exterior ladder: " + variant);
                                    yes(!model.containsKey(new ShipHullVoxelLayout.Cell(-2, 8, 0)),
                                            "no floating midships second-deck hatch: " + variant);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(-2, 8, 1))
                                            != ShipHullVoxelLayout.Part.LADDER,
                                            "old amidships shaft stops at main deck: " + variant);
                                }
                                if (cabin && !bridge) {
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 9,
                                            -length / 2 + 4))
                                            == ShipHullVoxelLayout.Part.HELM,
                                            "cabin roof helm: " + variant);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(-2, 9,
                                            -length / 2 + 9))
                                            == ShipHullVoxelLayout.Part.LADDER,
                                            "cabin roof access: " + variant);
                                }
                                for (int mastZ : variant.mastPositions())
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 6, mastZ))
                                            == ShipHullVoxelLayout.Part.MAST,
                                            "mast survives upgrades: " + variant);
                            }
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
        // Exhaust the supported MEDIUM hull/room/deck/rig variants. This is
        // the actual voxel generator used by parked AND moving modular ships.
        int configurations = 0;
        for (int length : new int[]{21, 25, 29})
            for (int width : new int[]{13, 15})
                for (int count : new int[]{1, 2})
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true})
                            for (List<Integer> sails : List.of(
                                    List.of(1), List.of(2), List.of(1, 1), List.of(2, 2))) {
                                var mediumVariant = plan(ShipSectionPlan.ShipClass.MEDIUM,
                                        width, length, sails, count, bridge, cabin);
                                var geometry = ShipHullVoxelLayout.generate(mediumVariant);
                                configurations++;
                                yes(geometry.values().stream().filter(part ->
                                        part == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                        "exactly one MEDIUM helm " + mediumVariant);
                                int rear = -length / 2 + 2;
                                int helmHeight = bridge ? 10 : cabin || count == 2 ? 9 : 5;
                                int variantHelmZ = bridge || cabin || count == 2
                                        ? rear + 2 : rear + 1;
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(0, helmHeight, variantHelmZ))
                                        == ShipHullVoxelLayout.Part.HELM,
                                        "MEDIUM helm aligned " + mediumVariant);
                                if (cabin) {
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 8, rear + 2))
                                            == ShipHullVoxelLayout.Part.DECK,
                                            "accessible cabin roof under helm");
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 5, rear + 6))
                                            == ShipHullVoxelLayout.Part.DOOR,
                                            "unblocked cabin entry");
                                }
                                if (cabin && !bridge) {
                                    for (int y = 5; y <= 9; y++) {
                                        yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, y, rear + 7))
                                                == ShipHullVoxelLayout.Part.LADDER,
                                                "cabin exterior ladder " + y);
                                        yes(geometry.get(new ShipHullVoxelLayout.Cell(-3, y, rear + 7))
                                                == ShipHullVoxelLayout.Part.FRAME,
                                                "cabin ladder attached " + y);
                                    }
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 9, rear + 6)) == null,
                                            "roof railing entrance must remain open");
                                }
                                for (int mastZ : mediumVariant.mastPositions()) {
                                    yes(mastZ > rear + (cabin ? 7 : 4),
                                            "mast clear of stern structures");
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 6, mastZ))
                                            == ShipHullVoxelLayout.Part.MAST,
                                            "mast base remains solid");
                                }
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 1, 1))
                                        == ShipHullVoxelLayout.Part.LADDER,
                                        "hold shaft accessible");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 4, 0)) == null,
                                        "hold hatch is open");
                                if (count == 2) {
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 8, 0)) == null,
                                            "upper hatch is open");
                                    for (int mastZ : mediumVariant.mastPositions())
                                        yes(geometry.get(new ShipHullVoxelLayout.Cell(1, 9, mastZ + 1))
                                                != ShipHullVoxelLayout.Part.SAIL,
                                                "sail does not cut through raised deck");
                                }
                            }
        yes(configurations == 192, "all MEDIUM variants enumerated");
        System.out.println("PASS ShipHullVoxelLayoutCheck assertions=" + checks);
    }
}
