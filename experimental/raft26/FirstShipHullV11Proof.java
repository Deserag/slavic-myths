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
            case 1 -> Math.abs(x) <= chine && (Math.abs(x) > keel || edge(x, z));
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
