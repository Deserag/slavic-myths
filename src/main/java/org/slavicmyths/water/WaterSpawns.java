package org.slavicmyths.water;
import net.minecraft.entity.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WaterSpawns {
 public static void placements(){
  EntitySpawnPlacementRegistry.register(ModEntities.VODYANOY.get(),EntitySpawnPlacementRegistry.PlacementType.IN_WATER,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.World.OVERWORLD&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&w.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(8)==0);
  EntitySpawnPlacementRegistry.register(ModEntities.RUSALKA.get(),EntitySpawnPlacementRegistry.PlacementType.IN_WATER,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.World.OVERWORLD&&!w.getLevel().isDay()&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(10)==0);
  EntitySpawnPlacementRegistry.register(ModEntities.PIKE.get(),EntitySpawnPlacementRegistry.PlacementType.IN_WATER,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkFishSpawnRules);
  EntitySpawnPlacementRegistry.register(ModEntities.CARP.get(),EntitySpawnPlacementRegistry.PlacementType.IN_WATER,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkFishSpawnRules);
  EntitySpawnPlacementRegistry.register(ModEntities.CRAYFISH.get(),EntitySpawnPlacementRegistry.PlacementType.IN_WATER,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkFishSpawnRules);
 }
 @SubscribeEvent public static void biomes(BiomeLoadingEvent e){if(e.getName()==null||!e.getName().getNamespace().equals("minecraft"))return;if(e.getCategory()==Biome.Category.RIVER||e.getCategory()==Biome.Category.SWAMP){e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.VODYANOY.get(),1,1,1));e.getSpawns().addSpawn(EntityClassification.MONSTER,new MobSpawnInfo.Spawners(ModEntities.RUSALKA.get(),1,1,1));e.getSpawns().addSpawn(EntityClassification.WATER_AMBIENT,new MobSpawnInfo.Spawners(ModEntities.PIKE.get(),4,1,2));e.getSpawns().addSpawn(EntityClassification.WATER_AMBIENT,new MobSpawnInfo.Spawners(ModEntities.CARP.get(),8,2,4));e.getSpawns().addSpawn(EntityClassification.WATER_AMBIENT,new MobSpawnInfo.Spawners(ModEntities.CRAYFISH.get(),3,1,2));}}
}
