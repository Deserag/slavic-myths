import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/**
 * Exports orthographic raster views of REAL ShipHullVoxelLayout cells.
 * No concept art: every colored square corresponds to an emitted voxel.
 * Use hull-only images to inspect bilge/rails/decks hidden behind rigging.
 */
public final class ShipFlagshipImageExport {
    private static final int SCALE = 9;
    private static final int HALF_WIDTH = 13;
    private static final int HALF_LENGTH = 23;
    private static final int TOP_Y = 32;

    private static Color color(ShipHullVoxelLayout.Part part) {
        return switch (part) {
            case KEEL -> new Color(76, 49, 36);
            case HULL -> new Color(139, 93, 57);
            case FRAME -> new Color(94, 61, 38);
            case DECK -> new Color(183, 134, 84);
            case RAIL -> new Color(113, 74, 45);
            case LADDER -> new Color(226, 157, 68);
            case MAST -> new Color(86, 50, 30);
            case YARD -> new Color(111, 57, 25);
            case SAIL -> new Color(234, 229, 203);
            case CABIN_WALL -> new Color(111, 71, 49);
            case WINDOW -> new Color(75, 170, 192);
            case DOOR -> new Color(203, 133, 52);
            case BRIDGE -> new Color(154, 106, 60);
            case HELM -> new Color(228, 177, 64);
        };
    }

    private static boolean visible(ShipHullVoxelLayout.Part part, boolean hullOnly) {
        return !hullOnly || (part != ShipHullVoxelLayout.Part.MAST
                && part != ShipHullVoxelLayout.Part.YARD
                && part != ShipHullVoxelLayout.Part.SAIL);
    }

    private static void drawProjection(Graphics2D g,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> layout,
            boolean hullOnly, int originX, int originY, int view) {
        List<Map.Entry<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>> cells =
                new ArrayList<>(layout.entrySet());
        // Far cells first, near cells last, consistently for each view.
        Comparator<Map.Entry<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>> depth =
                switch (view) {
                    case 0 -> Comparator.comparingInt(e -> -e.getKey().x());
                    case 1 -> Comparator.comparingInt(e -> e.getKey().z());
                    default -> Comparator.comparingInt(e -> e.getKey().y());
                };
        cells.sort(depth);
        for (var entry : cells) {
            if (!visible(entry.getValue(), hullOnly)) continue;
            var p = entry.getKey();
            int x = view == 0 ? p.z() + HALF_LENGTH : p.x() + HALF_WIDTH;
            int y = view == 2 ? HALF_LENGTH - p.z() : TOP_Y - p.y();
            g.setColor(color(entry.getValue()));
            g.fillRect(originX + x * SCALE, originY + y * SCALE, SCALE, SCALE);
            g.setColor(new Color(20, 25, 25, 65));
            g.drawRect(originX + x * SCALE, originY + y * SCALE, SCALE, SCALE);
        }
    }

    private static void render(Path file, ShipSuperstructurePlan plan,
                               boolean hullOnly) throws Exception {
        int width = 1290, height = 660;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 243, 237));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(new Color(31, 40, 49));
        g.drawString("LARGEST IV / revision 2 / actual Java voxels", 30, 34);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.drawString(plan.hullWidth() + " x " + plan.hullLength()
                + " | decks " + plan.decks()
                + " | masts " + plan.sailTiers()
                + " | cabin " + plan.cabin()
                + " | bridge " + plan.bridge()
                + " | " + (hullOnly ? "HULL ONLY" : "FULL RIG"), 30, 59);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("SIDE — bow right", 32, 92);
        g.drawString("BOW / FRONT", 570, 92);
        g.drawString("TOP — bow up", 912, 92);
        var layout = ShipHullVoxelLayout.generate(plan);
        drawProjection(g, layout, hullOnly, 30, 113, 0);
        drawProjection(g, layout, hullOnly, 565, 113, 1);
        drawProjection(g, layout, hullOnly, 910, 113, 2);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g.setColor(new Color(48, 49, 50));
        g.drawString("VOXEL COUNT: " + layout.size(), 34, 628);
        g.drawString("No AI reconstruction. Exact block palette projection.", 34, 648);
        g.dispose();
        ImageIO.write(image, "png", file.toFile());
    }

    public static void main(String[] args) throws Exception {
        Path destination = Path.of(args.length > 0 ? args[0] : "build/flagship-views");
        Files.createDirectories(destination);
        List<ShipSuperstructurePlan> plans = List.of(
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        21, 33, List.of(1), 1, false, false, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        21, 33, List.of(1), 1, true, true, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        23, 37, List.of(2, 1), 2, true, false, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        25, 45, List.of(1, 2, 3), 3, true, true, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        25, 45, List.of(3, 2, 1, 3), 3, true, true, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        23, 41, List.of(2, 2, 2, 2), 3, false, true, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        25, 45, List.of(), 3, false, false, 2),
                new ShipSuperstructurePlan(ShipSectionPlan.ShipClass.LARGEST,
                        21, 37, List.of(1, 3, 2, 1), 2, false, true, 2));
        for (int i = 0; i < plans.size(); i++)
            for (boolean hullOnly : new boolean[]{false, true}) {
                String name = String.format("flagship-%02d-%s.png", i + 1,
                        hullOnly ? "hull" : "full");
                render(destination.resolve(name), plans.get(i), hullOnly);
                System.out.println("EXPORTED " + name);
            }
    }
}
