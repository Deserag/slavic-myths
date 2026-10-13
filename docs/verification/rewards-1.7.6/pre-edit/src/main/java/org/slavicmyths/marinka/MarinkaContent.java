package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class MarinkaContent {
 public static final DeferredHolder<EntityType<?>,EntityType<DobrynyaEntity>> DOBRYNYA=ModEntities.ENTITIES.register("dobrynya_nikitich",()->EntityType.Builder.of(DobrynyaEntity::new,MobCategory.CREATURE).sized(.8F,2.2F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:dobrynya_nikitich"));
 public static final DeferredHolder<EntityType<?>,EntityType<MarinkaEntity>> MARINKA=ModEntities.ENTITIES.register("marinka",()->EntityType.Builder.of(MarinkaEntity::new,MobCategory.MONSTER).sized(.68F,1.96F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:marinka"));
 public static final DeferredHolder<EntityType<?>,EntityType<HexAnchor>> ANCHOR=ModEntities.ENTITIES.register("marinka_hex_focus",()->EntityType.Builder.of(HexAnchor::new,MobCategory.MISC).sized(1,1.8F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:marinka_hex_focus"));
 public static final DeferredHolder<EntityType<?>,EntityType<TurMinion>> TUR=ModEntities.ENTITIES.register("marinka_tur_minion",()->EntityType.Builder.of(TurMinion::new,MobCategory.MONSTER).sized(1.15F,1.7F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:marinka_tur_minion"));
 public static final DeferredHolder<EntityType<?>,EntityType<WitchBolt>> BOLT=ModEntities.ENTITIES.register("marinka_bolt",()->EntityType.Builder.<WitchBolt>of(WitchBolt::new,MobCategory.MISC).sized(.35F,.35F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:marinka_bolt"));
 public static final DeferredHolder<EntityType<?>,EntityType<WitchRune>> RUNE=ModEntities.ENTITIES.register("marinka_rune",()->EntityType.Builder.<WitchRune>of(WitchRune::new,MobCategory.MISC).sized(2.5F,.05F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:marinka_rune"));
 public static final DeferredHolder<net.minecraft.world.level.block.Block,WitchSeal> SEAL=ModBlocks.BLOCKS.register("witch_yard_altar",WitchSeal::new);
 public static final List<String> CUES=List.of("dobrynya.breath_idle_01","dobrynya.armor_step_01","dobrynya.spear_parry_01","dobrynya.shield_ward_01","dobrynya.magic_deflect_01","dobrynya.spear_swing_01","dobrynya.spear_impact_01","dobrynya.spear_sweep_01","dobrynya.unravel_hex_01","dobrynya.grunt_hurt_01","dobrynya.aurochs_transform_01","dobrynya.aurochs_charge_01","dobrynya.elixir_drink_01","dobrynya.hero_return_01","dobrynya.spear_swing_02","dobrynya.armor_step_02","marinka.witch_whisper_01","marinka.cloth_step_01","marinka.trace_hex_01","marinka.fire_charge_01","marinka.fire_release_01","marinka.shadow_dissolve_01","marinka.shadow_clone_break_01","marinka.barrier_raise_01","marinka.barrier_break_01","marinka.witch_blink_01","marinka.magpie_change_01","marinka.magpie_wings_01","marinka.aurochs_hex_01","marinka.enchanted_herd_01","marinka.witch_hurt_01","marinka.witch_rage_01","marinka.witch_defeat_01","marinka.witch_whisper_02","marinka.magpie_wings_02","arena.yard_wind_01","arena.ritual_circle_pulse_01","arena.hatch_open_01","arena.basement_ambience_01");
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {for(String key:CUES)SOUNDS.put(key,ModSounds.SOUNDS.register(key,()->SoundEvent.createVariableRangeEvent(id(key))));ModItems.ITEMS.register("witch_yard_altar",()->new net.minecraft.world.item.BlockItem(SEAL.get(),new net.minecraft.world.item.Item.Properties()));}
 public static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("slavicmyths",p);}public static SoundEvent sound(String key){return SOUNDS.get(key).get();}public static void init(){}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
 e.put(DOBRYNYA.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,230).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.35).build());
 e.put(MARINKA.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,600).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.65).build());
 e.put(ANCHOR.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,0).build());
 e.put(TUR.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,35).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.ATTACK_DAMAGE,5).build());}
 private MarinkaContent(){}
}
