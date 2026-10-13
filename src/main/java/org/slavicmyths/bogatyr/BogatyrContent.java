package org.slavicmyths.bogatyr;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class BogatyrContent {
    public static final DeferredHolder<EntityType<?>,EntityType<IlyaEntity>> ILYA=ModEntities.ENTITIES.register("ilya_muromets",()->EntityType.Builder.of(IlyaEntity::new,MobCategory.CREATURE).sized(.93F,2.45F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:ilya_muromets"));
    public static final DeferredHolder<EntityType<?>,EntityType<IdolishcheEntity>> IDOL=ModEntities.ENTITIES.register("idolishche_poganoe",()->EntityType.Builder.of(IdolishcheEntity::new,MobCategory.MONSTER).sized(4.2F,8.8F).clientTrackingRange(12).updateInterval(1).fireImmune().build("slavicmyths:idolishche_poganoe"));
    public static final List<String> HERO_CUES=List.of("ambient_breath_01","herbal_drink_01","mace_impact_01","mace_swing_01","pain_nonverbal_01","pain_nonverbal_02","recover_breath_01","retreat_breath_01","return_nonverbal_01","shield_block_01","shield_block_02","shield_raise_01","step_heavy_01","taunt_nonverbal_01","unyielding_nonverbal_01");
    public static final List<String> BOSS_CUES=List.of("death_01","eat_01","grasp_01","hurt_01","hurt_02","idle_growl_01","idle_growl_02","minions_summon_01","poison_breath_01","punch_impact_01","rage_01","roar_01","rush_charge_01","step_heavy_01","stomp_mid_01","stomp_near_01","stomp_outer_01","throw_01");
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
    static {for(String who:List.of("ilya_muromets","idolishche_poganoe"))for(String cue:who.equals("ilya_muromets")?HERO_CUES:BOSS_CUES){String key=who+"."+cue;SOUNDS.put(key,ModSounds.SOUNDS.register(key,()->SoundEvent.createVariableRangeEvent(id(key))));}}
    public static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
    public static SoundEvent sound(String who,String cue){return SOUNDS.get(who+"."+cue).get();}
    public static void init(){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
        e.put(ILYA.get(),net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH,280).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,.5).build());
        e.put(IDOL.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,900).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.ATTACK_DAMAGE,20).add(Attributes.FOLLOW_RANGE,72).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,1).build());
    }
    private BogatyrContent(){}
}
