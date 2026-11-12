import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/**
 * Renders vanilla-sized voxel cells directly from the authoritative pure Java
 * ship generators. Excludes rigging to assess ONLY physical hull geometry.
 * No generative images, mockups, or AI art. Works headless on Java 21 CI.
 */
public final class ShipHullImageExport {
    private static final Path OUTPUT = Path.of("build", "ship-hull-images");
    private record P(int x, int y) {}
    private static final int WHITE = 0xF6F2E9;

    private static boolean smallEdge(int x, int z) {
        return !smallInside(x - 1, z) || !smallInside(x + 1, z)
                || !smallInside(x, z - 1) || !smallInside(x, z + 1);
    }

    private static boolean smallInside(int x, int z) {
        return z >= -5 && z <= 5
                && Math.abs(x) <= (int)Math.floor(FirstShipHull.halfBeam(z));
    }

    /** V11 shell only. Uses V11 widths and the original V10 deck outline. */
    private static Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> smallHull() {
        var hull = new HashMap<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>();
        for (int z = -5; z <= 5; z++) {
            int beam = (int)Math.floor(FirstShipHull.halfBeam(z));
            for (int x = -beam; x <= beam; x++) {
                int keel = FirstShipHull.halfWidthV11(z, 0);
                int chine = FirstShipHull.halfWidthV11(z, 1);
                boolean edge = smallEdge(x, z);
                if (Math.abs(x) <= keel)
                    hull.put(new ShipHullVoxelLayout.Cell(x, 0, z),
                            x == 0 ? ShipHullVoxelLayout.Part.KEEL
                                    : ShipHullVoxelLayout.Part.HULL);
                if ((Math.abs(x) <= chine || FirstShipHull.exposedEndV11(x, z))
                        && (Math.abs(x) > keel || edge))
                    hull.put(new ShipHullVoxelLayout.Cell(x, 1, z),
                            ShipHullVoxelLayout.Part.HULL);
                if (edge) hull.put(new ShipHullVoxelLayout.Cell(x, 2, z),
                        ShipHullVoxelLayout.Part.HULL);
                boolean hatch = (x == -1 || x == 0) && (z == 1 || z == 2);
                if (!hatch) {
                    hull.put(new ShipHullVoxelLayout.Cell(x, 3, z),
                            ShipHullVoxelLayout.Part.DECK);
                    hull.put(new ShipHullVoxelLayout.Cell(x, 4, z),
                            ShipHullVoxelLayout.Part.DECK);
                } else if (edge) hull.put(new ShipHullVoxelLayout.Cell(x, 3, z),
                        ShipHullVoxelLayout.Part.HULL);
                if (edge) hull.put(new ShipHullVoxelLayout.Cell(x, 5, z),
                        ShipHullVoxelLayout.Part.RAIL);
            }
        }
        for (int y = 1; y <= 3; y++)
            hull.put(new ShipHullVoxelLayout.Cell(-2, y, 2), ShipHullVoxelLayout.Part.FRAME);
        for (int y = 1; y <= 4; y++)
            hull.put(new ShipHullVoxelLayout.Cell(-1, y, 2), ShipHullVoxelLayout.Part.LADDER);
        for (int y = 5; y <= 6; y++)
            hull.put(new ShipHullVoxelLayout.Cell(0, y, 5), ShipHullVoxelLayout.Part.FRAME);
        hull.put(new ShipHullVoxelLayout.Cell(0, 0, -6), ShipHullVoxelLayout.Part.HULL);
        return hull;
    }

    private static Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> withoutRigging(
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> source) {
        var result = new HashMap<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>();
        for (var e : source.entrySet()) {
            var part = e.getValue();
            if (part != ShipHullVoxelLayout.Part.MAST
                    && part != ShipHullVoxelLayout.Part.YARD
                    && part != ShipHullVoxelLayout.Part.SAIL) result.put(e.getKey(), part);
        }
        return result;
    }

