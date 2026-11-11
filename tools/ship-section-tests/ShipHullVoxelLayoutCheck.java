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
        // New V2 boats must be actual stepped wooden vessels while old saves
        // use the unchanged version-one palette.
        for (var config : List.of(small, medium, large, largest)) {
            var legacy = ShipHullVoxelLayout.generate(config, 1);
            var v2 = ShipHullVoxelLayout.generate(config, 2);
            int halfLength = config.hullLength() / 2;
            yes(legacy.equals(ShipHullVoxelLayout.generate(config)), "V1 layout remains exact");
            yes(v2.equals(ShipHullVoxelLayout.generate(config, 2)), "V2 deterministic");
            yes(!v2.equals(legacy), "V2 is not a cosmetic V1 alias");
            yes(ShipHullVoxelLayout.halfBeam(config, halfLength, 2) == 0, "V2 pointed bow");
            yes(ShipHullVoxelLayout.halfBeam(config, -halfLength, 2) == 1, "V2 tapered stern");
            yes(ShipHullVoxelLayout.halfBeam(config, 0, 2) > 1, "V2 broad midship");
            yes(ShipHullVoxelLayout.halfBeam(config, 0, 2)
                    > ShipHullVoxelLayout.halfBeam(config, halfLength - 2, 2),
                    "V2 bow tapers gradually");
            yes(ShipHullVoxelLayout.halfBeam(config, 0, 2)
                    > ShipHullVoxelLayout.halfBeam(config, -halfLength + 1, 2),
                    "V2 stern tapers gradually");
            for (int z = -halfLength; z <= halfLength; z++) {
                int beam = ShipHullVoxelLayout.halfBeam(config, z, 2);
                yes(v2.get(new ShipHullVoxelLayout.Cell(0, 0, z))
                        == ShipHullVoxelLayout.Part.KEEL, "V2 continuous keel " + z);
                for (int x = -beam; x <= beam; x++) {
                    var floorBlock = v2.get(new ShipHullVoxelLayout.Cell(x, 1, z));
                    var expectedFloor = z == 1 && x == -2 ? ShipHullVoxelLayout.Part.LADDER
                            : z == 1 && x == -3 ? ShipHullVoxelLayout.Part.FRAME
                            : ShipHullVoxelLayout.Part.HULL;
                    yes(floorBlock == expectedFloor,
                            "V2 sealed dry hold floor " + x + ":" + z);
                    if (Math.abs(x) > Math.max(0, beam - 2) && !(z == 1 && x == -2))
                        yes(!v2.containsKey(new ShipHullVoxelLayout.Cell(x, 0, z)),
                                "V2 narrowed underwater hull " + x + ":" + z);
                }
            }
            yes(v2.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.HELM)
                    .count() == 1, "V2 has exactly one helm");
            yes(v2.values().contains(ShipHullVoxelLayout.Part.BOWSPRIT),
                    "V2 timber bowsprit");
            yes(v2.values().contains(ShipHullVoxelLayout.Part.SAIL),
                    "V2 physical sailcloth");
            yes(v2.values().contains(ShipHullVoxelLayout.Part.SAIL_TRIM),
                    "V2 red woven ornament");
            yes(v2.values().contains(ShipHullVoxelLayout.Part.STANDING_LANTERN),
                    "V2 illuminated outdoor deck");
            yes(v2.containsValue(ShipHullVoxelLayout.Part.LIGHT),
                    "V2 has real lantern inside");
            for (int i = 0; i < config.sailTiers().size(); i++) {
                int z = config.mastPositions().get(i), tier = config.sailTiers().get(i);
                yes(v2.get(new ShipHullVoxelLayout.Cell(0, 6, z))
                        == ShipHullVoxelLayout.Part.MAST, "V2 supported mast");
                yes(v2.get(new ShipHullVoxelLayout.Cell(0, 13 + (tier - 1) * 7 + 2, z))
                        == ShipHullVoxelLayout.Part.MAST, "V2 full mast height");
                for (int layer = 0; layer < tier; layer++) {
                    int low = 8 + layer * 7;
                    boolean panel = false;
                    for (int x = -config.hullWidth() / 2; x <= config.hullWidth() / 2; x++)
                        for (int y = low; y <= low + 4; y++)
                            for (int zz = z + 1; zz <= z + 2; zz++) {
                                var material = v2.get(new ShipHullVoxelLayout.Cell(x, y, zz));
                                if (material == ShipHullVoxelLayout.Part.SAIL
                                        || material == ShipHullVoxelLayout.Part.SAIL_TRIM)
                                    panel = true;
                            }
                    yes(panel, "V2 mast " + i + " level " + layer + " carries sail");
                }
            }
            for (int floor = 1; floor <= config.decks(); floor++) {
                int lightY = floor == 1 ? 2 : floor == 2 ? 7 : 11;
                boolean lit = v2.entrySet().stream().anyMatch(e ->
                        e.getValue() == ShipHullVoxelLayout.Part.LIGHT
                        && e.getKey().y() == lightY);
                yes(lit, "V2 interior level " + floor + " is lit");
            }
            if (config.cabin()) yes(v2.values().contains(ShipHullVoxelLayout.Part.WINDOW),
                    "V2 captain cabin retains windows");
        }

        System.out.println("PASS ShipHullVoxelLayoutCheck assertions=" + checks);
    }
}
