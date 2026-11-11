import java.util.List;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Run with javac using only the two pure-Java planning source files. */
public final class ShipSuperstructureCheck {
    private static int assertions;
    private static void yes(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
    private static void invalid(Runnable operation, String message) {
        assertions++;
        try { operation.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected invalid configuration: " + message);
    }
    private static ShipSuperstructurePlan plan(ShipSectionPlan.ShipClass type,
            int width, int length, List<Integer> sails, int decks,
            boolean bridge, boolean cabin) {
        return new ShipSuperstructurePlan(type, width, length, sails, decks, bridge, cabin);
    }

    public static void main(String[] args) {
        var small = plan(ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, false);
        yes(small.mastPositions().equals(List.of(0)), "small center mast");
        yes(small.rawThrust() == 1D, "basic sail thrust");
        yes(Math.abs(small.propulsionFactor() - .80D) < 1E-9D, "small starter propulsion baseline");
        invalid(() -> small.withSail(1), "small second mast");
        invalid(() -> small.withSail(2), "small tier II");
        invalid(() -> small.withDecks(2), "small second deck");
        invalid(() -> small.withCabin(true), "small cabin");
        invalid(() -> small.withBridge(true), "small bridge");

        var medium = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1, 2), 2, true, true);
        yes(medium.mastPositions().equals(List.of(0, 4)), "two centered masts");
        yes(Math.abs(medium.rawThrust() - 2.7D) < 1E-9D, "sum mixed sail tiers");
        yes(medium.propulsionFactor() > 0D && medium.propulsionFactor() < 1D,
                "large hull with small sails is underpowered");
        invalid(() -> medium.withSail(2), "medium mast limit");
        invalid(() -> medium.withDecks(3), "medium deck limit");
        invalid(() -> plan(ShipSectionPlan.ShipClass.MEDIUM, 9, 13,
                List.of(1), 2, true, true), "bridge cabin minimum footprint");

        var large = plan(ShipSectionPlan.ShipClass.LARGE, 21, 33,
                List.of(1, 2, 3), 3, true, true);
        yes(large.mastPositions().equals(List.of(-4, 0, 4)), "three masts");
        yes(Math.abs(large.rawThrust() - 5.2D) < 1E-9D, "all three sail tiers");
        yes(large.propulsionFactor() <= 1D, "forward thrust is bounded");

        var largest = plan(ShipSectionPlan.ShipClass.LARGEST, 21, 33,
                List.of(3, 3, 2, 1), 3, true, true);
        yes(largest.mastPositions().equals(List.of(-6, -2, 2, 6)), "four masts");
        yes(largest.sailTiers().equals(List.of(3, 3, 2, 1)), "independent sail tiers");
        yes(largest.withCabin(false).cabin() == false, "cabin removal");
        yes(largest.withBridge(false).bridge() == false, "bridge removal");

        invalid(() -> plan(ShipSectionPlan.ShipClass.LARGE, 11, 19,
                List.of(1), 3, false, false), "third deck clearance");
        invalid(() -> plan(ShipSectionPlan.ShipClass.LARGEST, 7, 11,
                List.of(1, 1, 1, 1), 1, false, false), "mast fit");
        invalid(() -> plan(ShipSectionPlan.ShipClass.LARGE, 13, 21,
                List.of(4), 1, false, false), "invalid sail tier");
        invalid(() -> plan(ShipSectionPlan.ShipClass.LARGEST, 13, 21,
                List.of(1, 1, 1, 1), 1, false, true), "stern cabin intersection");
        invalid(() -> plan(ShipSectionPlan.ShipClass.MEDIUM, 12, 21,
                List.of(1), 1, false, false), "even width");
        invalid(() -> plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 20,
                List.of(1), 1, false, false), "even length");
        invalid(() -> plan(ShipSectionPlan.ShipClass.SMALL, 7, 11,
                List.of(), 0, false, false), "zero decks");

        System.out.println("PASS ShipSuperstructureCheck assertions=" + assertions);
    }
}
