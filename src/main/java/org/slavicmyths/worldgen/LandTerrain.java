package org.slavicmyths.worldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
/** Read only the currently available worldgen column; tree canopies are not foundation soil. */
public final class LandTerrain {
 public static boolean vegetation(net.minecraft.world.level.block.state.BlockState state){return state.is(BlockTags.LEAVES)||state.is(BlockTags.LOGS)||state.is(BlockTags.REPLACEABLE_BY_TREES)||state.getBlock() instanceof BushBlock||state.getBlock() instanceof CactusBlock||state.getBlock() instanceof SugarCaneBlock||state.getBlock() instanceof BambooStalkBlock||state.getBlock() instanceof BambooSaplingBlock||state.is(Blocks.SNOW)||state.is(Blocks.SNOW_BLOCK)||state.canBeReplaced();}
 public static int ground(WorldGenLevel world,int x,int z){
  int y=world.getHeight(world instanceof net.minecraft.server.level.ServerLevel?Heightmap.Types.OCEAN_FLOOR:Heightmap.Types.OCEAN_FLOOR_WG,x,z)-1;
  var pos=new BlockPos.MutableBlockPos();while(y>world.getMinBuildHeight()){pos.set(x,y,z);var state=world.getBlockState(pos);if(!state.getFluidState().isEmpty())break;if(vegetation(state))y--;else break;}return y;
 }
 private LandTerrain(){}
}
