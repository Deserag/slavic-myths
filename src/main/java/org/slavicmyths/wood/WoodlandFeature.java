package org.slavicmyths.wood;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.tags.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
public final class WoodlandFeature extends Feature<NoFeatureConfig> {
 public final String species;
 public WoodlandFeature(String s){super(NoFeatureConfig.CODEC);species=s;}
 @Override public boolean place(ISeedReader w,ChunkGenerator gen,Random r,BlockPos origin,NoFeatureConfig cfg){
  int x=(origin.getX()&~15)+5+r.nextInt(6),z=(origin.getZ()&~15)+5+r.nextInt(6);
  BlockPos p=new BlockPos(x,w.getHeight(Heightmap.Type.OCEAN_FLOOR_WG,x,z),z);
  if(species.equals("willow")&&!nearWater(w,p))return false;
  return grow(w,r,p);
 }
 private boolean nearWater(ISeedReader w,BlockPos p){for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=-2;y<=0;y++)if(w.getFluidState(p.offset(x,y,z)).is(FluidTags.WATER))return true;return false;}
 private boolean replaceable(BlockState s,Woodlands.Set set){return s.isAir()||s.is(set.get("sapling"))||s.is(Blocks.GRASS)||s.is(Blocks.TALL_GRASS)||s.is(Blocks.FERN)||s.is(Blocks.LARGE_FERN)||s.is(Blocks.SNOW);}
 private BlockPos pos(BlockPos p,TreeShape.Cell c){return p.offset(c.x,c.y,c.z);}
 public boolean grow(ISeedReader w,Random r,BlockPos p){
  Woodlands.Set set=Woodlands.SETS.get(species);BlockState soil=w.getBlockState(p.below());
  if(!(soil.is(Blocks.DIRT)||soil.is(Blocks.COARSE_DIRT)||soil.is(Blocks.PODZOL)||soil.is(Blocks.MYCELIUM)||soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.FARMLAND)))return false;
  TreeShape shape=new TreeShape(species,r);
  Set<TreeShape.Cell> cells=new HashSet<>(shape.logs.keySet());cells.addAll(shape.leaves.keySet());cells.addAll(shape.hanging);
  // All-or-nothing preflight: never replace builds, other trees, fluids, or terrain.
  for(TreeShape.Cell c:cells){BlockPos q=pos(p,c);if(q.getY()<1||q.getY()>254||!replaceable(w.getBlockState(q),set))return false;}
  for(Map.Entry<TreeShape.Cell,Integer>e:shape.logs.entrySet())w.setBlock(pos(p,e.getKey()),set.get("log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[e.getValue()]),3);
  for(Map.Entry<TreeShape.Cell,Integer>e:shape.leaves.entrySet()){
   BlockState s=set.get("leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,e.getValue()).setValue(LeavesBlock.PERSISTENT,false);
   if(species.equals("rowan"))s=s.setValue(RowanLeaves.BERRIES,r.nextInt(3)==0);w.setBlock(pos(p,e.getKey()),s,3);
  }
  // Top-down order supplies each decorative segment with an existing support.
  ArrayList<TreeShape.Cell> hanging=new ArrayList<>(shape.hanging);hanging.sort(Comparator.comparingInt((TreeShape.Cell c)->c.y).reversed());
  for(TreeShape.Cell c:hanging)w.setBlock(pos(p,c),Woodlands.HANGING.get().defaultBlockState(),3);
  return true;
 }
}
