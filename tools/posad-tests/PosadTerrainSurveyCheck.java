package org.slavicmyths.posad;

import java.util.ArrayList;
import java.util.List;

/** Exact-parity and read-budget contract. Pure Java 21, not a native seed or TPS test. */
public final class PosadTerrainSurveyCheck {
    private record Scenario(String name, PosadTerrain.Columns columns, int x, int y, int z, int rotation) {}
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void verify(Scenario scenario, int budget) {
        PosadTerrain.Decision reference = PosadTerrain.inspect(scenario.columns(), scenario.x(), scenario.y(), scenario.z(), scenario.rotation());
        PosadTerrainSurvey survey = new PosadTerrainSurvey(scenario.columns(), scenario.x(), scenario.y(), scenario.z(), scenario.rotation());
        int ticks = 0;
        do {
            int before = survey.inspected();
            boolean finished = survey.advance(budget);
            int performed = survey.inspected() - before;
            require(performed >= 1 && performed <= budget, scenario.name() + ": per-tick terrain reads exceeded " + budget + " at step " + ticks);
            require(finished == survey.completed(), scenario.name() + ": inconsistent finished state");
            require(++ticks <= 300_000, scenario.name() + ": nonterminating survey");
        } while (!survey.completed());
        require(reference.equals(survey.result()), scenario.name() + ": decision/bridge/support/rotation mismatch for budget=" + budget + ", reference=" + reference + " incremental=" + survey.result());
        require(reference.inspected() == survey.inspected(), scenario.name() + ": read count mismatch");
        require(survey.advance(budget), scenario.name() + ": completed survey must remain completed");
    }

    public static void main(String[] args) {
        List<Scenario> scenarios = new ArrayList<>();
        for (int rotation = 0; rotation < 4; rotation++) {
            scenarios.add(new Scenario("land-" + rotation, (x, y, z) -> y < 64 ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.AIR, -57, 64, -57, rotation));
            PosadTerrain.Plan plan = new PosadTerrain.Plan(0, 64, 0, rotation, 24, List.of());
            PosadGeometry.Cell bank = plan.world(56, 136);
            final int orientation = rotation;
            scenarios.add(new Scenario("lava-" + rotation, (x, y, z) -> {
                if (y >= 64) return PosadTerrain.Material.AIR;
                boolean shore = switch (orientation) {
                    case 0 -> z >= bank.z();
                    case 1 -> x <= bank.x();
                    case 2 -> z <= bank.z();
                    default -> x >= bank.x();
                };
                return shore || y < 40 ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.LAVA;
            }, 0, 64, 0, rotation));
        }
        scenarios.add(new Scenario("ceiling", (x, y, z) -> y == 65 ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.AIR, 0, 64, 0, 0));
        scenarios.add(new Scenario("protected-platform", (x, y, z) -> x == 17 && y == 63 && z == 19 ? PosadTerrain.Material.PROTECTED : y < 64 ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.AIR, 0, 64, 0, 0));
        scenarios.add(new Scenario("no-footing", (x, y, z) -> PosadTerrain.Material.AIR, 0, 64, 0, 0));
        scenarios.add(new Scenario("bank-blocked", (x, y, z) -> y > 64 ? PosadTerrain.Material.AIR : (x < 0 || x > 112 || z < 0 || z > 112) ? PosadTerrain.Material.LAVA : PosadTerrain.Material.NATURAL, 0, 64, 0, 0));
        scenarios.add(new Scenario("bridge-guarded", (x, y, z) -> y == 64 && (x < 0 || x > 112 || z < 0 || z > 112) ? PosadTerrain.Material.PROTECTED : y < 64 ? PosadTerrain.Material.NATURAL : PosadTerrain.Material.AIR, 0, 64, 0, 2));

        for (Scenario scenario : scenarios) {
            for (int budget : new int[]{1, 97, 4096}) verify(scenario, budget);
        }
        try {
            new PosadTerrainSurvey((x, y, z) -> PosadTerrain.Material.AIR, 0, 64, 0, 0).advance(0);
            throw new AssertionError("Zero scan budget was accepted");
        } catch (IllegalArgumentException expected) {
            // A job must not silently advance with an unbounded or zero budget.
        }
        System.out.println("PASS PosadTerrainSurvey: " + scenarios.size() + " scenarios x 3 budgets, exact native decision, support coordinates and read counts. NOT a TPS/seed/native-place test.");
    }

    private PosadTerrainSurveyCheck() {}
}
