import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipClearanceSailPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Standalone Java 21 full voxel equality and large-ship sail clearance. */
public final class ShipRiggingOrderCheck {
    private static int cases, assertions;

    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static void compare(ShipSuperstructurePlan a,
            ShipSuperstructurePlan b, String label) {
        check(a.equals(b), label + " final blueprints are not equal");
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> first =
                ShipHullVoxelLayout.generate(a, 4);
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> second =
                ShipHullVoxelLayout.generate(b, 4);
        check(first.equals(second), label + " resulting physical voxels differ");
        for (var sail : ShipClearanceSailPlan.forPlan(a).sails()) {
            check(sail.lowerYardY() - sail.highestDeckY() - 1 >= 3,
                    label + " physical lower yard blocks upper deck");
            check(first.get(new ShipHullVoxelLayout.Cell(
                    sail.halfSpan(), sail.lowerYardY(), sail.mastZ()))
                    == ShipHullVoxelLayout.Part.YARD, label + " physical yard missing");
            check(first.get(new ShipHullVoxelLayout.Cell(
                    sail.rowHalfSpan(sail.bottom()), sail.bottom(), sail.mastZ() + 1))
                    == ShipHullVoxelLayout.Part.SAIL, label + " canvas missing");
        }
        cases++;
    }

    public static void main(String[] args) {
        for (ShipSectionPlan.ShipClass type : new ShipSectionPlan.ShipClass[]{
                ShipSectionPlan.ShipClass.LARGE, ShipSectionPlan.ShipClass.LARGEST}) {
            for (int length : new int[]{33, 37, 45})
                for (int width : new int[]{21, 23, 25})
                    for (int decks = 1; decks <= 3; decks++)
                        for (boolean cabin : new boolean[]{false, true})
                            for (boolean bridge : new boolean[]{false, true}) {
                                ShipSuperstructurePlan original = new ShipSuperstructurePlan(
                                        type, width, length, List.of(1), 1, false, false);
                                ShipSuperstructurePlan sailsFirst = original.withSail(2)
                                        .withDecks(decks).withCabin(cabin).withBridge(bridge);
                                ShipSuperstructurePlan structureFirst = original
                                        .withDecks(decks).withCabin(cabin).withBridge(bridge)
                                        .withSail(2);
                                compare(sailsFirst, structureFirst,
                                        type + " " + width + "x" + length + " " + decks);
                                if (type == ShipSectionPlan.ShipClass.LARGEST) {
                                    ShipSuperstructurePlan fourSailsFirst = sailsFirst
                                            .withSail(3).withSail(2);
                                    ShipSuperstructurePlan fourStructureFirst = original
                                            .withDecks(decks).withCabin(cabin).withBridge(bridge)
                                            .withSail(2).withSail(3).withSail(2);
                                    compare(fourSailsFirst, fourStructureFirst,
                                            "four masts " + length + " " + decks);
                                }
                            }
        }
        var legacy = new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.LARGE, 21, 33, List.of(1), 1, false, false);
        check(ShipHullVoxelLayout.generate(legacy, 2).get(
                new ShipHullVoxelLayout.Cell(2, 7, 0)) == ShipHullVoxelLayout.Part.YARD,
                "saved V2 geometry must retain its original low yard");
        FirstShipHull.Point old = new FirstShipHull.Point(1D, 2D);
        FirstShipHull.Point next = FirstShipHull.transfer(
                old.x(), old.z(), 0D, 0D, 0D, .2D, .15D, 8D);
        check(FirstShipHull.likelyAlreadyCarried(old, next, next),
                "duplicate client pose must be detected");
        check(!FirstShipHull.likelyAlreadyCarried(old, old, next),
                "untouched client pose must be carried");
        System.out.println("SHIP_P0_RIG_ORDER_PASS cases=" + cases + " assertions=" + assertions);
    }

    private ShipRiggingOrderCheck() {}
}
