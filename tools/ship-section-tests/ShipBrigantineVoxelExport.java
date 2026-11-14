import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slavicmyths.ships.ShipSuperstructurePlan;
import java.util.List;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;

/** Exact pure-Java voxel exports for Python and block-silhouette inspection. */
public final class ShipBrigantineVoxelExport {
    private static void write(Path path, ShipSuperstructurePlan config) throws Exception {
        StringBuilder csv = new StringBuilder("x,y,z,part\n");
        var blocks = ShipHullVoxelLayout.generate(config);
        for (var entry : blocks.entrySet()) {
            var p = entry.getKey();
            csv.append(p.x()).append(',').append(p.y()).append(',')
                    .append(p.z()).append(',').append(entry.getValue()).append('\n');
        }
        Files.writeString(path, csv, StandardCharsets.UTF_8);
        System.out.println("Exported " + blocks.size() + " Java cells -> " + path);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected export directory");
        Path destination = Path.of(args[0]);
        Files.createDirectories(destination);
        var base = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                15, 33, List.of(1, 1), 1, false, false, 3);
        var full = new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGE,
                19, 45, List.of(3, 3), 3, true, true, 3);
        write(destination.resolve("brigantine_v3_base.csv"), base);
        write(destination.resolve("brigantine_v3_full.csv"), full);
    }
}
