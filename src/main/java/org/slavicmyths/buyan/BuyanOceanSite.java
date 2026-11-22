package org.slavicmyths.buyan;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.Heightmap;
/** Bounded terrain sampling without generating or loading distant chunks. */
public final class BuyanOceanSite {
 public static boolean suitable(ServerLevel world,int cx,int cz){
  var generator=world.getChunkSource().getGenerator();var random=world.getChunkSource().randomState();int sea=world.getSeaLevel();
  int extent=BuyanTerrain.MAX_RADIUS+48;
  for(int x=-extent;x<=extent;x+=64)for(int z=-extent;z<=extent;z+=64){
   if((long)x*x+(long)z*z>(long)extent*extent)continue;
   int px=cx+x,pz=cz+z;
   if(!world.getWorldBorder().isWithinBounds(new BlockPos(px,sea,pz)))return false;
   var biome=generator.getBiomeSource().getNoiseBiome(px>>2,sea>>2,pz>>2,random.sampler());
   if(!biome.is(BiomeTags.IS_OCEAN))return false;
   if(generator.getBaseHeight(px,pz,Heightmap.Types.OCEAN_FLOOR_WG,world,random)>=sea-2)return false;
  }
  return true;
 }
 private BuyanOceanSite(){}
}