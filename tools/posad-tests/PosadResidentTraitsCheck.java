import java.util.HashSet;
import org.slavicmyths.posad.PosadPopulationPlan;
import org.slavicmyths.posad.PosadResidentTraits;

/** Standalone Java 21 check. */
public final class PosadResidentTraitsCheck {
    public static void main(String[] args) {
        var appearances = new HashSet<PosadResidentTraits>();
        for (int turns = 0; turns < 4; turns++) {
            var members = PosadPopulationPlan.at("minecraft:the_nether", 100, 70, 200, turns);
            if (members.size() != 17) throw new AssertionError("Roster mismatch");
            for (var member : members) {
                var traits = PosadResidentTraits.of(member);
                if (!traits.equals(PosadResidentTraits.of(member))) throw new AssertionError("Unstable appearance");
                if (member.member().role() == PosadPopulationPlan.Role.POSADNIK && traits.clothes() != 3) throw new AssertionError("Posadnik outfit");
                if (member.member().role() == PosadPopulationPlan.Role.GUARD && traits.clothes() != 2) throw new AssertionError("Guard outfit");
                appearances.add(traits);
            }
        }
        if (appearances.size() < 5) throw new AssertionError("Insufficient variety");
        System.out.println("PosadResidentTraits: PASS, 17 identities and 4 rotations");
    }
}
