package org.slavicmyths.ships;

import java.util.HashSet;
import java.util.Set;

/** Standalone JDK21-only tests of the 0.5-block rotated hull footprint. */
public final class RaftFootprintProof {
    private static int assertions;

    private static void check(boolean condition, String label) {
        assertions++;
        if (!condition) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        for (RaftSize size : RaftSize.values()) {
            for (int angle = 0; angle < 360; angle += 15) {
                for (int sx = 0; sx < size.width * 2; sx++)
                    for (int sz = 0; sz < size.length * 2; sz++) {
                        RaftFootprint.Point p = RaftFootprint.sample(size.width, size.length, sx, sz, angle);
                        RaftFootprint.Point reverse = RaftFootprint.rotate(p.x(), p.z(), -angle);
                        double localX = (sx + .5D) / 2D - size.width / 2D;
                        double localZ = (sz + .5D) / 2D - size.length / 2D;
                        check(Math.abs(reverse.x() - localX) < 1e-9, "X inverse rotation");
                        check(Math.abs(reverse.z() - localZ) < 1e-9, "Z inverse rotation");
                        check(Math.hypot(p.x(), p.z()) <= Math.hypot(size.width / 2D, size.length / 2D),
                                "Rotated samples must stay inside deck radius");
                    }
            }
            for (int sx = 0; sx < size.width * 2; sx++)
                for (int sz = 0; sz < size.length * 2; sz++) {
                    RaftFootprint.Point p = RaftFootprint.sample(size.width, size.length, sx, sz, 90);
                    check(Math.abs(p.x() - ((sz + .5D) / 2D - size.length / 2D) * -1D) < 1e-9,
                            "At 90 degrees world X spans ship length");
                }
        }
        for (int i = 0; i < 2; i++) {
            assertions++;
            try {
                RaftFootprint.sample(3, 3, i == 0 ? -1 : 6, 0, 0);
                throw new AssertionError("Invalid sub-cell index accepted");
            } catch (IllegalArgumentException expected) {
                // Correct.
            }
        }
        System.out.println("RAFT ROTATED PLACEMENT: PASS " + assertions + " JDK21 assertions.");
    }

    private RaftFootprintProof() {}
}
