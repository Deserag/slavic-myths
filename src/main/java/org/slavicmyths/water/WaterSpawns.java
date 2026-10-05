package org.slavicmyths.water;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
public final class WaterSpawns {
 public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){
  event.register(ModEntities.VODYANOY.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&w.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(8)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.RUSALKA.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&!w.getLevel().isDay()&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(10)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.PIKE.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkSurfaceWaterAnimalSpawnRules,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.CARP.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkSurfaceWaterAnimalSpawnRules,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.CRAYFISH.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,RiverFish::checkSurfaceWaterAnimalSpawnRules,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }
}
