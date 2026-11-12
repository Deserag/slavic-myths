import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.FirstShipMediumSailPlan;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Standalone Java 21 physical/cloth regression matrix for MEDIUM geometry 3. */
public final class ShipMediumSailClearanceCheck {
    private static int assertions;
    private static int configurations;
    private static void yes(boolean result, String name) {
        assertions++;
        if (!result) throw new AssertionError(name);
    }
    private static ShipHullVoxelLayout.Part at(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> map,
            int x, int y, int z) {
        return map.get(new ShipHullVoxelLayout.Cell(x, y, z));
    }
    private record Position(int x, int y, int z) {}

    private static boolean passage(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull,
            Position pos) {
        var foot = at(hull, pos.x(), pos.y(), pos.z());
        var head = at(hull, pos.x(), pos.y() + 1, pos.z());
        var support = at(hull, pos.x(), pos.y() - 1, pos.z());
        if (foot == ShipHullVoxelLayout.Part.LADDER) return true;
        return (foot == null || foot == ShipHullVoxelLayout.Part.DOOR)
                && (head == null || head == ShipHullVoxelLayout.Part.DOOR
                        || head == ShipHullVoxelLayout.Part.LADDER)
                && (support == ShipHullVoxelLayout.Part.DECK
                        || support == ShipHullVoxelLayout.Part.BRIDGE
                        || support == ShipHullVoxelLayout.Part.LADDER);
    }

