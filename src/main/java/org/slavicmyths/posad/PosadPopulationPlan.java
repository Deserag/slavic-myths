package org.slavicmyths.posad;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Immutable rotation-aware population manifest for a completed posad.
 * Candidate anchors are NOT safe spawn points; the spawn service must validate
 * collision, floor, fluids and headroom before placing any entity.
 */
public final class PosadPopulationPlan {
    public enum Role { POSADNIK, CIVILIAN, GUARD }
    public enum Weapon { NONE, SWORD, SPEAR, SWORD_SHIELD, SPEAR_SHIELD, BOW }
    public record Member(String key, Role role, Weapon weapon, int health, int localX, int localZ) {
        public Member {
            if (key == null || key.isBlank() || localX < 0 || localX > 112 || localZ < 0 || localZ > 112) throw new IllegalArgumentException("Invalid member");
            if (health != switch (role) { case POSADNIK -> 150; case CIVILIAN -> 50; case GUARD -> 70; }) throw new IllegalArgumentException("Incorrect health");
            if ((role == Role.GUARD) == (weapon == Weapon.NONE)) throw new IllegalArgumentException("Incorrect weapon");
        }
    }
    public record WorldMember(UUID identity, Member member, int worldX, int worldZ) {}
    private static final List<Member> ROSTER = createRoster();
    private static List<Member> createRoster() {
        List<Member> result = new ArrayList<>();
        result.add(new Member("posadnik", Role.POSADNIK, Weapon.NONE, 150, 66, 85));
        int[][] homes = {{19,20},{26,20},{31,27},{19,31},{27,36},{42,43},{48,50},{59,57},{66,65},{81,60}};
        for (int i = 0; i < homes.length; i++) result.add(new Member("civilian_" + (i + 1), Role.CIVILIAN, Weapon.NONE, 50, homes[i][0], homes[i][1]));
        int[][] posts = {{53,7},{59,7},{12,83},{24,83},{34,74},{46,81}};
        Weapon[] weapons = {Weapon.SWORD,Weapon.SPEAR,Weapon.SWORD_SHIELD,Weapon.SPEAR_SHIELD,Weapon.BOW,Weapon.SWORD};
        for (int i = 0; i < posts.length; i++) result.add(new Member("guard_" + (i + 1), Role.GUARD, weapons[i], 70, posts[i][0], posts[i][1]));
        return List.copyOf(result);
    }
    public static List<Member> roster() { return ROSTER; }
    public static UUID settlementId(String dimension, int anchorX, int deckY, int anchorZ) {
        return UUID.nameUUIDFromBytes(("slavicmyths:nether_posad:" + dimension + ":" + anchorX + ":" + deckY + ":" + anchorZ).getBytes(StandardCharsets.UTF_8));
    }
    public static UUID memberId(UUID settlementId, String key) {
        return UUID.nameUUIDFromBytes((settlementId + ":" + key).getBytes(StandardCharsets.UTF_8));
    }
    public static List<WorldMember> at(String dimension, int anchorX, int deckY, int anchorZ, int turns) {
        if (turns < 0 || turns > 3) throw new IllegalArgumentException("Turns must be 0..3");
        UUID city = settlementId(dimension, anchorX, deckY, anchorZ);
        List<WorldMember> placed = new ArrayList<>(ROSTER.size());
        for (Member member : ROSTER) {
            int x = member.localX(), z = member.localZ();
            int dx = switch (turns) { case 1 -> 112 - z; case 2 -> 112 - x; case 3 -> z; default -> x; };
            int dz = switch (turns) { case 1 -> x; case 2 -> 112 - z; case 3 -> 112 - x; default -> z; };
            placed.add(new WorldMember(memberId(city, member.key()), member, Math.addExact(anchorX, dx), Math.addExact(anchorZ, dz)));
        }
        return List.copyOf(placed);
    }
    private PosadPopulationPlan() {}
}
