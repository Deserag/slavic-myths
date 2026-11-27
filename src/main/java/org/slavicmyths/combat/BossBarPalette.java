package org.slavicmyths.combat;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;

/**
 * Bossbar color is determined by the creature danger level from the 1.9.8.1 audit.
 * Bosses introduced after that audit have explicitly documented provisional levels.
 * Unknown entity IDs preserve their previous color instead of silently receiving a rank.
 */
public final class BossBarPalette {
    private static final Map<String,Integer> LEVELS=Map.ofEntries(
        Map.entry("ataman",4),
        Map.entry("pharaohka_leader",6),
        Map.entry("tsar_pike",6),
        Map.entry("deep_sea_serpent",7),
        Map.entry("buried_volkhv",5),
        Map.entry("cursed_volkolak",6),
        Map.entry("defiled_spring_keeper",7),
        Map.entry("earth_spirit",7),
        Map.entry("elder_vodyanoy",5),
        Map.entry("extinguished_flame_guardian",7),
        Map.entry("fire_serpent",5),
        Map.entry("great_poloz",8),
        Map.entry("grey_wolf",6),
        Map.entry("idolishche_poganoe",7),
        Map.entry("kalin_tsar",8),
        Map.entry("karachun",7),
        Map.entry("kartaus",7),
        Map.entry("kladovik",5),
        Map.entry("kurgan_voevoda",5),
        Map.entry("large_podzemnik",6),
        Map.entry("likho_one_eyed",6),
        Map.entry("marinka",7),
        Map.entry("morozko",7),
        Map.entry("nightingale",6),
        Map.entry("ovinnik",5),
        Map.entry("podvey",5),
        Map.entry("skim_beast",7),
        Map.entry("tugarin_zmey",6),
        Map.entry("unresting_prince",6),
        Map.entry("volkolak",5),
        Map.entry("zmey_gorynych",8),
        // Bosses added after the audit. Provisional assignments are tracked in the report.
        Map.entry("agneya",6),
        Map.entry("ledeya",6),
        Map.entry("lomeya",8),
        Map.entry("neveya",8),
        Map.entry("viy",8),
        Map.entry("tryaseya",7),
        Map.entry("gneteya",8),
        Map.entry("cursed_mytar",7),
        Map.entry("bauk",8),
        Map.entry("morova_deva",7),
        Map.entry("trizna_prince",8),
        Map.entry("magma_lord",7),
        Map.entry("zharenitsa",7),
        Map.entry("posten",8),
        Map.entry("grove_guardian",7),
        Map.entry("morovoy_raven",7),
        Map.entry("morovoy_fire_zmey",8),
        Map.entry("brown_bear",5),
        Map.entry("ice_queen",9),
        Map.entry("ant_queen",6),
        Map.entry("div",8),
        Map.entry("skarapeya",9),
        Map.entry("polkan",7),
        Map.entry("hala",7),
        Map.entry("storm_idol",8),
        Map.entry("lamya",9),
        Map.entry("nine_mor_serpent",9)
    );

    /** Existing danger rank, without changing the color or assigning an unknown level. */
    public static int levelForId(String id){return LEVELS.getOrDefault(id,0);}

    public static BossEvent.BossBarColor colorFor(EntityType<?> entityType,BossEvent.BossBarColor previousColor){
        var key=BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if(key==null||!"slavicmyths".equals(key.getNamespace()))return previousColor;
        Integer level=LEVELS.get(key.getPath());
        return level==null?previousColor:colorForLevel(level);
    }

    public static BossEvent.BossBarColor colorForLevel(int level){
        return switch(level){
            case 4->BossEvent.BossBarColor.GREEN;
            case 5->BossEvent.BossBarColor.YELLOW;
            case 6->BossEvent.BossBarColor.BLUE;
            case 7->BossEvent.BossBarColor.RED;
            case 8->BossEvent.BossBarColor.PURPLE;
            case 9->BossEvent.BossBarColor.WHITE;
            default->BossEvent.BossBarColor.WHITE;
        };
    }

    private BossBarPalette(){}
}
