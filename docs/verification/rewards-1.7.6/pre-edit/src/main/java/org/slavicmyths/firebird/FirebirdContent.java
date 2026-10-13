package org.slavicmyths.firebird;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class FirebirdContent {
    public static final DeferredHolder<EntityType<?>,EntityType<FirebirdEntity>> ENTITY=ModEntities.ENTITIES.register("firebird",()->EntityType.Builder.of(FirebirdEntity::new,MobCategory.CREATURE).sized(1.35F,2.6F).fireImmune().clientTrackingRange(16).updateInterval(2).build("slavicmyths:firebird"));
    public static final List<String> RESOURCES=List.of("firebird_feather","smoldering_feather","firebird_down");
    public static final List<DeferredHolder<Item,Item>> ITEMS=new ArrayList<>();
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("firebird_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0xa92b18,0xffd55b,new Item.Properties()));
    public static final List<String> CUES=List.of("ambient","call","wing_flap","takeoff","glide","perch","warning","retaliation","hurt","drop","depart","death");
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    static {for(String name:RESOURCES)ITEMS.add(ModItems.ITEMS.register(name,()->new Item(new Item.Properties())));for(String cue:CUES)SOUNDS.put(cue,ModSounds.SOUNDS.register("firebird_"+cue,()->SoundEvent.createVariableRangeEvent(id("firebird_"+cue))));}
    public static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("slavicmyths",p);}
    public static SoundEvent sound(String cue){return SOUNDS.get(cue).get();}
    public static void init(){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,1024).add(Attributes.MOVEMENT_SPEED,.18).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.6).build());}
    private FirebirdContent(){}
}
