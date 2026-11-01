package org.slavicmyths.morovoy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;

/** Bounded read-only noise column preflight. No generated chunks or tickets. */
public final class MorovoyArenaTerrain {
 public record Plan(BlockPos center,int floorY,long seed,int entranceMask,int[] entranceY){
  public Plan {entranceY=entranceY.clone();}
 }
 public static boolean editable(BlockState s){
  return s.isAir()||s.is(Blocks.LAVA)||NetherBridgeTerrain.natural(s)||
    s.is(Blocks.ANCIENT_DEBRIS)||s.is(Blocks.GLOWSTONE)||s.is(Blocks.CRIMSON_STEM)||s.is(Blocks.WARPED_STEM)||
    s.is(Blocks.CRIMSON_HYPHAE)||s.is(Blocks.WARPED_HYPHAE)||s.is(Blocks.NETHER_WART_BLOCK)||
    s.is(Blocks.WARPED_WART_BLOCK)||s.is(Blocks.SHROOMLIGHT)||s.is(Blocks.BONE_BLOCK)||
    s.is(Blocks.CRIMSON_ROOTS)||s.is(Blocks.WARPED_ROOTS)||s.is(Blocks.NETHER_SPROUTS)||
    s.is(Blocks.CRIMSON_FUNGUS)||s.is(Blocks.WARPED_FUNGUS)||s.is(Blocks.TWISTING_VINES)||
    s.is(Blocks.TWISTING_VINES_PLANT)||s.is(Blocks.WEEPING_VINES)||s.is(Blocks.WEEPING_VINES_PLANT)||
    s.is(Blocks.RED_MUSHROOM)||s.is(Blocks.BROWN_MUSHROOM)||s.is(Blocks.FIRE)||s.is(Blocks.SOUL_FIRE);
 }
 public static Plan find(Structure.GenerationContext c){
  if(c.heightAccessor().getMinBuildHeight()>0||c.heightAccessor().getMaxBuildHeight()<200)return null;
  int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
  long seed=c.seed()^((long)x*341873128712L)^((long)z*132897987541L);
  for(int floor=46;floor<=58;floor+=4){
   int[] entry={-1,-1,-1,-1};int mask=0;
   for(int side=0;side<4;side++){
    int ox=x+(side==0?122:side==2?-122:0),oz=z+(side==1?122:side==3?-122:0);
    NoiseColumn col=c.chunkGenerator().getBaseColumn(ox,oz,c.heightAccessor(),c.randomState());
    for(int y=floor+40;y<=floor+65;y++)if(editable(col.getBlock(y-1))&&!col.getBlock(y-1).isAir()&&
      col.getBlock(y).isAir()&&col.getBlock(y+1).isAir()&&col.getBlock(y+2).isAir()){
      entry[side]=y-1;mask|=1<<side;break;
    }
   }
   // Every accepted arena has at least two safe exterior-to-rim approaches.
   if(Integer.bitCount(mask)<2)continue;
   boolean safe=true;
   for(int dz=-112;dz<=112&&safe;dz+=28)for(int dx=-112;dx<=112;dx+=28){
    if(!MorovoyArenaGeometry.inside(dx,dz,seed))continue;
    if(!c.biomeSource().getNoiseBiome((x+dx)>>2,floor>>2,(z+dz)>>2,c.randomState().sampler())
      .is(MorovoyArenaWorldgen.BIOMES)){safe=false;break;}
    NoiseColumn col=c.chunkGenerator().getBaseColumn(x+dx,z+dz,c.heightAccessor(),c.randomState());
    for(int y=floor-MorovoyArenaGeometry.FOUNDATION;y<=MorovoyArenaGeometry.CLEAR_TOP_Y;y+=3){
     BlockState old=col.getBlock(y);
     if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()||!editable(old)){safe=false;break;}
    }
    if(!safe)break;
   }
   if(safe)return new Plan(new BlockPos(x,floor,z),floor,seed,mask,entry);
  }
  return null;
 }
 private MorovoyArenaTerrain(){}
}
