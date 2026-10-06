package org.slavicmyths.world;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
public final class WildlifeSpawns {
 public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){
  for(EntityType<? extends net.minecraft.world.entity.Mob> type:new EntityType[]{ModEntities.BROWN_BEAR.get(),ModEntities.BEAR_CUB.get(),ModEntities.FOREST_WOLF.get(),ModEntities.BOAR.get(),ModEntities.STAG.get(),ModEntities.DOE.get()})
   event.register(type,net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,WildlifeSpawns::canSpawn,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }
 public static boolean canSpawn(EntityType<? extends Mob> type,net.minecraft.world.level.ServerLevelAccessor world,MobSpawnType reason,net.minecraft.core.BlockPos pos,net.minecraft.util.RandomSource random){
  var ground=world.getBlockState(pos.below());
  // Woodland floors include coarse dirt, rooted dirt and moss; the old two-block
  // allowlist rejected otherwise valid animals in their explicitly eligible biomes.
  return world.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD
   &&world.getMaxLocalRawBrightness(pos)>7
   &&(ground.is(net.minecraft.tags.BlockTags.DIRT)||ground.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK))
   &&world.getFluidState(pos).isEmpty()&&world.getFluidState(pos.above()).isEmpty()
   &&world.getBlockState(pos).getCollisionShape(world,pos).isEmpty()
   &&world.getBlockState(pos.above()).getCollisionShape(world,pos.above()).isEmpty()
   &&Mob.checkMobSpawnRules(type,world,reason,pos,random);
 }
 private WildlifeSpawns(){}
}
