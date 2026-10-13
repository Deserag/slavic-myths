package org.slavicmyths.wood;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.tags.*;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.*;
public final class WoodlandFeature extends Feature<WoodlandFeature.Config> {
 public record Config(boolean sapling,boolean giant) implements net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration {
  public static final com.mojang.serialization.Codec<Config> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.create(i->i.group(com.mojang.serialization.Codec.BOOL.optionalFieldOf("sapling",false).forGetter(Config::sapling),com.mojang.serialization.Codec.BOOL.optionalFieldOf("giant",false).forGetter(Config::giant)).apply(i,Config::new));
 }
 public final String species;
 public WoodlandFeature(String s){super(Config.CODEC);species=s;}
 @Override public boolean place(FeaturePlaceContext<Config> context){WorldGenLevel w=context.level();ChunkGenerator gen=context.chunkGenerator();RandomSource r=context.random();BlockPos origin=context.origin();Config cfg=context.config();if(cfg.sapling())return grow(w,r,origin,cfg.giant());
  int x=(origin.getX()&~15)+5+r.nextInt(6),z=(origin.getZ()&~15)+5+r.nextInt(6);
  BlockPos p=new BlockPos(x,w.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z),z);
  if(species.equals("willow")&&!nearWater(w,p))return false;
  return grow(w,r,p);
 }
 private boolean nearWater(WorldGenLevel w,BlockPos p){for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=-2;y<=0;y++)if(w.getFluidState(p.offset(x,y,z)).is(FluidTags.WATER))return true;return false;}
 private boolean replaceable(BlockState s,Woodlands.Set set){return s.isAir()||s.is(set.get("sapling"))||s.is(Blocks.SHORT_GRASS)||s.is(Blocks.TALL_GRASS)||s.is(Blocks.FERN)||s.is(Blocks.LARGE_FERN)||s.is(Blocks.SNOW);}
 private BlockPos pos(BlockPos p,TreeShape.Cell c){return p.offset(c.x,c.y,c.z);}
 public boolean grow(WorldGenLevel w,RandomSource r,BlockPos p){return grow(w,r,p,false);}
 private boolean grow(WorldGenLevel w,RandomSource r,BlockPos p,boolean giant){
  Woodlands.Set set=Woodlands.SETS.get(species);BlockState soil=w.getBlockState(p.below());
  if(!(soil.is(Blocks.DIRT)||soil.is(Blocks.COARSE_DIRT)||soil.is(Blocks.PODZOL)||soil.is(Blocks.MYCELIUM)||soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.FARMLAND)))return false;
  if(giant)for(int x=0;x<2;x++)for(int z=0;z<2;z++){var below=w.getBlockState(p.offset(x,-1,z));if(!below.is(net.minecraft.tags.BlockTags.DIRT)&&!below.is(Blocks.FARMLAND))return false;}
  TreeShape shape=giant?TreeShape.giantPine(r::nextInt):new TreeShape(species,r::nextInt,r::nextDouble,r::nextBoolean);
  Set<TreeShape.Cell> cells=new HashSet<>(shape.logs.keySet());cells.addAll(shape.leaves.keySet());cells.addAll(shape.hanging);
  // All-or-nothing preflight: never replace builds, other trees, fluids, or terrain.
  for(TreeShape.Cell c:cells){BlockPos q=pos(p,c);if(!w.hasChunkAt(q)||q.getY()<w.getMinBuildHeight()||q.getY()>=w.getMaxBuildHeight()||!replaceable(w.getBlockState(q),set))return false;}
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
