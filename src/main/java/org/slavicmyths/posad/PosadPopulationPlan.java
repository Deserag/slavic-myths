package org.slavicmyths.posad;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fixed, rotation-aware population manifest. Coordinates are candidates, never trusted spawn locations. */
public final class PosadPopulationPlan {
    public enum Role {
        POSADNIK, GUARD, SOTNIK, BLACKSMITH, WEAPONSMITH, CRAFTER, FARMER,
        MUSHROOM_GROWER, HERBALIST, COOK, STOREKEEPER, CIVILIAN;
        public boolean guard() { return this == GUARD || this == SOTNIK; }
        public int health() { return guard() ? 70 : 50; }
    }
    public enum Weapon { NONE, SWORD, SPEAR, SWORD_SHIELD, SPEAR_SHIELD, BOW }
    public record Member(String key, Role role, Weapon weapon, int health, int localX, int localZ) {
        public Member {
            if (key == null || key.isBlank() || role == null || weapon == null || localX < 0 || localX > 112 || localZ < 0 || localZ > 112) throw new IllegalArgumentException("Invalid member");
            if (health != role.health()) throw new IllegalArgumentException("Incorrect role health");
            if (role.guard() == (weapon == Weapon.NONE)) throw new IllegalArgumentException("Incorrect weapon");
        }
    }
    public record WorldMember(UUID identity, Member member, int worldX, int worldZ) {}
    private static final List<Member> ROSTER = createRoster();
    private static List<Member> createRoster() {
        List<Member> members = new ArrayList<>();
        members.add(member("posadnik", Role.POSADNIK, Weapon.NONE, 66, 85));
        members.add(member("blacksmith", Role.BLACKSMITH, Weapon.NONE, 20, 52));
        members.add(member("weaponsmith", Role.WEAPONSMITH, Weapon.NONE, 29, 54));
        members.add(member("crafter", Role.CRAFTER, Weapon.NONE, 81, 82));
        members.add(member("farmer", Role.FARMER, Weapon.NONE, 82, 45));
        members.add(member("mushroom_grower", Role.MUSHROOM_GROWER, Weapon.NONE, 83, 53));
        members.add(member("herbalist", Role.HERBALIST, Weapon.NONE, 86, 29));
        members.add(member("cook", Role.COOK, Weapon.NONE, 22, 25));
        members.add(member("storekeeper", Role.STOREKEEPER, Weapon.NONE, 88, 90));
        members.add(member("civilian_1", Role.CIVILIAN, Weapon.NONE, 26, 20));
        members.add(member("civilian_2", Role.CIVILIAN, Weapon.NONE, 27, 36));
        members.add(member("guard_1", Role.GUARD, Weapon.SWORD, 53, 7));
        members.add(member("guard_2", Role.GUARD, Weapon.SPEAR, 59, 7));
        members.add(member("guard_3", Role.GUARD, Weapon.SWORD_SHIELD, 12, 83));
        members.add(member("guard_4", Role.GUARD, Weapon.SPEAR_SHIELD, 24, 83));
        members.add(member("guard_5", Role.GUARD, Weapon.BOW, 34, 74));
        members.add(member("sotnik", Role.SOTNIK, Weapon.SWORD, 46, 81));
        if (members.stream().map(Member::role).distinct().count() != Role.values().length) throw new AssertionError("Missing profession");
        return List.copyOf(members);
    }
    private static Member member(String key, Role role, Weapon weapon, int x, int z) { return new Member(key, role, weapon, role.health(), x, z); }
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
