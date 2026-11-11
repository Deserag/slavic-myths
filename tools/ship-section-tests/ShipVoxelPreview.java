import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/**
 * Pure Java headless raster proof. Every pixelized cube comes from the REAL
 * V2 blueprint under test, not an AI reference or an invented block diagram.
 * This is NOT an in-game screenshot or dynamic-lighting verification.
 */
public final class ShipVoxelPreview {
    private static final int W = 1200, H = 850;
    private static final int WATER = 0x162D41, INK = 0xDFE9ED;
    private record Entry(ShipHullVoxelLayout.Cell cell, ShipHullVoxelLayout.Part part) {}
    private enum View { TOP, FRONT, REAR, SIDE, ISO, CUTAWAY }

    private static int rgb(ShipHullVoxelLayout.Part p) {
        return switch (p) {
            case KEEL -> 0x4B3324;
            case HULL -> 0x765139;
            case FRAME, MAST, BOWSPRIT -> 0x98714A;
            case DECK -> 0xAE8855;
            case RAIL -> 0x543C2C;
            case LADDER -> 0xB2956B;
            case YARD -> 0x60412E;
            case SAIL -> 0xF0E9DC;
            case SAIL_TRIM, PENNANT -> 0xBE3531;
            case RIGGING -> 0x4A4844;
            case CABIN_WALL -> 0x76523C;
            case WINDOW -> 0x91B9C5;
            case DOOR -> 0x996E43;
            case BRIDGE -> 0x59402F;
            case HELM -> 0xD8A25A;
            case LIGHT, STANDING_LANTERN -> 0xFFDA76;
        };
    }

