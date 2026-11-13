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

        // First-tier Khrabr: each allowed length remains the SAME shallow v3 design.
        var lengthened = plan(ShipSectionPlan.ShipClass.SMALL, 7, 15,
                List.of(1), 1, false, false);
        for (var selected : List.of(small, lengthened)) {
            var khrabr = ShipHullVoxelLayout.generate(selected, true);
            yes(!khrabr.equals(ShipHullVoxelLayout.generate(selected)),
                    "new shallow layout never switches to legacy hull");
            yes(khrabr.equals(ShipHullVoxelLayout.generate(selected, true)),
                    "repeatable Khrabr layout");
            int halfLength = selected.hullLength() / 2;
            int minX = 100, maxX = -100, minZ = 100, maxZ = -100;
            int minHullY = 100, maxHullY = -100;
            for (var entry : khrabr.entrySet()) {
                var cell = entry.getKey();
                if (entry.getValue() == ShipHullVoxelLayout.Part.SAIL
                        || entry.getValue() == ShipHullVoxelLayout.Part.THIN_MAST
                        || entry.getValue() == ShipHullVoxelLayout.Part.YARD_SLAB
                        || entry.getValue() == ShipHullVoxelLayout.Part.HELM) continue;
                minX = Math.min(minX, cell.x());
                maxX = Math.max(maxX, cell.x());
                minZ = Math.min(minZ, cell.z());
                maxZ = Math.max(maxZ, cell.z());
                minHullY = Math.min(minHullY, cell.y());
                maxHullY = Math.max(maxHullY, cell.y());
            }
            yes(maxX - minX + 1 == 7, "maximum hull beam seven");
            yes(maxZ - minZ + 1 == selected.hullLength(), "real extended hull length");
            yes(minHullY == 0 && maxHullY <= 3, "keel at waterline and four-block hull");
            for (int z = -halfLength; z <= halfLength; z++) {
                int half = ShipHullVoxelLayout.halfBeam(selected, z, true);
                yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 0, z))
                        == (Math.abs(z) <= 1 ? ShipHullVoxelLayout.Part.KEEL : null),
                        "no protruding full-length waterline keel");
                yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 4, z))
                        == (z == 1 ? ShipHullVoxelLayout.Part.THIN_MAST : null),
                        "no fake upper deck or enclosed hold");
                for (int x = -Math.max(0, half - 1); x <= Math.max(0, half - 1); x++)
                    yes(khrabr.get(new ShipHullVoxelLayout.Cell(x, 2, z))
                            == ShipHullVoxelLayout.Part.DECK,
                            "solid walkable main deck across extension");
            }
            for (int side : new int[]{-1, 1})
                for (int z = -1; z <= 1; z++) {
                    yes(khrabr.get(new ShipHullVoxelLayout.Cell(side, 2, z))
                            == ShipHullVoxelLayout.Part.DECK, "both mast bypasses walkable");
                    yes(!khrabr.containsKey(new ShipHullVoxelLayout.Cell(side, 3, z)),
                            "mast bypass headroom");
                }
            yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 3, 1))
                    == ShipHullVoxelLayout.Part.THIN_MAST, "one mast begins at deck");
            yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 3, -1))
                    == ShipHullVoxelLayout.Part.HELM, "helm on deck");
            yes(khrabr.values().stream().filter(p -> p == ShipHullVoxelLayout.Part.HELM)
                    .count() == 1, "exactly one helm");
            yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 3, halfLength))
                    == ShipHullVoxelLayout.Part.SHEER_SLAB, "raised bow");
            yes(khrabr.get(new ShipHullVoxelLayout.Cell(0, 3, -halfLength))
                    == ShipHullVoxelLayout.Part.SHEER_SLAB, "raised stern");
            yes(ShipHullVoxelLayout.KHRABR_SAIL_TOP + 1
                    == ShipHullVoxelLayout.KHRABR_YARD_Y, "sail meets the upper yard");
            for (int y = ShipHullVoxelLayout.KHRABR_SAIL_BOTTOM;
                    y <= ShipHullVoxelLayout.KHRABR_SAIL_TOP; y++)
                for (int x : new int[]{-2, -1, 1, 2})
                    yes(khrabr.get(new ShipHullVoxelLayout.Cell(x, y, 2))
                            == ShipHullVoxelLayout.Part.SAIL, "matching sail markers");
            for (int x = -3; x <= 3; x++)
                yes(khrabr.get(new ShipHullVoxelLayout.Cell(x,
                        ShipHullVoxelLayout.KHRABR_YARD_Y, 1))
                        == ShipHullVoxelLayout.Part.YARD_SLAB, "continuous upper yard");
            for (int turn = 0; turn < 4; turn++) {
                java.util.Set<ShipHullVoxelLayout.Cell> rotated = new java.util.HashSet<>();
                for (var cell : khrabr.keySet()) {
                    int x = cell.x(), z = cell.z();
                    var rotatedCell = switch (turn) {
                        case 1 -> new ShipHullVoxelLayout.Cell(-z, cell.y(), x);
                        case 2 -> new ShipHullVoxelLayout.Cell(-x, cell.y(), -z);
                        case 3 -> new ShipHullVoxelLayout.Cell(z, cell.y(), -x);
                        default -> cell;
                    };
                    yes(rotated.add(rotatedCell), "no overlaps in rotated Khrabr");
                }
            }
        }
        yes(!ShipHullVoxelLayout.generate(small, true)
                .equals(ShipHullVoxelLayout.generate(lengthened, true)),
                "length extension actually changes physical geometry");
        yes(ShipHullVoxelLayout.additions(
                ShipHullVoxelLayout.generate(small, true),
                ShipHullVoxelLayout.generate(lengthened, true)).values().stream()
                .mapToInt(Integer::intValue).sum() > 0,
                "length extension charges real added blocks");
        yes(small.khrabrPropulsionFactor() < 1D,
                "starter sail leaves room for a real speed upgrade");
        yes(plan(ShipSectionPlan.ShipClass.SMALL, 7, 11,
                List.of(2), 1, false, false).khrabrPropulsionFactor()
                > small.khrabrPropulsionFactor(),
                "upgraded small sail increases forward speed");
        yes(plan(ShipSectionPlan.ShipClass.SMALL, 7, 11,
                List.of(2), 1, false, false).khrabrPropulsionFactor() == 1D,
                "upgraded small sail reaches full available throttle");
        // First-tier shipyard options: length, beam, higher sail, extra mast,
        // and mixed upgrade order must keep a shallow physical hull.
        for (int length : new int[]{11, 15})
            for (int width : new int[]{7, 9})
                for (var sails : List.of(List.of(1), List.of(2),
                        List.of(1, 1), List.of(2, 1), List.of(2, 2))) {
                    var config = plan(ShipSectionPlan.ShipClass.SMALL,
                            width, length, sails, 1, false, false);
                    var voxels = ShipHullVoxelLayout.generate(config, true);
                    int beam = 0, hullMinZ = 100, hullMaxZ = -100;
                    for (var entry : voxels.entrySet()) {
                        var p = entry.getValue();
                        if (p == ShipHullVoxelLayout.Part.SAIL
                                || p == ShipHullVoxelLayout.Part.THIN_MAST
                                || p == ShipHullVoxelLayout.Part.YARD_SLAB
                                || p == ShipHullVoxelLayout.Part.HELM) continue;
                        var cell = entry.getKey();
                        yes(cell.y() >= 0 && cell.y() <= 3,
                                "every configured hull stays within four layers");
                        hullMinZ = Math.min(hullMinZ, cell.z());
                        hullMaxZ = Math.max(hullMaxZ, cell.z());
                        beam = Math.max(beam, Math.abs(cell.x()));
                    }
                    yes(hullMaxZ - hullMinZ + 1 == length, "extended hull length");
                    yes(beam * 2 + 1 == width, "expanded beam is physical");
                    yes(voxels.values().stream().filter(p ->
                            p == ShipHullVoxelLayout.Part.THIN_MAST).count() > 0,
                            "at least one physical mast");
                    for (int i = 0; i < sails.size(); i++) {
                        int station = ShipHullVoxelLayout.khrabrMastZ(config, i);
                        int top = ShipHullVoxelLayout.khrabrSailTop(sails.get(i));
                        int span = ShipHullVoxelLayout.khrabrSailSpan(sails.get(i));
                        yes(voxels.get(new ShipHullVoxelLayout.Cell(0, 3, station))
                                == ShipHullVoxelLayout.Part.THIN_MAST, "mast foot on deck");
                        yes(voxels.get(new ShipHullVoxelLayout.Cell(0, top + 1, station))
                                == ShipHullVoxelLayout.Part.YARD_SLAB, "yard meets mast");
                        for (int x : new int[]{-span, span})
                            yes(voxels.get(new ShipHullVoxelLayout.Cell(x, top, station + 1))
                                    == ShipHullVoxelLayout.Part.SAIL, "correct sail upgrade");
                    }
                    yes(voxels.values().stream().filter(p ->
                            p == ShipHullVoxelLayout.Part.HELM).count() == 1,
                            "one unchanged helm per configured hull");
                    yes(!voxels.equals(ShipHullVoxelLayout.generate(config)),
                            "never fallback to large deep old model");
                }
        System.out.println("PASS ShipHullVoxelLayoutCheck assertions=" + checks);
    }
}
