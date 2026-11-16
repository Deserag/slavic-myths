package org.slavicmyths.ships;

import java.util.List;
import java.util.Map;

/**
 * Pure-Java IV regression check. No Minecraft, loader, JUnit or world required.
 *
 * Run from repository root (JDK 21):
 * javac -encoding UTF-8 -d .ship-iv-checks src/main/java/org/slavicmyths/ships/ShipSectionPlan.java src/main/java/org/slavicmyths/ships/ShipSuperstructurePlan.java src/main/java/org/slavicmyths/ships/ShipHullVoxelLayout.java src/main/java/org/slavicmyths/ships/ShipRigidMotion.java tools/ship-iv-checks/ShipIVHeadless.java
 * java -cp .ship-iv-checks org.slavicmyths.ships.ShipIVHeadless
 */
public final class ShipIVHeadless {
    private static final ShipSectionPlan.ShipClass TYPE =
            ShipSectionPlan.ShipClass.LARGEST;
    private static int configurations;
    private static int checkedSteps;
    private static int checkedHullCells;

    private static void require(boolean pass, String message) {
        if (!pass) throw new AssertionError(message);
    }

    private static ShipHullVoxelLayout.Part at(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            int x, int y, int z) {
        return blocks.get(new ShipHullVoxelLayout.Cell(x, y, z));
    }

