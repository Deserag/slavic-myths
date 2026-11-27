package org.slavicmyths.registry;

import java.util.Map;
import java.util.Set;
import org.slavicmyths.combat.BossBarPalette;

/** Display-only order: ordinary creatures first, then boss ranks; no changes to creature balance. */
public final class MobEggOrder {
    private static final String SUFFIX="_spawn_egg";
    public static final int UNRANKED_BOSS=1000;
    // Encounter eggs do not use the individual entity's ID. The two savanna pack
    // ranks are the level VI candidates recorded in SAVANNA_GROUP_ENCOUNTERS.
    private static final Map<String,Integer> ENCOUNTER_LEVELS=Map.of(
        "steppe_jackal_pack",6,"black_hyena_pack",6,"pharaohka_leader",5);
    // Boss encounters without a verified level in the shared palette or design.
    private static final Set<String> UNRANKED=Set.of("auka","winter_owl","northern_wolverine",
        "deep_sea_serpent","ocean_zmey","stratim_bird","tsar_pike","zhelva",
        "sand_king","indrik_zmey","gluheya");
    private MobEggOrder(){}
    public static int rank(String path,String group){
        String id=path.endsWith(SUFFIX)?path.substring(0,path.length()-SUFFIX.length()):path;
        if(!"kalin_war_horse".equals(id)){
            int rank=ENCOUNTER_LEVELS.getOrDefault(id,BossBarPalette.levelForId(id));
            if(rank>0)return 10+rank;
            if(UNRANKED.contains(id)||group.startsWith("boss")||group.equals("miniboss"))return UNRANKED_BOSS;
        }
        return switch(group){case "peaceful"->0;case "neutral"->1;case "hostile","elite"->2;default->3;};
    }
    public static int rank(String path){
        var e=CreativeCatalog.entry(path);
        return rank(path,e==null?"":e.group());
    }
}
