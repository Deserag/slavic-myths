package org.slavicmyths.loot;

/** Assignment for newly created containers; existing saved loot/inventory stays
 * authoritative. No runtime polling or new world/player reward ledger. */
public final class OverworldRewards276 {
    private static final String ROOT = "rewards276/overworld/";
    public static String sanctuary(boolean spring, String role) {
        return ROOT + (spring ? "spring_" : "fire_") + (role.equals("cache") ? "main" : role);
    }
    public static String fortress(int x, int y, int z, boolean barrel) {
        if (!barrel && x == 45 && y == 81 && z == 44) return ROOT + "fortress_main";
        return ROOT + (barrel ? "fortress_supply" : "fortress_cache");
    }
    private OverworldRewards276() { }
}
