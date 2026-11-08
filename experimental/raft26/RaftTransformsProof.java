package org.slavicmyths.ships;

/**
 * Verifies the same local frame is used before and after raft movement.
 * JDK21-only: no Minecraft classes, generated from the model geometry.
 */
public final class RaftTransformsProof {
    private static int checks;
    private static void ok(boolean b, String message) {
        checks++;
        if (!b) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        for (RaftSize size : RaftSize.values()) {
            for (int yaw = -180; yaw <= 180; yaw += 15) {
                for (int z = 0; z < size.length; z++)
                    for (int x = 0; x < size.width; x++) {
                        double lx = x + .5D - size.width / 2D;
                        double lz = z + .5D - size.length / 2D;
                        RaftTransforms.Flat w = RaftTransforms.toWorld(lx, lz, yaw);
                        RaftTransforms.Flat local = RaftTransforms.toLocal(w.x(), w.z(), yaw);
                        ok(Math.abs(local.x() - lx) < 1e-9, "X inverse failed");
                        ok(Math.abs(local.z() - lz) < 1e-9, "Z inverse failed");
                        // Move+turn by 35 degrees. The player's local tile must not change.
                        RaftTransforms.Flat moved = RaftTransforms.toWorld(lx, lz, yaw + 35D);
                        RaftTransforms.Flat local2 = RaftTransforms.toLocal(
                                moved.x(), moved.z(), yaw + 35D);
                        ok(Math.abs(local2.x() - lx) < 1e-9, "Turn caused side slip");
                        ok(Math.abs(local2.z() - lz) < 1e-9, "Turn caused longitudinal slip");
                    }
            }
        }
        System.out.println("RAFT RENDER/DECK TRANSFORMS: PASS " + checks +
                " assertions; Minecraft network interpolation still requires gameplay test");
    }
    private RaftTransformsProof() {}
}
