package org.slavicmyths.world;
import net.minecraft.entity.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WildlifeSpawns {
 public static void placements(){
  for(EntityType<? extends net.minecraft.entity.MobEntity> type:new EntityType[]{ModEntities.BROWN_BEAR.get(),ModEntities.BEAR_CUB.get(),ModEntities.FOREST_WOLF.get(),ModEntities.BOAR.get(),ModEntities.STAG.get(),ModEntities.DOE.get()})
   EntitySpawnPlacementRegistry.register(type,EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->w.getLevel().dimension()==net.minecraft.world.World.OVERWORLD&&w.getMaxLocalRawBrightness(p)>7&&(w.getBlockState(p.below()).is(net.minecraft.block.Blocks.GRASS_BLOCK)||w.getBlockState(p.below()).is(net.minecraft.block.Blocks.PODZOL))&&MobEntity.checkMobSpawnRules(t,w,r,p,n));
 }
 @SubscribeEvent public static void biomes(BiomeLoadingEvent e){
  if(e.getName()==null||!e.getName().getNamespace().equals("minecraft"))return;Biome.Category c=e.getCategory();
  if(c==Biome.Category.FOREST||c==Biome.Category.TAIGA){
   add(e,ModEntities.BROWN_BEAR.get(),2,1,1);add(e,ModEntities.FOREST_WOLF.get(),5,2,4);add(e,ModEntities.BOAR.get(),6,1,3);add(e,ModEntities.STAG.get(),3,1,1);add(e,ModEntities.DOE.get(),6,2,3);
  } else if(c==Biome.Category.PLAINS){add(e,ModEntities.STAG.get(),2,1,2);add(e,ModEntities.DOE.get(),4,2,3);add(e,ModEntities.BOAR.get(),2,1,2);}
 }
 private static void add(BiomeLoadingEvent e,EntityType<?> t,int weight,int min,int max){e.getSpawns().addSpawn(EntityClassification.CREATURE,new MobSpawnInfo.Spawners(t,weight,min,max));}
 private WildlifeSpawns(){}
}
