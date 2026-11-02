import java.util.HashSet;
import org.slavicmyths.posad.PosadPopulationPlan;

/** Standalone Java 21 check: javac -d out src/main/java/org/slavicmyths/posad/PosadPopulationPlan.java tools/posad-tests/PosadPopulationPlanCheck.java && java -cp out PosadPopulationPlanCheck */
public final class PosadPopulationPlanCheck {
    public static void main(String[] args) {
        var roster = PosadPopulationPlan.roster();
        if (roster.size() != 17 || roster.stream().filter(m -> m.role() == PosadPopulationPlan.Role.POSADNIK).count() != 1 || roster.stream().filter(m -> m.role() == PosadPopulationPlan.Role.CIVILIAN).count() != 10 || roster.stream().filter(m -> m.role() == PosadPopulationPlan.Role.GUARD).count() != 6) throw new AssertionError("Roster mismatch");
        for (int turn = 0; turn < 4; turn++) {
            var members = PosadPopulationPlan.at("minecraft:the_nether", 100, 70, 200, turn);
            if (!members.equals(PosadPopulationPlan.at("minecraft:the_nether", 100, 70, 200, turn))) throw new AssertionError("Unstable assignment");
            if (new HashSet<>(members.stream().map(m -> m.identity()).toList()).size() != 17) throw new AssertionError("Duplicate identity");
            if (new HashSet<>(members.stream().map(m -> m.worldX() + ":" + m.worldZ()).toList()).size() != 17) throw new AssertionError("Duplicate anchor");
            if (members.stream().anyMatch(m -> m.worldX() < 100 || m.worldX() > 212 || m.worldZ() < 200 || m.worldZ() > 312)) throw new AssertionError("Out of city");
        }
        System.out.println("PosadPopulationPlan: PASS, 17 members, 4 rotations, deterministic settlement IDs");
    }
}
