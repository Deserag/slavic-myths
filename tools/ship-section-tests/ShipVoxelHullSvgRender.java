import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/**
 * Generates SVG projections DIRECTLY from the exact version-2 Java voxel
 * palette, not from AI images or a separate manually approximated drawing.
 */
public final class ShipVoxelHullSvgRender {
    private static final int W = 1400, H = 1000;
    private static final int MARGIN = 70;
    private record Key(int a, int b) {}
    private record Point(double x, double y) {}

    private static int color(ShipHullVoxelLayout.Part part) {
        return switch (part) {
            case KEEL -> 0x5f3722;
            case HULL -> 0x925a32;
            case FRAME, MAST -> 0x694429;
            case DECK -> 0xc18b4c;
            case RAIL -> 0x86512f;
            case LADDER -> 0x9c7443;
            case YARD -> 0x492c1d;
            case SAIL -> 0xece8d7;
            case SAIL_TRIM -> 0xa83b35;
            case RIGGING -> 0x52412d;
            case BOWSPRIT -> 0x6e4e31;
            case CABIN_WALL -> 0x6e4632;
            case WINDOW -> 0x9ccbd1;
            case DOOR -> 0x6b4728;
            case BRIDGE -> 0x9e7346;
            case HELM -> 0x3d291f;
            case LIGHT -> 0xffcc69;
        };
    }
    private static String rgb(int rgb) {
        return String.format("#%06x", rgb & 0xffffff);
    }
    private static String shade(int rgb, double brightness) {
        int red = Math.min(255, (int)(((rgb >> 16) & 255) * brightness));
        int green = Math.min(255, (int)(((rgb >> 8) & 255) * brightness));
        int blue = Math.min(255, (int)((rgb & 255) * brightness));
        return rgb((red << 16) | (green << 8) | blue);
    }
    private static String begin(String title) {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + W
                + "\" height=\"" + H + "\" viewBox=\"0 0 " + W + " " + H + "\">"
                + "<rect width=\"100%\" height=\"100%\" fill=\"#b9d6df\"/>"
                + "<text x=\"36\" y=\"46\" font-family=\"monospace\" font-size=\"27\""
                + " font-weight=\"bold\" fill=\"#253d45\">" + title + "</text>";
    }
    private static void end(StringBuilder out) {
        out.append("<text x=\"36\" y=\"970\" font-family=\"monospace\" font-size=\"17\""
                + " fill=\"#314750\">Actual ShipHullVoxelLayout.generate(plan, 2) voxels</text></svg>");
    }

    private static Point iso(double x, double y, double z) {
        return new Point(x - z, .52D * (x + z) - 1.26D * y);
    }
    private static double[] bounds(Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> map) {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (var cell : map.keySet()) {
            for (double dx : new double[]{-.5D, .5D})
                for (double dz : new double[]{-.5D, .5D})
                    for (double dy : new double[]{-.5D, .5D}) {
                        Point p = iso(cell.x() + dx, cell.y() + dy, cell.z() + dz);
                        minX = Math.min(minX, p.x());
                        maxX = Math.max(maxX, p.x());
                        minY = Math.min(minY, p.y());
                        maxY = Math.max(maxY, p.y());
                    }
        }
        double scale = Math.min((W - 2D * MARGIN) / (maxX - minX + 1),
                (H - 2D * MARGIN - 60D) / (maxY - minY + 1));
        return new double[]{minX, minY, scale, (W - scale * (maxX - minX)) / 2D,
                (H - scale * (maxY - minY)) / 2D + 20D};
    }
    private static String point(double[] b, double x, double y, double z) {
        Point p = iso(x, y, z);
        return String.format(java.util.Locale.ROOT, "%.2f,%.2f",
                b[3] + (p.x() - b[0]) * b[2], b[4] + (p.y() - b[1]) * b[2]);
    }
    private static void face(StringBuilder out, String fill, String... vertices) {
        out.append("<polygon points=\"");
        for (String vertex : vertices) out.append(vertex).append(' ');
        out.append("\" fill=\"").append(fill)
                .append("\" stroke=\"#423a32\" stroke-opacity=\".32\" stroke-width=\".36\"/>");
    }
    private static void renderIso(Path file, String title,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> palette, boolean cutaway)
            throws Exception {
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> shown = new HashMap<>();
        for (var entry : palette.entrySet())
            if (!cutaway || entry.getKey().x() <= 0) shown.put(entry.getKey(), entry.getValue());
        double[] b = bounds(palette);
        List<ShipHullVoxelLayout.Cell> cells = new ArrayList<>(shown.keySet());
        cells.sort(Comparator.comparingInt((ShipHullVoxelLayout.Cell c) -> c.x() + c.z() + c.y())
                .thenComparingInt(ShipHullVoxelLayout.Cell::y));
        StringBuilder out = new StringBuilder(begin(title));
        for (var c : cells) {
            int x = c.x(), y = c.y(), z = c.z(), rgb = color(shown.get(c));
            double left = x - .5D, right = x + .5D;
            double down = y - .5D, up = y + .5D;
            double rear = z - .5D, front = z + .5D;
            if (!shown.containsKey(new ShipHullVoxelLayout.Cell(x + 1, y, z)))
                face(out, shade(rgb, .77D),
                        point(b, right, up, rear), point(b, right, up, front),
                        point(b, right, down, front), point(b, right, down, rear));
            if (!shown.containsKey(new ShipHullVoxelLayout.Cell(x, y, z + 1)))
                face(out, shade(rgb, .88D),
                        point(b, left, up, front), point(b, right, up, front),
                        point(b, right, down, front), point(b, left, down, front));
            if (!shown.containsKey(new ShipHullVoxelLayout.Cell(x, y + 1, z)))
                face(out, shade(rgb, 1.07D),
                        point(b, left, up, rear), point(b, right, up, rear),
                        point(b, right, up, front), point(b, left, up, front));
        }
        end(out);
        Files.writeString(file, out, StandardCharsets.UTF_8);
    }

