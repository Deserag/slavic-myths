import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipSailRigGeometry;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;

/** Standalone Java 21 proof for issue #507, without a Minecraft client. */
public final class ShipSailRigProof {
    private static int checked;
    private static int surfaces;
    private static int anchors;

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static int expectedPanels(boolean iv, List<Integer> tiers) {
        int total = 0;
        for (int i = 0; i < tiers.size(); i++) {
            int tier = tiers.get(i);
            if (!iv) total += tier;
            else total += 2 + (tier == 3 || tier == 2 && (i == 1 || i == 2) ? 1 : 0);
        }
        return total;
    }

    private static int expectedStaysails(boolean iv, List<Integer> tiers) {
        int total = 0;
        for (int i = 0; i + 1 < tiers.size(); i++) {
            int a = tiers.get(i), b = tiers.get(i + 1);
            if (a >= 3 && b >= 3
                    || iv && a >= 2 && b >= 2 && i != 1)
                total++;
        }
        return total;
    }

    private static void checkPoint(ShipSailRigGeometry.Point point) {
        require(Double.isFinite(point.x()) && Double.isFinite(point.y())
                && Double.isFinite(point.z()), "Non-finite rigging coordinate");
    }

    private static void inspect(boolean iv, List<Integer> tiers, int decks,
            boolean bridge, boolean cabin) {
        ShipSuperstructurePlan plan = new ShipSuperstructurePlan(
                iv ? ShipSectionPlan.ShipClass.LARGEST : ShipSectionPlan.ShipClass.LARGE,
                iv ? 21 : 15, iv ? 77 : 55, tiers,
                decks, bridge, cabin, iv ? 4 : 3);
        var result = ShipSailRigGeometry.forPlan(plan);
        require(result.panels().size() == expectedPanels(iv, tiers),
                "Unexpected panel count " + tiers + " for " + plan.shipClass());
        require(result.staysails().size() == expectedStaysails(iv, tiers),
                "Unexpected staysail count " + tiers + " for " + plan.shipClass());
        Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> hull =
                ShipHullVoxelLayout.generate(plan);
        for (var panel : result.panels()) {
            require(panel.mast() < tiers.size(), "Sail belongs to a nonexistent mast");
            require(panel.tier() == tiers.get(panel.mast()), "Mixed-tier mast leak");
            require(panel.top() > panel.bottom(), "Inverted sail");
            require(panel.bottom() >= 10D && panel.top() <= 46D,
                    "Panel is outside structural mast range");
            require(panel.mastZ() == plan.mastPositions().get(panel.mast()),
                    "Sail is not attached to actual mast station");
            for (double u : new double[]{0D, 1D})
                for (double v : new double[]{0D, .25D, .5D, .75D, 1D}) {
                    var fixed = ShipSailRigGeometry.clothSurface(panel, 1D, u, v, 20D);
                    require(Math.abs(fixed.z() - .12D) < 1E-6D,
                            "Cloth side edge moved with wind");
                    checkPoint(fixed);
                }
            for (double v : new double[]{0D, 1D})
                for (double u : new double[]{0D, .2D, .5D, .8D, 1D}) {
                    var fixed = ShipSailRigGeometry.clothSurface(panel, 1D, u, v, 20D);
                    require(Math.abs(fixed.z() - .12D) < 1E-6D,
                            "Cloth top/bottom edge moved with wind");
                }
            var weak = ShipSailRigGeometry.clothSurface(panel, .13D, .5D, .5D, 21D);
            var medium = ShipSailRigGeometry.clothSurface(panel, .52D, .5D, .5D, 21D);
            var strong = ShipSailRigGeometry.clothSurface(panel, 1D, .5D, .5D, 21D);
            require(weak.z() + .3D < medium.z()
                    && medium.z() + .3D < strong.z(),
                    "Missing visibly different fill states");
            surfaces++;
        }
        for (var staysail : result.staysails()) {
            checkPoint(staysail.a());
            checkPoint(staysail.b());
            checkPoint(staysail.c());
            require(staysail.a().y() >= 20D, "Staysail enters walking space");
        }
        for (var rope : result.ropes()) {
            checkPoint(rope.start());
            checkPoint(rope.end());
            require(rope.start().y() >= 0D && rope.end().y() >= 0D,
                    "Rope is under the keel");
            if (rope.kind() == ShipSailRigGeometry.RopeKind.SHROUD
                    || rope.kind() == ShipSailRigGeometry.RopeKind.SHEET
                    || rope.kind() == ShipSailRigGeometry.RopeKind.BACKSTAY) {
                var foot = rope.end();
                var cell = new ShipHullVoxelLayout.Cell(
                        (int)Math.round(foot.x()), (int)Math.floor(foot.y()),
                        (int)Math.round(foot.z()));
                var part = hull.get(cell);
                require(part == ShipHullVoxelLayout.Part.RAIL
                        || part == ShipHullVoxelLayout.Part.DECK
                        || part == ShipHullVoxelLayout.Part.BRIDGE
                        || part == ShipHullVoxelLayout.Part.TRIM,
                        "Floating rope foot: " + cell + ", plan=" + tiers);
                anchors++;
            }
        }
        checked++;
    }

    private static void allTiers(boolean iv, int masts) {
        int variants = (int)Math.pow(3D, masts);
        for (int code = 0; code < variants; code++) {
            int value = code;
            List<Integer> tiers = new ArrayList<>();
            for (int mast = 0; mast < masts; mast++) {
                tiers.add(1 + value % 3);
                value /= 3;
            }
            inspect(iv, tiers, 1, false, false);
        }
    }

    public static void main(String[] args) {
        for (int masts = 0; masts <= 3; masts++)
            allTiers(false, masts);
        for (int masts = 0; masts <= 4; masts++)
            allTiers(true, masts);
        inspect(false, List.of(3, 1, 2), 3, true, true);
        inspect(false, List.of(1, 2, 3), 2, false, true);
        inspect(true, List.of(3, 2, 1, 3), 3, true, true);
        inspect(true, List.of(2, 3, 2, 3), 2, true, false);
        require(checked > 150, "Not enough mixed-tier scenarios checked");
        require(surfaces > 300 && anchors > 0, "Insufficient sail/rope coverage");
        System.out.println("ShipSailRigProof PASS: configurations=" + checked
                + ", surfaces=" + surfaces + ", physicalAnchors=" + anchors);
    }
}
