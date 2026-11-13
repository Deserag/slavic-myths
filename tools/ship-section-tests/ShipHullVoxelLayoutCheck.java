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
        // Exercise the actual mast/sail upgrades independently of the hull
        // dimensions. A rig upgrade must never reset geometry revision 2.
        for (int count : new int[]{1, 2, 3})
            for (int tier : new int[]{1, 2, 3})
                for (int decksCount : new int[]{1, 2, 3}) {
                    List<Integer> tiers = new java.util.ArrayList<>();
                    for (int i = 0; i < count; i++) tiers.add(tier);
                    var rig = new ShipSuperstructurePlan(
                            ShipSectionPlan.ShipClass.LARGE, 25, 45,
                            tiers, decksCount, true, true, 2);
                    var voxels = ShipHullVoxelLayout.generate(rig);
                    yes(voxels.values().stream().filter(p ->
                            p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                            "LARGE rig has only one helm");
                    for (int z : rig.mastPositions()) {
                        yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 6, z))
                                == ShipHullVoxelLayout.Part.MAST,
                                "LARGE upgraded mast stays anchored");
                        int upperDeck = 4 + (decksCount - 1) * 4;
                        for (int height = upperDeck + 1; height <= upperDeck + 2; height++) {
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(1, height, z + 1))
                                    != ShipHullVoxelLayout.Part.SAIL,
                                    "LARGE sail clears deck headroom");
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(1, height, z))
                                    != ShipHullVoxelLayout.Part.YARD,
                                    "LARGE lower yard clears deck headroom");
                        }
                    }
                    yes(voxels.get(new ShipHullVoxelLayout.Cell(-2, 4, 0)) == null,
                            "LARGE main hatch stays open");
                    yes(voxels.get(new ShipHullVoxelLayout.Cell(-2, 4, 1))
                            == ShipHullVoxelLayout.Part.LADDER,
                            "LARGE hold remains accessible");
                }
        // LARGE V2 is opt-in: old blueprints retain exactly their legacy shape.
        for (int width : new int[]{21, 23, 25})
            for (int length : new int[]{33, 37, 41, 45})
                for (int floors : new int[]{1, 2, 3})
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true}) {
                            var oldLarge = plan(ShipSectionPlan.ShipClass.LARGE,
                                    width, length, List.of(1, 2), floors, bridge, cabin);
                            var newLarge = new ShipSuperstructurePlan(
                                    ShipSectionPlan.ShipClass.LARGE, width, length,
                                    List.of(1, 2), floors, bridge, cabin, 2);
                            var oldCells = ShipHullVoxelLayout.generate(oldLarge);
                            var newCells = ShipHullVoxelLayout.generate(newLarge);
                            yes(!oldCells.equals(newCells), "new LARGE revision changes hull");
                            // The transverse section must no longer be a
                            // flat-bottomed, straight-sided rectangular tub.
                            int midZ = 0;
                            int fullHalf = ShipHullVoxelLayout.halfBeam(newLarge, midZ);
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf, 0, midZ))
                                    == null, "LARGE bilge draws in the lower hull");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf - 2, 0, midZ))
                                    == ShipHullVoxelLayout.Part.HULL,
                                    "LARGE dry structural floor");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf - 1, 1, midZ))
                                    == ShipHullVoxelLayout.Part.HULL,
                                    "LARGE outward first chine");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf, 2, midZ))
                                    == ShipHullVoxelLayout.Part.HULL,
                                    "LARGE stepped upper chine");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf, 3, midZ))
                                    != null, "LARGE sheer supported");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(fullHalf, 5, midZ))
                                    == ShipHullVoxelLayout.Part.RAIL,
                                    "LARGE outer rail follows hull outline");
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(0, 5,
                                    length / 2)) == ShipHullVoxelLayout.Part.RAIL,
                                    "LARGE forward stem railing");

                            yes(ShipHullVoxelLayout.halfBeam(newLarge, length / 2) == 0,
                                    "LARGE sharp stem");
                            yes(ShipHullVoxelLayout.halfBeam(newLarge, -length / 2) >= 2,
                                    "LARGE wide transom");
                            yes(ShipHullVoxelLayout.halfBeam(newLarge, length / 2 - 5)
                                    < ShipHullVoxelLayout.halfBeam(newLarge, 0),
                                    "LARGE tapered bow shoulders");
                            yes(ShipHullVoxelLayout.halfBeam(newLarge, -length / 2 + 5)
                                    < ShipHullVoxelLayout.halfBeam(newLarge, 0),
                                    "LARGE curved stern shoulders");
                            yes(newCells.equals(ShipHullVoxelLayout.generate(newLarge)),
                                    "LARGE deterministic across upgrades");
                            yes(newCells.values().stream().filter(p ->
                                    p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                    "LARGE one helm");
                            int rear = -length / 2 + 2;
                            int helmY = floors == 3 ? 13
                                    : bridge ? 10
                                    : cabin && floors == 1 ? 9
                                    : floors == 2 ? 9 : 5;
                            int largeHelmZ = -length / 2 + (bridge || cabin ? 4 : 3);
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(0, helmY, largeHelmZ))
                                    == ShipHullVoxelLayout.Part.HELM,
                                    "LARGE helm above its deck " + newLarge);
                            int helmDeckY = helmY == 10 ? 9 : helmY - 1;
                            yes(newCells.get(new ShipHullVoxelLayout.Cell(0, helmDeckY, largeHelmZ))
                                    == (helmDeckY == 9
                                            ? ShipHullVoxelLayout.Part.BRIDGE
                                            : ShipHullVoxelLayout.Part.DECK),
                                    "LARGE helm has physical deck " + newLarge);
                            if (bridge) {
                                int bridgeFront = -length / 2 + 6;
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(-2, 10, bridgeFront))
                                        != ShipHullVoxelLayout.Part.RAIL,
                                        "LARGE bridge ladder has unblocked upper exit");
                                if (!cabin)
                                    for (int accessY = 5; accessY <= 8; accessY++)
                                        yes(newCells.get(new ShipHullVoxelLayout.Cell(-3,
                                                accessY, bridgeFront))
                                                == ShipHullVoxelLayout.Part.FRAME,
                                                "LARGE freestanding bridge ladder supported");
                            }
                            if (cabin && !bridge) {
                                for (int accessY = 5; accessY <= 9; accessY++) {
                                    yes(newCells.get(new ShipHullVoxelLayout.Cell(-2,
                                            accessY, rear + 7))
                                            == ShipHullVoxelLayout.Part.LADDER,
                                            "LARGE cabin roof ladder " + newLarge);
                                }
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(-2, 9,
                                        rear + 6)) != ShipHullVoxelLayout.Part.RAIL,
                                        "LARGE cabin roof rail has entrance");
                            }
                            if (floors >= 2)
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(2, 8,
                                        length / 2 - 5)) != ShipHullVoxelLayout.Part.DECK,
                                        "LARGE no full-length upper box");
                            if (floors >= 2) {
                                int aftStart = -length / 2 + 2;
                                int upperFront = Math.min(length / 2 - 5,
                                        aftStart + length * 3 / 5);
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(0, 9,
                                        upperFront)) == ShipHullVoxelLayout.Part.RAIL,
                                        "LARGE second deck has forward safety rail");
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(0, 9,
                                        aftStart)) == (bridge
                                                ? ShipHullVoxelLayout.Part.BRIDGE
                                                : ShipHullVoxelLayout.Part.RAIL),
                                        "LARGE second deck stern guarded or joined to bridge");
                            }
                            if (floors == 3) {
                                int thirdFront = Math.min(length / 2 - 5,
                                        -length / 2 + 2 + length * 2 / 5);
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(0, 13,
                                        thirdFront)) == ShipHullVoxelLayout.Part.RAIL,
                                        "LARGE third deck has forward safety rail");
                            }
                            if (floors == 3) {
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(2, 12, 0))
                                        == ShipHullVoxelLayout.Part.DECK,
                                        "LARGE third-deck central walking floor");
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(2, 12,
                                        length / 2 - 5)) != ShipHullVoxelLayout.Part.DECK,
                                        "LARGE third deck leaves open forward bow");
                            }

                            if (floors == 3) {
                                int thirdAccessZ = -length / 2 + 11;
                                for (int yy = 9; yy <= 12; yy++) {
                                    yes(newCells.get(new ShipHullVoxelLayout.Cell(-2,
                                            yy, thirdAccessZ))
                                            == ShipHullVoxelLayout.Part.LADDER,
                                            "LARGE third floor reachable by supported aft ladder");
                                    yes(newCells.get(new ShipHullVoxelLayout.Cell(-3,
                                            yy, thirdAccessZ))
                                            == ShipHullVoxelLayout.Part.FRAME,
                                            "LARGE third floor ladder has physical supports");
                                }
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(-2, 12, 1))
                                        != ShipHullVoxelLayout.Part.LADDER,
                                        "LARGE no floating central shaft above second floor");
                            }
                            yes(oldCells.equals(ShipHullVoxelLayout.generate(oldLarge)),
                                    "LARGE legacy layout stable");
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
                                    int quarterdeckFront = rear + (cabin ? 6 : 5);
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 8, rear + 2))
                                                    == ShipHullVoxelLayout.Part.DECK,
                                            "raised stern platform has walkable planks");
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 8, quarterdeckFront + 2))
                                                    != ShipHullVoxelLayout.Part.DECK,
                                            "medium second deck cannot become full-length slab");
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 8, quarterdeckFront + 1))
                                                    == ShipHullVoxelLayout.Part.LADDER,
                                            "raised platform has external ladder");
                                    if (!bridge && !cabin)
                                        yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 9, rear + 2))
                                                        == ShipHullVoxelLayout.Part.HELM,
                                                "wheel is above deck, not trapped underneath");
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
