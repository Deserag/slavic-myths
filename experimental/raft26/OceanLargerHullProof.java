import java.util.*;
import org.slavicmyths.ships.experimental.OceanLargerHullBlueprint;

public final class OceanLargerHullProof {
    private static long checks;
    private static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        for (var size : OceanLargerHullBlueprint.Size.values()) {
            var plan = OceanLargerHullBlueprint.generate(size);
            check(size.width == (size == OceanLargerHullBlueprint.Size.MEDIUM ? 13 : 21), "beam");
            check(size.length == (size == OceanLargerHullBlueprint.Size.MEDIUM ? 21 : 33), "length");
            check(!plan.movementApproved(), "WIP size must never sail");
            check(plan.estimatedWrites() == plan.blockCount() * 2, "touch estimate");
            check(plan.materials().logs() > 48 && plan.materials().planks() > 96,
                    "materials grow with footprint");
            for (int z = size.stern(); z <= size.bow(); z++) {
                int row = 0;
                for (int x = -size.beam(); x <= size.beam(); x++) {
                    boolean in = OceanLargerHullBlueprint.inside(size, x, z);
                    var keel = plan.blocks().get(new OceanLargerHullBlueprint.Cell(x, 0, z));
                    check(in == (keel == OceanLargerHullBlueprint.Part.KEEL), "continuous ship keel");
                    if (in) row++;
                }
                check(row > 0, "missing hull row");
            }
            for (int x : new int[]{-1,0}) for (int z : new int[]{1,2}) {
                var hatchCell = plan.blocks().get(new OceanLargerHullBlueprint.Cell(x,4,z));
                check(x == -1 && z == 2
                        ? hatchCell == OceanLargerHullBlueprint.Part.HATCH_LADDER
                        : hatchCell == null, "2x2 deck hatch blocked");
                check(OceanLargerHullBlueprint.inside(size,x,z), "hatch not in hull");
            }
            check(plan.blocks().get(new OceanLargerHullBlueprint.Cell(-1,2,2))
                    == OceanLargerHullBlueprint.Part.HATCH_LADDER, "access ladder");
            check(plan.blocks().containsValue(OceanLargerHullBlueprint.Part.CABIN_GLASS),
                    "captain's cabin windows");
            check(plan.blocks().containsValue(OceanLargerHullBlueprint.Part.CABIN_DOOR),
                    "captain's cabin door");
            check(plan.blocks().containsValue(OceanLargerHullBlueprint.Part.CABIN_ROOF),
                    "raised steering deck");
            check(plan.blocks().containsValue(OceanLargerHullBlueprint.Part.HELM_WHEEL),
                    "raised wheel");
            check(plan.blocks().containsValue(OceanLargerHullBlueprint.Part.CABIN_LADDER),
                    "upper-deck access");
            check(plan.estimatedWrites() > 700, "large hull relocation must require profiling");
        }
        System.out.println("PASS OceanLargerHullProof: " + checks
                + " WIP geometry checks; NOT in Minecraft, movement disabled");
    }
}
