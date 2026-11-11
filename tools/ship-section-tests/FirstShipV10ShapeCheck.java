import java.util.Map;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipV10Shape;

public final class FirstShipV10ShapeCheck {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Map<FirstShipV10Shape.Cell, FirstShipV10Shape.Part> hull = FirstShipV10Shape.sections();
        check(FirstShipHull.WIDTH == 7 && FirstShipHull.LENGTH == 11, "native vessel remains 7x11");
        check(FirstShipHull.HELM_Z == -3D && FirstShipHull.MAST_Z == -1D, "helm and mast fixed");
        check(FirstShipHull.LADDER_X == -1D && FirstShipHull.LADDER_Z == 2D, "ladder fixed");
        check(hull.equals(FirstShipV10Shape.sections()), "deterministic physical shell");

        int minX = 99, maxX = -99, minZ = 99, maxZ = -99;
        for (var cell : hull.keySet()) {
            minX = Math.min(minX, cell.x());
            maxX = Math.max(maxX, cell.x());
            minZ = Math.min(minZ, cell.z());
            maxZ = Math.max(maxZ, cell.z());
            check(cell.x() >= -3 && cell.x() <= 3, "width bound " + cell);
            check(cell.z() >= -5 && cell.z() <= 5, "length bound " + cell);
            check(cell.y() >= 0 && cell.y() <= 5, "section height " + cell);
        }
        check(minX == -3 && maxX == 3 && minZ == -5 && maxZ == 5, "exact 7x11 extents");
        for (int z = -5; z <= 5; z++) {
            int beam = FirstShipV10Shape.deckHalfBeam(z);
            int floor = FirstShipV10Shape.floorHalfBeam(z);
            check(floor >= 0 && floor <= beam, "inward taper at " + z);
            check(FirstShipHull.halfBeam(z) == beam + .49D, "existing deck walking bounds " + z);
            check(hull.get(new FirstShipV10Shape.Cell(0, 0, z)) == FirstShipV10Shape.Part.KEEL,
                    "continuous spruce keel " + z);
            for (int x = -beam; x <= beam; x++) {
                for (int y = 0; y <= 5; y++)
                    check(FirstShipV10Shape.partAt(x, y, z)
                            == FirstShipV10Shape.partAt(-x, y, z),
                            "symmetric lower shell " + x + "," + y + "," + z);
                boolean floorHere = hull.containsKey(new FirstShipV10Shape.Cell(x, 0, z));
                boolean sideHere = hull.containsKey(new FirstShipV10Shape.Cell(x, 1, z));
                check(floorHere || sideHere, "no flooded hold floor " + x + "," + z);
                if (!sideHere) check(floorHere, "air cell has dry floor " + x + "," + z);
                check(hull.containsKey(new FirstShipV10Shape.Cell(x, 4, z))
                        == !((x == -1 || x == 0) && (z == 1 || z == 2)),
                        "unchanged deck and hatch " + x + "," + z);
            }
        }
        check(FirstShipV10Shape.deckHalfBeam(5) == 0
                && FirstShipV10Shape.deckHalfBeam(4) == 1
                && FirstShipV10Shape.deckHalfBeam(3) == 2
                && FirstShipV10Shape.deckHalfBeam(2) == 2, "bow sharpens toward centerline");
        check(FirstShipV10Shape.floorHalfBeam(-5) == 0
                && FirstShipV10Shape.deckHalfBeam(-5) == 1
                && FirstShipV10Shape.deckHalfBeam(-4) == 2, "stern lower than quarterdeck");
        check(FirstShipV10Shape.floorHalfBeam(0) < FirstShipV10Shape.deckHalfBeam(0),
                "keel narrower than deck");
        check(hull.get(new FirstShipV10Shape.Cell(0, 4, -4)) == FirstShipV10Shape.Part.DECK,
                "captain standing surface retained");
        check(hull.get(new FirstShipV10Shape.Cell(0, 4, -3)) == FirstShipV10Shape.Part.DECK,
                "helm standing surface retained");
        check(FirstShipV10Shape.partAt(-1, 4, 2) == null
                && FirstShipV10Shape.partAt(0, 4, 2) == null, "hatch not blocked");
        System.out.println("PASS FirstShipV10ShapeCheck assertions=" + checks + " hullCells=" + hull.size());
    }
}