    private static void renderOrthographic(Path file, String title, String view,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> palette) throws Exception {
        Map<Key, ShipHullVoxelLayout.Cell> visible = new HashMap<>();
        for (var cell : palette.keySet()) {
            if (view.equals("top") && cell.y() > 5) continue;
            int a = view.equals("side") ? cell.z() : cell.x();
            int b = switch (view) {
                case "top" -> -cell.z();
                default -> -cell.y();
            };
            Key key = new Key(a, b);
            ShipHullVoxelLayout.Cell prior = visible.get(key);
            if (prior == null || (view.equals("top") && cell.y() > prior.y())
                    || (view.equals("side") && cell.x() > prior.x())
                    || (view.equals("front") && cell.z() > prior.z())
                    || (view.equals("stern") && cell.z() < prior.z()))
                visible.put(key, cell);
        }
        int minA = visible.keySet().stream().mapToInt(Key::a).min().orElse(0);
        int maxA = visible.keySet().stream().mapToInt(Key::a).max().orElse(0);
        int minB = visible.keySet().stream().mapToInt(Key::b).min().orElse(0);
        int maxB = visible.keySet().stream().mapToInt(Key::b).max().orElse(0);
        double scale = Math.min((W - MARGIN * 2D) / (maxA - minA + 1),
                (H - MARGIN * 2D - 50D) / (maxB - minB + 1));
        scale = Math.min(scale, 55D);
        double x0 = (W - (maxA - minA + 1D) * scale) / 2D;
        double y0 = (H - (maxB - minB + 1D) * scale) / 2D + 20D;
        StringBuilder out = new StringBuilder(begin(title));
        for (var entry : visible.entrySet()) {
            var k = entry.getKey();
            int color = color(palette.get(entry.getValue()));
            double px = x0 + (k.a() - minA) * scale;
            double py = y0 + (k.b() - minB) * scale;
            out.append(String.format(java.util.Locale.ROOT,
                    "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\""
                    + " fill=\"%s\" stroke=\"#493524\" stroke-opacity=\".3\" stroke-width=\".5\"/>",
                    px, py, scale, scale, rgb(color)));
        }
        end(out);
        Files.writeString(file, out, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args.length == 0 ? "build/ship-visuals" : args[0]);
        Files.createDirectories(dir);
        for (ShipSectionPlan.ShipClass type : ShipSectionPlan.ShipClass.values()) {
            int width = type == ShipSectionPlan.ShipClass.SMALL ? 7
                    : type == ShipSectionPlan.ShipClass.MEDIUM ? 13 : 21;
            int length = type == ShipSectionPlan.ShipClass.SMALL ? 11
                    : type == ShipSectionPlan.ShipClass.MEDIUM ? 21 : 33;
            List<Integer> sails = switch (type) {
                case SMALL -> List.of(1);
                case MEDIUM -> List.of(1, 2);
                case LARGE -> List.of(1, 2, 3);
                case LARGEST -> List.of(3, 3, 2, 1);
            };
            int decks = type.maxDecks;
            boolean rooms = type != ShipSectionPlan.ShipClass.SMALL;
            var plan = new ShipSuperstructurePlan(type, width, length,
                    sails, decks, rooms, rooms);
            var palette = ShipHullVoxelLayout.generate(plan, 2);
            String name = type.name().toLowerCase(java.util.Locale.ROOT);
            renderIso(dir.resolve(name + "-iso.svg"), type.name() + " / isometric", palette, false);
            renderIso(dir.resolve(name + "-cutaway.svg"), type.name() + " / interior cutaway", palette, true);
            for (String view : List.of("top", "side", "front", "stern"))
                renderOrthographic(dir.resolve(name + "-" + view + ".svg"),
                        type.name() + " / " + view, view, palette);
            System.out.println("RENDER " + type + " voxels=" + palette.size());
        }
    }
}
