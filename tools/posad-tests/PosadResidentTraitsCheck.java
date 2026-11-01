import java.util.*;
import org.slavicmyths.posad.PosadPopulationPlan;
import org.slavicmyths.posad.PosadResidentTraits;

public final class PosadResidentTraitsCheck {
    public static void main(String[] args) {
        Set<PosadResidentTraits> appearances = new HashSet<>();
        Set<PosadPopulationPlan.Role> roles = EnumSet.noneOf(PosadPopulationPlan.Role.class);
        for (int turns = 0; turns < 4; turns++) {
            var members = PosadPopulationPlan.at("minecraft:the_nether", 100, 70, 200, turns);
            if (members.size() != 17) throw new AssertionError("Roster mismatch");
            for (var member : members) {
                var traits = PosadResidentTraits.of(member);
                if (!traits.equals(PosadResidentTraits.of(member.identity(), member.member().role()))) throw new AssertionError("Unstable appearance");
                if (member.member().health() != member.member().role().health()) throw new AssertionError("Incorrect health");
                if (member.member().role() == PosadPopulationPlan.Role.POSADNIK && traits.clothes() != 3) throw new AssertionError("Posadnik outfit");
                if (member.member().role().guard() && traits.clothes() != 2) throw new AssertionError("Guard outfit");
                appearances.add(traits);roles.add(member.member().role());
            }
        }
        if (roles.size() != 12 || appearances.size() < 5) throw new AssertionError("Insufficient roles or variety");
        System.out.println("PosadResidentTraits: PASS, 12 professions, 17 identities, 4 rotations");
    }
}
