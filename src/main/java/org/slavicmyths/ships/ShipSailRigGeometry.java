package org.slavicmyths.ships;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-independent rigging coordinates for the III brigantine and IV frigate.
 * Positions are in ship-local blocks: X is beam, Y is height, +Z is bow.
 * Geometry is decorative and never changes server thrust or collision.
 */
public final class ShipSailRigGeometry {
    public record Point(double x, double y, double z) {
        public Point interpolate(Point other, double fraction) {
            return new Point(x + (other.x - x) * fraction,
                    y + (other.y - y) * fraction,
                    z + (other.z - z) * fraction);
        }
    }

    public enum RopeKind { SHROUD, RATLINE, STAY, BACKSTAY, BRACE, LIFT, HALYARD, SHEET }

    public record Rope(Point start, Point end, RopeKind kind) {
        public Rope {
            if (start == null || end == null || kind == null)
                throw new IllegalArgumentException("Incomplete rigging rope");
        }
    }

    public record Panel(int mast, int tier, double mastZ,
            double bottom, double top, double halfWidth) {
        public Panel {
            if (mast < 0 || tier < 1 || tier > 3 || top <= bottom || halfWidth <= 0D)
                throw new IllegalArgumentException("Invalid sail panel");
        }
    }

    public record Staysail(Point a, Point b, Point c) {}

    public record Scheme(List<Panel> panels, List<Staysail> staysails,
            List<Rope> ropes) {
        public Scheme {
            panels = List.copyOf(panels);
            staysails = List.copyOf(staysails);
            ropes = List.copyOf(ropes);
        }
    }

