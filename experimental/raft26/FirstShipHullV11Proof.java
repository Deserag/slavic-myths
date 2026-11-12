import org.slavicmyths.ships.FirstShipHull;

/** Java 21 geometry proof for the authoritative V11 section function. */
public final class FirstShipHullV11Proof {
    private static int checks;
    private static void yes(boolean valid, String name) {
        checks++;
        if (!valid) throw new AssertionError(name);
    }
    private static int oldBeam(int z) {
        return (int)Math.floor(FirstShipHull.halfBeam(z));
    }
    private static boolean inside(int x, int z) {
        return z >= -5 && z <= 5 && Math.abs(x) <= oldBeam(z);
    }
    private static boolean edge(int x, int z) {
        return !inside(x - 1, z) || !inside(x + 1, z)
                || !inside(x, z - 1) || !inside(x, z + 1);
    }
    /** V10 hull shell plus the Y0/Y1 V11 changes; excludes ladder furniture. */
    private static boolean hull(int x, int y, int z) {
        if (!inside(x, z)) return false;
        int keel = FirstShipHull.halfWidthV11(z, 0);
        int chine = FirstShipHull.halfWidthV11(z, 1);
        return switch (y) {
            case 0 -> Math.abs(x) <= keel;
            case 1 -> (Math.abs(x) <= chine || FirstShipHull.exposedEndV11(x, z))
                    && (Math.abs(x) > keel || edge(x, z));
            case 2 -> edge(x, z);
            default -> false;
        };
    }
    public static void main(String[] args) {
        yes(FirstShipHull.WIDTH == 7 && FirstShipHull.LENGTH == 11, "dimensions");
        yes(FirstShipHull.halfWidthV11(0, 0) == 1, "3 wide keel");
        yes(FirstShipHull.halfWidthV11(0, 1) == 2, "5 wide chine");
        yes(FirstShipHull.halfWidthV11(0, 2) == 3, "7 wide upper wall");
        yes(FirstShipHull.HELM_Z == -3, "helm position");
        yes(FirstShipHull.LADDER_X == -1 && FirstShipHull.LADDER_Z == 2, "ladder position");
        yes(FirstShipHull.hatch(-1, 2) && FirstShipHull.hatch(0, 1), "hatch position");
        int[] oldWidths = {1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0};
        for (int z = -5; z <= 5; z++) {
            int top = oldBeam(z), keel = FirstShipHull.halfWidthV11(z, 0);
            int chine = FirstShipHull.halfWidthV11(z, 1);
            yes(top == oldWidths[z + 5], "unchanged V10 widths z=" + z);
            yes(0 <= keel && keel <= chine && chine <= top, "taper z=" + z);
            for (int x = -3; x <= 3; x++)
                for (int y = 0; y <= 2; y++)
                    yes(hull(x, y, z) == hull(-x, y, z), "symmetry");
            for (int x = -top; x <= top; x++) {
                // Any Y1 free space under the chine has a wood floor at Y0.
                if (Math.abs(x) <= chine && !hull(x, 1, z))
                    yes(hull(x, 0, z), "dry lower-hold floor");
                if (!hull(x, 2, z) && Math.abs(x) > keel)
                    yes(hull(x, 1, z), "Y2 headroom above solid chine");
            }
        }
        // Nose face (z=4, x=+/-1) was cut away by the generic Y1 inset.
        // Also assert the next narrowing rows and the stern are watertight.
        for (int z : new int[]{2, 3, 4, -3, -4}) {
            int top = oldBeam(z);
            for (int x = -top; x <= top; x++) {
                if (FirstShipHull.exposedEndV11(x, z)) {
                    yes(hull(x, 1, z), "V11 narrowing face sealed x=" + x + " z=" + z);
                    yes(hull(-x, 1, z), "mirror narrowing face sealed");
                }
            }
        }
        for (int turns = 0; turns < 4; turns++) {
            for (int z = -5; z <= 5; z++) for (int x = -3; x <= 3; x++) {
                int rx = turns == 0 ? x : turns == 1 ? -z : turns == 2 ? -x : z;
                int rz = turns == 0 ? z : turns == 1 ? x : turns == 2 ? -z : -x;
                int lx = turns == 0 ? rx : turns == 1 ? rz : turns == 2 ? -rx : -rz;
                int lz = turns == 0 ? rz : turns == 1 ? -rx : turns == 2 ? -rz : rx;
                yes(lx == x && lz == z, "rotation inverse");
            }
        }
        for (int y = 0; y <= 2; y++) {
            yes(FirstShipHull.halfWidthV11(5, y) == 0, "bow remains one block");
            yes(FirstShipHull.halfWidthV11(-5, y) <= FirstShipHull.halfWidthV11(-4, y), "stern taper");
            yes(FirstShipHull.halfWidthV11(4, y) <= FirstShipHull.halfWidthV11(3, y), "bow taper");
        }
        yes(FirstShipHull.halfWidthV11(-6, 0) == -1, "outside length");
        yes(FirstShipHull.halfWidthV11(6, 1) == -1, "outside length");
        System.out.println("PASS FirstShipHullV11Proof assertions=" + checks);
    }
}
