package org.slavicmyths.skim;
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
public final class SkimContent {
 public static final DeferredHolder<EntityType<?>,EntityType<AlyoshaEntity>> ALYOSHA=ModEntities.ENTITIES.register("alyosha_popovich",()->EntityType.Builder.of(AlyoshaEntity::new,MobCategory.CREATURE).sized(.72F,2.15F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:alyosha_popovich"));
 public static final DeferredHolder<EntityType<?>,EntityType<SkimBeastEntity>> SKIM=ModEntities.ENTITIES.register("skim_beast",()->EntityType.Builder.of(SkimBeastEntity::new,MobCategory.MONSTER).sized(2.1F,2.95F).clientTrackingRange(14).updateInterval(1).build("slavicmyths:skim_beast"));
 public static final DeferredHolder<EntityType<?>,EntityType<AlyoshaArrow>> ARROW=ModEntities.ENTITIES.register("alyosha_arrow",()->EntityType.Builder.<AlyoshaArrow>of(AlyoshaArrow::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:alyosha_arrow"));
 public static final List<String> CUES=List.of("alyosha.summon","alyosha.footstep_soft_1","alyosha.footstep_soft_2","alyosha.taunt_1","alyosha.taunt_2","alyosha.dodge_swish_1","alyosha.dodge_swish_2","alyosha.saber_swing_1","alyosha.saber_swing_2","alyosha.saber_hit_1","alyosha.bow_draw","alyosha.arrow_release","alyosha.feint","alyosha.counter","alyosha.hurt_1","alyosha.hurt_2","alyosha.drink_tonic","alyosha.recover_breath","alyosha.victory","skim.ambient_1","skim.ambient_2","skim.step_1","skim.step_2","skim.run_paws","skim.claw_windup","skim.claw_hit","skim.leap_charge","skim.leap_land","skim.whistle_charge","skim.whistle_release","skim.roar","skim.grab","skim.grip_hold","skim.throw","skim.rage","skim.exhausted","skim.stone_collision","skim.hurt_1","skim.hurt_2","skim.death","arena.wind","arena.stone_impact","arena.den_stone_open","arena.camp_creak","items.alyosha_whistle","items.ilya_token","items.dobrynya_emblem","items.beast_horn");
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {
 for(String k:CUES)SOUNDS.put(k,ModSounds.SOUNDS.register(k,()->SoundEvent.createVariableRangeEvent(id(k))));
 for(String k:List.of("alyosha_call_whistle","ilya_call_token","dobrynya_call_emblem","beast_challenge_horn"))ModItems.ITEMS.register(k,()->new HeroCallItem(k));
 ModItems.ITEMS.register("skim_beast_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SKIM,0x3B2A25,0x9A3430,new net.minecraft.world.item.Item.Properties()));
 ModItems.ITEMS.register("idolishche_poganoe_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.bogatyr.BogatyrContent.IDOL,0x59413B,0xC1A48A,new net.minecraft.world.item.Item.Properties()));
 ModItems.ITEMS.register("marinka_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.marinka.MarinkaContent.MARINKA,0x2F222C,0x9B3351,new net.minecraft.world.item.Item.Properties()));
 }
 public static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("slavicmyths",p);}public static SoundEvent sound(String k){return SOUNDS.get(k).get();}public static void init(){}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
 e.put(ALYOSHA.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,175).add(Attributes.ARMOR,6).add(Attributes.MOVEMENT_SPEED,.34).add(Attributes.FOLLOW_RANGE,28).add(Attributes.KNOCKBACK_RESISTANCE,.12).build());
 e.put(SKIM.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,750).add(Attributes.ARMOR,8).add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,36).add(Attributes.KNOCKBACK_RESISTANCE,.85).build());
 }
 private SkimContent(){}
}
