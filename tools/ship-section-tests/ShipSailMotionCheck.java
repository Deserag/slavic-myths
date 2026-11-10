import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.ShipSailMotion;

public final class ShipSailMotionCheck {
    private static int checked;
    private static void check(boolean okay, String message) {
        checked++;
        if (!okay) throw new AssertionError(message);
    }
    private static void invalid(Runnable action) {
        checked++;
        try { action.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid sail state accepted");
    }
    public static void main(String[] args) {
        double rowing = ShipSailMotion.power(0, 0);
        double first = ShipSailMotion.power(1, 1);
        double second = ShipSailMotion.power(1, 2);
        double third = ShipSailMotion.power(1, 3);
        check(rowing > 0D && rowing < first, "rowing must be slow");
        check(first < second && second < third, "strict progression of sail tiers");
        check(Math.abs(first - 0.4D) < 1e-9, "tier I");
        check(Math.abs(second - 0.68D) < 1e-9, "tier II");
        check(Math.abs(third - 1D) < 1e-9, "tier III");
        for (int rig = 0; rig <= 4; rig++)
            for (int sail = 0; sail <= 3; sail++) {
                if ((rig == 0) != (sail == 0)) continue;
                double factor = ShipSailMotion.power(rig, sail);
                check(Double.isFinite(factor) && factor >= 0D && factor <= 1D,
                        "safe clamped throttle " + rig + "/" + sail);
                var control = ShipSailMotion.apply(new ShipRigidMotion.Control(1, -.7D), rig, sail);
                check(Math.abs(control.throttle() - factor) < 1e-9, "throttle matches");
                check(Math.abs(control.rudder() + .7D) < 1e-9, "steering preserved");
            }
        invalid(() -> ShipSailMotion.power(1, 0));
        invalid(() -> ShipSailMotion.power(0, 1));
        invalid(() -> ShipSailMotion.power(5, 1));
        invalid(() -> ShipSailMotion.power(1, 4));
        invalid(() -> ShipSailMotion.apply(null, 1, 1));

        var fast = new ShipRigidMotion(new ShipRigidMotion.Pose(0, 0, 0));
        var slow = new ShipRigidMotion(new ShipRigidMotion.Pose(0, 0, 0));
        for (int i = 0; i < 120; i++) {
            fast.advance(ShipSailMotion.apply(new ShipRigidMotion.Control(1, 0), 1, 3), pose -> true);
            slow.advance(ShipSailMotion.apply(new ShipRigidMotion.Control(1, 0), 1, 1), pose -> true);
        }
        check(fast.pose().z() > slow.pose().z(), "higher tier travels farther");
        check(fast.speed() <= ShipRigidMotion.MAX_FORWARD, "physics forward cap remains");
        check(slow.speed() <= ShipRigidMotion.MAX_FORWARD, "slow physics cap remains");
        System.out.println("PASS ShipSailMotionCheck assertions=" + checked);
    }
}
