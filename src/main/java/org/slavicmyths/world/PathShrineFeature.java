package org.slavicmyths.world;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.*;
import org.slavicmyths.registry.ModBlocks;
public final class PathShrineFeature extends Feature<NoneFeatureConfiguration>{
 public PathShrineFeature(){super(NoneFeatureConfiguration.CODEC);}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){WorldGenLevel w=context.level();ChunkGenerator g=context.chunkGenerator();RandomSource r=context.random();BlockPos origin=context.origin();NoneFeatureConfiguration c=context.config();
  if(!w.getLevel().dimension().equals(Level.OVERWORLD))return false;
  int x=(origin.getX()&~15)+8,z=(origin.getZ()&~15)+8;BlockPos center=new BlockPos(x,w.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z),z);
  for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
   BlockPos p=center.offset(dx,-1,dz);Block b=w.getBlockState(p).getBlock();if(b!=Blocks.GRASS_BLOCK&&b!=Blocks.DIRT&&b!=Blocks.PODZOL)return false;
   for(int y=0;y<3;y++){BlockPos air=p.above(y+1);if(!w.getFluidState(air).isEmpty()||!w.isEmptyBlock(air)&&!w.getBlockState(air).canBeReplaced())return false;}
  }
  for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){BlockPos p=center.offset(dx,0,dz);w.setBlock(p,Blocks.AIR.defaultBlockState(),2);if(r.nextBoolean())w.setBlock(p.below(),Blocks.COARSE_DIRT.defaultBlockState(),2);}
  w.setBlock(center,ModBlocks.PATH_STONE.get().defaultBlockState(),2);
  for(int dx:new int[]{-2,2})for(int dz:new int[]{-2,2})w.setBlock(center.offset(dx,0,dz),Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState(),2);
  w.setBlock(center.offset(-2,0,0),Blocks.OAK_FENCE.defaultBlockState(),2);return true;
 }
}
