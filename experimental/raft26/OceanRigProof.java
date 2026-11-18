import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipRigging;

/** Pure Java21 rigging and deterministic crew coordinate invariants.
 *  NO NeoForge runtime or actual Minecraft world is launched by this proof.
 */
public final class OceanRigProof {
    private static long count;
    private static void ok(boolean test, String message) {
        count++;
        if (!test) throw new AssertionError("OceanRigProof: " + message + " #" + count);
    }
    private static void approx(double actual, double expected, String message) {
        approx(actual, expected, 1e-9, message);
    }
    private static void approx(double actual, double expected, double tolerance, String message) {
        ok(Double.isFinite(actual) && Math.abs(actual - expected) < tolerance, message);
    }
    private static long simulatedSteps(int tier, int cloth, long totalTicks) {
        ShipRigging ship = new ShipRigging(tier, cloth);
        long steps = 0;
        double next = -100D;
        for (long tick = 0; tick < totalTicks; tick++)
            if (tick + 1e-6D >= next) {
                steps++;
                next = ship.nextAvailableTick(next, tick);
            }
        return steps;
    }
    public static void main(String[] args) {
        double[][] speeds = new double[5][4];
        for (int tier = 0; tier <= 4; tier++) {
            for (int cloth = (tier == 0 ? 0 : 1); cloth <= (tier == 0 ? 0 : 3); cloth++) {
                ShipRigging rig = new ShipRigging(tier, cloth);
                ok(rig.level() == tier && rig.fabric() == cloth, "saved tier/quality");
                int cd = rig.cooldownTicks();
                ok(cd >= 5 && cd <= 17, "bounded server movement");
                speeds[tier][cloth] = rig.clothMultiplier() * rig.rigMultiplier();
                if (tier > 0 && cloth > 1) {
                    ok(speeds[tier][cloth] > speeds[tier][cloth - 1], "cloth grade faster");
                    ok(cd <= new ShipRigging(tier, cloth - 1).cooldownTicks(), "cloth cooldown monotonic");
                }
                if (tier > 1) ok(speeds[tier][cloth] > speeds[tier - 1][cloth], "taller rig stronger");
            }
        }
        for (int invalidTier = -10; invalidTier <= 10; invalidTier++)
            for (int invalidCloth = -10; invalidCloth <= 10; invalidCloth++) {
                boolean legal = invalidTier == 0 ? invalidCloth == 0
                        : invalidTier >= 1 && invalidTier <= 4
                                && invalidCloth >= 1 && invalidCloth <= 3;
                try {
                    new ShipRigging(invalidTier, invalidCloth);
                    ok(legal, "invalid NBT rig rejected");
                } catch (IllegalArgumentException e) {
                    ok(!legal, "valid saved rig must load");
                }
            }

        // Simulate sustained W input: the exact fractional schedule must keep
        // 15% and 30% fabric speed improvements even at the largest mast.
        for (int tier = 1; tier <= 4; tier++) {
            long base = simulatedSteps(tier, 1, 50000);
            long linen = simulatedSteps(tier, 2, 50000);
            long reinforced = simulatedSteps(tier, 3, 50000);
            ok(base < linen && linen < reinforced, "all grades must differ at tier " + tier);
            approx((double)linen / base, 1.15D, 0.01D, "linen multiplier");
            approx((double)reinforced / base, 1.30D, 0.01D, "reinforced multiplier");
        }

        // Rider, deck walker and hold passenger have independent ship-local positions.
        double[][] crew = {{0D, FirstShipHull.HELM_Z}, {1.1D, -1.8D}, {-1.25D, 1.5D}};
        for (int turns = 0; turns < 4; turns++) {
            for (int nextTurns = 0; nextTurns < 4; nextTurns++)
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        for (double[] person : crew) {
                            double ox = 37.5D, oz = -121.5D;
                            FirstShipHull.Point prev = FirstShipHull.toWorld(
                                    person[0], person[1], ox, oz, turns * 90D);
                            FirstShipHull.Point after = FirstShipHull.transfer(
                                    prev.x(), prev.z(), ox, oz, turns * 90D,
                                    ox + dx, oz + dz, nextTurns * 90D);
                            FirstShipHull.Point local = FirstShipHull.toLocal(
                                    after.x(), after.z(), ox + dx, oz + dz, nextTurns * 90D);
                            approx(local.x(), person[0], "captain/crew local X");
                            approx(local.z(), person[1], "captain/crew local Z");
                        }
        }
        ok(FirstShipHull.mainDeckSupports(0D, -2D), "exit cell has real deck support");
        ok(!FirstShipHull.hatch(0D, -2D), "exit not above a hatch");
        ok(FirstShipHull.mainDeckSupports(0D, FirstShipHull.HELM_Z), "wheel seat supported");
        System.out.println("PASS OceanRigProof: " + count
                + " pure-Java state/coordinate checks (Minecraft runtime NOT tested)");
    }
}
