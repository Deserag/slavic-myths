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
                            if (floors >= 2)
                                yes(newCells.get(new ShipHullVoxelLayout.Cell(2, 8,
                                        length / 2 - 5)) != ShipHullVoxelLayout.Part.DECK,
                                        "LARGE no full-length upper box");
                            yes(oldCells.equals(ShipHullVoxelLayout.generate(oldLarge)),
                                    "LARGE legacy layout stable");
                        }
        // Flagship geometry and all dimensional, room and deck upgrades.
        for (int width : new int[]{21, 23, 25})
            for (int length : new int[]{33, 37, 41, 45})
                for (int floors : new int[]{1, 2, 3})
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true}) {
                            var oldFlagship = new ShipSuperstructurePlan(
                                    ShipSectionPlan.ShipClass.LARGEST, width, length,
                                    List.of(1, 2), floors, bridge, cabin, 1);
                            var flagship = new ShipSuperstructurePlan(
                                    ShipSectionPlan.ShipClass.LARGEST, width, length,
                                    List.of(1, 2), floors, bridge, cabin, 2);
                            var geometry = ShipHullVoxelLayout.generate(flagship);
                            yes(!geometry.equals(ShipHullVoxelLayout.generate(oldFlagship)),
                                    "flagship must differ from legacy");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, length / 2) == 1,
                                    "flagship clipper stem");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, -length / 2) >= 4,
                                    "flagship broad transom");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, length / 2 - 5)
                                    < ShipHullVoxelLayout.halfBeam(flagship, 0),
                                    "flagship bow shoulders taper");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, -length / 2 + 3)
                                    < ShipHullVoxelLayout.halfBeam(flagship, 0),
                                    "flagship stern shoulders taper");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, 0)
                                            <= width / 2 - 2,
                                    "flagship amidships has sculpted inward waist");
                            yes(ShipHullVoxelLayout.halfBeam(flagship, -8)
                                            > ShipHullVoxelLayout.halfBeam(flagship, 0),
                                    "flagship aft shoulders remain fuller than pinched waist");
                            yes(geometry.values().stream().filter(p ->
                                    p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                    "flagship keeps one helm");
                            yes(geometry.equals(ShipHullVoxelLayout.generate(flagship)),
                                    "flagship deterministic geometry");
                            if (floors == 1 && cabin && !bridge) {
                                int rear = -length / 2 + 2;
                                int flagshipRoofFront = rear + 6;
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(3, 9, rear + 3))
                                                == ShipHullVoxelLayout.Part.RAIL,
                                        "single-deck flagship cabin roof has side rail");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(0, 9, flagshipRoofFront))
                                                == ShipHullVoxelLayout.Part.RAIL,
                                        "single-deck flagship cabin roof has forward guard");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 9, flagshipRoofFront))
                                                == null,
                                        "cabin roof ladder entrance remains open");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(-2, 9, flagshipRoofFront + 1))
                                                == ShipHullVoxelLayout.Part.LADDER,
                                        "single-deck flagship cabin roof keeps accessible ladder");
                            }
                            // Every exposed end of the shortened flagship
                            // deck has a transverse guard, not just side rails.
                            for (int upper = 2; upper <= floors; upper++) {
                                int rear = -length / 2 + 2;
                                int front = Math.min(length / 2 - 5,
                                        rear + (upper == 2 ? length * 2 / 3 : length / 2));
                                int y = 4 + (upper - 1) * 4;
                                // The raised deck must taper into the fore
                                // and aft ends, not finish as a rectangular
                                // slab whose full width reaches the end rail.
                                int inset = upper == 3 ? 3 : 1;
                                int middleHalf = ShipHullVoxelLayout.halfBeam(flagship, front - 6) - inset;
                                int forwardHalf = Math.max(3,
                                        ShipHullVoxelLayout.halfBeam(flagship, front) - inset - 3);
                                yes(middleHalf >= forwardHalf + 2,
                                        "flagship elevated bow deck tapers " + flagship);
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(forwardHalf + 1, y, front))
                                                != ShipHullVoxelLayout.Part.DECK,
                                        "flagship upper deck forefoot must not be rectangular");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(1, y, front))
                                                == ShipHullVoxelLayout.Part.DECK,
                                        "flagship bow access remains connected");
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(1, y + 1, front))
                                                == ShipHullVoxelLayout.Part.RAIL,
                                        "flagship forward raised deck has end guard " + flagship);
                                if (upper == 3)
                                    yes(geometry.get(new ShipHullVoxelLayout.Cell(1, y + 1, rear))
                                                    == ShipHullVoxelLayout.Part.RAIL,
                                            "flagship third deck stern has end guard " + flagship);
                            }
                            // At every flagship station the narrow keel,
                            // stepped solid hold-floor and outer chine form
                            // a closed bottom; a full-width y=0 slab is banned.
                            for (int z = -length / 2; z <= length / 2; z++) {
                                int beam = ShipHullVoxelLayout.halfBeam(flagship, z);
                                int keelHalf = Math.max(1, beam - 3);
                                int floorHalf = Math.max(keelHalf, beam - 2);
                                for (int x = -beam; x <= beam; x++) {
                                    var bottom = geometry.get(new ShipHullVoxelLayout.Cell(x, 0, z));
                                    boolean liftedKeel = length / 2 - z <= 4
                                            || length / 2 + z <= 3;
                                    yes((bottom != null)
                                                    == (!liftedKeel && Math.abs(x) <= keelHalf),
                                            "flagship rising narrow keel station " + z);
                                    if (Math.abs(x) <= floorHalf) {
                                        // The vertical hold access intentionally cuts
                                        // a one-cell opening above the sealed y=0 keel.
                                        boolean holdShaft = x == -2 && z == 0;
                                        yes(holdShaft
                                                ? geometry.get(new ShipHullVoxelLayout.Cell(x, 1, z)) == null
                                                        && geometry.get(new ShipHullVoxelLayout.Cell(x, 0, z)) != null
                                                : geometry.get(new ShipHullVoxelLayout.Cell(x, 1, z)) != null,
                                                "flagship closed bilge and safe hold shaft " + z);
                                    }
                                    else
                                        yes(geometry.get(new ShipHullVoxelLayout.Cell(x, 2, z))
                                                        != null,
                                                "flagship solid stepped chine " + z);
                                }
                            }

                            if (floors >= 2)
                                yes(geometry.get(new ShipHullVoxelLayout.Cell(2, 8,
                                        length / 2 - 5)) != ShipHullVoxelLayout.Part.DECK,
                                        "flagship avoids full-length upper deck slab");
                        }
        // All 144 flagship room/deck/dimension combinations must place the
        // one helm at a walkable height, never under the top sterncastle.
        int flagshipHelmVariants = 0;
        for (int width : new int[]{21, 23, 25})
            for (int length : new int[]{33, 37, 41, 45})
                for (int floors : new int[]{1, 2, 3})
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true}) {
                            var flagship = new ShipSuperstructurePlan(
                                    ShipSectionPlan.ShipClass.LARGEST, width, length,
                                    List.of(1, 2), floors, bridge, cabin, 2);
                            var voxels = ShipHullVoxelLayout.generate(flagship);
                            flagshipHelmVariants++;
                            int rear = -length / 2 + 2;
                            int floorY = bridge ? (floors >= 3 ? 12 : 9)
                                    : floors >= 3 ? 12 : floors >= 2 || cabin ? 8 : 4;
                            int flagshipHelmZ = rear + (bridge ? 2 : cabin ? 3 : 4);
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(0, floorY + 1, flagshipHelmZ))
                                            == ShipHullVoxelLayout.Part.HELM,
                                    "LARGEST helm stands above occupied deck " + flagship);
                            yes(voxels.values().stream().filter(p ->
                                    p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                    "LARGEST has one helm " + flagship);
                            var support = voxels.get(new ShipHullVoxelLayout.Cell(0, floorY, flagshipHelmZ));
                            yes(support == ShipHullVoxelLayout.Part.DECK
                                            || support == ShipHullVoxelLayout.Part.BRIDGE,
                                    "LARGEST helm supported by floor " + flagship);
                            if (bridge) {
                                int front = rear + 4;
                                yes(voxels.get(new ShipHullVoxelLayout.Cell(-2, floorY, front))
                                                == ShipHullVoxelLayout.Part.LADDER,
                                        "LARGEST bridge entrance has ladder " + flagship);
                                yes(voxels.get(new ShipHullVoxelLayout.Cell(-2, floorY + 1, front))
                                                != ShipHullVoxelLayout.Part.RAIL,
                                        "LARGEST bridge ladder exit unblocked " + flagship);
                            }
                            if (cabin && !bridge && floors == 1)
                                yes(voxels.get(new ShipHullVoxelLayout.Cell(-2, 9, rear + 7))
                                                == ShipHullVoxelLayout.Part.LADDER,
                                        "LARGEST cabin-only roof remains accessible");
                        }
        yes(flagshipHelmVariants == 144, "all flagship captain station variants covered");
        // Flagship side elevation has a real stepped sheer, not a horizontal
        // rail from the broad transom to the clipper stem.
        for (int length : new int[]{33, 37, 41, 45})
            for (int width : new int[]{21, 23, 25}) {
                var flagshipSheer = new ShipSuperstructurePlan(
                        ShipSectionPlan.ShipClass.LARGEST, width, length,
                        List.of(), 1, false, false, 2);
                var model = ShipHullVoxelLayout.generate(flagshipSheer);
                int halfLength = length / 2;
                int bowHalf = ShipHullVoxelLayout.halfBeam(flagshipSheer, halfLength);
                int sternHalf = ShipHullVoxelLayout.halfBeam(flagshipSheer, -halfLength);
                yes(model.get(new ShipHullVoxelLayout.Cell(bowHalf, 8, halfLength))
                                == ShipHullVoxelLayout.Part.RAIL,
                        "flagship high clipper stem rail");
                yes(model.get(new ShipHullVoxelLayout.Cell(sternHalf, 7, -halfLength))
                                == ShipHullVoxelLayout.Part.RAIL,
                        "flagship raised transom");
                yes(model.get(new ShipHullVoxelLayout.Cell(width / 2, 6, 0)) == null,
                        "flagship amidships never grows vertical wall");
                yes(model.get(new ShipHullVoxelLayout.Cell(0, 6, halfLength))
                                == null,
                        "flagship bow centerline has no tall solid pillar");
            }
        // Four independent masts and all three sail tiers must remain above
        // each LARGEST sterncastle variant, including the narrow third floor.
        int flagshipRigVariants = 0;
        for (int width : new int[]{21, 23, 25})
            for (int length : new int[]{33, 37, 41, 45})
                for (int floors : new int[]{1, 2, 3})
                    for (int tier : new int[]{1, 2, 3}) {
                        var flagship = new ShipSuperstructurePlan(
                                ShipSectionPlan.ShipClass.LARGEST, width, length,
                                List.of(tier, tier, tier, tier), floors, true, true, 2);
                        var voxels = ShipHullVoxelLayout.generate(flagship);
                        flagshipRigVariants++;
                        int highestDeck = 4 + 4 * (floors - 1);
                        int lowerYard = Math.max(7, highestDeck + 3);
                        for (int mastZ : flagship.mastPositions()) {
                            int topYard = lowerYard + (tier == 1 ? 5 : tier == 2 ? 8 : 12) + 1;
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(1, topYard, mastZ))
                                            == ShipHullVoxelLayout.Part.YARD,
                                    "LARGEST retains full sail tier height " + flagship);
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(1, topYard - 1, mastZ + 1))
                                            == ShipHullVoxelLayout.Part.SAIL,
                                    "LARGEST sailcloth reaches lifted upper yard " + flagship);
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(1, lowerYard, mastZ))
                                            == ShipHullVoxelLayout.Part.YARD,
                                    "LARGEST lower yard above raised deck " + flagship);
                            for (int y = 8; y <= highestDeck + 2; y++)
                                yes(voxels.get(new ShipHullVoxelLayout.Cell(1, y, mastZ + 1))
                                                != ShipHullVoxelLayout.Part.SAIL,
                                        "LARGEST sail clears occupied floors " + flagship);
                        }
                        if (floors == 3) {
                            int station = -length / 2 + 12;
                            int shoulder = ShipHullVoxelLayout.halfBeam(flagship, station) - 2;
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(shoulder, 8, station))
                                            == ShipHullVoxelLayout.Part.DECK,
                                    "flagship second deck remains wide");
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(shoulder, 12, station))
                                            != ShipHullVoxelLayout.Part.DECK,
                                    "flagship third deck tapers to sterncastle");
                        }
                    }
        yes(flagshipRigVariants == 108, "LARGEST width/length/deck/sail matrix");
        // Every one of the 3^4 mixed four-mast sail combinations must
        // preserve independent yard heights and avoid occupied deck space.
        int mixedFlagshipRigs = 0;
        for (int code = 0; code < 81; code++) {
            int encoded = code;
            java.util.List<Integer> tiers = new java.util.ArrayList<>();
            for (int mast = 0; mast < 4; mast++) {
                tiers.add(1 + encoded % 3);
                encoded /= 3;
            }
            for (int floors : new int[]{1, 2, 3}) {
                var flagship = new ShipSuperstructurePlan(
                        ShipSectionPlan.ShipClass.LARGEST, 23, 41,
                        tiers, floors, true, true, 2);
                var voxels = ShipHullVoxelLayout.generate(flagship);
                mixedFlagshipRigs++;
                int lowerYard = Math.max(7, 4 + (floors - 1) * 4 + 3);
                for (int mast = 0; mast < 4; mast++) {
                    int z = flagship.mastPositions().get(mast);
                    int height = tiers.get(mast) == 1 ? 5
                            : tiers.get(mast) == 2 ? 8 : 12;
                    yes(voxels.get(new ShipHullVoxelLayout.Cell(1, lowerYard + height + 1, z))
                                    == ShipHullVoxelLayout.Part.YARD,
                            "mixed flagship rig keeps each yard independent " + code);
                    yes(voxels.get(new ShipHullVoxelLayout.Cell(1, lowerYard + height, z + 1))
                                    == ShipHullVoxelLayout.Part.SAIL,
                            "mixed flagship rig has full-height cloth " + code);
                    yes(voxels.get(new ShipHullVoxelLayout.Cell(1, lowerYard, z))
                                    == ShipHullVoxelLayout.Part.YARD,
                            "mixed flagship rig has supported lower yard " + code);
                }
            }
        }
        yes(mixedFlagshipRigs == 243, "all 81 mixed flagship rigs on 3 deck arrangements");
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
