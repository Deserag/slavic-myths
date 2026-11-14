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
        for (var withdrawn : List.of(ShipSectionPlan.ShipClass.LARGE,
                ShipSectionPlan.ShipClass.LARGEST)) {
            boolean rejected = false;
            try {
                ShipHullVoxelLayout.generate(new ShipSuperstructurePlan(
                        withdrawn, 21, 33, List.of(1), 1, false, false, 2));
            } catch (UnsupportedOperationException expected) {
                rejected = true;
            }
            yes(rejected, "Withdrawn tier III/IV hull must not generate");
        }

        // Complete revision-3 tier III matrix: every supported change in
        // hull dimensions, heights, cabin, bridge and individual sail tiers.
        int brigantines = 0;
        for (int width : new int[]{15, 17, 19})
            for (int length : new int[]{33, 37, 41, 45})
                for (int decks : new int[]{1, 2, 3})
                    for (boolean bridge : new boolean[]{false, true})
                        for (boolean cabin : new boolean[]{false, true})
                            for (List<Integer> sails : List.of(
                                    List.of(1, 1), List.of(1, 2), List.of(1, 3),
                                    List.of(2, 1), List.of(2, 2), List.of(2, 3),
                                    List.of(3, 1), List.of(3, 2), List.of(3, 3))) {
                                var p = new ShipSuperstructurePlan(
                                        ShipSectionPlan.ShipClass.LARGE, width, length,
                                        sails, decks, bridge, cabin, 3);
                                var model = ShipHullVoxelLayout.generate(p);
                                brigantines++;
                                int highest = ShipHullVoxelLayout.brigantineTopDeck(p);
                                int rear = -length / 2 + 2;
                                yes(model.get(new ShipHullVoxelLayout.Cell(0, highest + 1, rear + 3))
                                                == ShipHullVoxelLayout.Part.HELM,
                                        "top wheel misplaced: " + p);
                                yes(model.values().stream().filter(part ->
                                        part == ShipHullVoxelLayout.Part.HELM).count() == 1,
                                        "duplicate wheel: " + p);
                                yes(model.get(new ShipHullVoxelLayout.Cell(0, 5, 0))
                                                == ShipHullVoxelLayout.Part.DECK,
                                        "missing separate gun floor: " + p);
                                yes(model.get(new ShipHullVoxelLayout.Cell(0, 10, 0))
                                                == ShipHullVoxelLayout.Part.DECK,
                                        "missing main weather deck: " + p);
                                // Two separated and walkable access routes
                                // must connect hold -> gun deck -> weather deck.
                                for (int[] route : new int[][]{{-2, -10}, {2, 4}}) {
                                    for (int i = 1; i <= 4; i++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(
                                                        route[0], 1 + i, route[1] + i))
                                                        == ShipHullVoxelLayout.Part.STAIR,
                                                "hold staircase missing: " + p);
                                    for (int i = 1; i <= 5; i++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(
                                                        route[0], 5 + i, route[1] - 1 + i))
                                                        == ShipHullVoxelLayout.Part.STAIR,
                                                "gun staircase missing: " + p);
                                }
                                yes(ShipHullVoxelLayout.halfBeam(p, length / 2)
                                                < ShipHullVoxelLayout.halfBeam(p, 0),
                                        "brigantine lost sharp bow: " + p);
                                // Every broadside skin level is solid, including
                                // the gun deck and the previously gaping lower hull.
                                // Gunports must be CLOSED, never empty air.
                                for (int z = -length / 2 + 1; z < length / 2; z++) {
                                    int beam = ShipHullVoxelLayout.halfBeam(p, z);
                                    if (beam < 4) continue;
                                    for (int side : new int[]{-1, 1})
                                        for (int y = 2; y <= 9; y++)
                                            yes(model.containsKey(new ShipHullVoxelLayout.Cell(
                                                            side * (beam - 1), y, z)),
                                                    "missing continuous broadside shell: "
                                                            + p + " z=" + z + " y=" + y);
                                }
                                // Lantern coverage on BOTH lower floors; a
                                // pair of distant lights is insufficient.
                                int lowerLights = 0, gunLights = 0;
                                for (var entry : model.entrySet()) {
                                    if (entry.getValue() != ShipHullVoxelLayout.Part.LANTERN) continue;
                                    if (entry.getKey().y() == 2) lowerLights++;
                                    if (entry.getKey().y() == 6) gunLights++;
                                }
                                yes(lowerLights >= 4 && gunLights >= 4,
                                        "hold or gun deck still unlit: " + p);
                                if (bridge) {
                                    int rearX = -length / 2 + 2;
                                    yes(model.get(new ShipHullVoxelLayout.Cell(3, highest + 1,
                                                    rearX + 4)) == ShipHullVoxelLayout.Part.RAIL,
                                            "bridge upgrade adds no visible geometry: " + p);
                                }
                                for (int z = -length / 2 + 5; z <= length / 2 - 5; z++) {
                                    if (Math.abs(z) < 3
                                            || Math.floorMod(z + length / 2, 4) != 0)
                                        continue;
                                    int beam = ShipHullVoxelLayout.halfBeam(p, z);
                                    for (int side : new int[]{-1, 1})
                                        for (int y : new int[]{7, 8})
                                            yes(model.get(new ShipHullVoxelLayout.Cell(
                                                            side * (beam - 1), y, z))
                                                    == ShipHullVoxelLayout.Part.GUNPORT_SHUTTER,
                                                    "gunport shutter missing: " + p);
                                }
                                if (highest >= 15)
                                    for (int x : new int[]{-2, 2})
                                        for (int i = 1; i <= 5; i++)
                                            yes(model.get(new ShipHullVoxelLayout.Cell(
                                                            x, 10 + i, rear + 3 + i))
                                                            == ShipHullVoxelLayout.Part.STAIR,
                                                    "quarterdeck twin staircase missing: " + p);
                                if (highest >= 20)
                                    for (int x : new int[]{-1, 1})
                                        for (int i = 1; i <= 5; i++)
                                            yes(model.get(new ShipHullVoxelLayout.Cell(
                                                            x, 15 + i, rear + i))
                                                            == ShipHullVoxelLayout.Part.STAIR,
                                                    "third-deck twin staircase missing: " + p);
                                if (highest >= 15) {
                                    for (int z : new int[]{rear + 2, rear + 5})
                                        yes(model.get(new ShipHullVoxelLayout.Cell(0, 11, z))
                                                        == ShipHullVoxelLayout.Part.LANTERN,
                                                "quarterdeck interior light missing: " + p);
                                }
                                if (highest >= 20)
                                    for (int z : new int[]{rear + 2, rear + 5})
                                        yes(model.get(new ShipHullVoxelLayout.Cell(0, 16, z))
                                                        == ShipHullVoxelLayout.Part.LANTERN,
                                                "upper captain floor light missing: " + p);
                                if (highest >= 15)
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 15, rear + 3))
                                                    == ShipHullVoxelLayout.Part.DECK,
                                            "raised captain deck missing: " + p);
                                if (highest >= 20)
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 20, rear + 3))
                                                    == ShipHullVoxelLayout.Part.BRIDGE,
                                            "third captain deck missing: " + p);
                                // Closed stern faces must survive EVERY
                                // cabin/bridge/deck combination, including
                                // a raised deck added before the cabin.
                                if (highest >= 15)
                                    for (int y = 11; y <= 14; y++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(0, y, rear))
                                                        == ShipHullVoxelLayout.Part.CABIN_WALL,
                                                "open sterncastle below second deck: " + p);
                                if (highest >= 20)
                                    for (int y = 16; y <= 19; y++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(0, y, rear))
                                                        == ShipHullVoxelLayout.Part.CABIN_WALL,
                                                "open sterncastle below third deck: " + p);
                                if (highest >= 15) {
                                    int half = Math.min(5, width / 2 - 3);
                                    for (int z = rear + 1; z <= rear + 8; z++)
                                        for (int y = 11; y <= 14; y++) {
                                            var sideWall = model.get(
                                                    new ShipHullVoxelLayout.Cell(half, y, z));
                                            yes(sideWall == ShipHullVoxelLayout.Part.CABIN_WALL
                                                            || sideWall == ShipHullVoxelLayout.Part.WINDOW,
                                                    "raised stern has open side wall: " + p);
                                        }
                                }
                                if (highest >= 20) {
                                    int half = Math.min(3, width / 2 - 5);
                                    for (int z = rear + 1; z <= rear + 5; z++)
                                        for (int y = 16; y <= 19; y++) {
                                            var sideWall = model.get(
                                                    new ShipHullVoxelLayout.Cell(half, y, z));
                                            yes(sideWall == ShipHullVoxelLayout.Part.CABIN_WALL
                                                            || sideWall == ShipHullVoxelLayout.Part.WINDOW,
                                                    "third deck has open side wall: " + p);
                                        }
                                }
                                if (bridge)
                                    yes(model.get(new ShipHullVoxelLayout.Cell(
                                                    0, highest + 4, rear + 1))
                                                    == ShipHullVoxelLayout.Part.BRIDGE,
                                            "bridge upgrade missing physical canopy: " + p);
                                // Main corridor must stay at least four blocks high
                                // between the gun floor and the weather deck. A
                                // future gun carriage needs more room than a walker.
                                for (int z : new int[]{-6, -1, 4, 9})
                                    for (int y = 6; y <= 9; y++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(2, y, z)) == null,
                                                "gun-deck headroom obstructed: " + p);
                                // Each height upgrade moves the highest helm
                                // with its supporting floor, never below a roof.
                                yes(model.get(new ShipHullVoxelLayout.Cell(0, highest, rear + 3))
                                                == (highest == 20
                                                        ? ShipHullVoxelLayout.Part.BRIDGE
                                                        : ShipHullVoxelLayout.Part.DECK),
                                        "top wheel has no structural support: " + p);
                                if (cabin) {
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 11, rear + 6))
                                                    == ShipHullVoxelLayout.Part.DOOR,
                                            "cabin lower doorway missing: " + p);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, 12, rear + 6))
                                                    == ShipHullVoxelLayout.Part.DOOR,
                                            "cabin upper doorway missing: " + p);
                                }
                                for (int mast = 0; mast < p.mastPositions().size(); mast++) {
                                    int z = p.mastPositions().get(mast);
                                    int topY = ShipHullVoxelLayout.sailTop(p, sails.get(mast));
                                    int yardY = ShipHullVoxelLayout.sailLowerYard(p);
                                    yes(yardY > highest, "mast yard buried below top deck: " + p);
                                    yes(topY > yardY, "sail flattened by deck upgrade: " + p);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(0, highest + 2, z))
                                                    == ShipHullVoxelLayout.Part.MAST,
                                            "mast does not continue above upper deck: " + p);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(1, yardY, z))
                                                    == ShipHullVoxelLayout.Part.YARD,
                                            "lower yard missing: " + p);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(1, topY + 1, z))
                                                    == ShipHullVoxelLayout.Part.YARD,
                                            "upper yard missing: " + p);
                                    yes(model.get(new ShipHullVoxelLayout.Cell(1, topY, z + 1))
                                                    == ShipHullVoxelLayout.Part.SAIL,
                                            "sail panel missing: " + p);
                                    for (int y = 11; y <= highest + 3; y++)
                                        yes(model.get(new ShipHullVoxelLayout.Cell(1, y, z + 1))
                                                        != ShipHullVoxelLayout.Part.SAIL,
                                                "sail intersects occupied level: " + p);
                                }
                            }
        yes(brigantines == 1296, "exhaustive brigantine upgrade matrix");
        // Visual-shell regression: no open transverse end or stair-step
        // gaps along the bow/stern. Only two-block SIDE gunports may remain.
        int sealedProfiles = 0;
        for (int width : new int[]{15, 17, 19})
            for (int length : new int[]{33, 37, 41, 45})
                for (int decks = 1; decks <= 3; decks++) {
                    var p = new ShipSuperstructurePlan(
                            ShipSectionPlan.ShipClass.LARGE, width, length,
                            List.of(1, 1), decks, false, false, 3);
                    var voxels = ShipHullVoxelLayout.generate(p);
                    int end = length / 2;
                    for (int z = -end; z <= end; z++) {
                        int beam = ShipHullVoxelLayout.halfBeam(p, z);
                        int outwardBeam = Math.abs(z) == end ? -1
                                : ShipHullVoxelLayout.halfBeam(p, z + (z >= 0 ? 1 : -1));
                        for (int x = -beam; x <= beam; x++) {
                            if (Math.abs(z) != end && Math.abs(x) <= outwardBeam) continue;
                            for (int y = 5; y <= 9; y++) {
                                boolean port = Math.abs(x) == beam && (y == 7 || y == 8)
                                        && Math.abs(z) <= end - 5 && Math.abs(z) >= 3
                                        && Math.floorMod(z + end, 4) == 0;
                                if (port) continue;
                                var material = voxels.get(new ShipHullVoxelLayout.Cell(x, y, z));
                                yes(material == ShipHullVoxelLayout.Part.HULL
                                                || material == ShipHullVoxelLayout.Part.TRIM,
                                        "open bow/stern end or exposed hull step: " + p
                                                + " @ " + x + "," + y + "," + z);
                            }
                        }
                    }
                    sealedProfiles++;
                }
        yes(sealedProfiles == 36, "tier III sealed silhouette matrix");

        // Adding the third mast must be possible in every legal hull/height
        // configuration, and all three masts/cloth panels stay above the
        // highest deck when cabin, bridge and sail upgrades are combined.
        int threeMastProfiles = 0;
        for (int width : new int[]{15, 17, 19})
            for (int length : new int[]{33, 37, 41, 45})
                for (int decks = 1; decks <= 3; decks++)
                    for (boolean cabin : new boolean[]{false, true})
                        for (boolean bridge : new boolean[]{false, true})
                            for (int tier : new int[]{1, 2, 3}) {
                                var p = new ShipSuperstructurePlan(
                                        ShipSectionPlan.ShipClass.LARGE, width, length,
                                        List.of(1, 2, tier), decks, bridge, cabin, 3);
                                var voxels = ShipHullVoxelLayout.generate(p);
                                yes(p.mastPositions().size() == 3
                                                && new java.util.HashSet<>(p.mastPositions()).size() == 3,
                                        "third mast missing or overlapping: " + p);
                                int topDeck = ShipHullVoxelLayout.brigantineTopDeck(p);
                                for (int i = 0; i < 3; i++) {
                                    int z = p.mastPositions().get(i);
                                    int sailTop = ShipHullVoxelLayout.sailTop(p, p.sailTiers().get(i));
                                    yes(voxels.get(new ShipHullVoxelLayout.Cell(0, topDeck + 2, z))
                                                    == ShipHullVoxelLayout.Part.MAST,
                                            "third mast cannot clear upper deck: " + p);
                                    yes(voxels.get(new ShipHullVoxelLayout.Cell(1, sailTop, z + 1))
                                                    == ShipHullVoxelLayout.Part.SAIL,
                                            "third mast missing its upgraded sail: " + p);
                                }
                                threeMastProfiles++;
                            }
        yes(threeMastProfiles == 432, "three mast full modification matrix");

        // Metamorphic upgrades: height changes must move every mast, yard
        // and sail together, including the unusual cabin/bridge combinations.
        int elevationTransitions = 0;
        for (int width : new int[]{15, 17, 19})
            for (int length : new int[]{33, 37, 41, 45})
                for (boolean cabin : new boolean[]{false, true})
                    for (boolean bridge : new boolean[]{false, true})
                        for (int[] tiers : new int[][]{{1, 1}, {1, 3}, {3, 1}, {3, 3}}) {
                            int lastHeight = -1, lastYard = -1;
                            for (int decks = 1; decks <= 3; decks++) {
                                var plan = new ShipSuperstructurePlan(
                                        ShipSectionPlan.ShipClass.LARGE, width, length,
                                        List.of(tiers[0], tiers[1]), decks, bridge, cabin, 3);
                                var voxels = ShipHullVoxelLayout.generate(plan);
                                int height = ShipHullVoxelLayout.brigantineTopDeck(plan);
                                int yard = ShipHullVoxelLayout.sailLowerYard(plan);
                                yes(yard == height + 4,
                                        "mast foundation and sail elevation desynchronized: " + plan);
                                if (lastHeight >= 0) {
                                    yes(height >= lastHeight && yard >= lastYard,
                                            "additional deck lowered mast or sail: " + plan);
                                    yes(yard - lastYard == height - lastHeight,
                                            "deck upgrade changed sail spacing: " + plan);
                                }
                                for (int mastZ : plan.mastPositions()) {
                                    yes(voxels.get(new ShipHullVoxelLayout.Cell(0, height + 2, mastZ))
                                                    == ShipHullVoxelLayout.Part.MAST,
                                            "mast stops below upgraded deck: " + plan);
                                    for (int tier : tiers)
                                        yes(ShipHullVoxelLayout.sailTop(plan, tier) > yard,
                                                "sail upper edge under raised floor: " + plan);
                                }
                                lastHeight = height;
                                lastYard = yard;
                                elevationTransitions++;
                            }
                        }
        yes(elevationTransitions == 576, "brigantine staged height transitions");

        // Room and bridge upgrades are independently allowed on a one-deck
        // brigantine. They must raise the helm and ALL rigging, not leave
        // cloth behind the newly added roof or silently create a dead wheel.
        int superstructureTransitions = 0;
        for (int width : new int[]{15, 17, 19})
            for (int length : new int[]{33, 37, 41, 45})
                for (List<Integer> tiers : List.of(
                        List.of(1, 1), List.of(1, 3), List.of(3, 1), List.of(3, 3))) {
                    var bare = new ShipSuperstructurePlan(
                            ShipSectionPlan.ShipClass.LARGE, width, length,
                            tiers, 1, false, false, 3);
                    var base = ShipHullVoxelLayout.generate(bare);
                    yes(ShipHullVoxelLayout.brigantineTopDeck(bare) == 10,
                            "unmodified brigantine top platform");
                    for (boolean bridge : new boolean[]{false, true})
                        for (boolean cabin : new boolean[]{false, true}) {
                            var upgrade = new ShipSuperstructurePlan(
                                    ShipSectionPlan.ShipClass.LARGE, width, length,
                                    tiers, 1, bridge, cabin, 3);
                            var palette = ShipHullVoxelLayout.generate(upgrade);
                            int top = ShipHullVoxelLayout.brigantineTopDeck(upgrade);
                            int delta = top - ShipHullVoxelLayout.brigantineTopDeck(bare);
                            yes(delta == (bridge || cabin ? 5 : 0),
                                    "cabin/bridge changes wrong captain level");
                            yes(ShipHullVoxelLayout.sailLowerYard(upgrade)
                                            - ShipHullVoxelLayout.sailLowerYard(bare) == delta,
                                    "cabin/bridge did not move lower sail yard");
                            for (int mast = 0; mast < tiers.size(); mast++)
                                yes(ShipHullVoxelLayout.sailTop(upgrade, tiers.get(mast))
                                                - ShipHullVoxelLayout.sailTop(bare, tiers.get(mast)) == delta,
                                        "cabin/bridge did not move upper sail panel");
                            int wheelZ = -length / 2 + 5;
                            yes(palette.get(new ShipHullVoxelLayout.Cell(0, top + 1, wheelZ))
                                            == ShipHullVoxelLayout.Part.HELM,
                                    "cabin/bridge cannot expose buried helm");
                            yes(base.get(new ShipHullVoxelLayout.Cell(0, 11, wheelZ))
                                            == ShipHullVoxelLayout.Part.HELM,
                                    "initial wheel unexpectedly changed");
                            superstructureTransitions++;
                        }
                }
        yes(superstructureTransitions == 192,
                "all cabin and bridge height transitions across hull sizes");



        for (var config : List.of(small, medium)) {
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
