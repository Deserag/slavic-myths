package org.slavicmyths.verify;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.ShipSailMotion;
import org.slavicmyths.ships.ShipSectionPlan;

@GameTestHolder("slavicmyths_ocean26")
@PrefixGameTestTemplate(false)
public final class Ocean26ShipSpeedGameTests {
    @GameTest(template = "empty", timeoutTicks = 40, batch = "ocean_speed_balance")
    public static void nativeStarterOutrunsBoatAndPreservesSailUpgrades(GameTestHelper t) {
        double rowing = ShipSailMotion.power(0, 0);
        double starter = ShipSailMotion.power(1, 1);
        double betterFabric = ShipSailMotion.power(1, 2);
        double betterRig = ShipSailMotion.power(2, 1);
        double ultimate = ShipSailMotion.power(4, 3);
        t.assertTrue(rowing < starter && starter < betterRig && starter < betterFabric
                        && betterFabric < ultimate && Math.abs(ultimate - 1D) < 1E-9D,
                "Legacy sails must be strictly upgradeable from basic rowing");

        ShipRigidMotion ship = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D));
        ShipRigidMotion.Control input = ShipSailMotion.apply(new ShipRigidMotion.Control(1D, 0D), 1, 1);
        for (int tick = 0; tick < 100; tick++) ship.advance(input, pose -> true);
        t.assertTrue(ship.speed() > 0.4D && ship.speed() < ShipRigidMotion.MAX_FORWARD,
                "Basic sail must pass the vanilla water boat 0.4 blocks/tick baseline");
        t.assertTrue(Math.abs(ShipRigidMotion.MAX_FORWARD - .54D) < 1E-9D,
                "Forward motion ceiling must be exactly three times the accepted .18 baseline");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "ocean_speed_balance")
    public static void modularThrustAccountsForSizeDeckLoadAndSails(GameTestHelper t) {
        ShipBuildConfiguration small = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.SMALL);
        ShipBuildConfiguration medium = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.MEDIUM);
        ShipBuildConfiguration large = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.LARGE);
        ShipBuildConfiguration largest = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.LARGEST);
        ShipBuildConfiguration mediumRigged = medium.withSail(0, 2).withSail(1, 2);
        ShipBuildConfiguration largeRigged = large.withSail(0, 3).withSail(1, 3).withSail(2, 3);
        ShipBuildConfiguration largestRigged = largest.withSail(0, 3).withSail(1, 3)
                .withSail(2, 3).withSail(3, 3);

        t.assertTrue(small.width() == 7 && small.length() == 11
                        && medium.width() == 13 && medium.length() == 21
                        && large.width() == 21 && large.length() == 33,
                "Wrong actual hull footprint");
        t.assertTrue(Math.abs(small.structure().propulsionFactor() - ShipSailMotion.power(1, 1)) < 1E-9D,
                "Same 7x11 single tier-I sail must match native starter propulsion");
        t.assertTrue(small.structure().propulsionFactor() > medium.structure().propulsionFactor()
                        && medium.structure().propulsionFactor() > large.structure().propulsionFactor(),
                "One sail must propel a smaller, lighter hull more efficiently");
        t.assertTrue(mediumRigged.structure().propulsionFactor() > medium.structure().propulsionFactor()
                        && largeRigged.structure().propulsionFactor() > large.structure().propulsionFactor()
                        && largestRigged.structure().propulsionFactor() > largest.structure().propulsionFactor(),
                "More and higher-tier sails must compensate for larger hull displacement");
        t.assertTrue(medium.withDecks(2).structure().propulsionFactor()
                        < medium.structure().propulsionFactor(),
                "An additional deck must increase displacement");
        t.assertTrue(ShipRigidMotion.MAX_FORWARD * .985D
                        * mediumRigged.structure().propulsionFactor() > .4D
                        && ShipRigidMotion.MAX_FORWARD * .985D
                        * largeRigged.structure().propulsionFactor() > .4D,
                "Properly equipped medium and large ships must outrun vanilla boats");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "ocean_speed_balance")
    public static void fastHullStillSweepsAndStopsAtObstacle(GameTestHelper t) {
        ShipRigidMotion ship = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D));
        boolean blocked = false;
        for (int tick = 0; tick < 100; tick++) {
            ShipRigidMotion.Frame frame = ship.advance(new ShipRigidMotion.Control(1D, 0D),
                    pose -> pose.z() <= 2.4D);
            if (frame.blocked()) {
                blocked = true;
                break;
            }
        }
        t.assertTrue(blocked && ship.pose().z() <= 2.4D
                        && ship.speed() == 0D && ship.angularVelocity() == 0D,
                "Triple-speed hull crossed the solid clearance boundary");
        t.succeed();
    }

    private Ocean26ShipSpeedGameTests() {}
}
