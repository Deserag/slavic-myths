package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Uses GUI coordinates: an icon is exactly the vanilla inventory item's 16 GUI pixels. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT)
public final class WeaponSkillHud {
    private static final long VANILLA=Long.MIN_VALUE;
    private record Skill(String icon, String key, long remaining) {}
    // Approved 2026-11-23 marine-skill art; transparent 32px PNGs rendered at 16 GUI pixels.
    private static final Map<String, ResourceLocation> MARINE_ICONS = Map.of(
        "abyss_staff_shot", skillTexture("abyss_staff_shot"),
        "abyss_staff_bubble", skillTexture("abyss_staff_bubble"),
        "abyss_staff_vortex", skillTexture("abyss_staff_vortex"),
        "storm_anchor_slam", skillTexture("storm_anchor_slam"),
        "pharaohka_trident_wave", skillTexture("pharaohka_trident_wave"),
        "storm_harpoon_tether", skillTexture("storm_harpoon_tether")
    );
    // Approved group 2 art: four Morozko, two Karachun, two Neveya abilities.
    private static final Map<String, ResourceLocation> GROUP_TWO_ICONS = Map.of(
        "morozko_staff_bolt", skillTexture("morozko_staff_bolt"),
        "morozko_staff_stream", skillTexture("morozko_staff_stream"),
        "morozko_staff_icicles", skillTexture("morozko_staff_icicles"),
        "morozko_staff_wave", skillTexture("morozko_staff_wave"),
        "karachun_scythe_last_breath", skillTexture("karachun_scythe_last_breath"),
        "karachun_scythe_black_harvest", skillTexture("karachun_scythe_black_harvest"),
        "neveya_scythe_reap", skillTexture("neveya_scythe_reap"),
        "neveya_scythe_slash", skillTexture("neveya_scythe_slash")
    );
    // Group 3: approved art for Morovoy Staff, Nine Mor Spear and Nine Mor Axe.
    private static final Map<String, ResourceLocation> GROUP_THREE_ICONS = Map.of(
        "morovoy_staff_shot_mor", skillTexture("morovoy_staff_shot_mor"),
        "morovoy_staff_shot_fire", skillTexture("morovoy_staff_shot_fire"),
        "morovoy_staff_switch", skillTexture("morovoy_staff_switch"),
        "morovoy_staff_flock", skillTexture("morovoy_staff_flock"),
        "morovoy_staff_area", skillTexture("morovoy_staff_area"),
        "nine_mor_spear_spears", skillTexture("nine_mor_spear_spears"),
        "nine_mor_spear_sweep", skillTexture("nine_mor_spear_sweep"),
        "nine_mor_axe_rift", skillTexture("nine_mor_axe_rift"),
        "nine_mor_axe_execution", skillTexture("nine_mor_axe_execution")
    );
    // Group 4: heavy weapons. Two Gneteya resources are staged until abilities exist.
    private static final Map<String, ResourceLocation> GROUP_FOUR_ICONS = Map.of(
        "gneteya_crushing_blow", skillTexture("gneteya_crushing_blow"),
        "gneteya_bone_fracture_radial", skillTexture("gneteya_bone_fracture_radial"),
        "gneteya_sentence", skillTexture("gneteya_sentence"),
        "lomeya_single_wave", skillTexture("lomeya_single_wave"),
        "lomeya_dual_wave", skillTexture("lomeya_dual_wave"),
        "lomeya_dual_fracture", skillTexture("lomeya_dual_fracture"),
        "ashen_greatsword_blow", skillTexture("ashen_greatsword_blow"),
        "volcanic_heart_strike", skillTexture("volcanic_heart_strike")
    );
    // Native 32x32 sprites for groups 5-8, target modes and passive equipment cues.
    private static final Map<String, ResourceLocation> REMAINING_ICONS = Map.ofEntries(
        Map.entry("navya_ostroga_lunge", skillTexture("navya_ostroga_lunge")),
        Map.entry("shadow_throwing_knife_curse", skillTexture("shadow_throwing_knife_curse")),
        Map.entry("infernal_bow_flame_arrow", skillTexture("infernal_bow_flame_arrow")),
        Map.entry("empowered_infernal_bow_chainfire", skillTexture("empowered_infernal_bow_chainfire")),
        Map.entry("storm_staff_thunder_pulse", skillTexture("storm_staff_thunder_pulse")),
        Map.entry("veles_staff_forest_guardian", skillTexture("veles_staff_forest_guardian")),
        Map.entry("likho_staff_morok_eye", skillTexture("likho_staff_morok_eye")),
        Map.entry("poloz_whip_spikes", skillTexture("poloz_whip_spikes")),
        Map.entry("poloz_whip_earth_rift", skillTexture("poloz_whip_earth_rift")),
        Map.entry("trophy_kalin_sabre_sweeping_strike", skillTexture("trophy_kalin_sabre_sweeping_strike")),
        Map.entry("nightingale_dagger_wind_gust", skillTexture("nightingale_dagger_wind_gust")),
        Map.entry("pool_spear_underwater_dash", skillTexture("pool_spear_underwater_dash")),
        Map.entry("mytar_shackled_sickle_throw", skillTexture("mytar_shackled_sickle_throw")),
        Map.entry("mytar_shackled_sickle_chain_break", skillTexture("mytar_shackled_sickle_chain_break")),
        Map.entry("nine_mor_shield_soul_dome", skillTexture("nine_mor_shield_soul_dome")),
        Map.entry("three_breath_crossbow_triple_volley", skillTexture("three_breath_crossbow_triple_volley")),
        Map.entry("three_breath_crossbow_mode_switch", skillTexture("three_breath_crossbow_mode_switch")),
        Map.entry("finist_bow_homing_target", skillTexture("finist_bow_homing_target")),
        Map.entry("finist_bow_manual_aim", skillTexture("finist_bow_manual_aim")),
        Map.entry("victor_spear_marked_followup", skillTexture("victor_spear_marked_followup")),
        Map.entry("thunder_axe_passive_lightning", skillTexture("thunder_axe_passive_lightning")),
        Map.entry("thunder_spear_passive_discharge", skillTexture("thunder_spear_passive_discharge")),
        Map.entry("zhelva_shield_perfect_guard", skillTexture("zhelva_shield_perfect_guard")),
        Map.entry("serpent_crossbow_serpent_bolt", skillTexture("serpent_crossbow_serpent_bolt"))
    );
    private static ResourceLocation skillTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/gui/weapon_skills/" + name + ".png");
    }
    @SubscribeEvent public static void draw(RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui
                ||mc.player.isSpectator()||mc.getCameraEntity()!=mc.player)return;
        var stack=mc.player.getMainHandItem();
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!id.getNamespace().equals("slavicmyths"))return;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        long now=mc.level.getGameTime();
        List<Skill> skills=new ArrayList<>();
        switch(id.getPath()) {
            case "scorpion_double_sting" -> {
                skills.add(new Skill("scorpion_double_strike","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("scorpion_double_sting",1))-now));
            }
            case "stinger_dagger" -> {
                skills.add(new Skill("dagger_throw","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("stinger_dagger",1))-now));
            }
            case "sand_sling" -> {
                skills.add(new Skill("sling_shot","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("sand_sling",1))-now));
            }
            case "desert_glass_blade" -> {
                skills.add(new Skill("glass_dash","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("desert_glass_blade",1))-now));
            }
            case "hala_solar_staff" -> {
                skills.add(new Skill("hala_beam","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"hala_solar_staff",false,now)));
                skills.add(new Skill("hala_burst","S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"hala_solar_staff",true,now)));
            }
            case "dune_mace" -> {
                skills.add(new Skill("dune_wave","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("dune_mace",1))-now));
                skills.add(new Skill("dune_smash","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("dune_mace",2))-now));
            }
            case "mirage_bow" -> {
                skills.add(new Skill("mirage_trishot","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("mirage_bow",1))-now));
                skills.add(new Skill("mirage_pierce","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("mirage_bow",2))-now));
            }
            case "quicksand_whip" -> {
                skills.add(new Skill("whip_pull","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("quicksand_whip",1))-now));
                skills.add(new Skill("whip_sweep","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("quicksand_whip",2))-now));
            }
            case "colony_scepter" -> {
                skills.add(new Skill("colony_summon","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("colony_scepter",1))-now));
                skills.add(new Skill("colony_swarm","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("colony_scepter",2))-now));
                skills.add(new Skill("colony_command","S+L",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("colony_scepter",3))-now));
            }
            case "four_winds_fists" -> {
                skills.add(new Skill("winds_punch","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("four_winds_fists",1))-now));
                skills.add(new Skill("winds_barrage","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("four_winds_fists",2))-now));
                skills.add(new Skill("winds_crush","S+L",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("four_winds_fists",3))-now));
            }
            case "lamya_weapon" -> {
                skills.add(new Skill("lamya_fire","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("lamya_weapon",1))-now));
                skills.add(new Skill("lamya_solar","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("lamya_weapon",2))-now));
                skills.add(new Skill("lamya_vortex","S+L",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("lamya_weapon",3))-now));
            }
            case "buried_hammer" -> {
                skills.add(new Skill("hammer_slam","R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("buried_hammer",1))-now));
                skills.add(new Skill("hammer_quicksand","S+R",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("buried_hammer",2))-now));
                skills.add(new Skill("hammer_pillars","S+L",tag.getLong(org.slavicmyths.desert.DesertWeaponItem.cooldownKey("buried_hammer",3))-now));
            }
            case "ice_healing_staff", "fire_healing_staff" -> {
                String shared=org.slavicmyths.combat.IndependentWeaponCooldowns.HEALING_STAFF;
                skills.add(new Skill(id.getPath().equals("ice_healing_staff")?"ice_heal_self":"fire_heal_self","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,shared,false,now)));
                skills.add(new Skill(id.getPath().equals("ice_healing_staff")?"ice_heal_area":"fire_heal_area","S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,shared,true,now)));
            }
            case "ice_captain_axes" -> {
                skills.add(new Skill("winter_captain_throw","R",tag.getLong("CD_winter_captain_1")-now));
                skills.add(new Skill("winter_captain_crush","S+L",tag.getLong("CD_winter_captain_2")-now));
                skills.add(new Skill("winter_captain_vortex","S+R",tag.getLong("CD_winter_captain_3")-now));
            }
            case "snow_wanderer_hammers" -> {
                skills.add(new Skill("winter_hammer_wave","R",tag.getLong("CD_winter_hammers_1")-now));
                skills.add(new Skill("winter_hammer_crush","S+L",tag.getLong("CD_winter_hammers_2")-now));
            }
            case "absolute_silence_sword" -> {
                skills.add(new Skill("winter_silence_wave","R",tag.getLong("CD_winter_silence_1")-now));
                skills.add(new Skill("winter_silence_seven","S+L",tag.getLong("CD_winter_silence_2")-now));
                skills.add(new Skill("winter_silence_execution","S+R",tag.getLong("CD_winter_silence_3")-now));
            }
            case "silence_blades" -> {
                skills.add(new Skill("winter_blades_throw","R",tag.getLong("CD_winter_BLADES_1")-now));
                skills.add(new Skill("winter_blades_behind","S+R",tag.getLong("CD_winter_BLADES_3")-now));
            }
            case "northern_beast_claws" -> {
                skills.add(new Skill("winter_claw_dash","R",tag.getLong("CD_winter_CLAWS_1")-now));
                skills.add(new Skill("winter_claw_flurry","S+L",tag.getLong("CD_winter_CLAWS_2")-now));
            }
            case "ice_sword" -> {
                skills.add(new Skill("winter_sword_wave","R",tag.getLong("CD_winter_SWORD_1")-now));
            }
            case "ice_axe" -> {
                skills.add(new Skill("winter_axe_crush","R",tag.getLong("CD_winter_AXE_1")-now));
            }
            case "ice_mace" -> {
                skills.add(new Skill("winter_mace_slam","R",tag.getLong("CD_winter_MACE_1")-now));
            }
            case "ice_halberd" -> {
                skills.add(new Skill("winter_halberd_sweep","R",tag.getLong("CD_winter_HALBERD_1")-now));
                skills.add(new Skill("winter_halberd_projectile","S+R",tag.getLong("CD_winter_HALBERD_3")-now));
            }
            case "ice_scythe" -> {
                skills.add(new Skill("winter_scythe_harvest","R",tag.getLong("CD_winter_SCYTHE_1")-now));
            }
            case "winter_hunt_bow" -> {
                skills.add(new Skill("winter_bow_draw","R",0));
                skills.add(new Skill("winter_bow_ice","S+L",tag.getLong("CD_winter_bow_2")-now));
                skills.add(new Skill("winter_bow_volley","S+R",tag.getLong("CD_winter_bow_3")-now));
            }
            case "ice_crossbow" -> {
                skills.add(new Skill("winter_crossbow_load","R",0));
                skills.add(new Skill("winter_crossbow_ice","S+R",tag.getLong("CD_winter_crossbow_charge")-now));
                skills.add(new Skill("winter_crossbow_shot","S+L",tag.getLong("CD_winter_crossbow_fire")-now));
            }
            case "ice_queen_spear_staff" -> {
                boolean staff=org.slavicmyths.winter.WinterLegendItem.staffMode(stack);
                skills.add(new Skill("winter_queen_switch","R",tag.getLong("CD_winter_mode")-now));
                if(staff)skills.add(new Skill("winter_queen_bolt","L",tag.getLong("CD_winter_staff_0")-now));
                skills.add(new Skill(staff?"winter_queen_spikes":"winter_queen_dash","S+L",tag.getLong(staff?"CD_winter_staff_2":"CD_winter_spear_2")-now));
                skills.add(new Skill(staff?"winter_queen_golem":"winter_queen_sweep","S+R",tag.getLong(staff?"CD_winter_staff_3":"CD_winter_spear_3")-now));
            }
            case "abyss_staff" -> {
                skills.add(new Skill("abyss_staff_shot","R",tag.getLong("MarineWeapon_staffShot")-now));
                skills.add(new Skill("abyss_staff_bubble","S+L",tag.getLong("MarineWeapon_bubble")-now));
                skills.add(new Skill("abyss_staff_vortex","S+R",tag.getLong("MarineWeapon_vortex")-now));
            }
            case "storm_anchor" -> {
                skills.add(new Skill("chain","R",tag.getLong("MarineWeapon_anchor")-now));
                skills.add(new Skill("storm_anchor_slam","S+R",tag.getLong("MarineWeapon_slam")-now));
            }
            case "pharaohka_trident" -> skills.add(new Skill("pharaohka_trident_wave","S+R",tag.getLong("MarineWeapon_wave")-now));
            case "storm_harpoon" -> {
                skills.add(new Skill("shot","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"storm_harpoon",false,now)));
                skills.add(new Skill("storm_harpoon_tether","S+R",Math.max(tag.getLong("MarineHarpoonReady")-now,org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"storm_harpoon",true,now))));
            }
            case "morozko_staff" -> {
                skills.add(new Skill("morozko_staff_bolt","L",FrostClient.skillRemaining("BoltReady")));
                skills.add(new Skill("morozko_staff_stream","R",FrostClient.skillRemaining("StreamReady")));
                skills.add(new Skill("morozko_staff_icicles","S+L",FrostClient.skillRemaining("IcicleReady")));
                skills.add(new Skill("morozko_staff_wave","S+R",FrostClient.skillRemaining("WaveReady")));
            }
            case "karachun_scythe" -> {
                skills.add(new Skill("karachun_scythe_last_breath","S+L",NightClient.skillRemaining("BreathReady")));
                skills.add(new Skill("karachun_scythe_black_harvest","S+R",NightClient.skillRemaining("HarvestReady")));
            }
            case "morovoy_staff" -> {
                String shotIcon = tag.getInt("MorovoyStaffMode") == 1
                    ? "morovoy_staff_shot_fire" : "morovoy_staff_shot_mor";
                skills.add(new Skill(shotIcon,"L",tag.getLong("MorStaff25_bolt")-now));
                skills.add(new Skill("morovoy_staff_switch","R",0));
                skills.add(new Skill("morovoy_staff_flock","S+L",tag.getLong("MorStaff25_flock")-now));
                skills.add(new Skill("morovoy_staff_area","S+R",tag.getLong("MorStaff25_area")-now));
            }
            case "gneteya_great_hammer" -> {
                skills.add(new Skill("gneteya_crushing_blow", "S+R", tag.getLong("GneteyaSkillSlamReady") - now));
                skills.add(new Skill("gneteya_bone_fracture_radial", "S+L", tag.getLong("GneteyaSkillFractureReady") - now));
                skills.add(new Skill("gneteya_sentence", "R", tag.getLong("GneteyaSkillSentenceReady") - now));
            }
            case "neveya_scythe" -> {
                skills.add(new Skill("neveya_scythe_reap","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.NEVEYA,false,now)));skills.add(new Skill("neveya_scythe_slash","S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.NEVEYA,true,now)));
            }
            case "lomeya_hammer", "lomeya_hammer_offhand" -> {
                boolean dual = stack.is(org.slavicmyths.sisters.SistersContent.LOMEYA_HAMMER.get());
                skills.add(new Skill(dual ? "lomeya_dual_wave" : "lomeya_single_wave", "R", org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.LOMEYA,false,now)));
                if (dual) skills.add(new Skill("lomeya_dual_fracture", "S+R", org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.LOMEYA,true,now)));
            }
            case "nine_mor_spear" -> {
                skills.add(new Skill("nine_mor_spear_spears","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.SPEAR,false,now)));
                skills.add(new Skill("nine_mor_spear_sweep","S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.SPEAR,true,now)));
            }
            case "nine_mor_axe" -> {
                skills.add(new Skill("nine_mor_axe_rift","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.AXE,false,now)));
                skills.add(new Skill("nine_mor_axe_execution","S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.AXE,true,now)));
            }
            case "ashen_greatsword" ->
                skills.add(new Skill("ashen_greatsword_blow", "S+R", Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_ashen_greatsword"))-now));
            case "volcanic_heart_flail" -> {
                skills.add(new Skill("shot","R",VANILLA));
                skills.add(new Skill("volcanic_heart_strike", "S+R", Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_volcanic_heart_flail"))-now));
            }
            case "navya_ostroga" ->
                skills.add(new Skill("navya_ostroga_lunge", "R",Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_navya_ostroga"))-now));
            case "shadow_throwing_knife" -> {
                skills.add(new Skill("shot","R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"shadow_throwing_knife",false,now)));
                skills.add(new Skill("shadow_throwing_knife_curse", "S+R",Math.max(Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_shadow_throwing_knife"))-now,org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,"shadow_throwing_knife",true,now))));
            }
            case "infernal_bow" ->
                skills.add(new Skill("infernal_bow_flame_arrow", "S+R",Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_infernal_bow"))-now));
            case "empowered_infernal_bow" ->
                skills.add(new Skill("empowered_infernal_bow_chainfire", "S+R",Math.max(tag.getLong("NetherHudReady"),tag.getLong("slavicmyths_skill_empowered_infernal_bow"))-now));
            case "storm_staff" ->
                skills.add(new Skill("storm_staff_thunder_pulse", "R",VANILLA));
            case "veles_staff" ->
                skills.add(new Skill("veles_staff_forest_guardian", "R",VANILLA));
            case "likho_staff" ->
                skills.add(new Skill("likho_staff_morok_eye", "R",VANILLA));
            case "poloz_whip" -> {
                skills.add(new Skill("poloz_whip_spikes", "R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.POLOZ,false,now)));
                skills.add(new Skill("poloz_whip_earth_rift", "S+R",org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.POLOZ,true,now)));
            }
            case "trophy_kalin_sabre" -> {
                skills.add(new Skill("trophy_kalin_sabre_sweeping_strike","R",tag.getLong("KalinWaveReady")-now));
                skills.add(new Skill("summon","S+R",tag.getLong("KalinSummonReady")-now));
                skills.add(new Skill("crit","",0));
            }
            case "nightingale_dagger" ->
                skills.add(new Skill("nightingale_dagger_wind_gust", "R",VANILLA));
            case "pool_spear" ->
                skills.add(new Skill("pool_spear_underwater_dash", "R",VANILLA));
            case "mytar_shackled_sickle" -> {
                skills.add(new Skill("mytar_shackled_sickle_throw", "R", org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.MYTAR,false,now)));
                skills.add(new Skill("mytar_shackled_sickle_chain_break", "S+R", org.slavicmyths.combat.IndependentWeaponCooldowns.remaining(tag,org.slavicmyths.combat.IndependentWeaponCooldowns.MYTAR,true,now)));
            }
            case "nine_mor_shield" ->
                skills.add(new Skill("nine_mor_shield_soul_dome", "S+R",tag.getLong("SlavicNineMorDomeReady")-now));
            case "three_breath_crossbow" -> {
                skills.add(new Skill("three_breath_crossbow_triple_volley", "R",0));
                skills.add(new Skill("three_breath_crossbow_mode_switch", "S+R", 0));
                skills.add(new Skill("mode","S+L",0));
            }
            case "serpent_crossbow" ->
                skills.add(new Skill("serpent_crossbow_serpent_bolt", "R",VANILLA));
            case "finist_bow" -> {
                skills.add(new Skill("finist_bow_homing_target", "R",0));
                skills.add(new Skill("finist_bow_manual_aim", "S+R",0));
            }
            case "victor_spear" ->
                skills.add(new Skill("victor_spear_marked_followup", "L",VANILLA));
            case "thunder_axe" ->
                skills.add(new Skill("thunder_axe_passive_lightning", "",VANILLA));
            case "thunder_spear" ->
                skills.add(new Skill("thunder_spear_passive_discharge", "",VANILLA));
            case "zhelva_shield" ->
                skills.add(new Skill("zhelva_shield_perfect_guard", "R",tag.getLong("MarineWeapon_perfectBlock")-now));
            default -> {
                if(!WeaponTooltips.isWeapon(id.getPath()))return;
                boolean chargedWeapon=stack.getItem() instanceof org.slavicmyths.combat.FlailItem
                    ||stack.getItem() instanceof org.slavicmyths.combat.ThrowingWeaponItem;
                if(!chargedWeapon&&!mc.player.getCooldowns().isOnCooldown(stack.getItem()))return;
                skills.add(new Skill("shot","",VANILLA));
            }
        }
        var g=event.getGuiGraphics();
        // Keep the panel against the left edge, above the hotbar/offhand even on narrow GUI sizes.
        int x=6,y=g.guiHeight()-44;
        for(var skill:skills){
            g.fill(x-1,y-1,x+17,y+17,0xC0181B25);
            icon(g,skill.icon,x,y);
            float shared=skill.remaining==VANILLA?mc.player.getCooldowns().getCooldownPercent(stack.getItem(),0):0;
            if(shared>0)g.fill(x,y+16-(int)Math.ceil(16*shared),x+16,y+16,0xB0000000);
            if(skill.remaining>0){
                g.fill(x,y,x+16,y+16,0xA0000000);
                String seconds=Long.toString((skill.remaining+19)/20);
                g.drawCenteredString(mc.font,seconds,x+8,y+4,0xFFFFFF);
            }
            if(id.getPath().equals("storm_harpoon"))for(int i=0;i<4;i++)
                g.fill(x+i*4,y+16,x+i*4+3,y+18,i<tag.getInt("MarineHarpoonHits")?0xFF89E1F2:0xFF454955);
            if(!skill.key.isEmpty()){
                if(DESERT_ICONS.contains(skill.icon))mouseBinding(g,skill.key,x+8,y-11);
                else g.drawCenteredString(mc.font,skill.key,x+8,y-9,0xFFE4F7FF);
            }
            x+=22;
        }
    }
    private static void mouseBinding(GuiGraphics g,String key,int x,int y){
        boolean shift=key.startsWith("S+"),left=key.endsWith("L");int a=x-(shift?1:4);
        if(shift){g.fill(x-8,y+3,x-4,y+4,0xFFE4F7FF);g.fill(x-7,y+2,x-5,y+8,0xFFE4F7FF);g.fill(x-6,y+1,x-5,y+3,0xFFE4F7FF);}
        g.fill(a,y,a+8,y+9,0xFFB6C5CB);g.fill(a+1,y+1,a+7,y+8,0xFF283138);
        g.fill(a+1+(left?0:3),y+1,a+4+(left?0:3),y+4,0xFFFFD779);g.fill(a+3,y+1,a+4,y+5,0xFF182127);
    }
    private static final java.util.List<String> WINTER_ICONS=java.util.List.of("winter_captain_throw","winter_captain_crush","winter_captain_vortex","winter_hammer_wave","winter_hammer_crush","winter_silence_wave","winter_silence_seven","winter_silence_execution","winter_blades_throw","winter_blades_behind","winter_claw_dash","winter_claw_flurry","winter_sword_wave","winter_axe_crush","winter_mace_slam","winter_halberd_sweep","winter_halberd_projectile","winter_scythe_harvest","winter_bow_draw","winter_bow_ice","winter_bow_volley","winter_crossbow_load","winter_crossbow_ice","winter_crossbow_shot","winter_queen_switch","winter_queen_dash","winter_queen_sweep","winter_queen_bolt","winter_queen_spikes","winter_queen_golem");
    private static final java.util.List<String> DESERT_ICONS=java.util.List.of("scorpion_double_strike","dagger_throw","sling_shot","glass_dash","hala_beam","hala_burst","dune_wave","dune_smash","mirage_trishot","mirage_pierce","whip_pull","whip_sweep","colony_summon","colony_swarm","colony_command","winds_punch","winds_barrage","winds_crush","lamya_fire","lamya_solar","lamya_vortex","hammer_slam","hammer_quicksand","hammer_pillars","ice_heal_self","ice_heal_area","fire_heal_self","fire_heal_area");
    private static void icon(GuiGraphics g,String kind,int x,int y){
        int desert=DESERT_ICONS.indexOf(kind);
        if(desert>=0){g.blit(skillTexture("desert_atlas"),x,y,(float)(desert%8*16),(float)(desert/8*16),16,16,128,64);return;}
        int winter=WINTER_ICONS.indexOf(kind);
        if(winter>=0){
            g.blit(skillTexture("winter_atlas"),x,y,(float)(winter%8*16),(float)(winter/8*16),16,16,128,64);
            return;
        }
        ResourceLocation texture = MARINE_ICONS.get(kind);
        if (texture == null) texture = GROUP_TWO_ICONS.get(kind);
        if (texture == null) texture = GROUP_THREE_ICONS.get(kind);
        if (texture == null) texture = GROUP_FOUR_ICONS.get(kind);
        if (texture == null) texture = REMAINING_ICONS.get(kind);
        if (texture != null) {
            // UV covers the full image; GUI size remains 16x16 and follows Minecraft GUI scale.
            g.blit(texture, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            return;
        }
        String[] rows=switch(kind){
            case "healing_cross" -> new String[]{"        ","   ##   ","   ##   "," ###### "," ###### ","   ##   ","   ##   ","        "};
            case "healing_area" -> new String[]{"  ####  "," ##  ## ","#  ##  #","# #### #","# #### #","#  ##  #"," ##  ## ","  ####  "};
            case "summon" -> new String[]{" #   #  "," ##  ## ","  # #   ","   #    ","  # #   "," #   #  ","#     # ","        "};
            case "crit" -> new String[]{"   ##   ","   ##   ","#  ##  #"," ###### ","  ####  "," ###### ","#  ##  #","   ##   "};
            case "shot" -> new String[]{"   ##   ","  ####  ","   ##   ","   ##   ","   ##   "," # ## # ","  ####  ","   ##   "};
            case "bubble" -> new String[]{"  ####  "," #    # ","#  #   #","#      #","#      #","#      #"," #    # ","  ####  "};
            case "vortex" -> new String[]{"  ##### "," ##   ##","## ##  #","# #  # #","# # ## #","#  ### #","##    ##"," #####  "};
            case "ice" -> new String[]{"  #  #  "," ### ## "," ### ## "," ### ## ","  #  #  ","  #  #  ","  #     ","        "};
            case "wave" -> new String[]{"    ### ","   #   #","  ##    ","  #     "," ##     ","##   #  ","########"," ###### "};
            case "chain" -> new String[]{" ##     ","#  #    ","# ##    "," ###    ","   ###  ","   ## # ","    #  #","     ## "};
            case "raven" -> new String[]{"#      #","##    ##","###  ###"," ###### ","  ####  ","   ##   ","  ####  "," #    # "};
            case "fire" -> new String[]{"    #   ","   ##   "," # ###  "," #### # ","####### ","### ### "," ## ##  ","  ###   "};
            case "mode" -> new String[]{"   ##   "," ###### ","#  ##  #","      # "," #      ","#  ##  #"," ###### ","   ##   "};
            case "slash" -> new String[]{"      ##","     ## ","    ##  ","   ##   ","  ##    "," ###    ","## ##   ","#   #   "};
            case "harvest" -> new String[]{" #####  ","#     # ","     ## ","    ##  ","   ##   ","  ##    "," ##     ","##      "};
            case "breath" -> new String[]{"##      "," ### ## ","   ##  #","###     ","   #### ","##     #","  ##### ","        "};
            default -> new String[]{"   ##   ","   ##   ","#  ##  #"," # ## # ","  ####  ","   ##   ","########"," # ## # "};
        };
        int color=switch(kind){case "fire","slam"->0xFFFFAE55;case "raven","harvest","breath"->0xFFC298ED;default->0xFF89E1F2;};
        for(int row=0;row<8;row++)for(int col=0;col<8;col++)if(rows[row].charAt(col)=='#')
            g.fill(x+col*2,y+row*2,x+col*2+2,y+row*2+2,color);
    }
    private WeaponSkillHud(){}
}
