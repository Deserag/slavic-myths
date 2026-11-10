import java.util.List;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipSuperstructurePlan;
import org.slavicmyths.ships.ShipStructureBlueprint;

public final class ShipStructureBlueprintCheck {
    private static int total;
    private static void yes(boolean value, String reason) {
        total++;
        if (!value) throw new AssertionError(reason);
    }
    private static long count(ShipStructureBlueprint ship, ShipStructureBlueprint.Part part) {
        return ship.blocks().values().stream().filter(p -> p == part).count();
    }
    private static ShipSuperstructurePlan plan(ShipSectionPlan.ShipClass kind,
            int width, int length, List<Integer> sails, int decks, boolean bridge, boolean cabin) {
        return new ShipSuperstructurePlan(kind, width, length, sails, decks, bridge, cabin);
    }
    public static void main(String[] args) {
        var cls = ShipSectionPlan.ShipClass.LARGE;
        var bare = ShipStructureBlueprint.generate(plan(cls, 13, 21, List.of(), 1, false, false));
        var one = ShipStructureBlueprint.generate(plan(cls, 13, 21, List.of(1), 1, false, false));
        var three = ShipStructureBlueprint.generate(plan(cls, 13, 21, List.of(3), 1, false, false));
        yes(count(bare, ShipStructureBlueprint.Part.SAIL) == 0, "no free sail without a mast");
        yes(count(one, ShipStructureBlueprint.Part.SAIL) > 0, "first sail cloth is real geometry");
        yes(count(three, ShipStructureBlueprint.Part.SAIL) > count(one, ShipStructureBlueprint.Part.SAIL),
                "tier III sail is physically bigger");
        yes(count(three, ShipStructureBlueprint.Part.MAST) > count(one, ShipStructureBlueprint.Part.MAST),
                "advanced mast extends higher");
        yes(one.materialsFrom(bare).getOrDefault(ShipStructureBlueprint.Part.MAST, 0) > 0,
                "mast materials are counted");
        yes(one.materialsFrom(one).isEmpty(), "unchanged ship has no construction cost");
        yes(one.changesFrom(one).isEmpty(), "unchanged ship has no voxel changes");
        var doubleDeck = ShipStructureBlueprint.generate(plan(cls, 13, 21, List.of(1), 2, false, false));
        var tripleDeck = ShipStructureBlueprint.generate(plan(cls, 13, 21, List.of(1), 3, false, false));
        yes(count(doubleDeck, ShipStructureBlueprint.Part.UPPER_DECK) > 0,
                "upper deck has physical flooring");
        yes(count(tripleDeck, ShipStructureBlueprint.Part.UPPER_DECK)
                > count(doubleDeck, ShipStructureBlueprint.Part.UPPER_DECK),
                "third deck adds additional flooring");
        yes(count(tripleDeck, ShipStructureBlueprint.Part.LADDER)
                >= count(doubleDeck, ShipStructureBlueprint.Part.LADDER),
                "access between decks is preserved");
        var rooms = ShipStructureBlueprint.generate(plan(cls, 21, 33,
                List.of(1, 2, 3), 3, true, true));
        yes(count(rooms, ShipStructureBlueprint.Part.ROOF) > 0, "cabin roof");
        yes(count(rooms, ShipStructureBlueprint.Part.WINDOW) > 0, "cabin windows");
        yes(count(rooms, ShipStructureBlueprint.Part.HELM) == 1, "single functional helm position");
        yes(count(rooms, ShipStructureBlueprint.Part.ROOM_WALL) > 0, "cabin walls");
        yes(rooms.blocks().equals(ShipStructureBlueprint.generate(rooms.design()).blocks()),
                "same design produces identical voxels");
        yes(rooms.materialsFrom(bare).values().stream().allMatch(v -> v >= 0),
                "never charge negative material quantities");
        System.out.println("PASS ShipStructureBlueprintCheck assertions=" + total);
    }
}
