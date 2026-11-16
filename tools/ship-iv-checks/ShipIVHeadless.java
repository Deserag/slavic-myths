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
    private static int checkedBrigantineSteps;
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
        var finish = at(blocks, x, upper, startZ - 6);
        require(start == ShipHullVoxelLayout.Part.DECK
                || start == ShipHullVoxelLayout.Part.BRIDGE
                || lower == 1 && start == ShipHullVoxelLayout.Part.HULL,
                label + " has no walkable lower support");
        require(finish == ShipHullVoxelLayout.Part.DECK
                || finish == ShipHullVoxelLayout.Part.BRIDGE,
                label + " has no walkable upper landing");
        for (int i = 0; i <= 6; i++) {
            int z = startZ - i;
            int y = i == 0 ? lower : i == 6 ? upper : lower + i;
            if (i > 0 && i < 6)
                require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.STAIR,
                        label + " missing step " + i);
            require(at(blocks, x, y + 1, z) == null,
                    label + " head collision at " + x + "," + (y + 1) + "," + z);
            require(at(blocks, x, y + 2, z) == null,
                    label + " upper head collision at " + x + "," + (y + 2) + "," + z);
            require(at(blocks, x, y + 3, z) == null,
                    label + " third head collision at " + x + "," + (y + 3) + "," + z);
            checkedSteps++;
        }
    }

    /**
     * Every floor has its own ceiling-height lighting row. Along that row
     * successive lantern stations leave at least four unoccupied Z cells,
     * even if a different lateral track was selected.
     */
    private static void lanternRows(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            String label, boolean frigate) {
        Map<Integer, List<ShipHullVoxelLayout.Cell>> rows = new java.util.HashMap<>();
        int count = 0;
        for (var entry : blocks.entrySet()) {
            var part = entry.getValue();
            if (part != ShipHullVoxelLayout.Part.HANGING_LANTERN
                    && part != ShipHullVoxelLayout.Part.LANTERN) continue;
            var cell = entry.getKey();
            // IV helm posts deliberately carry standing lanterns, while
            // interiors remain suspended from their ceiling.
            boolean correctHeight = frigate
                    && part == ShipHullVoxelLayout.Part.LANTERN && cell.y() == 29;
            for (int floor : frigate
                    ? new int[]{1, 6, 11, 16, 21}
                    : new int[]{1, 5, 10, 15, 20})
                if (cell.y() == (floor == 1 && !frigate ? 4 : floor + 4))
                    correctHeight = true;
            require(correctHeight, label + ": lantern at inconsistent Y " + cell);
            var support = at(blocks, cell.x(),
                    cell.y() + (part == ShipHullVoxelLayout.Part.LANTERN ? -1 : 1),
                    cell.z());
            require(support == ShipHullVoxelLayout.Part.DECK
                    || support == ShipHullVoxelLayout.Part.BRIDGE
                    || support == ShipHullVoxelLayout.Part.HULL
                    || support == ShipHullVoxelLayout.Part.FRAME
                    || support == ShipHullVoxelLayout.Part.TRIM,
                    label + ": lantern has no ceiling anchor " + cell);
            var row = rows.computeIfAbsent(cell.y(),
                    unused -> new java.util.ArrayList<>());
            for (var other : row)
                require(Math.max(Math.abs(cell.x() - other.x()),
                        Math.abs(cell.z() - other.z())) >= (frigate ? 3 : 5),
                        label + ": lanterns too close at " + cell + " and " + other);
            row.add(cell);
            count++;
        }
        require(count >= 2, label + ": missing interior lanterns");
    }

    private static void geometry(int length, int width, int decks,
            boolean bridge, boolean cabin, int tier) {
        List<Integer> rig = List.of(tier, tier, tier, tier);
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(
                TYPE, width, length, rig, decks, bridge, cabin, 4);
        var blocks = ShipHullVoxelLayout.generate(plan);
        String key = length + "x" + width + " decks=" + decks
                + " bridge=" + bridge + " cabin=" + cabin + " tier=" + tier;
        long guardrails = blocks.values().stream().filter(part ->
                part == ShipHullVoxelLayout.Part.RAIL).count();
        require(guardrails >= 30, key + ": missing open-deck safety rails");
        int rear = -length / 2 + 4;
        require(at(blocks, 0, 12, rear + 15) == ShipHullVoxelLayout.Part.DOOR
                && at(blocks, 0, 13, rear + 15) == ShipHullVoxelLayout.Part.DOOR,
                key + ": captain door no longer has paired halves");
        int wheelFloor = decks >= 2 || bridge ? 21 : 16;
        require(at(blocks, 0, wheelFloor + 1, rear + 4)
                    == ShipHullVoxelLayout.Part.HELM
                && at(blocks, 0, wheelFloor + 1, rear + 5) == null,
                key + ": the sole wheel is not accessible");
        require(at(blocks, 0, wheelFloor + 1, rear + 1)
                    == ShipHullVoxelLayout.Part.TRIM,
                key + ": missing timber helm screen");
        for (int x : new int[]{-4, 4})
            require(at(blocks, x, wheelFloor + 4, rear + 1)
                    == ShipHullVoxelLayout.Part.LANTERN,
                    key + ": helm lighting post missing");
        int station = rear + 20;
        int outside = ShipHullVoxelLayout.halfBeam(plan, station);
        for (int x : new int[]{-outside, outside})
            require(at(blocks, x, 12, station)
                    == ShipHullVoxelLayout.Part.RAIL,
                    key + ": weather rail still sits two blocks inside");
        require(blocks.values().stream().filter(part ->
                        part == ShipHullVoxelLayout.Part.HELM).count() == 1,
                key + ": captain helm must remain after deleting fences");
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
        // The sketch specifies a forward-projecting wedge: almost level
        // on top, with the underside gradually rising toward the top edge.
        require(at(blocks, 0, 0, end) == ShipHullVoxelLayout.Part.KEEL
                && at(blocks, 0, 1, end) == ShipHullVoxelLayout.Part.HULL,
                key + ": forward heel lost its keel and sealed bottom");
        int previousUnderside = 5;
        for (int z = end + 1; z <= end + 18; z++) {
            double t = (z - end) / 18D;
            int underside = 5 + (int)Math.round(8D * t * t * (3D - 2D * t));
            int upper = z >= end + 13 ? 16 : 15;
            int halfStem = z <= end + 7 ? 2 : 1;
            require(underside >= previousUnderside
                    && underside <= previousUnderside + 1,
                    key + ": lower bow curve kinks at z=" + z);
            require(at(blocks, 0, underside - 1, z) == null,
                    key + ": bow hangs BELOW the intended rising underside at z=" + z);
            require(at(blocks, 0, upper + 1, z) == null,
                    key + ": bow goes above the level upper profile at z=" + z);
            for (int x = -halfStem; x <= halfStem; x++)
                for (int y = underside; y <= upper; y++) {
                    var expected = x == 0 && y <= underside + 1
                            ? ShipHullVoxelLayout.Part.KEEL
                            : y == upper ? ShipHullVoxelLayout.Part.TRIM
                            : Math.abs(x) == halfStem
                                    ? ShipHullVoxelLayout.Part.FRAME
                                    : ShipHullVoxelLayout.Part.HULL;
                    require(at(blocks, x, y, z) == expected,
                            key + ": gap in triangular bow wedge at "
                                    + x + "," + y + "," + z);
                }
            previousUnderside = underside;
        }
        require(previousUnderside == 13
                && at(blocks, 0, 16, end + 18) == ShipHullVoxelLayout.Part.TRIM,
                key + ": raised forward tip lacks proper thickness");
        // The two-layer wooden spine must be connected by block FACES,
        // never diagonally separated like isolated floating steps.
        var visited = new java.util.HashSet<ShipHullVoxelLayout.Cell>();
        var queue = new java.util.ArrayDeque<ShipHullVoxelLayout.Cell>();
        var root = new ShipHullVoxelLayout.Cell(0, 5, end + 1);
        var tip = new ShipHullVoxelLayout.Cell(0, 13, end + 18);
        visited.add(root);
        queue.add(root);
        while (!queue.isEmpty() && !visited.contains(tip)) {
            var pos = queue.removeFirst();
            for (int[] delta : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
                int y = pos.y() + delta[0], z = pos.z() + delta[1];
                if (y < 0 || y > 16 || z < end + 1 || z > end + 18)
                    continue;
                var next = new ShipHullVoxelLayout.Cell(0, y, z);
                if (at(blocks, 0, y, z) != ShipHullVoxelLayout.Part.KEEL
                        || !visited.add(next)) continue;
                queue.addLast(next);
            }
        }
        require(visited.contains(tip),
                key + ": knyavdiged spine is disconnected by block faces");
        int shaftZ = rear + 30;
        int[][] flights = {{-3, shaftZ, 1, 6},
                {-3, shaftZ - 6, 6, 11}};
        for (int[] flight : flights) {
            for (int x : new int[]{flight[0], flight[0] + 1})
                stair(blocks, x, flight[1], flight[2], flight[3], key);
            require(at(blocks, 3, flight[2] + 1, flight[1] - 1)
                    != ShipHullVoxelLayout.Part.STAIR,
                    key + ": staircase shifted to opposite side of hull");
        }
        for (int i = 1; i < flights.length; i++)
            require(flights[i - 1][1] - 6 == flights[i][1]
                    && flights[i - 1][3] == flights[i][2],
                    key + ": stair flights have a disconnected landing");
        for (int x : new int[]{3, 4})
            stair(blocks, x, rear + 21, 11, 16, key + " weather deck");
        if (decks >= 2 || bridge)
            for (int x : new int[]{-5, -4})
                stair(blocks, x, rear + 10, 16, 21, key + " captain bridge");
        require(at(blocks, -2, 17, rear + 11) != ShipHullVoxelLayout.Part.STAIR,
                key + ": old internal bridge staircase remains");
        if (decks >= 3) {
            require(at(blocks, -3, 27, rear + 2) == ShipHullVoxelLayout.Part.TRIM,
                    key + ": wheelhouse roof has no perimeter trim");
            for (int x : new int[]{-3, 3})
                require(at(blocks, x, 29, rear + 1)
                        == ShipHullVoxelLayout.Part.LANTERN,
                        key + ": roof-top corner light missing");
        }
        var preRefinement = ShipHullVoxelLayout.previousRecentFrigateIVLayout(plan);
        require(at(preRefinement, -3, 2, rear + 23)
                    == ShipHullVoxelLayout.Part.STAIR,
                key + ": prior IV stair geometry was not preserved");
        require(at(preRefinement, ShipHullVoxelLayout.halfBeam(plan, rear + 20) - 2,
                        12, rear + 20) == ShipHullVoxelLayout.Part.RAIL,
                key + ": older IV weather-deck rail geometry was not preserved");
        require(!preRefinement.equals(blocks),
                key + ": current and prior IV revisions unexpectedly identical");
        var legacy = ShipHullVoxelLayout.previousFrigateIVLayout(plan, false);
        require(legacy.values().stream().noneMatch(part ->
                part == ShipHullVoxelLayout.Part.RAIL),
                key + ": migration baseline unexpectedly has guardrails");
        require(!legacy.equals(blocks), key + ": IV retrofit did not change its topology");
        lanternRows(blocks, key, true);
        configurations++;
    }

    private static void brigantineGeometry(int length, int width, int decks,
            boolean bridge, boolean cabin) {
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.LARGE, width, length,
                List.of(1, 1, 1), decks, bridge, cabin, 3);
        var blocks = ShipHullVoxelLayout.generate(plan);
        String label = "III " + length + "x" + width + " decks=" + decks
                + " bridge=" + bridge + " cabin=" + cabin;
        int top = ShipHullVoxelLayout.brigantineTopDeck(plan);
        int[][] heights = top == 20 ? new int[][]{{1, 5}, {5, 10}, {10, 15}, {15, 20}}
                : top == 15 ? new int[][]{{1, 5}, {5, 10}, {10, 15}}
                : new int[][]{{1, 5}, {5, 10}};
        int start = -length / 2 + 2 + 6
                + 5 + 6 + (top >= 15 ? 6 : 0) + (top >= 20 ? 6 : 0);
        require(blocks.values().stream().noneMatch(p -> p == ShipHullVoxelLayout.Part.RAIL),
                label + ": unwanted deck fences");
        for (int[] flight : heights) {
            int lower = flight[0], upper = flight[1];
            int exit = start - (upper - lower + 1);
            for (int x : new int[]{-3, -2}) {
                var approach = at(blocks, x, lower, start);
                require(approach == ShipHullVoxelLayout.Part.HULL
                                || approach == ShipHullVoxelLayout.Part.DECK
                                || approach == ShipHullVoxelLayout.Part.BRIDGE,
                        label + ": no supported stair approach at z=" + start);
                for (int i = 1; i <= upper - lower; i++) {
                    int y = lower + i, z = start - i;
                    require(at(blocks, x, y, z) == ShipHullVoxelLayout.Part.STAIR,
                            label + ": missing continuous stair at " + x + "," + y + "," + z);
                    for (int h = 1; h <= 3; h++)
                        require(at(blocks, x, y + h, z) == null,
                                label + ": stair head blocked at " + x + "," + (y + h) + "," + z);
                    checkedBrigantineSteps++;
                }
                var landing = at(blocks, x, upper, exit);
                require(landing == ShipHullVoxelLayout.Part.DECK
                                || landing == ShipHullVoxelLayout.Part.BRIDGE,
                        label + ": no landing at " + x + "," + upper + "," + exit);
                require(at(blocks, 3, lower + 1, start - 1)
                                != ShipHullVoxelLayout.Part.STAIR,
                        label + ": stairs jumped to the opposite side");
            }
            start = exit; // next flight starts on the previous landing
        }
        require(start == -length / 2 + 2 + 6,
                label + ": uppermost flight does not terminate inside the stern deck");
        lanternRows(blocks, label, false);
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
        for (int[] dims : new int[][]{{55, 15}, {63, 17}, {69, 19}})
            for (int decks = 1; decks <= 3; decks++)
                for (boolean bridge : new boolean[]{false, true})
                    for (boolean cabin : new boolean[]{false, true})
                        brigantineGeometry(dims[0], dims[1], decks, bridge, cabin);
        upgrades();
        motion();
        collisionAndRestore();
        System.out.println("PASS: " + configurations + " IV configurations; "
                + checkedSteps + " IV stair stations; "
                + checkedBrigantineSteps + " III stair treads; "
                + checkedHullCells + " bottom station probes; IV vs legacy motion");
    }

    private ShipIVHeadless() {}
}
