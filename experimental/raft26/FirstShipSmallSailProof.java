import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipSmallSailPlan;

/** Java 21 deterministic SMALL/V12 sail and walkable-deck proof (not a client test). */
public final class FirstShipSmallSailProof {
    private static int assertions;

    private static void check(boolean condition, String explanation) {
        assertions++;
        if (!condition) throw new AssertionError(explanation);
    }

    private static boolean available(int x, int z) {
        if (z < -5 || z > 5 || Math.abs(x) > FirstShipHull.halfBeam(z))
            return false;
        // The mast is preserved at (0,-1); the real hatch is not a walkable floor.
        return !(x == 0 && z == -1)
                && !((x == -1 || x == 0) && (z == 1 || z == 2));
    }

    private static boolean route(int destinationX, int destinationZ) {
        Set<String> visited = new HashSet<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{0, -3});
        while (!queue.isEmpty()) {
            int[] p = queue.removeFirst();
            if (!available(p[0], p[1]) || !visited.add(p[0] + ":" + p[1])) continue;
            if (p[0] == destinationX && p[1] == destinationZ) return true;
            queue.add(new int[]{p[0] + 1, p[1]});
            queue.add(new int[]{p[0] - 1, p[1]});
            queue.add(new int[]{p[0], p[1] + 1});
            queue.add(new int[]{p[0], p[1] - 1});
        }
        return false;
    }

    private static int sailArea(FirstShipSmallSailPlan.Layout layout) {
        int total = 0;
        for (FirstShipSmallSailPlan.Sail sail : layout.sails())
            for (int y = sail.bottom(); y <= sail.top(); y++)
                total += 2 * sail.rowHalfSpan(y) + 1;
        return total;
    }

    public static void main(String[] args) throws Exception {
        check(FirstShipHull.WIDTH == 7 && FirstShipHull.LENGTH == 11, "SMALL hull size");
        check(FirstShipHull.MAIN_DECK == 5D && FirstShipHull.MAST_Z == -1D, "V11 deck and mast unchanged");
        check(FirstShipSmallSailPlan.FIRST_SAIL_Y == 10, "raised cloth");
        check(route(0, 4), "helm-to-bow route around mast");
        check(route(1, -1) && route(-1, -1), "both sides of mast passable");
        check(route(1, 1) && route(1, 2), "hatch accessible from adjacent deck");

        for (int rig = 0; rig <= 4; rig++) {
            for (int fabric = 0; fabric <= 3; fabric++) {
                if ((rig == 0) != (fabric == 0)) continue;
                var layout = FirstShipSmallSailPlan.forRig(rig, fabric);
                check(layout.mastTop() >= 12 && layout.mastTop() <= 22, "mast within native clearance");
                check((rig == 0) == layout.sails().isEmpty(), "unrigged vessel has no free cloth");
                for (var sail : layout.sails()) {
                    check(sail.bottom() >= 10 && sail.lowerYardY() >= 9, "three fully open walking blocks");
                    check(sail.upperYardY() < layout.mastTop(), "upper yard anchored to physical mast");
                    check(sail.rowHalfSpan(sail.bottom()) < sail.halfSpan(), "stepped lower shoulder");
                    check(sail.rowHalfSpan(sail.top()) < sail.halfSpan(), "stepped upper shoulder");
                    for (int y = sail.bottom(); y <= sail.top(); y++) {
                        int half = sail.rowHalfSpan(y);
                        check(half >= 1 && half <= sail.halfSpan(), "valid full-width cloth band");
                        for (int x = -half; x <= half; x++)
                            for (int t = 0; t < 4; t++) {
                                int rx = t == 0 ? x : t == 1 ? 1 : t == 2 ? -x : -1;
                                int rz = t == 0 ? -1 : t == 1 ? x : t == 2 ? 1 : -x;
                                int backX = t == 0 ? rx : t == 1 ? rz : t == 2 ? -rx : -rz;
                                int backZ = t == 0 ? rz : t == 1 ? -rx : t == 2 ? -rz : rx;
                                check(backX == x && backZ == -1, "four yaw orientations");
                            }
                    }
                }
                check(rig < 2 || sailArea(layout) > sailArea(FirstShipSmallSailPlan.forRig(1, fabric)),
                        "enhanced sail surface exceeds starter");
            }
        }

        check(sailArea(FirstShipSmallSailPlan.forRig(1, 1))
                < sailArea(FirstShipSmallSailPlan.forRig(1, 2)), "fabric tier II silhouette");
        check(sailArea(FirstShipSmallSailPlan.forRig(1, 2))
                < sailArea(FirstShipSmallSailPlan.forRig(1, 3)), "fabric tier III silhouette");

        String blockCode = Files.readString(Path.of(
                "src/main/java/org/slavicmyths/ships/FirstShipBlockHull.java"));
        String renderer = Files.readString(Path.of(
                "src/main/java/org/slavicmyths/ships/client/FirstShipRenderer.java"));
        String entity = Files.readString(Path.of(
                "src/main/java/org/slavicmyths/ships/FirstShipEntity.java"));
        check(blockCode.contains("geometry >= 12 ? layoutV12("), "V11 layout remains versioned");
        check(blockCode.contains("migrateGeometry(level, anchor, turns, rig, fabric, 11, 12)"),
                "safe native version transition");
        check(blockCode.contains("sail.lowerYardY()") && blockCode.contains("sail.upperYardY()"),
                "physical yards share V12 plan");
        check(renderer.contains("ship.geometryVersion() >= 12")
                && renderer.contains("smallSailPanel("), "actual visual cloth follows V12 plan");
        check(entity.contains("!hasWalkers()") && entity.contains("migrateSmallRigV12"),
                "occupied old saves are not blindly rewritten");
        System.out.println("PASS FirstShipSmallSailProof assertions=" + assertions
                + " (runtime collision / Minecraft client: NOT_RUN)");
    }
}