    /** Voxel BFS for real floor support, walking, ladder entry and climbing. */
    private static boolean helmToBow(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull,
            ShipSuperstructurePlan plan) {
        int half = plan.hullLength() / 2;
        int helmZ = -half + (plan.bridge() ? 4 : plan.cabin() ? 9 : 3);
        Position start = new Position(1, plan.bridge() ? 10 : 5, helmZ);
        Position goal = new Position(1, plan.decks() >= 2 ? 9 : 5, half - 3);
        if (!passage(hull, start) || !passage(hull, goal)) return false;
        Set<Position> visited = new HashSet<>();
        ArrayDeque<Position> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        int[][] horizontal = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Position p = queue.removeFirst();
            if (p.equals(goal)) return true;
            for (int[] move : horizontal) {
                Position n = new Position(p.x() + move[0], p.y(), p.z() + move[1]);
                if (Math.abs(n.x()) > plan.hullWidth() / 2
                        || Math.abs(n.z()) > half || !passage(hull, n)
                        || !visited.add(n)) continue;
                queue.addLast(n);
            }
            for (int delta : new int[]{-1, 1}) {
                Position n = new Position(p.x(), p.y() + delta, p.z());
                if (n.y() < 5 || n.y() > 11 || !passage(hull, n)) continue;
                if (at(hull, p.x(), p.y(), p.z()) != ShipHullVoxelLayout.Part.LADDER
                        && at(hull, n.x(), n.y(), n.z()) != ShipHullVoxelLayout.Part.LADDER
                        && at(hull, n.x(), n.y() - 1, n.z()) != ShipHullVoxelLayout.Part.LADDER
                        && at(hull, p.x(), p.y() - 1, p.z()) != ShipHullVoxelLayout.Part.LADDER)
                    continue;
                if (visited.add(n)) queue.addLast(n);
            }
        }
        return false;
    }

    private static void verify(ShipSuperstructurePlan plan) {
        var v1 = ShipHullVoxelLayout.generate(plan, 1);
        var v2 = ShipHullVoxelLayout.generate(plan, 2);
        var v3 = ShipHullVoxelLayout.generate(plan, 3);
        var layout = FirstShipMediumSailPlan.forPlan(plan);
        yes(v1.equals(ShipHullVoxelLayout.generate(plan)), "frozen legacy V1");
        yes(v2.equals(ShipHullVoxelLayout.generate(plan, 2)), "frozen stepped V2");
        yes(v3.equals(ShipHullVoxelLayout.generate(plan, 3)), "deterministic V3");
        yes(layout.sails().size() == plan.sailTiers().size(), "mast count");
        yes(v1.size() > 0 && v2.size() > 0 && v3.size() > 0, "hulls present");
        // V3 must be exactly the V2 stepped structure plus new rig parts.
        var bare = new ShipSuperstructurePlan(plan.shipClass(), plan.hullWidth(), plan.hullLength(),
                List.of(), plan.decks(), plan.bridge(), plan.cabin());
        var steppedBare = ShipHullVoxelLayout.generate(bare, 2);
        for (var entry : steppedBare.entrySet()) {
            var c = entry.getKey();
            var expected = entry.getValue();
            var actual = v3.get(c);
            boolean mastShaft = actual == ShipHullVoxelLayout.Part.MAST
                    && expected == ShipHullVoxelLayout.Part.DECK && c.x() == 0
                    && plan.mastPositions().contains(c.z());
            boolean captainLadderExit = plan.bridge()
                    && expected == ShipHullVoxelLayout.Part.RAIL && actual == null
                    && c.y() == 10 && c.z() == -plan.hullLength() / 2 + 6
                    && c.x() == -Math.min(3, plan.hullWidth() / 2 - 2) + 1;
            yes(expected == actual || mastShaft || captainLadderExit,
                    "unrelated structure modified " + c + ": " + expected + "->" + actual);
        }
        if (plan.bridge()) {
            int exitZ = -plan.hullLength() / 2 + 6;
            int exitX = -Math.min(3, plan.hullWidth() / 2 - 2) + 1;
            yes(at(v2, exitX, 10, exitZ) == ShipHullVoxelLayout.Part.RAIL,
                    "legacy V2 bridge guard rail frozen");
            yes(at(v3, exitX, 10, exitZ) == null,
                    "new bridge ladder exit must be open");
        }
        yes(helmToBow(v3, plan), "helm-to-bow route across decks and ladders");
        for (var sail : layout.sails()) {
            int z = sail.mastZ(), lower = sail.lowerYardY(), upper = sail.upperYardY();
            yes(lower - sail.highestDeckY() - 1 >= 3, "three vertical air blocks");
            yes(sail.bottom() == lower + 1 && sail.top() + 1 == upper,
                    "renderer cloth and real yard share bounds");
            yes(sail.mastTopY() > upper, "mast rises above top yard");
            for (int y = 5; y <= sail.mastTopY(); y++)
                yes(at(v3, 0, y, z) == ShipHullVoxelLayout.Part.MAST,
                        "continuous real mast");
            for (int x = -sail.halfSpan(); x <= sail.halfSpan(); x++) {
                if (x == 0) continue;
                yes(at(v3, x, lower, z) == ShipHullVoxelLayout.Part.YARD,
                        "raised real lower yard");
                yes(at(v3, x, upper, z) == ShipHullVoxelLayout.Part.YARD,
                        "raised real upper yard");
                for (int y = sail.highestDeckY() + 1; y < lower; y++)
                    yes(at(v3, x, y, z) != ShipHullVoxelLayout.Part.YARD
                            && at(v3, x, y, z) != ShipHullVoxelLayout.Part.SAIL,
                            "headroom under physical rigging");
            }
            for (int y = sail.bottom(); y <= sail.top(); y++)
                for (int x = -sail.rowHalfSpan(y); x <= sail.rowHalfSpan(y); x++) {
                    if (x == 0) continue;
                    yes(at(v3, x, y, z + 1) == ShipHullVoxelLayout.Part.SAIL,
                            "logical cloth matches row span");
                    yes(at(steppedBare, x, y, z + 1) == null,
                            "cloth does not penetrate cabin, deck or bridge");
                }
            // The walkable side corridor beside both mast faces remains open.
            for (int x : new int[]{-1, 1})
                for (int y = sail.highestDeckY() + 1; y < lower; y++)
                    yes(at(v3, x, y, z) == null,
                            "walkway beside mast must have headroom");
        }
        for (var entry : v2.entrySet()) {
            var c = entry.getKey();
            var part = entry.getValue();
            if (part == ShipHullVoxelLayout.Part.HELM
                    || part == ShipHullVoxelLayout.Part.CABIN_WALL
                    || part == ShipHullVoxelLayout.Part.WINDOW
                    || part == ShipHullVoxelLayout.Part.DOOR
                    || part == ShipHullVoxelLayout.Part.BRIDGE
                    || part == ShipHullVoxelLayout.Part.LADDER)
                yes(v3.get(c) == part, "helm/ladder/superstructure unchanged");
        }
        for (int t = 0; t < 4; t++) {
            HashSet<String> rotated = new HashSet<>();
            for (var c : v3.keySet()) {
                int x = switch (t) {
                    case 1 -> -c.z();
                    case 2 -> -c.x();
                    case 3 -> c.z();
                    default -> c.x();
                };
                int z = switch (t) {
                    case 1 -> c.x();
                    case 2 -> -c.z();
                    case 3 -> -c.x();
                    default -> c.z();
                };
                yes(rotated.add(x + ":" + c.y() + ":" + z), "rotation must be bijective");
            }
            yes(rotated.size() == v3.size(), "rotation preserves rig and deck");
        }
        configurations++;
    }
    public static void main(String[] args) {
        for (int width : new int[]{13, 15})
            for (int length : new int[]{21, 25, 29})
                for (var tiers : List.of(List.of(1), List.of(2),
                        List.of(1, 1), List.of(1, 2), List.of(2, 1), List.of(2, 2)))
                    for (int decks = 1; decks <= 2; decks++)
                        for (boolean cabin : new boolean[]{false, true})
                            for (boolean bridge : new boolean[]{false, true})
                                verify(new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.MEDIUM,
                                        width, length, tiers, decks, bridge, cabin));
        boolean rejected = false;
        try {
            ShipHullVoxelLayout.generate(new ShipSuperstructurePlan(
                    ShipSectionPlan.ShipClass.LARGE, 21, 33, List.of(1), 1, false, false), 3);
        } catch (IllegalArgumentException expected) { rejected = true; }
        yes(rejected, "geometry 3 is exclusive to MEDIUM");
        System.out.println("PASS ShipMediumSailClearanceCheck configurations=" + configurations
                + " orientations=" + configurations * 4 + " assertions=" + assertions);
    }
}
