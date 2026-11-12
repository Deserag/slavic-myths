import java.util.List;
import org.slavicmyths.ships.ShipClearanceSailPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Pure Java21 assertions against NEW large V5 decks; V2/V4 remain frozen. */
public final class ShipLargeClosedDeckCheck {
    private static int assertions, cases;

    private static void check(boolean result, String context) {
        assertions++;
        if (!result) throw new AssertionError(context);
    }

    private static void inspect(ShipSuperstructurePlan plan) {
        var voxels = ShipHullVoxelLayout.generate(plan, 5);
        int halfLength = plan.hullLength() / 2;
        int stern = ShipHullVoxelLayout.asymmetricLargeBeam(plan, -halfLength);
        int bow = ShipHullVoxelLayout.asymmetricLargeBeam(plan, halfLength);
        check(stern >= 3 && stern > bow, "V5 stern must exceed pointed bow");
        check(bow == 0, "V5 bow is not pointed");
        check(ShipHullVoxelLayout.asymmetricLargeBeam(plan, 0) == plan.hullWidth() / 2,
                "Midship beam changed");
        check(voxels.get(new ShipHullVoxelLayout.Cell(0, 0, -halfLength))
                == ShipHullVoxelLayout.Part.KEEL, "Stern keel is missing");
        check(voxels.get(new ShipHullVoxelLayout.Cell(0, 0, halfLength))
                == ShipHullVoxelLayout.Part.KEEL, "Bow keel is missing");
        check(voxels.get(new ShipHullVoxelLayout.Cell(0, 4, halfLength))
                == ShipHullVoxelLayout.Part.DECK, "Bow deck is not sealed");
        check(voxels.get(new ShipHullVoxelLayout.Cell(stern, 4, -halfLength))
                == ShipHullVoxelLayout.Part.DECK, "Wide stern lacks a deck edge");

        int walls = 0, windows = 0, openRailing = 0;
        for (var entry : voxels.entrySet()) {
            switch (entry.getValue()) {
                case CABIN_WALL -> walls++;
                case WINDOW -> windows++;
                case RAIL -> {
                    if (entry.getKey().y() == 4 + (plan.decks() - 1) * 4 + 1)
                        openRailing++;
                }
                default -> {}
            }
        }
        check(walls > 40, "Upper stories contain only a wooden frame");
        check(windows > 0, "Upper stories have no windows");
        check(openRailing > 10, "Top deck must remain open with guard rails");

        for (int floor = 2; floor <= plan.decks(); floor++) {
            int top = 4 + (floor - 1) * 4;
            int front = ShipHullVoxelLayout.enclosedFloorFront(plan, floor);
            for (int y = top - 3; y <= top - 2; y++) {
                var entrance = voxels.get(new ShipHullVoxelLayout.Cell(0, y, front));
                check(entrance == null || entrance == ShipHullVoxelLayout.Part.DOOR,
                        "Closed ship has no entry on floor " + floor + ": " + entrance);
            }
            int side = ShipHullVoxelLayout.asymmetricLargeBeam(plan, 0) - 1;
            var exterior = voxels.get(new ShipHullVoxelLayout.Cell(side, top - 2, 0));
            check(exterior == ShipHullVoxelLayout.Part.CABIN_WALL
                    || exterior == ShipHullVoxelLayout.Part.WINDOW,
                    "Exposed side wall missing on floor " + floor);
            if (front + 2 <= halfLength) {
                var forwardOpening = voxels.get(new ShipHullVoxelLayout.Cell(0, top, front + 2));
                check(forwardOpening != ShipHullVoxelLayout.Part.DECK,
                        "New V5 upper storey covers the forward working deck");
            }
        }
        for (var sail : ShipClearanceSailPlan.forPlan(plan).sails())
            check(sail.lowerYardY() - sail.highestDeckY() - 1 >= 3,
                    "New V5 deck obstructs lower yard");
        cases++;
    }

    public static void main(String[] args) {
        var large = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                21, 33, List.of(1), 1, false, false);
        var largest = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                21, 33, List.of(1), 1, false, false);
        check(ShipHullVoxelLayout.asymmetricLargeBeam(largest, -16)
                > ShipHullVoxelLayout.asymmetricLargeBeam(large, -16),
                "LARGEST stern must be fuller than LARGE with identical dimensions");
        check(!ShipHullVoxelLayout.generate(large, 5).equals(
                ShipHullVoxelLayout.generate(largest, 5)),
                "LARGEST hull must not match LARGE except for mast count");
        for (ShipSectionPlan.ShipClass type : new ShipSectionPlan.ShipClass[]{
                ShipSectionPlan.ShipClass.LARGE, ShipSectionPlan.ShipClass.LARGEST})
            for (int length : new int[]{33, 37, 45})
                for (int width : new int[]{21, 25})
                    for (int decks = 2; decks <= 3; decks++)
                        for (boolean cabin : new boolean[]{false, true})
                            for (boolean bridge : new boolean[]{false, true}) {
                                var before = new ShipSuperstructurePlan(type, width, length,
                                        List.of(1), 1, false, false);
                                var a = before.withSail(2).withDecks(decks)
                                        .withCabin(cabin).withBridge(bridge);
                                var b = before.withDecks(decks).withCabin(cabin)
                                        .withBridge(bridge).withSail(2);
                                check(a.equals(b), "Order changes blueprint");
                                check(ShipHullVoxelLayout.generate(a, 5).equals(
                                        ShipHullVoxelLayout.generate(b, 5)),
                                        "Order changes new V5 geometry");
                                inspect(a);
                                check(ShipHullVoxelLayout.generate(a, 4).equals(
                                        ShipHullVoxelLayout.generate(b, 4)),
                                        "V4 compatibility changed");
                            }
        System.out.println("SHIP_P1_CLOSED_DECK_PASS cases=" + cases
                + " assertions=" + assertions);
    }

    private ShipLargeClosedDeckCheck() {}
}
