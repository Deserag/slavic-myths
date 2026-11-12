import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Java 21: pure-voxel proof for new modular geometry and immutable V1 layouts. */
public final class ShipSteppedHullCheck {
    private static int checks;
    private static void yes(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static boolean solid(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull,
            int x, int y, int z) {
        return hull.containsKey(new ShipHullVoxelLayout.Cell(x, y, z));
    }
    private static void verify(ShipSuperstructurePlan plan) {
        var old = ShipHullVoxelLayout.generate(plan);
        var legacy = ShipHullVoxelLayout.generate(plan, 1);
        var hull = ShipHullVoxelLayout.generate(plan, 2);
        yes(old.equals(legacy), "legacy V1 unchanged");
        yes(!hull.equals(old), "new chine differs from legacy floor");
        yes(hull.equals(ShipHullVoxelLayout.generate(plan, 2)), "deterministic V2");
        int half = plan.hullLength() / 2;
        int top = plan.hullWidth() / 2;
        int prev = -1;
        for (int y = 0; y <= 3; y++) {
            int beam = ShipHullVoxelLayout.steppedHalfBeam(plan, 0, y);
            yes(beam > prev && beam <= top, "step width grows upward");
            yes(solid(hull, beam, y, 0) && solid(hull, -beam, y, 0),
                    "both sides of chine exist");
            prev = beam;
        }
        for (int z = -half; z <= half; z++) {
            int upper = ShipHullVoxelLayout.halfBeam(plan, z);
            for (int y = 0; y <= 3; y++) {
                int edge = ShipHullVoxelLayout.steppedHalfBeam(plan, z, y);
                yes(edge >= 0 && edge <= upper, "no larger than deck outline");
                yes(solid(hull, edge, y, z) && solid(hull, -edge, y, z),
                        "outboard surfaces sealed");
                for (int x = 0; x <= top; x++) {
                    // Ladder + backing at z=1 and the open hatch entry z=0
                    // are functional asymmetric interior cutouts.
                    if (z == 1 && y >= 1 && y <= 3 && (x == 2 || x == 3)
                            || z == 0 && y >= 1 && y <= 3 && x == 2) continue;
                    yes(solid(hull, x, y, z) == solid(hull, -x, y, z),
                            "port/starboard occupancy x=" + x + " y=" + y + " z=" + z);
                }
            }
            for (int x = -upper; x <= upper; x++) {
                var cell = new ShipHullVoxelLayout.Cell(x, 4, z);
                yes(hull.get(cell) == old.get(cell), "deck and hatch retained");
            }
        }
        for (var entry : old.entrySet())
            if (entry.getKey().y() >= 4)
                yes(hull.get(entry.getKey()) == entry.getValue(),
                        "mast/sail/rails/upper deck/helm retained");
        yes(hull.get(new ShipHullVoxelLayout.Cell(0, 0, 0)) ==
                ShipHullVoxelLayout.Part.KEEL, "continuous center keel");
        yes(!solid(hull, 0, 1, 2) && !solid(hull, 0, 2, 2),
                "two open standing cells above central keel");
        yes(!solid(hull, -2, 1, 0) && !solid(hull, -2, 2, 0)
                && !solid(hull, -2, 3, 0), "hatch shaft clear at all lower levels");
        yes(solid(hull, -2, 0, 0) && solid(hull, -2, 0, 1),
                "hatch and ladder waterline sealed");
        yes(hull.get(new ShipHullVoxelLayout.Cell(-2, 1, 1))
                == ShipHullVoxelLayout.Part.LADDER, "ladder unchanged");
        for (int t = 0; t < 4; t++) {
            HashSet<String> rotated = new HashSet<>();
            for (var c : hull.keySet()) {
                int x = t == 0 ? c.x() : t == 1 ? -c.z() : t == 2 ? -c.x() : c.z();
                int z = t == 0 ? c.z() : t == 1 ? c.x() : t == 2 ? -c.z() : -c.x();
                yes(rotated.add(x + ":" + c.y() + ":" + z), "four rotations bijective");
            }
            yes(rotated.size() == hull.size(), "rotated cell count preserved");
        }
    }
    public static void main(String[] args) {
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.MEDIUM,
                13, 21, List.of(1, 2), 2, true, true));
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.MEDIUM,
                15, 25, List.of(1, 2), 2, false, false));
        // Large vessels keep three actual decks while gaining a broader,
        // tapered keel than MEDIUM. Verify added length/beam sections as well.
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                21, 33, List.of(1, 2, 3), 3, true, true));
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                23, 37, List.of(1, 2, 3), 3, true, true));
        // LARGEST shares the 21x33 base beam but is a separate versioned
        // class with four mast stations, three decks and a full aft fit-out.
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                21, 33, List.of(3, 3, 2, 1), 3, true, true));
        verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                23, 37, List.of(3, 3, 2, 1), 3, true, true));
        System.out.println("PASS ShipSteppedHullCheck assertions=" + checks);
    }
}
