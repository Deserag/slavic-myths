package org.slavicmyths.kuzya;

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
public final class KuzyaContent {
    public static final DeferredHolder<EntityType<?>,EntityType<KuzyaEntity>> ENTITY=ModEntities.ENTITIES.register("kuzya",()->EntityType.Builder.of(KuzyaEntity::new,MobCategory.MISC).sized(.52F,.70F).fireImmune().clientTrackingRange(8).updateInterval(1).build("slavicmyths:kuzya"));
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    public static void init(){for(String cue:KuzyaAssets.CUES)SOUNDS.put(cue,ModSounds.SOUNDS.register("kuzya."+cue,()->SoundEvent.createVariableRangeEvent(id("kuzya."+cue))));}
    public static SoundEvent sound(String cue){return SOUNDS.get(cue).get();}
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.12).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,.6).build());}
    private KuzyaContent(){}
}