    private static final Map<ShipSuperstructurePlan, Scheme> CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(16, .75F, true) {
                @Override protected boolean removeEldestEntry(
                        Map.Entry<ShipSuperstructurePlan, Scheme> oldest) {
                    return size() > 12;
                }
            });

    public static Scheme forPlan(ShipSuperstructurePlan plan) {
        if (plan == null) throw new IllegalArgumentException("Missing ship plan");
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(plan, ShipSailRigGeometry::build);
        }
    }

    public static boolean isBrigantine(ShipSuperstructurePlan plan) {
        return plan.shipClass() == ShipSectionPlan.ShipClass.LARGE
                && plan.hullRevision() == 3;
    }

    public static boolean isFrigate(ShipSuperstructurePlan plan) {
        return plan.shipClass() == ShipSectionPlan.ShipClass.LARGEST
                && plan.hullRevision() == 4;
    }

    /** A mast's appearance depends on its own tier, not any global rig level. */
    public static List<Panel> panelsAt(ShipSuperstructurePlan plan, int mast) {
        if (!isBrigantine(plan) && !isFrigate(plan)) return List.of();
        if (mast < 0 || mast >= plan.sailTiers().size())
            throw new IllegalArgumentException("Unknown mast station");
        int tier = plan.sailTiers().get(mast);
        double z = plan.mastPositions().get(mast);
        List<Panel> panels = new ArrayList<>(3);
        if (isBrigantine(plan)) {
            double yard = ShipHullVoxelLayout.sailLowerYard(plan);
            double halfWidth = tier == 1 ? 2D : tier == 2 ? 3D : 4D;
            if (tier == 1) {
                panels.add(new Panel(mast, tier, z, yard + 1D,
                        ShipHullVoxelLayout.sailTop(plan, tier), halfWidth));
            } else {
                panels.add(new Panel(mast, tier, z, yard + 1D, yard + 5D, halfWidth));
                if (tier == 2)
                    panels.add(new Panel(mast, tier, z, yard + 6D, yard + 8D, 2.3D));
                else {
                    panels.add(new Panel(mast, tier, z, yard + 6D, yard + 9D, 3.0D));
                    panels.add(new Panel(mast, tier, z, yard + 10D, yard + 12D, 2.2D));
                }
            }
        } else {
            boolean central = mast == 1 || mast == 2;
            double span = (central ? 8D : 6D) + tier - 1D;
            panels.add(new Panel(mast, tier, z, 22D, 29D, span));
            panels.add(new Panel(mast, tier, z, 31D, 38D, span - 2D));
            if (tier >= 3 || (tier == 2 && central))
                panels.add(new Panel(mast, tier, z, 40D,
                        tier == 3 && central ? 44D : 42D,
                        Math.max(3D, span - 3D)));
        }
        return List.copyOf(panels);
    }

    /**
     * Animated sail vertex with pinned edges (u/v = 0 or 1).
     * Pressure ranges from calm (0) to full fill (1); the phase only adds
     * small interior flutter and can be tested without Minecraft classes.
     */
    public static Point clothSurface(Panel panel, double pressure,
            double u, double v, double phase) {
        if (panel == null || !Double.isFinite(pressure)
                || !Double.isFinite(u) || !Double.isFinite(v)
                || !Double.isFinite(phase) || u < 0D || u > 1D
                || v < 0D || v > 1D)
            throw new IllegalArgumentException("Invalid cloth parameters");
        double fill = Math.max(0D, Math.min(1D, pressure));
        // A square sail hangs from a straight yard. Its foot dips gently at
        // the middle; the broad sides stay almost straight instead of making
        // the round barrel silhouette of the previous panel.
        double width = 1D - .08D * (1D - v);
        double x = panel.halfWidth() * (u * 2D - 1D) * width;
        double y = panel.bottom() + (panel.top() - panel.bottom()) * v
                - .42D * Math.sin(Math.PI * u) * (1D - v);
        double pinned = Math.sin(Math.PI * u) * Math.sin(Math.PI * v);
        double swell = (.08D + 2.35D * fill) * pinned;
        double flutter = (.04D + .11D * fill) * Math.sin(phase * .11D
                + u * 10D + v * 5D) * pinned;
        return new Point(x, y, .12D + swell + flutter);
    }

    private static int mastHead(ShipSuperstructurePlan plan, int mast) {
        int tier = plan.sailTiers().get(mast);
        if (isBrigantine(plan))
            return ShipHullVoxelLayout.sailTop(plan, tier) + 1;
        return Math.min(46, (mast == 1 || mast == 2 ? 43 : 40) + (tier - 1) * 2);
    }

    /**
     * Locate a real supporting rail/deck block for each shroud/sheet endpoint.
     * Do not put artificial attachment blocks inside walking paths.
     */
    private static Point railAnchor(ShipSuperstructurePlan plan,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks,
            int stationZ, int displacementZ, int side) {
        int maximumZ = plan.hullLength() / 2 - 2;
        int z = Math.max(-maximumZ, Math.min(maximumZ, stationZ + displacementZ));
        int beam = ShipHullVoxelLayout.halfBeam(plan, z);
        for (int inset = 0; inset <= 3; inset++) {
            int x = side * (beam - inset);
            if (side * x <= 0) break;
            for (int y = 26; y >= 9; y--) {
                var part = blocks.get(new ShipHullVoxelLayout.Cell(x, y, z));
                if (part == ShipHullVoxelLayout.Part.RAIL
                        || part == ShipHullVoxelLayout.Part.DECK
                        || part == ShipHullVoxelLayout.Part.BRIDGE
                        || part == ShipHullVoxelLayout.Part.TRIM)
                    return new Point(x + side * .2D, y + .6D, z);
            }
        }
        return null; // An absent anchor must not become a floating line.
    }

    private static Point bowAnchor(ShipSuperstructurePlan plan,
            Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> blocks) {
        Point farthest = null;
        for (var entry : blocks.entrySet()) {
            if (entry.getValue() != ShipHullVoxelLayout.Part.BOWSPRIT) continue;
            var c = entry.getKey();
            if (farthest == null || c.z() > farthest.z())
                farthest = new Point(c.x(), c.y() + .3D, c.z());
        }
        return farthest;
    }

    private static void rope(List<Rope> ropes, Point a, Point b, RopeKind kind) {
        if (a != null && b != null
                && Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y())
                        + Math.abs(a.z() - b.z()) > .1D)
            ropes.add(new Rope(a, b, kind));
    }

    private static Scheme build(ShipSuperstructurePlan plan) {
        if (!isBrigantine(plan) && !isFrigate(plan))
            return new Scheme(List.of(), List.of(), List.of());
        var layout = ShipHullVoxelLayout.generate(plan);
        List<Panel> panels = new ArrayList<>();
        List<Staysail> staysails = new ArrayList<>();
        List<Rope> ropes = new ArrayList<>();
        List<Integer> masts = plan.mastPositions();
        for (int i = 0; i < masts.size(); i++) {
            int mastZ = masts.get(i);
            int tier = plan.sailTiers().get(i);
            int head = mastHead(plan, i);
            List<Panel> onMast = panelsAt(plan, i);
            panels.addAll(onMast);
            int count = tier + 1; // two, three and four shrouds per side.
            for (int side : new int[]{-1, 1}) {
                List<Rope> shrouds = new ArrayList<>(count);
                for (int r = 0; r < count; r++) {
                    int shift = (int)Math.round((r - (count - 1D) / 2D) * 2D);
                    Point anchor = railAnchor(plan, layout, mastZ, shift, side);
                    Point headPoint = new Point(side * .35D,
                            head - .4D - r * .25D, mastZ);
                    if (anchor == null) continue;
                    Rope shroud = new Rope(headPoint, anchor, RopeKind.SHROUD);
                    shrouds.add(shroud);
                    ropes.add(shroud);
                }
                if (tier >= 2) {
                    for (int pair = 0; pair + 1 < shrouds.size(); pair++) {
                        Rope a = shrouds.get(pair), b = shrouds.get(pair + 1);
                        for (int step = 1; step <= 6; step++) {
                            double f = step / 8D;
                            rope(ropes, a.start().interpolate(a.end(), f),
                                    b.start().interpolate(b.end(), f),
                                    RopeKind.RATLINE);
                        }
                    }
                }
            }
            for (Panel p : onMast) {
                for (int side : new int[]{-1, 1}) {
                    double x = side * p.halfWidth() * .86D;
                    Point upper = new Point(x, p.top(), mastZ + .2D);
                    Point lower = new Point(x, p.bottom(), mastZ + .2D);
                    rope(ropes, upper, new Point(0D, p.top() - 2D,
                            mastZ + (side > 0 ? 2.5D : -2.5D)), RopeKind.BRACE);
                    rope(ropes, upper, new Point(.12D * side,
                            Math.min(head, p.top() + 4D), mastZ), RopeKind.LIFT);
                    rope(ropes, lower, railAnchor(plan, layout, mastZ,
                            side > 0 ? 3 : -3, side), RopeKind.SHEET);
                }
                rope(ropes, new Point(0D, p.top() + .5D, mastZ),
                        new Point(0D, Math.min(head, p.top() + 3D), mastZ),
                        RopeKind.HALYARD);
            }
        }
        for (int i = 0; i + 1 < masts.size(); i++) {
            int aftZ = masts.get(i), frontZ = masts.get(i + 1);
            int aftHead = mastHead(plan, i), frontHead = mastHead(plan, i + 1);
            rope(ropes, new Point(0D, aftHead - 1D, aftZ),
                    new Point(0D, Math.max(19D, frontHead - 6D), frontZ), RopeKind.STAY);
            int aftTier = plan.sailTiers().get(i);
            int forwardTier = plan.sailTiers().get(i + 1);
            boolean addStaysail = aftTier >= 3 && forwardTier >= 3
                    || isFrigate(plan) && aftTier >= 2 && forwardTier >= 2
                            && (i != 1 || aftTier >= 3 && forwardTier >= 3);
            if (addStaysail) {
                double y = Math.max(21D, Math.min(aftHead, frontHead) - 8D);
                double z0 = aftZ + 1.4D, z1 = frontZ - 1.4D;
                if (z1 - z0 > 1.0D)
                    staysails.add(new Staysail(new Point(.28D, y + 5D, z0),
                            new Point(.28D, y, z0),
                            new Point(.28D, y, z1)));
            }
        }
        if (!masts.isEmpty()) {
            Point bow = bowAnchor(plan, layout);
            int front = masts.size() - 1;
            if (bow != null)
                rope(ropes, new Point(0D, mastHead(plan, front) - 1D,
                                masts.get(front)), bow, RopeKind.STAY);
            int stern = -plan.hullLength() / 2 + 3;
            Point sternAnchor = railAnchor(plan, layout, stern, 0, 1);
            rope(ropes, new Point(0D, mastHead(plan, 0) - 2D,
                    masts.get(0)), sternAnchor, RopeKind.BACKSTAY);
        }
        return new Scheme(panels, staysails, ropes);
    }

    private ShipSailRigGeometry() {}
}