    private static Color color(ShipHullVoxelLayout.Part p) {
        return new Color(switch (p) {
            case KEEL -> 0x514232;
            case HULL -> 0xAD8353;
            case FRAME -> 0x77563D;
            case DECK -> 0xCBA574;
            case RAIL -> 0x765236;
            case LADDER -> 0xD0B07D;
            case CABIN_WALL -> 0x8E5F43;
            case WINDOW -> 0x7AAFC4;
            case DOOR -> 0x6E4835;
            case BRIDGE -> 0x987152;
            case HELM -> 0x4E3928;
            default -> 0xB99971;
        });
    }

    private static Graphics2D painter(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(WHITE));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1f));
        return g;
    }

    private static void label(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0x2C2C2C));
        g.setFont(new Font("SansSerif", Font.BOLD, 25));
        g.drawString(title, 36, 46);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString(subtitle, 36, 70);
    }

    private static void top(String name, Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> shape)
            throws IOException {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        Map<String, Map.Entry<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>> columns =
                new HashMap<>();
        for (var e : shape.entrySet()) {
            var c = e.getKey();
            minX = Math.min(minX, c.x()); maxX = Math.max(maxX, c.x());
            minZ = Math.min(minZ, c.z()); maxZ = Math.max(maxZ, c.z());
            String key = c.x() + ":" + c.z();
            var prev = columns.get(key);
            if (prev == null || prev.getKey().y() < c.y()) columns.put(key, e);
        }
        int cell = Math.min(45, 850 / Math.max(1, maxZ - minZ + 3));
        int w = Math.max(620, (maxX - minX + 1) * cell + 160);
        int h = (maxZ - minZ + 1) * cell + 160;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = painter(img);
        label(g, name + " | TOP", "Bow (+Z) upward · material colors · rigging hidden");
        int left = (w - (maxX - minX + 1) * cell) / 2;
        for (var e : columns.values()) {
            var c = e.getKey();
            int px = left + (c.x() - minX) * cell;
            int py = 105 + (maxZ - c.z()) * cell;
            g.setColor(color(e.getValue()));
            g.fillRect(px, py, cell, cell);
            g.setColor(new Color(0x5C4937));
            g.drawRect(px, py, cell, cell);
        }
        g.setColor(new Color(0x444444));
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("BOW +Z", left, 92);
        g.drawString("STERN -Z", left, h - 22);
        g.dispose();
        ImageIO.write(img, "png", OUTPUT.resolve(name + "-top.png").toFile());
    }

    private static P iso(double x, double y, double z, int scale, int cx, int cy) {
        return new P((int)Math.round(cx + (x - z) * scale),
                (int)Math.round(cy + (x + z) * scale * .49D - y * scale * .84D));
    }

    private static Color shade(Color c, double factor) {
        return new Color(Math.min(255, (int)(c.getRed() * factor)),
                Math.min(255, (int)(c.getGreen() * factor)),
                Math.min(255, (int)(c.getBlue() * factor)));
    }

    private static void face(Graphics2D g, Color c, P a, P b, P d, P e) {
        Polygon polygon = new Polygon(
                new int[]{a.x(), b.x(), d.x(), e.x()},
                new int[]{a.y(), b.y(), d.y(), e.y()}, 4);
        g.setColor(c); g.fillPolygon(polygon);
        g.setColor(new Color(0x493A2F)); g.drawPolygon(polygon);
    }

    private static void isometric(String name,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> shape) throws IOException {
        BufferedImage img = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = painter(img);
        label(g, name + " | ISOMETRIC", "Actual voxel occupancy · 3 visible faces · mast and sails hidden");
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (var c : shape.keySet()) {
            minX = Math.min(minX, c.x()); maxX = Math.max(maxX, c.x());
            minZ = Math.min(minZ, c.z()); maxZ = Math.max(maxZ, c.z());
        }
        int scale = Math.min(44, 790 / (maxX - minX + maxZ - minZ + 5));
        int cx = 500 + (int)Math.round(((minX + maxX) - (minZ + maxZ)) * scale * -.5);
        int cy = 435 - (int)Math.round(((minX + maxX) + (minZ + maxZ)) * scale * .245);
        var blocks = new ArrayList<>(shape.entrySet());
        blocks.sort(Comparator
                .comparingInt((Map.Entry<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> e) ->
                        e.getKey().x() + e.getKey().z())
                .thenComparingInt(e -> e.getKey().y())
                .thenComparingInt(e -> e.getKey().x()));
        Set<ShipHullVoxelLayout.Cell> occupied = new HashSet<>(shape.keySet());
        for (var entry : blocks) {
            var c = entry.getKey();
            int x = c.x(), y = c.y(), z = c.z();
            Color base = color(entry.getValue());
            P a = iso(x - .5, y, z - .5, scale, cx, cy);
            P b = iso(x + .5, y, z - .5, scale, cx, cy);
            P d = iso(x + .5, y, z + .5, scale, cx, cy);
            P e = iso(x - .5, y, z + .5, scale, cx, cy);
            P at = iso(x - .5, y + 1, z - .5, scale, cx, cy);
            P bt = iso(x + .5, y + 1, z - .5, scale, cx, cy);
            P dt = iso(x + .5, y + 1, z + .5, scale, cx, cy);
            P et = iso(x - .5, y + 1, z + .5, scale, cx, cy);
            if (!occupied.contains(new ShipHullVoxelLayout.Cell(x + 1, y, z)))
                face(g, shade(base, .76), b, d, dt, bt);
            if (!occupied.contains(new ShipHullVoxelLayout.Cell(x, y, z + 1)))
                face(g, shade(base, .90), e, d, dt, et);
            if (!occupied.contains(new ShipHullVoxelLayout.Cell(x, y + 1, z)))
                face(g, shade(base, 1.13), at, bt, dt, et);
        }
        g.dispose();
        ImageIO.write(img, "png", OUTPUT.resolve(name + "-isometric.png").toFile());
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUTPUT);
        var small = withoutRigging(smallHull());
        top("small-v11", small);
        isometric("small-v11", small);
        var nose = new HashMap<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part>();
        for (var e : small.entrySet())
            if (e.getKey().z() >= 2) nose.put(e.getKey(), e.getValue());
        isometric("small-v11-bow", nose);
        for (var type : List.of(ShipSectionPlan.ShipClass.MEDIUM,
                ShipSectionPlan.ShipClass.LARGE, ShipSectionPlan.ShipClass.LARGEST)) {
            int width = type == ShipSectionPlan.ShipClass.MEDIUM ? 13 : 21;
            int length = type == ShipSectionPlan.ShipClass.MEDIUM ? 21 : 33;
            List<Integer> sails = type == ShipSectionPlan.ShipClass.MEDIUM
                    ? List.of(1, 2) : type == ShipSectionPlan.ShipClass.LARGE
                    ? List.of(1, 2, 3) : List.of(3, 3, 2, 1);
            int decks = type == ShipSectionPlan.ShipClass.MEDIUM ? 2 : 3;
            var plan = new ShipSuperstructurePlan(type, width, length,
                    sails, decks, true, true);
            var shape = withoutRigging(ShipHullVoxelLayout.generate(plan, 2));
            String name = type.name().toLowerCase();
            top(name, shape);
            isometric(name, shape);
        }
        // V5 renders are generated directly from the versioned Java voxel
        // palette. The geometry-2 renders above remain available as BEFORE.
        for (var type : List.of(ShipSectionPlan.ShipClass.LARGE,
                ShipSectionPlan.ShipClass.LARGEST)) {
            List<Integer> sails = type == ShipSectionPlan.ShipClass.LARGE
                    ? List.of(1, 2, 3) : List.of(3, 3, 2, 1);
            var plan = new ShipSuperstructurePlan(type, 21, 33,
                    sails, 3, true, true);
            var newer = withoutRigging(ShipHullVoxelLayout.generate(plan, 5));
            String name = type.name().toLowerCase() + "-v5";
            top(name, newer);
            isometric(name, newer);
        }
        System.out.println("PASS ShipHullImageExport 13 PNGs in " + OUTPUT);
    }
}
