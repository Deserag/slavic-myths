package org.slavicmyths.depth;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import org.slavicmyths.registry.ModBlocks;
public final class DeepPoolFeature extends Feature<NoFeatureConfig>{
 public DeepPoolFeature(){super(NoFeatureConfig.CODEC);}
 public boolean place(ISeedReader w,ChunkGenerator g,Random r,BlockPos origin,NoFeatureConfig cfg){if(org.slavicmyths.swamp.SwampStructures.occupied(w,origin))return false;int x=(origin.getX()&~15)+8,z=(origin.getZ()&~15)+8;int surface=w.getHeight(Heightmap.Type.WORLD_SURFACE_WG,x,z);BlockPos base=new BlockPos(x,surface-6,z);if(surface<40||!w.getFluidState(base.above(5)).is(net.minecraft.tags.FluidTags.WATER))return false;
  for(int a=-6;a<=6;a++)for(int b=-6;b<=6;b++){boolean edge=Math.abs(a)==6||Math.abs(b)==6;for(int y=0;y<=6;y++){BlockPos p=base.offset(a,y,b);BlockState block=y==0||edge?Blocks.MOSSY_COBBLESTONE.defaultBlockState():y<=3?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState();w.setBlock(p,block,2);}if(!edge&&(Math.abs(a)==3||Math.abs(b)==3)&&r.nextInt(3)!=0)w.setBlock(base.offset(a,3,b),Blocks.MOSSY_COBBLESTONE.defaultBlockState(),2);}
  for(int a:new int[]{-4,4})for(int b:new int[]{-4,4}){for(int y=1;y<=5;y++)w.setBlock(base.offset(a,y,b),Blocks.OAK_LOG.defaultBlockState(),2);w.setBlock(base.offset(a,5,b+1),Blocks.SPRUCE_PLANKS.defaultBlockState(),2);w.setBlock(base.offset(a+1,5,b),Blocks.SPRUCE_PLANKS.defaultBlockState(),2);}
  for(int i=0;i<8;i++){BlockPos p=base.offset(r.nextInt(9)-4,1,r.nextInt(9)-4);if(w.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER))w.setBlock(p,ModBlocks.WATER_GRASS.get().defaultBlockState(),2);}
  BlockPos marker=base.above();w.setBlock(marker,ModBlocks.POOL_STONE.get().defaultBlockState(),2);if(w.getBlockEntity(marker) instanceof PoolStoneTile){PoolStoneTile t=(PoolStoneTile)w.getBlockEntity(marker);t.natural=true;t.setChanged();}
  BlockPos barrel=base.offset(4,6,4);w.setBlock(barrel,Blocks.BARREL.defaultBlockState(),2);if(w.getBlockEntity(barrel) instanceof net.minecraft.tileentity.LockableLootTileEntity)((net.minecraft.tileentity.LockableLootTileEntity)w.getBlockEntity(barrel)).setLootTable(new net.minecraft.util.ResourceLocation("slavicmyths","chests/fishing_supplies"),r.nextLong());return true;
 }
}
