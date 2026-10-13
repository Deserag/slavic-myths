package org.slavicmyths.finist;
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
public final class FinistContent {
 public static final DeferredHolder<EntityType<?>,EntityType<FinistEntity>> FINIST=ModEntities.ENTITIES.register("finist",()->EntityType.Builder.of(FinistEntity::new,MobCategory.CREATURE).sized(.76F,2.2F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:finist"));
 public static final DeferredHolder<EntityType<?>,EntityType<KartausEntity>> KARTAUS=ModEntities.ENTITIES.register("kartaus",()->EntityType.Builder.of(KartausEntity::new,MobCategory.MONSTER).sized(.92F,2.65F).clientTrackingRange(14).updateInterval(1).build("slavicmyths:kartaus"));
 public static final DeferredHolder<EntityType<?>,EntityType<BannerEntity>> BANNER=ModEntities.ENTITIES.register("kartaus_banner",()->EntityType.Builder.of(BannerEntity::new,MobCategory.MISC).sized(.85F,1.95F).clientTrackingRange(12).build("slavicmyths:kartaus_banner"));
 public static final DeferredHolder<EntityType<?>,EntityType<BannerEntity>> ANCHOR=ModEntities.ENTITIES.register("kartaus_curse_anchor",()->EntityType.Builder.of(BannerEntity::new,MobCategory.MISC).sized(.7F,.95F).clientTrackingRange(12).build("slavicmyths:kartaus_curse_anchor"));
 public static final List<String> CUES=List.of("entity.finist.summon_chime","entity.finist.mark_cast","entity.finist.wing_dash","entity.finist.inspire","entity.finist.transform","entity.finist.banner_break","entity.finist.hurt_1","entity.finist.hurt_2","entity.finist.hurt_3","entity.finist.retreat","entity.finist.tonic","entity.finist.victory","entity.finist.sword_swing_1","entity.finist.sword_swing_2","entity.finist.sword_contact","entity.finist.run_step_1","entity.finist.run_step_2","entity.falcon.scout_call","entity.falcon.wing_flap_1","entity.falcon.wing_flap_2","entity.falcon.dive_start","entity.falcon.dive_impact","entity.falcon.intercept","entity.falcon.chained","entity.falcon.chain_break","entity.falcon.perch","entity.falcon.morph_flight","entity.falcon.ambient_call","entity.kartaus.intro","entity.kartaus.banner_raise","entity.kartaus.summon_guard","entity.kartaus.staff_wave","entity.kartaus.staff_melee","entity.kartaus.chain_bind","entity.kartaus.black_eye_charge","entity.kartaus.black_eye_impact","entity.kartaus.curse_falcon","entity.kartaus.barrier_break","entity.kartaus.rage","entity.kartaus.hurt_1","entity.kartaus.hurt_2","entity.kartaus.hurt_3","entity.kartaus.death","entity.kartaus.heavy_step_1","entity.kartaus.heavy_step_2","entity.kartaus.staff_ground","item.items.finist_call","item.items.kartaus_seal","item.items.kartaus_egg","event.encounter.runic_emerge","event.encounter.black_banner_die","event.encounter.curse_anchor_die","event.encounter.victory_clear","event.encounter.ambient_wind");
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static{for(var k:CUES)SOUNDS.put(k,ModSounds.SOUNDS.register(k,()->SoundEvent.createVariableRangeEvent(id(k))));
 ModItems.ITEMS.register("finist_call_feather",()->new FalconCallItem(false));ModItems.ITEMS.register("kartaus_black_seal",()->new FalconCallItem(true));
 ModItems.ITEMS.register("kartaus_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(KARTAUS,0x242127,0xC65B31,new net.minecraft.world.item.Item.Properties()));}
 public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}public static SoundEvent sound(String s){return SOUNDS.get(s).get();}public static void init(){}
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
 e.put(FINIST.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,170).add(Attributes.ARMOR,12).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.12).build());
 e.put(KARTAUS.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,650).add(Attributes.ARMOR,10).add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.MOVEMENT_SPEED,.21).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.7).build());
 e.put(BANNER.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,45).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).build());
 e.put(ANCHOR.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,12).add(Attributes.MOVEMENT_SPEED,0).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
}