    private static void stair(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            int x, int startZ, int lower, int upper, String label) {
        require(upper - lower == 5, "Unexpected IV stair height");
        var start = at(blocks, x, lower, startZ);
        var finish = at(blocks, x, upper, startZ + 6);
        require(start == ShipHullVoxelLayout.Part.DECK
                || start == ShipHullVoxelLayout.Part.BRIDGE
                || lower == 1 && start == ShipHullVoxelLayout.Part.HULL,
                label + " has no walkable lower support");
        require(finish == ShipHullVoxelLayout.Part.DECK
                || finish == ShipHullVoxelLayout.Part.BRIDGE,
                label + " has no walkable upper landing");
        for (int i = 0; i <= 6; i++) {
            int z = startZ + i;
            int y = i == 0 ? lower : i == 6 ? upper : lower + i;
            if (i > 0 && i < 6)
                require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.STAIR,
                        label + " missing step " + i);
            require(at(blocks, x, y + 1, z) == null,
                    label + " head collision at " + x + "," + (y + 1) + "," + z);
            require(at(blocks, x, y + 2, z) == null,
                    label + " upper head collision at " + x + "," + (y + 2) + "," + z);
            checkedSteps++;
        }
    }

    private static void geometry(int length, int width, int decks,
            boolean bridge, boolean cabin, int tier) {
        List<Integer> rig = List.of(tier, tier, tier, tier);
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(
                TYPE, width, length, rig, decks, bridge, cabin, 4);
        var blocks = ShipHullVoxelLayout.generate(plan);
        String key = length + "x" + width + " decks=" + decks
                + " bridge=" + bridge + " cabin=" + cabin + " tier=" + tier;
        require(plan.mastPositions().size() == 4, key + ": mast count");
        int end = length / 2;
        int cabinFront = -end + 10;
        for (int z : plan.mastPositions()) {
            require(z > cabinFront, key + ": mast intersects stern cabin z=" + z);
            require(z < end - 12, key + ": mast overlaps rising bow stem z=" + z);
            require(at(blocks, 0, 12, z) == ShipHullVoxelLayout.Part.MAST,
                    key + ": mast missing near deck z=" + z);
        }
        // +Z is the bow and must remain visibly substantial: the final
        // five-block-wide stem rises above the main deck. No fake bow
        // appears at the stern, and the narrowing remains monotonic.
        require(ShipHullVoxelLayout.halfBeam(plan, -end)
                > ShipHullVoxelLayout.halfBeam(plan, end),
                key + ": stern should be fuller than the bow tip");
        require(ShipHullVoxelLayout.halfBeam(plan, end) == 2,
                key + ": undersized or misplaced forward stem");
        require(ShipHullVoxelLayout.halfBeam(plan, end - 4)
                >= width / 2 - 6, key + ": bow shoulders are too narrow");
        require(at(blocks, 0, 12, end) == ShipHullVoxelLayout.Part.HULL
                && at(blocks, 0, 15, end) == ShipHullVoxelLayout.Part.TRIM,
                key + ": raised forward bow tip is missing");
        require(at(blocks, 0, 15, -end) == null,
                key + ": raised bow was generated at the stern");
        for (int z = end - 11; z <= end; z++)
            require(ShipHullVoxelLayout.halfBeam(plan, z)
                    <= ShipHullVoxelLayout.halfBeam(plan, z - 1),
                    key + ": bow has a reverse flare at station " + z);
        for (int z = -end; z <= end; z++) {
            int beam = ShipHullVoxelLayout.halfBeam(plan, z);
            require(at(blocks, -beam, 1, z) == at(blocks, beam, 1, z),
                    key + ": asymmetric bottom at station " + z);
        }
        // A real plank must cover every station of the under-waterline keel.
        // No diagonal-only seam may connect the sea to the hold.
        for (int z = -end; z <= end; z++) {
            int beam = ShipHullVoxelLayout.halfBeam(plan, z);
            int before = ShipHullVoxelLayout.halfBeam(plan, z - 1);
            int after = ShipHullVoxelLayout.halfBeam(plan, z + 1);
            require(beam >= 1, key + ": negative station beam");
            for (int x = -beam; x <= beam; x++) {
                require(at(blocks, x, 1, z) == ShipHullVoxelLayout.Part.HULL,
                        key + ": absent bottom at " + x + ",1," + z);
                int distance = Math.abs(x);
                boolean exterior = distance >= beam - 1
                        || before < 0 || after < 0
                        || distance > before || distance > after;
                if (exterior)
                    for (int y = 2; y <= 5; y++)
                        require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.HULL,
                                key + ": exposed bilge at " + x + "," + y + "," + z);
                checkedHullCells++;
            }
        }
        // IV knyavdiged is a single load-bearing continuation of the keel:
        // +18 blocks of forward run with at most eight blocks of center rise.
        require(at(blocks, 0, 0, end - 12) == ShipHullVoxelLayout.Part.KEEL
                && at(blocks, 0, 0, end) == ShipHullVoxelLayout.Part.KEEL
                && at(blocks, 0, 0, end + 1) == ShipHullVoxelLayout.Part.KEEL,
                key + ": lower knyavdiged is not rooted in the keel");
        require(at(blocks, 0, 1, end) == ShipHullVoxelLayout.Part.HULL,
                key + ": stem displaced the sealed bow bottom");
        require(at(blocks, 0, 9, end + 18) == ShipHullVoxelLayout.Part.KEEL,
                key + ": the long forward tip is missing");
        require(at(blocks, 0, 15, end + 8) == null,
                key + ": the old upward appendix was left in place");
        int previousRise = (int)Math.round(4D * (1D
                - Math.cos(12D / 30D * Math.PI)));
        for (int z = end + 1; z <= end + 18; z++) {
            double progress = (z - (end - 12)) / 30D;
            int rise = (int)Math.round(4D * (1D - Math.cos(progress * Math.PI)));
            require(rise >= previousRise && rise <= previousRise + 1,
                    key + ": kink in the forward keel curve at " + z);
            int lowerY = z <= end + 3 ? 0
                    : Math.max(0, Math.min(previousRise, rise) - 1);
            int upperY = Math.max(rise + 1, 15 - (int)Math.round(6D
                    * Math.sin((z - end) / 18D * Math.PI / 2D)));
            int radius = z <= end + 3 ? 2 : z <= end + 12 ? 1 : 0;
            for (int y = lowerY; y <= upperY; y++) {
                require(at(blocks, 0, y, z) == ShipHullVoxelLayout.Part.KEEL,
                        key + ": empty gap through the knyavdiged at z=" + z
                                + " y=" + y);
                if (y >= 2)
                    for (int x : new int[]{-radius, radius})
                        require(at(blocks, x, y, z) == (x == 0
                                        ? ShipHullVoxelLayout.Part.KEEL
                                        : ShipHullVoxelLayout.Part.FRAME),
                                key + ": detached bow scarf at x=" + x + " z=" + z
                                        + " y=" + y);
            }
            previousRise = rise;
        }
        // Only real KEEL cells can prove continuity; unrelated hull plates
        // and decks must not conceal a disconnected decorative appendage.
        var visited = new java.util.HashSet<ShipHullVoxelLayout.Cell>();
        var queue = new java.util.ArrayDeque<ShipHullVoxelLayout.Cell>();
        var root = new ShipHullVoxelLayout.Cell(0, 0, end);
        var tip = new ShipHullVoxelLayout.Cell(0, 9, end + 18);
        visited.add(root);
        queue.add(root);
        while (!queue.isEmpty() && !visited.contains(tip)) {
            var pos = queue.removeFirst();
            for (int[] delta : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                int y = pos.y() + delta[0], z = pos.z() + delta[1];
                if (y < 0 || y > 15 || z < end || z > end + 18)
                    continue;
                var next = new ShipHullVoxelLayout.Cell(0, y, z);
                if (at(blocks, 0, y, z) != ShipHullVoxelLayout.Part.KEEL
                        || !visited.add(next)) continue;
                queue.addLast(next);
            }
        }
        require(visited.contains(tip),
                key + ": forward stem is not connected to the keel by block faces");
        int shaftZ = -end + 2;
        for (int[] route : new int[][] {
                {-3, shaftZ, 1, 6}, {3, shaftZ, 6, 11},
                {-3, shaftZ, 11, 16}
        }) stair(blocks, route[0], route[1], route[2], route[3], key);
        if (decks >= 2 || bridge)
            stair(blocks, 3, shaftZ, 16, 21, key);
        for (var entry : blocks.entrySet()) {
            if (entry.getValue() != ShipHullVoxelLayout.Part.LANTERN) continue;
            var cell = entry.getKey();
            var support = at(blocks, cell.x(), cell.y() - 1, cell.z());
            require(support == ShipHullVoxelLayout.Part.HULL
                    || support == ShipHullVoxelLayout.Part.FRAME
                    || support == ShipHullVoxelLayout.Part.DECK
                    || support == ShipHullVoxelLayout.Part.BRIDGE,
                    key + ": floating lantern " + cell);
        }
        configurations++;
    }

    private static void upgrades() {
        // A deterministic path through all nine requested development phases.
        // Layout changes must be measurable, while propulsion improvements
        // must not reduce performance of a four-masted ship.
        int[][] phases = {
                {51, 21, 1, 0, 0, 1}, {55, 21, 1, 0, 0, 1},
                {55, 23, 1, 0, 0, 1}, {59, 23, 1, 0, 0, 1},
                {59, 23, 1, 0, 0, 2}, {59, 23, 2, 0, 0, 2},
                {59, 23, 2, 0, 1, 2}, {59, 23, 2, 1, 1, 2},
                {63, 25, 3, 1, 1, 3}
        };
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> previous = null;
        double previousThrust = 0D;
        for (int i = 0; i < phases.length; i++) {
            int[] phase = phases[i];
            var plan = new ShipSuperstructurePlan(TYPE, phase[1], phase[0],
                    List.of(phase[5], phase[5], phase[5], phase[5]),
                    phase[2], phase[3] == 1, phase[4] == 1, 4);
            var next = ShipHullVoxelLayout.generate(plan);
            require(previous == null || !previous.equals(next),
                    "IV upgrade phase " + (i + 1) + " has no geometry change");
            double thrust = plan.propulsionFactor();
            require(thrust >= previousThrust, "IV propulsion regresses at phase " + (i + 1));
            previousThrust = thrust;
            previous = next;
        }
        double basic = new ShipSuperstructurePlan(TYPE, 21, 51,
                List.of(1, 1, 1, 1), 1, false, false, 4).propulsionFactor();
        double full = new ShipSuperstructurePlan(TYPE, 25, 63,
                List.of(3, 3, 3, 3), 3, true, true, 4).propulsionFactor();
        require(full > basic * 1.25D, "IV upgraded sails do not deliver a clear speed increase");
    }

    private static void motion() {
        var fast = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D),
                35D, ShipRigidMotion.MAX_IV_FORWARD);
        var normal = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D), 20D);
        for (int i = 0; i < 200; i++) {
            fast.advance(new ShipRigidMotion.Control(1D, 0D), pose -> true);
            normal.advance(new ShipRigidMotion.Control(1D, 0D), pose -> true);
        }
        require(fast.speed() > normal.speed() * 2D,
                "IV ship has no meaningful speed advantage");
        require(fast.speed() <= ShipRigidMotion.MAX_IV_FORWARD,
                "IV speed exceeds its bound");
        require(normal.speed() <= ShipRigidMotion.MAX_FORWARD,
                "Legacy ship speed exceeds its bound");
        require(fast.pose().z() > normal.pose().z(),
                "IV hull does not travel farther under identical input");

        // Exercise the same throttle scaling used by FirstShipEntity rather
        // than checking only a theoretical sail factor in isolation.
        var basicPlan = new ShipSuperstructurePlan(TYPE, 21, 51,
                List.of(1, 1, 1, 1), 1, false, false, 4);
        var upgradedPlan = new ShipSuperstructurePlan(TYPE, 25, 63,
                List.of(3, 3, 3, 3), 3, true, true, 4);
        var basicMotion = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D),
                35D, ShipRigidMotion.MAX_IV_FORWARD);
        var upgradedMotion = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D),
                35D, ShipRigidMotion.MAX_IV_FORWARD);
        for (int i = 0; i < 200; i++) {
            basicMotion.advance(new ShipRigidMotion.Control(
                    Math.max(.16D, basicPlan.propulsionFactor()), 0D), pose -> true);
            upgradedMotion.advance(new ShipRigidMotion.Control(
                    Math.max(.16D, upgradedPlan.propulsionFactor()), 0D), pose -> true);
        }
        require(basicMotion.speed() > normal.speed(),
                "Unmodified IV is slower than the legacy full-speed vessel");
        require(upgradedMotion.speed() > basicMotion.speed() * 1.35D,
                "Upgraded IV sails do not produce a meaningful real speed increase");
        require(upgradedMotion.pose().z() > basicMotion.pose().z() * 1.2D,
                "IV sail upgrades do not improve actual distance travelled");
    }

    private static void collisionAndRestore() {
        var motion = new ShipRigidMotion(new ShipRigidMotion.Pose(0D, 0D, 0D),
                35D, ShipRigidMotion.MAX_IV_FORWARD);
        for (int i = 0; i < 25; i++)
            motion.advance(new ShipRigidMotion.Control(1D, 1D), pose -> true);
        var before = motion.pose();
        var impact = motion.advance(new ShipRigidMotion.Control(1D, 1D),
                pose -> false);
        require(impact.blocked(), "IV hull passed an impassable obstacle");
        require(motion.pose().equals(before), "Blocked collision changed ship position");
        require(motion.speed() == 0D && motion.angularVelocity() == 0D,
                "Blocked collision retained dangerous momentum");
        var savedPose = new ShipRigidMotion.Pose(10D, -8D, 73D);
        motion.restore(savedPose, .65D, .3D);
        require(motion.pose().equals(savedPose), "Restore changed saved pose");
        require(Math.abs(motion.speed() - .65D) < 1E-9,
                "Restore lost valid IV speed");
        motion.restore(savedPose, 200D, 200D);
        require(motion.speed() <= ShipRigidMotion.MAX_IV_FORWARD,
                "Corrupt save bypassed IV speed limit");
        require(Math.abs(motion.angularVelocity()) <= .85D,
                "Corrupt save bypassed IV rudder limit");
    }

    public static void main(String[] args) {
        for (int length : new int[]{51, 55, 59, 63})
            for (int width : new int[]{21, 23, 25})
                for (int decks = 1; decks <= 3; decks++)
                    for (boolean bridge : new boolean[]{false, true})
                        for (boolean cabin : new boolean[]{false, true})
                            for (int tier : new int[]{1, 2, 3})
                                geometry(length, width, decks, bridge, cabin, tier);
        upgrades();
        motion();
        collisionAndRestore();
        System.out.println("PASS: " + configurations + " IV configurations; "
                + checkedSteps + " stair stations; "
                + checkedHullCells + " bottom station probes; IV vs legacy motion");
    }

    private ShipIVHeadless() {}
}
