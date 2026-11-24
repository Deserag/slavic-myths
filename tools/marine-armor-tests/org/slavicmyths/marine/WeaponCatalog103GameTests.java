package org.slavicmyths.marine;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Historical 102 weapon IDs plus Gneteya's new hammer; registry regression, not a combat acceptance test. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class WeaponCatalog103GameTests {
    private static final Set<String> IDS=Set.of(
        "abyss_staff",
        "ancient_carolingian_sword",
        "ancient_chekan",
        "ancient_spear",
        "ashen_greatsword",
        "ataman_flail",
        "battle_axe",
        "berdysh",
        "bone_arrow",
        "bone_knife",
        "butchering_knife",
        "carved_staff",
        "chekan",
        "chud_multitool_axe",
        "club",
        "diamond_flail",
        "diamond_spear",
        "diamond_sulitsa",
        "diamond_throwing_knife",
        "empowered_infernal_bow",
        "finist_bow",
        "flail",
        "gift_ilya_shield",
        "gneteya_great_hammer",
        "gold_flail",
        "gold_spear",
        "gold_sulitsa",
        "gold_throwing_knife",
        "heavy_bow",
        "infernal_bow",
        "iron_spear",
        "iron_sulitsa",
        "iron_throwing_knife",
        "karachun_scythe",
        "likho_staff",
        "lomeya_hammer",
        "lomeya_hammer_offhand",
        "mace",
        "morovoy_staff",
        "morozko_staff",
        "mytar_shackled_sickle",
        "mytar_sickle",
        "navya_ostroga",
        "netherite_flail",
        "netherite_spear",
        "netherite_sulitsa",
        "netherite_throwing_knife",
        "neveya_scythe",
        "nightingale_dagger",
        "nine_mor_axe",
        "nine_mor_shield",
        "nine_mor_spear",
        "noon_sickle",
        "perunite_axe",
        "perunite_flail",
        "perunite_mace",
        "perunite_shield",
        "perunite_spear",
        "perunite_sulitsa",
        "perunite_sword",
        "perunite_throwing_knife",
        "pharaohka_trident",
        "poloz_whip",
        "pool_spear",
        "restored_carolingian_sword",
        "restored_spear",
        "retainer_shield",
        "ritual_knife",
        "serpent_crossbow",
        "shadow_throwing_knife",
        "short_bow",
        "silver_dagger",
        "silver_flail",
        "silver_mace",
        "silver_spear",
        "silver_sulitsa",
        "silver_sword",
        "silver_throwing_knife",
        "spear",
        "stone_flail",
        "stone_mace",
        "stone_sulitsa",
        "stone_throwing_knife",
        "storm_anchor",
        "storm_harpoon",
        "storm_staff",
        "three_breath_crossbow",
        "thunder_axe",
        "thunder_spear",
        "trophy_kalin_sabre",
        "tugarin_sword",
        "veles_staff",
        "victor_spear",
        "volcanic_heart_flail",
        "ward_shield",
        "wooden_mace",
        "zhara_axe",
        "zhara_flail",
        "zhara_spear",
        "zhara_sulitsa",
        "zhara_sword",
        "zhara_throwing_knife",
        "zhelva_shield"
    );

    @GameTest(template="empty")
    public static void all103CatalogIdsRemainRegistered(GameTestHelper t){
        t.assertTrue(IDS.size()==103,"Weapon catalog coverage changed");
        for(String id:IDS){
            var key=ResourceLocation.fromNamespaceAndPath("slavicmyths",id);
            var item=BuiltInRegistries.ITEM.get(key);
            t.assertTrue(item!=Items.AIR&&key.equals(BuiltInRegistries.ITEM.getKey(item)),
                "Missing or incorrect weapon registration: "+id);
            t.assertTrue(!item.getDefaultInstance().isEmpty(),"Cannot create weapon: "+id);
        }
        t.succeed();
    }
}
