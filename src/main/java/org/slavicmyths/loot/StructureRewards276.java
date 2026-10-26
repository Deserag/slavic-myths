package org.slavicmyths.loot;

/** Pure assignment policy for newly placed containers only. Existing placement
 * guards own persistence; no inventory migration, extra container or world scan. */
public final class StructureRewards276 {
    public static String kurgan(int tier, boolean finalRoom, int index, String role) {
        if (tier < 0 || tier > 2) throw new IllegalArgumentException("kurgan tier");
        if (finalRoom && index == 0) return "rewards276/kurgan_" + (tier + 1) + "_main";
        return tier == 2 ? "rewards276/kurgan_3_" + role : "kurgan/" + role;
    }

    public static String camp(boolean medium, String template) {
        if (template.equals(medium ? "leader" : "senior"))
            return "rewards276/camp_" + (medium ? 2 : 1) + "_main";
        return "chests/bandit_" + (medium ? "medium" : "small");
    }

    public static String stronghold(String template, String role) {
        if (template.equals("ataman_house") && role.equals("ataman")) return "rewards276/camp_3_main";
        if (role.equals("cache")) return "rewards276/camp_3_cache";
        return "chests/stronghold_" + role;
    }

    private StructureRewards276() { }
}
