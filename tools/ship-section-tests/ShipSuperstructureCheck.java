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
        yes(small.propulsionFactor() == 1D, "baseline thrust cap");
        invalid(() -> small.withSail(1), "small second mast");
        invalid(() -> small.withSail(2), "small tier II");
        invalid(() -> small.withDecks(2), "small second deck");
        invalid(() -> small.withCabin(true), "small cabin");
        invalid(() -> small.withBridge(true), "small bridge");

        var medium = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1, 2), 2, true, true);
        yes(medium.mastPositions().equals(List.of(0, 4)), "two centered masts");
        yes(Math.abs(medium.rawThrust() - 2.7D) < 1E-9D, "sum mixed sail tiers");
        yes(medium.propulsionFactor() > 0D && medium.propulsionFactor() <= 1D,
                "medium thrust remains bounded");
        var initialMedium = plan(ShipSectionPlan.ShipClass.MEDIUM, 13, 21,
                List.of(1), 1, false, false);
        yes(Math.abs(initialMedium.propulsionFactor() - 1D) < 1E-9D,
                "unloaded medium must reach the shared rigid-motion cap");
        var loadedBasic = plan(ShipSectionPlan.ShipClass.MEDIUM, 15, 29,
                List.of(1), 2, true, true);
        var loadedUpgraded = plan(ShipSectionPlan.ShipClass.MEDIUM, 15, 29,
                List.of(2), 2, true, true);
        yes(loadedUpgraded.propulsionFactor() > loadedBasic.propulsionFactor(),
                "tier II sail improves heavily loaded medium travel");
        var fullMedium = plan(ShipSectionPlan.ShipClass.MEDIUM, 15, 29,
                List.of(2, 2), 2, true, true);
        yes(fullMedium.propulsionFactor() == 1D,
                "fully upgraded MEDIUM reaches rigid-motion speed cap");
        yes(fullMedium.loadFactor() > initialMedium.loadFactor(),
                "extensions and upper deck still impose load");
        double oldInitial = 1D / ((13D * 21D / 77D));
        double oldFull = 3.4D / ((15D * 29D / 77D) * 1.60D);
        yes(initialMedium.propulsionFactor() > oldInitial,
                "initial MEDIUM faster than old load model");
        yes(fullMedium.propulsionFactor() > oldFull,
                "fully upgraded MEDIUM faster than old load model");
        var movement = new org.slavicmyths.ships.ShipRigidMotion(
                new org.slavicmyths.ships.ShipRigidMotion.Pose(0D, 0D, 0D));
        for (int tick = 0; tick < 100; tick++)
            movement.advance(new org.slavicmyths.ships.ShipRigidMotion.Control(
                    fullMedium.propulsionFactor(), 0D), pose -> true);
        yes(movement.speed() > .27D
                        && movement.speed() <= org.slavicmyths.ships.ShipRigidMotion.MAX_FORWARD,
                "MEDIUM propulsion must reach practical sailing speed");
        invalid(() -> medium.withSail(2), "medium mast limit");
        invalid(() -> medium.withDecks(3), "medium deck limit");
        invalid(() -> plan(ShipSectionPlan.ShipClass.MEDIUM, 9, 13,
                List.of(1), 2, true, true), "bridge cabin minimum footprint");

        var large = plan(ShipSectionPlan.ShipClass.LARGE, 21, 33,
                List.of(1, 2), 3, true, true);
        yes(large.mastPositions().equals(List.of(-2, 2)), "legacy planning unchanged");
        yes(Math.abs(large.rawThrust() - 2.7D) < 1E-9D, "two-mast rig");
        var brigantine = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                21, 33, List.of(1, 2), 3, true, true, 3);
        yes(brigantine.mastPositions().equals(List.of(-3, 6)),
                "new brigantine has separated fore and aft mast stations");
        yes(brigantine.propulsionFactor() > 0D && brigantine.propulsionFactor() <= 1D,
                "brigantine thrust remains usable");
        var starterBrigantine = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                21, 33, List.of(1, 1), 1, false, false, 3);
        var fastBrigantine = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                25, 45, List.of(3, 3), 3, true, true, 3);
        yes(starterBrigantine.propulsionFactor() > .60D,
                "base brigantine must not crawl");
        yes(fastBrigantine.propulsionFactor() > starterBrigantine.propulsionFactor(),
                "full rig upgrades must improve travel speed");
        var slowSail = new org.slavicmyths.ships.ShipRigidMotion(
                new org.slavicmyths.ships.ShipRigidMotion.Pose(0, 0, 0));
        var fastSail = new org.slavicmyths.ships.ShipRigidMotion(
                new org.slavicmyths.ships.ShipRigidMotion.Pose(0, 0, 0));
        for (int tick = 0; tick < 100; tick++) {
            slowSail.advance(new org.slavicmyths.ships.ShipRigidMotion.Control(
                    starterBrigantine.propulsionFactor(), 0), pose -> true);
            fastSail.advance(new org.slavicmyths.ships.ShipRigidMotion.Control(
                    fastBrigantine.propulsionFactor(), 0), pose -> true);
        }
        yes(fastSail.pose().z() > slowSail.pose().z(),
                "tier III speed upgrades must increase distance travelled");


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
