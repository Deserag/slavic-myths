package org.slavicmyths.water;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import org.slavicmyths.registry.ModBlocks;
public final class WaterFeature extends Feature<NoFeatureConfig>{
 public WaterFeature(){super(NoFeatureConfig.CODEC);}
 public boolean place(ISeedReader w,ChunkGenerator g,Random r,BlockPos origin,NoFeatureConfig cfg){int x=(origin.getX()&~15)+8,z=(origin.getZ()&~15)+8;boolean placed=false;
  for(int i=0;i<10;i++){int dx=x+r.nextInt(9)-4,dz=z+r.nextInt(9)-4;BlockPos surface=new BlockPos(dx,w.getHeight(Heightmap.Type.WORLD_SURFACE_WG,dx,dz),dz);BlockPos floor=new BlockPos(dx,w.getHeight(Heightmap.Type.OCEAN_FLOOR_WG,dx,dz),dz);
   if(w.getFluidState(surface.below()).is(FluidTags.WATER)&&w.isEmptyBlock(surface)&&r.nextInt(4)==0){w.setBlock(surface,ModBlocks.WHITE_LILY.get().defaultBlockState(),2);placed=true;}
   if(w.getFluidState(floor).is(FluidTags.WATER)&&ModBlocks.WATER_GRASS.get().defaultBlockState().canSurvive(w,floor)){w.setBlock(floor,ModBlocks.WATER_GRASS.get().defaultBlockState(),2);placed=true;}
   if(w.isEmptyBlock(surface)&&ModBlocks.REED.get().defaultBlockState().canSurvive(w,surface)){w.setBlock(surface,ModBlocks.REED.get().defaultBlockState(),2);placed=true;}
  }
  if(r.nextInt(120)!=0)return placed;
  BlockPos top=new BlockPos(x,w.getHeight(Heightmap.Type.WORLD_SURFACE_WG,x,z),z);if(!w.getFluidState(top.below()).is(FluidTags.WATER))return placed;int kind=r.nextInt(3);
  BlockPos base=kind==2?new BlockPos(x,w.getHeight(Heightmap.Type.OCEAN_FLOOR_WG,x,z),z):top;
  if(kind==1){BlockPos shore=null;for(int i=-4;i<=4;i++){BlockPos p=top.offset(i,0,0);if(w.isEmptyBlock(p)&&w.getBlockState(p.below()).getMaterial().isSolid()){shore=p;break;}}if(shore==null)return placed;base=shore;w.setBlock(base,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),2);w.setBlock(base.offset(1,0,0),Blocks.SPRUCE_STAIRS.defaultBlockState(),2);}
  else{for(int a=-1;a<=1;a++)for(int b=-2;b<=1;b++){if(r.nextInt(7)==0)continue;w.setBlock(base.offset(a,0,b),Blocks.SPRUCE_SLAB.defaultBlockState(),2);if(kind==2&&Math.abs(a)==1)w.setBlock(base.offset(a,1,b),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.WATERLOGGED,true),2);}if(kind==0)for(int a:new int[]{-1,1})for(int dy=1;dy<=3;dy++)if(w.getFluidState(base.offset(a,-dy,-2)).is(FluidTags.WATER))w.setBlock(base.offset(a,-dy,-2),Blocks.OAK_LOG.defaultBlockState(),2);}
  BlockPos loot=base.offset(0,1,1);w.setBlock(loot,Blocks.BARREL.defaultBlockState(),2);TileEntity te=w.getBlockEntity(loot);if(te instanceof LockableLootTileEntity)((LockableLootTileEntity)te).setLootTable(new ResourceLocation("slavicmyths","chests/fishing_supplies"),r.nextLong());return true;
 }
}
