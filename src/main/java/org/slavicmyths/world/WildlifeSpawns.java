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
   event.register(type,net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->w.getLevel().dimension()==net.minecraft.world.level.Level.OVERWORLD&&w.getMaxLocalRawBrightness(p)>7&&(w.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)||w.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.PODZOL))&&Mob.checkMobSpawnRules(t,w,r,p,n),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
 }
 private WildlifeSpawns(){}
}
