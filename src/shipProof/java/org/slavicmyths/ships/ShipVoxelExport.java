package org.slavicmyths.ships;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ShipVoxelExport {
    private static void append(StringBuilder csv, String name, ShipSuperstructurePlan plan) {
        for (var block : ShipHullVoxelLayout.generate(plan).entrySet()) {
            var point = block.getKey();
            csv.append(name).append(',').append(plan.hullWidth()).append(',')
                    .append(plan.hullLength()).append(',').append(point.x()).append(',')
                    .append(point.y()).append(',').append(point.z()).append(',')
                    .append(block.getValue()).append('\n');
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("CSV output file required");
        StringBuilder csv = new StringBuilder("class,width,length,x,y,z,part\n");
        append(csv, "SMALL", new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.SMALL, 7, 11, List.of(1), 1, false, false));
        append(csv, "MEDIUM", new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.MEDIUM, 13, 21, List.of(2, 2), 2, true, true));
        append(csv, "LARGE", new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.LARGE, 21, 33, List.of(3, 3, 3), 3, true, true));
        append(csv, "LARGEST", new ShipSuperstructurePlan(
                ShipSectionPlan.ShipClass.LARGEST, 21, 33, List.of(3, 3, 3, 3), 3, true, true));
        Files.writeString(Path.of(args[0]), csv);
        System.out.println("Exported " + csv.toString().lines().count() + " rows");
    }
    private ShipVoxelExport() {}
}
