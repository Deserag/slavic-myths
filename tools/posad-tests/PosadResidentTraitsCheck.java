import java.nio.charset.StandardCharsets;
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
                appearances.add(traits);
                roles.add(member.member().role());
            }
        }
        if (roles.size() != 12 || appearances.size() < 5) throw new AssertionError("Insufficient roles or variety");

        Set<Integer> hornVariants = new HashSet<>(), wingVariants = new HashSet<>(), faceVariants = new HashSet<>(), skinVariants = new HashSet<>(), textureVariants = new HashSet<>();
        for (int i = 0; i < 2048; i++) {
            UUID id = UUID.nameUUIDFromBytes(("posad-visual-" + i).getBytes(StandardCharsets.UTF_8));
            var traits = PosadResidentTraits.of(id, PosadPopulationPlan.Role.CIVILIAN);
            if (traits.horns() < 0 || traits.horns() > 4) throw new AssertionError("Horn variant out of range");
            if (traits.wings() < 0 || traits.wings() > 3) throw new AssertionError("Wing variant out of range");
            if (traits.face() < 0 || traits.face() > 3) throw new AssertionError("Face variant out of range");
            if (traits.skin() < 0 || traits.skin() > 4) throw new AssertionError("Skin variant out of range");
            if (traits.textureVariant() < 0 || traits.textureVariant() > 4) throw new AssertionError("Texture variant out of range");
            hornVariants.add(traits.horns());
            wingVariants.add(traits.wings());
            faceVariants.add(traits.face());
            skinVariants.add(traits.skin());
            textureVariants.add(traits.textureVariant());
        }
        if (hornVariants.size() != 5) throw new AssertionError("Missing 0/1/2/3/4 horn variants");
        if (wingVariants.size() != 4) throw new AssertionError("Missing no/small/full/torn wing variants");
        if (faceVariants.size() != 4 || skinVariants.size() != 5 || textureVariants.size() != 5) throw new AssertionError("Incomplete face/skin texture variety");
        System.out.println("PosadResidentTraits: PASS, 12 professions, 5 horn variants, 4 wing variants, 5 texture atlases");
    }
}
