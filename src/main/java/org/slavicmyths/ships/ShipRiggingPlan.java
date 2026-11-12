package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One deterministic sail/yard/mast layout for both moving rendering and
 * solid voxel construction. All dimensions are in real ship block coordinates.
 */
public final class ShipRiggingPlan {
    public record Yard(int y, int span) {}
    public record Panel(int bottom, int top, int span) {
        public int rowSpan(int y) {
            if (y < bottom || y > top)
                throw new IllegalArgumentException("Outside sail fabric");
            return Math.max(1, span - (y == bottom || y == top ? 1 : 0));
        }
    }
    public record Mast(int z, int topY, int strength, List<Yard> yards, List<Panel> panels) {
        public Mast {
            yards = List.copyOf(yards);
            panels = List.copyOf(panels);
        }
    }
    public record Layout(List<Mast> masts) {
        public Layout { masts = List.copyOf(masts); }
    }

    public static Layout forPlan(ShipSuperstructurePlan plan, int geometry) {
        if (plan == null) throw new IllegalArgumentException("Missing ship structure");
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull =
                ShipHullVoxelLayout.generateLegacy(plan, geometry);
        List<Mast> result = new ArrayList<>();
        for (int i = 0; i < plan.sailTiers().size(); i++) {
            int tier = plan.sailTiers().get(i), z = plan.mastPositions().get(i);
            ShipBuildConfiguration.Rigging equipment = plan.rigging().get(i);
            int span = Math.min(plan.hullWidth() / 2 + 2,
                    2 + tier + equipment.width());
            int deck = 4;
            for (var entry : hull.entrySet()) {
                ShipHullVoxelLayout.Cell cell = entry.getKey();
                ShipHullVoxelLayout.Part part = entry.getValue();
                if ((part == ShipHullVoxelLayout.Part.DECK
                        || part == ShipHullVoxelLayout.Part.BRIDGE)
                        && Math.abs(cell.x()) <= span
                        && (cell.z() == z || cell.z() == z + 1))
                    deck = Math.max(deck, cell.y());
            }
            int panelHeight = 4 + tier + equipment.area() * 2;
            int start = deck + 5;
            List<Panel> panels = new ArrayList<>();
            Map<Integer, Yard> yards = new LinkedHashMap<>();
            for (int layer = 0; layer < equipment.layers(); layer++) {
                int top = start + panelHeight - 1;
                panels.add(new Panel(start, top, span));
                yards.put(start - 1, new Yard(start - 1, span));
                yards.put(top + 1, new Yard(top + 1, span));
                start = top + 2;
            }
            int highestYard = start - 1;
            while (yards.size() < equipment.yards()) {
                highestYard += 3;
                yards.put(highestYard, new Yard(highestYard, span));
            }
            int upper = yards.keySet().stream().mapToInt(Integer::intValue).max().orElse(deck + 5);
            int mastTop = upper + 2 + equipment.height() * 2;
            if (mastTop > 32)
                throw new IllegalArgumentException("Sail rig exceeds 32-block vertical bound");
            result.add(new Mast(z, mastTop, equipment.strength(),
                    List.copyOf(yards.values()), panels));
        }
        return new Layout(result);
    }

    private ShipRiggingPlan() {}
}