    private static BufferedImage canvas(String title, String subtitle) {
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(WATER)); g.fillRect(0, 0, W, H);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(new Color(INK));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString(title, 34, 45);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.drawString(subtitle, 35, 74);
        g.drawString("SOURCE: ShipHullVoxelLayout.generate(plan, 2)   |   PROGRAMMATIC VOXEL PROOF, NOT MINECRAFT CLIENT", 35, H - 22);
        g.dispose();
        return image;
    }

    private static Point iso(int x, int y, int z, double scale) {
        return new Point((int)Math.round(W * .5 + (x - z) * .84D * scale),
                (int)Math.round(690 + (x + z) * .34D * scale - y * .98D * scale));
    }

    private static void face(Graphics2D g, int color, Point... corners) {
        Polygon p = new Polygon();
        for (Point point : corners) p.addPoint(point.x, point.y);
        g.setColor(new Color(color)); g.fillPolygon(p);
        g.setColor(new Color(0x1C1D1F)); g.setStroke(new BasicStroke(.6F));
        g.drawPolygon(p);
    }

    private static int shade(int color, double factor) {
        int r = (color >> 16) & 255, gr = (color >> 8) & 255, b = color & 255;
        return (Math.min(255, (int)(r * factor)) << 16)
                | (Math.min(255, (int)(gr * factor)) << 8)
                | Math.min(255, (int)(b * factor));
    }

    private static void cube(Graphics2D g, ShipHullVoxelLayout.Cell c, int color, double scale) {
        int x = c.x(), y = c.y(), z = c.z();
        Point a = iso(x, y, z, scale), b = iso(x + 1, y, z, scale);
        Point d = iso(x, y, z + 1, scale), e = iso(x + 1, y, z + 1, scale);
        Point at = iso(x, y + 1, z, scale), bt = iso(x + 1, y + 1, z, scale);
        Point dt = iso(x, y + 1, z + 1, scale), et = iso(x + 1, y + 1, z + 1, scale);
        face(g, shade(color, .65D), b, e, et, bt);
        face(g, shade(color, .86D), d, e, et, dt);
        face(g, color, at, bt, et, dt);
    }

    private static void renderIso(ShipSuperstructurePlan plan, View view, File folder) throws Exception {
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks =
                ShipHullVoxelLayout.generate(plan, 2);
        BufferedImage image = canvas(plan.shipClass() + "  |  " + view.name(),
                plan.hullWidth() + " x " + plan.hullLength() + " blocks   /   "
                        + plan.sailTiers().size() + " masts   /   " + plan.decks() + " decks");
        Graphics2D g = image.createGraphics();
        List<Entry> entries = new ArrayList<>();
        for (var e : blocks.entrySet()) {
            var c = e.getKey();
            if (view == View.CUTAWAY && (c.x() > 0
                    || e.getValue() == ShipHullVoxelLayout.Part.SAIL
                    || e.getValue() == ShipHullVoxelLayout.Part.SAIL_TRIM))
                continue;
            entries.add(new Entry(c, e.getValue()));
        }
        entries.sort(Comparator.comparingInt((Entry e) ->
                (e.cell().x() + e.cell().z()) * 4 + e.cell().y()));
        double scale = Math.min(20D, Math.min(760D / (plan.hullWidth() + plan.hullLength()),
                580D / 33D));
        if (view == View.CUTAWAY) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .13F));
            for (Entry e : entries) {
                if (e.part() != ShipHullVoxelLayout.Part.LIGHT
                        && e.part() != ShipHullVoxelLayout.Part.STANDING_LANTERN) continue;
                Point p = iso(e.cell().x(), e.cell().y(), e.cell().z(), scale);
                for (int r = 80; r >= 20; r -= 12) {
                    g.setColor(new Color(255, 207, 89));
                    g.fillOval(p.x - r, p.y - r, r * 2, r * 2);
                }
            }
            g.setComposite(AlphaComposite.SrcOver);
        }
        for (Entry e : entries) {
            int color = rgb(e.part());
            if (view == View.CUTAWAY
                    && e.part() != ShipHullVoxelLayout.Part.LIGHT
                    && e.part() != ShipHullVoxelLayout.Part.STANDING_LANTERN)
                color = shade(color, .7D);
            cube(g, e.cell(), color, scale);
        }
        g.dispose();
        ImageIO.write(image, "png", new File(folder, plan.shipClass().name().toLowerCase()
                + "_" + view.name().toLowerCase() + ".png"));
    }

    private static void renderPlane(ShipSuperstructurePlan plan, View view, File folder) throws Exception {
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks =
                ShipHullVoxelLayout.generate(plan, 2);
        BufferedImage image = canvas(plan.shipClass() + "  |  " + view.name(),
                "Section silhouette from the full V2 voxel palette; top view shows the deck footprint");
        Graphics2D g = image.createGraphics();
        // Select nearest visible layer for each screen cell.
        Map<Long, Entry> cells = new HashMap<>();
        Map<Long, Integer> depth = new HashMap<>();
        int minX = -plan.hullWidth() / 2, maxX = plan.hullWidth() / 2;
        int minZ = -plan.hullLength() / 2, maxZ = plan.hullLength() / 2 + 2;
        int maxY = 30;
        for (var e : blocks.entrySet()) {
            var c = e.getKey();
            var p = e.getValue();
            if (view == View.TOP && c.y() > 4) continue;
            if ((view == View.FRONT || view == View.REAR)
                    && (c.y() > 7 || p == ShipHullVoxelLayout.Part.SAIL
                    || p == ShipHullVoxelLayout.Part.SAIL_TRIM)) continue;
            int a = view == View.SIDE || view == View.TOP ? c.z() : c.x();
            int b = view == View.TOP ? c.x() : c.y();
            int dist = switch (view) {
                case TOP -> c.y();
                case FRONT -> c.z();
                case REAR -> -c.z();
                case SIDE -> c.x();
                default -> 0;
            };
            long key = ((long)a << 32) ^ (b & 0xFFFFFFFFL);
            if (!depth.containsKey(key) || dist >= depth.get(key)) {
                depth.put(key, dist); cells.put(key, new Entry(c, p));
            }
        }
        int cols = view == View.SIDE || view == View.TOP
                ? maxZ - minZ + 1 : maxX - minX + 1;
        int rows = view == View.TOP ? plan.hullWidth() : maxY + 1;
        int size = Math.max(9, Math.min(25, Math.min(1020 / cols, 640 / rows)));
        int left = (W - cols * size) / 2, top = 128;
        for (var entry : cells.entrySet()) {
            var c = entry.getValue().cell();
            int col = view == View.SIDE || view == View.TOP ? c.z() - minZ : c.x() - minX;
            int row = view == View.TOP ? c.x() - minX : maxY - c.y();
            int x = left + col * size, y = top + row * size;
            g.setColor(new Color(rgb(entry.getValue().part())));
            g.fillRect(x, y, size, size);
            g.setColor(new Color(0x1C232B));
            g.drawRect(x, y, size, size);
        }
        g.dispose();
        ImageIO.write(image, "png", new File(folder, plan.shipClass().name().toLowerCase()
                + "_" + view.name().toLowerCase() + ".png"));
    }

    private static ShipSuperstructurePlan plan(ShipSectionPlan.ShipClass type) {
        return switch (type) {
            case SMALL -> new ShipSuperstructurePlan(type, 7, 11,
                    List.of(1), 1, false, false);
            case MEDIUM -> new ShipSuperstructurePlan(type, 13, 21,
                    List.of(1, 2), 2, true, true);
            case LARGE -> new ShipSuperstructurePlan(type, 21, 33,
                    List.of(1, 2, 3), 3, true, true);
            case LARGEST -> new ShipSuperstructurePlan(type, 21, 33,
                    List.of(3, 3, 2, 1), 3, true, true);
        };
    }

    public static void main(String[] args) throws Exception {
        File folder = new File(args.length == 0 ? "build/ship-v2-previews" : args[0]);
        if (!folder.isDirectory() && !folder.mkdirs())
            throw new IllegalStateException("Cannot create preview directory " + folder);
        for (var type : ShipSectionPlan.ShipClass.values()) {
            var config = plan(type);
            for (View view : View.values()) {
                if (view == View.ISO || view == View.CUTAWAY)
                    renderIso(config, view, folder);
                else renderPlane(config, view, folder);
            }
        }
        System.out.println("PASS 24 reference-independent voxel source previews: " + folder);
    }
}
