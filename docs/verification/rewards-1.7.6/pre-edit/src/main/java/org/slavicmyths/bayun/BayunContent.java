package org.slavicmyths.bayun;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class BayunContent {
    public static final DeferredHolder<EntityType<?>,EntityType<BayunEntity>> ENTITY=ModEntities.ENTITIES.register("kot_bayun",()->EntityType.Builder.of(BayunEntity::new,MobCategory.CREATURE).sized(1.25F,1.65F).clientTrackingRange(12).updateInterval(2).build("slavicmyths:kot_bayun"));
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("kot_bayun_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0x302e36,0xeaa23f,new Item.Properties()));
    public static final DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>,BayunFeature> FEATURE=ModFeatures.FEATURES.register("bayun_nook",BayunFeature::new);
    public static final String[] CUES={"purr.soft","purr.happy","mew.soft","mew.annoyed","breath","settle","stretch","groom","milk","fish","pawstep","warn","song.intro","song.loop","pounce","claw","book.page","book.open","book.discovery"};
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
    static{for(String cue:CUES)SOUNDS.put(cue,ModSounds.SOUNDS.register("bayun."+cue,()->SoundEvent.createVariableRangeEvent(id("bayun."+cue))));}
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    public static SoundEvent sound(String cue){return SOUNDS.get(cue).get();}
    public static void init(){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.FOLLOW_RANGE,16).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
    private BayunContent(){}
}
