package org.slavicmyths.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Clipped entrance steps; no writes or supports outside the accepted piece bounds. */
public final class FoundationApproach {
 public static void place(WorldGenLevel world,ChunkGenerator generator,BoundingBox clip,BlockPos origin,int middle,int depth,BlockState stairs){
  for(int dx=0;dx<2;dx++)for(int d=1;d<=depth;d++){
   BlockPos step=origin.offset(middle+dx,1-d,-d);if(!clip.isInside(step)||step.getY()<=world.getMinBuildHeight())continue;
   int ground=generator.getBaseHeight(step.getX(),step.getZ(),Heightmap.Types.WORLD_SURFACE_WG,world,world.getLevel().getChunkSource().randomState());
   if(step.getY()<ground-1||!replaceable(world.getBlockState(step)))continue;
   boolean clear=true;
   for(int up=1;up<=3;up++)if(clip.isInside(step.above(up))&&!replaceable(world.getBlockState(step.above(up)))){clear=false;break;}
   if(!clear)continue;
   world.setBlock(step,stairs.setValue(StairBlock.FACING,Direction.SOUTH),2);
   for(int down=1;down<=depth;down++){
    var support=step.below(down);if(!clip.isInside(support)||support.getY()<=world.getMinBuildHeight()||world.getBlockState(support).isSolid()||world.getBlockState(support).hasBlockEntity())break;
    world.setBlock(support,Blocks.DIRT.defaultBlockState(),2);
   }
   for(int up=1;up<=3;up++){
    var air=step.above(up);if(clip.isInside(air)&&!world.getBlockState(air).hasBlockEntity())world.setBlock(air,Blocks.AIR.defaultBlockState(),2);
   }
  }
 }
 private static boolean replaceable(BlockState state){
  return !state.hasBlockEntity()&&(state.isAir()||state.canBeReplaced()||state.is(net.minecraft.tags.BlockTags.DIRT)||state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)||state.is(Blocks.GRAVEL)||state.is(Blocks.SAND)||state.is(Blocks.SNOW_BLOCK));
 }
 private FoundationApproach(){}
}
