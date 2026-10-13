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
  event.register(ModEntities.BOLOTNIK.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getBiome(p).is(Biomes.SWAMP)&&w.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL&&!w.getLevel().isDay()&&net.minecraft.world.entity.Mob.checkMobSpawnRules(t,w,s,p,r)&&r.nextInt(4)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.VODYANOY.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&w.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(8)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.RUSALKA.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,s,p,r)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&!w.getLevel().isDay()&&w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)&&r.nextInt(10)==0,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.PIKE.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.CARP.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
  event.register(ModEntities.CRAYFISH.get(),net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WaterSpawns::riverAnimal,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }
 public static boolean riverAnimal(EntityType<? extends RiverFish> type,net.minecraft.world.level.ServerLevelAccessor world,MobSpawnType reason,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){
  if(world.getLevel().dimension()!=net.minecraft.world.level.Level.OVERWORLD
   ||!world.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)
   ||!world.getFluidState(pos.above()).is(net.minecraft.tags.FluidTags.WATER))return false;
  if(type==ModEntities.CRAYFISH.get())return world.getBlockState(pos.below()).isSolid()
   &&world.getFluidState(pos.below()).isEmpty();
  // River elevations vary. The vanilla sea-level band excludes highland rivers.
  return world.getFluidState(pos.below()).is(net.minecraft.tags.FluidTags.WATER);
 }
}
