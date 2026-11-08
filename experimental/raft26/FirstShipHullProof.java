import org.slavicmyths.ships.FirstShipHull;

/** Executable pure-Java21 contracts. No Minecraft client/server required. */
public final class FirstShipHullProof {
    private static long checked;
    private static void check(boolean ok, String why) {
        checked++;
        if (!ok) throw new AssertionError(why + " (test " + checked + ")");
    }
    private static void approx(double actual, double expected, double tolerance, String what) {
        check(Math.abs(actual - expected) <= tolerance, what + ": " + actual + " vs " + expected);
    }

    public static void main(String[] ignored) {
        check(FirstShipHull.WIDTH == 7 && FirstShipHull.LENGTH == 11, "ship dimensions");
        check(FirstShipHull.MAIN_DECK - FirstShipHull.HOLD_FLOOR >= 2.7D, "two accessible levels");
        check(FirstShipHull.HOLD_FLOOR > 0D, "hold floor out of water");
        check(FirstShipHull.KEEL == 0D, "real planked hull replaces water-source floor");
        check(FirstShipHull.halfBeam(0D) > FirstShipHull.halfBeam(5D), "pinched bow");
        check(FirstShipHull.halfBeam(0D) > FirstShipHull.halfBeam(-5D), "pinched stern");
        check(FirstShipHull.hatch(FirstShipHull.LADDER_X, FirstShipHull.LADDER_Z), "ladder inside hatch");
        check(FirstShipHull.onLadder(FirstShipHull.LADDER_X, FirstShipHull.LADDER_Z), "ladder collision");
        check(!FirstShipHull.mainDeckSupports(FirstShipHull.LADDER_X, FirstShipHull.LADDER_Z), "open deck hatch");
        check(FirstShipHull.holdSupports(FirstShipHull.LADDER_X, FirstShipHull.LADDER_Z), "solid hold below hatch");
        check(FirstShipHull.mainDeckSupports(0D, FirstShipHull.HELM_Z), "helm stands on stern deck");
        check(FirstShipHull.mainDeckSupports(0D, FirstShipHull.MAST_Z), "mast anchored on deck");
        check(FirstShipHull.halfBeam(5.0D) < FirstShipHull.halfBeam(-5.0D),
                "bow must be sharper than stern");
        check(FirstShipHull.mainDeckSupports(2.7D, 0D), "full-width weather deck");
        check(FirstShipHull.MAIN_DECK == 5.0D, "real upper deck top");
        check(FirstShipHull.HOLD_FLOOR == 1.0D, "dry hold floor top");
        check(FirstShipHull.halfBeam(5D) < 0.51D, "bow ends in one real block");
        check(!FirstShipHull.holdSupports(2.7D, 0D), "narrower physically sloped lower hold");
        check(FirstShipHull.confineDeck(8D, 0D).x() >
                FirstShipHull.confineHold(8D, 0D).x() + .30D,
                "upper deck walking must not use lower-hold boundary");
        for (int iz = -20; iz <= 20; iz++) {
            double zz = iz * .25D;
            FirstShipHull.Point top = FirstShipHull.confineDeck(9D, zz);
            FirstShipHull.Point hold = FirstShipHull.confineHold(9D, zz);
            check(FirstShipHull.mainDeckSupports(top.x(), top.z()) ||
                    FirstShipHull.hatch(top.x(), top.z()), "deck clamp still on planks");
            check(FirstShipHull.holdSupports(hold.x(), hold.z()),
                    "hold clamp stays within inner clinker sides");
        }

        // Inverse coordinate transforms at many ship positions and yaw angles.
        for (int yaw = -360; yaw <= 360; yaw += 3) {
            for (int ix = -9; ix <= 9; ix++) {
                for (int iz = -13; iz <= 13; iz++) {
                    double x = ix * .31D, z = iz * .26D;
                    FirstShipHull.Point world = FirstShipHull.toWorld(x, z, 172.5D, -34.25D, yaw);
                    FirstShipHull.Point recovered = FirstShipHull.toLocal(
                            world.x(), world.z(), 172.5D, -34.25D, yaw);
                    approx(recovered.x(), x, 1e-10, "inverse X");
                    approx(recovered.z(), z, 1e-10, "inverse Z");
                }
            }
        }

        // Constant ship-local point while vessel moves and turns in a single tick.
        for (int yaw = -180; yaw <= 180; yaw += 6)
            for (int travel = 0; travel <= 10; travel++) {
                FirstShipHull.Point oldWorld = FirstShipHull.toWorld(
                        .78D, -3.1D, 100D, -200D, yaw);
                FirstShipHull.Point next = FirstShipHull.transfer(oldWorld.x(), oldWorld.z(),
                        100D, -200D, yaw, 100D + travel * .041D, -200D + travel * .027D, yaw + 1.9D);
                FirstShipHull.Point local = FirstShipHull.toLocal(next.x(), next.z(),
                        100D + travel * .041D, -200D + travel * .027D, yaw + 1.9D);
                approx(local.x(), .78D, 1e-9, "carried local X");
                approx(local.z(), -3.1D, 1e-9, "carried local Z");
            }

        // The cabin boundary can never return a point outside its own hull.
        for (int ix = -24; ix <= 24; ix++)
            for (int iz = -25; iz <= 25; iz++) {
                FirstShipHull.Point confined = FirstShipHull.confineHold(ix * .41D, iz * .33D);
                check(FirstShipHull.withinHull(confined.x(), confined.z(), .44D),
                        "lower-floor collision constraint");
            }
        System.out.println("PASS FirstShipHullProof: " + checked + " geometry/transform checks");
    }
}
