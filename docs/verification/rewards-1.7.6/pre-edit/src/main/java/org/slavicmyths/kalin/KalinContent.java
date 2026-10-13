package org.slavicmyths.kalin;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class KalinContent {
 public static final DeferredHolder<EntityType<?>,EntityType<KalinEntity>> KALIN=ModEntities.ENTITIES.register("kalin_tsar",()->EntityType.Builder.of(KalinEntity::new,MobCategory.MONSTER).sized(1.05F,2.8F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:kalin_tsar"));
 public static final DeferredHolder<EntityType<?>,EntityType<WarHorse>> HORSE=ModEntities.ENTITIES.register("kalin_war_horse",()->EntityType.Builder.of(WarHorse::new,MobCategory.CREATURE).sized(1.65F,3.2F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:kalin_war_horse"));
 public static final DeferredHolder<EntityType<?>,EntityType<KalinSoldier>> COMMON=ModEntities.ENTITIES.register("kalin_common_soldier",()->EntityType.Builder.of(KalinSoldier::new,MobCategory.MONSTER).sized(.65F,1.95F).clientTrackingRange(12).build("slavicmyths:kalin_common_soldier"));
 public static final DeferredHolder<EntityType<?>,EntityType<KalinSoldier>> ELITE=ModEntities.ENTITIES.register("kalin_elite_soldier",()->EntityType.Builder.of(KalinSoldier::new,MobCategory.MONSTER).sized(.8F,2.2F).clientTrackingRange(12).build("slavicmyths:kalin_elite_soldier"));
 public static final List<String> CUES=List.of("banner_flap","battle_seal_use","camp_fire_loop_short","encounter_start_fx","horse_gallop_loop","horse_rear","horse_snort","horse_step_01","horse_step_02","horse_stomp","kalin_command_shout_fx","kalin_kneel_surrender","kalin_parry","kalin_rage_burst","kalin_sabre_heavy","kalin_sabre_light","kalin_spin_attack","kalin_step_heavy_01","kalin_step_heavy_02","kalin_war_horn","soldier_spawn_common","soldier_spawn_elite","ui_select_two_heroes");
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> sounds=new HashMap<>();
 static {for(var k:CUES)sounds.put(k,ModSounds.SOUNDS.register("kalin."+k,()->SoundEvent.createVariableRangeEvent(id("kalin."+k))));
 ModItems.ITEMS.register("kalin_battle_seal",KalinSeal::new);
 ModItems.ITEMS.register("kalin_tsar_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(KALIN,0x222326,0x9B2833,new Item.Properties()));
 ModItems.ITEMS.register("kalin_war_horse_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(HORSE,0x302326,0xB59852,new Item.Properties()));}
 public static ResourceLocation id(String k){return ResourceLocation.fromNamespaceAndPath("slavicmyths",k);}public static SoundEvent sound(String k){return sounds.get(k).get();}public static void init(){}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
 e.put(KALIN.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,4).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,40).build());
 e.put(HORSE.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,300).add(Attributes.ARMOR,16).add(Attributes.MOVEMENT_SPEED,.32).add(Attributes.KNOCKBACK_RESISTANCE,1).build());
 e.put(COMMON.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.ARMOR,4).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FOLLOW_RANGE,40).build());
 e.put(ELITE.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,65).add(Attributes.ARMOR,12).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.FOLLOW_RANGE,40).build());}
}
