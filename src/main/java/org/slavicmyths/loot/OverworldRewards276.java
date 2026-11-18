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
    public static String swamp(String template,String role,int x,int y,int z) {
        return switch(template+":"+x+":"+y+":"+z) {
            case "ruin_0:4:1:7" -> ROOT+"swamp_ruins_main";
            case "ruin_1:4:1:8" -> ROOT+"swamp_ruins_main";
            case "ruin_2:3:1:2" -> ROOT+"swamp_ruins_main";
            case "v097_abandoned_0:4:2:3" -> ROOT+"swamp_home_main";
            case "v097_abandoned_1:4:2:3" -> ROOT+"swamp_home_main";
            case "v097_abandoned_2:4:2:3" -> ROOT+"swamp_home_main";
            case "v097_cluster_0:8:2:10" -> ROOT+"swamp_great_main";
            case "v097_cluster_1:8:2:10" -> ROOT+"swamp_great_main";
            case "v097_cluster_2:8:2:10" -> ROOT+"swamp_great_main";
            case "v097_fisher_0:5:2:14" -> ROOT+"swamp_fisher_main";
            case "v097_fisher_1:5:2:14" -> ROOT+"swamp_fisher_main";
            case "v097_fisher_2:5:2:14" -> ROOT+"swamp_fisher_main";
            case "v097_landing_0:7:2:4" -> ROOT+"swamp_fisher_main";
            case "v097_landing_1:7:2:4" -> ROOT+"swamp_fisher_main";
            case "v097_landing_2:7:2:4" -> ROOT+"swamp_fisher_main";
            case "v097_shrine_0:3:2:4" -> ROOT+"swamp_ritual_main";
            case "v097_shrine_1:3:2:4" -> ROOT+"swamp_ritual_main";
            case "v097_shrine_2:3:2:4" -> ROOT+"swamp_ritual_main";
            case "v097_watchtower_0:5:10:5" -> ROOT+"swamp_watch_main";
            case "v097_watchtower_1:5:12:5" -> ROOT+"swamp_watch_main";
            case "v097_watchtower_2:5:14:5" -> ROOT+"swamp_watch_main";
            default -> "chests/"+role;
        };
    }
    private OverworldRewards276() { }
}
