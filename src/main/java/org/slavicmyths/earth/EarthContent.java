package org.slavicmyths.earth;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class EarthContent {
    public static final DeferredHolder<EntityType<?>,EntityType<EarthSpirit>> BOSS=ModEntities.ENTITIES.register("earth_spirit",()->EntityType.Builder.of(EarthSpirit::new,MobCategory.MONSTER).sized(3.1F,6.35F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:earth_spirit"));
    public static final DeferredHolder<EntityType<?>,EntityType<EarthServant>> SERVANT=ModEntities.ENTITIES.register("earth_servant",()->EntityType.Builder.of(EarthServant::new,MobCategory.MONSTER).sized(.85F,1.4F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:earth_servant"));
    public static final DeferredHolder<EntityType<?>,EntityType<EarthActor>> ACTOR=ModEntities.ENTITIES.register("earth_encounter_actor",()->EntityType.Builder.<EarthActor>of(EarthActor::new,MobCategory.MISC).sized(.6F,.6F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:earth_encounter_actor"));
    public static final DeferredHolder<Block,Block> SEAL=ModBlocks.BLOCKS.register("earth_heart_seal",EarthSeal::new);
    public static final DeferredHolder<Block,Block> PILLAR=ModBlocks.BLOCKS.register("earth_pillar_stone",()->new Block(BlockBehaviour.Properties.of().strength(3,1200).sound(SoundType.STONE)));
    public static final DeferredHolder<Block,Block> SHELTER=ModBlocks.BLOCKS.register("earth_shelter_stone",()->new Block(BlockBehaviour.Properties.of().strength(-1,3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).sound(SoundType.STONE)));
    public static final DeferredHolder<Block,Block> GATE=ModBlocks.BLOCKS.register("earth_gate_stone",()->new Block(BlockBehaviour.Properties.of().strength(-1,3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK).sound(SoundType.STONE)));
    public static final DeferredHolder<net.minecraft.world.effect.MobEffect,net.minecraft.world.effect.MobEffect> IMBALANCE=ModEffects.EFFECTS.register("earth_imbalance",()->new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL,0xa0784d){}.addAttributeModifier(Attributes.MOVEMENT_SPEED,id("earth_imbalance_speed"),-.5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Item,Item> RUNE=ModItems.ITEMS.register("rune_earth_heart_iii",()->new Item(new Item.Properties().fireResistant()){
        @Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> out,TooltipFlag flag){out.addAll(EarthRune.tooltip());}
    });
    public static final List<String> CUES=List.of("ambient_1","ambient_2","armor_activate","awaken_rumble","death_fall","footstep_1","footstep_2","gate_close","melee_impact","melee_windup","phase_change","pillar_grip","pillar_impact","pillar_rip","pillar_throw","roar_1","roar_2","rune_reward","seal_break","servant_death","servant_emerge","servant_hit","servant_idle","shockwave","summon_warning","tremor_impact","tremor_warning","volley_charge","volley_impact","volley_release");
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    static {
        for(String cue:CUES)SOUNDS.put(cue,ModSounds.SOUNDS.register("earth_spirit."+cue,()->SoundEvent.createVariableRangeEvent(id("earth_spirit."+cue))));
        ModItems.ITEMS.register("earth_servant_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(SERVANT,0x5b4736,0xa66a39,new Item.Properties()));
        Map.of("earth_heart_seal",SEAL,"earth_pillar_stone",PILLAR,"earth_shelter_stone",SHELTER,"earth_gate_stone",GATE).forEach((key,block)->ModItems.ITEMS.register(key,()->new BlockItem(block.get(),new Item.Properties()){@Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> out,TooltipFlag flag){if(key.equals("earth_heart_seal"))out.add(net.minecraft.network.chat.Component.translatable("earth.slavicmyths.block_hint"));}}));
    }
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    public static SoundEvent sound(String cue){return SOUNDS.get(cue).get();}
    public static void init(){org.slavicmyths.rpg.RuneDefinition.ALL.put("earth_heart_iii",new org.slavicmyths.rpg.RuneDefinition("earth_heart_iii",Map.of(),Set.of("armor")));}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){
        e.put(BOSS.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,EarthRules.HEALTH).add(Attributes.MOVEMENT_SPEED,EarthRules.SPEED).add(Attributes.ATTACK_DAMAGE,EarthRules.MELEE_RAW).add(Attributes.FOLLOW_RANGE,38).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,1).build());
        e.put(SERVANT.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,EarthRules.SERVANT_HEALTH).add(Attributes.MOVEMENT_SPEED,EarthRules.SERVANT_SPEED).add(Attributes.ATTACK_DAMAGE,EarthRules.SERVANT_RAW).add(Attributes.FOLLOW_RANGE,18).build());
    }
    private EarthContent(){}
}
