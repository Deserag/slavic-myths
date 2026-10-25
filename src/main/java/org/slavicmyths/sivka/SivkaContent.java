package org.slavicmyths.sivka;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class SivkaContent {
    public static final DeferredHolder<EntityType<?>,EntityType<SivkaEntity>> ENTITY=ModEntities.ENTITIES.register("sivka_burka",()->EntityType.Builder.of(SivkaEntity::new,MobCategory.CREATURE).sized(SivkaRules.WIDTH,SivkaRules.HEIGHT).clientTrackingRange(12).updateInterval(1).build("slavicmyths:sivka_burka"));
    public static final DeferredHolder<Item,Item> WHISTLE=ModItems.ITEMS.register("sivka_whistle",SivkaWhistle::new);
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("sivka_burka_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(ENTITY,0xf4e9d8,0xdf8b2a,new Item.Properties()));
    public static final List<String> CUES=List.of("breath","snort_soft","whinny_soft","whinny_alarm","hoof_grass","hoof_stone","hoof_snow","hoof_wood","gallop_burst","feed_chew","magic_takeoff","air_stride","magic_land","whistle_player","summon_response","materialize","depart","bond_progress","bond_complete","bond_expired","summon_reject");
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    static {for(String cue:CUES)SOUNDS.put(cue,ModSounds.SOUNDS.register("sivka."+cue,()->SoundEvent.createVariableRangeEvent(id("sivka."+cue))));}
    public static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("slavicmyths",p);}
    public static SoundEvent sound(String cue){return SOUNDS.get(cue).get();}
    public static void init(){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(ENTITY.get(),net.minecraft.world.entity.PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.3).add(Attributes.STEP_HEIGHT,1).build());}
    @SubscribeEvent public static void placements(RegisterSpawnPlacementsEvent e){e.register(ENTITY.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,p,r)->{
        if(w.getLevel().dimension()!=net.minecraft.world.level.Level.OVERWORLD||!w.canSeeSky(p)||w.getMaxLocalRawBrightness(p)<=7)return false;
        // Test the actual horse volume, not a larger 3x4 clearing. Never load a border chunk.
        var box=new net.minecraft.world.phys.AABB(p.getX()+.5-SivkaRules.WIDTH/2,p.getY(),p.getZ()+.5-SivkaRules.WIDTH/2,
                p.getX()+.5+SivkaRules.WIDTH/2,p.getY()+SivkaRules.HEIGHT,p.getZ()+.5+SivkaRules.WIDTH/2);
        if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ))
                ||!w.hasChunkAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.maxZ))
                ||!w.hasChunkAt(net.minecraft.core.BlockPos.containing(box.maxX,box.minY,box.minZ))
                ||!w.hasChunkAt(net.minecraft.core.BlockPos.containing(box.maxX,box.minY,box.maxZ)))return false;
        return w.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT)&&w.noCollision(box)&&!w.containsAnyLiquid(box);
    },RegisterSpawnPlacementsEvent.Operation.REPLACE);}
    private SivkaContent(){}
}
