package org.slavicmyths.swamp;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.slavicmyths.wood.Woodlands;
import org.slavicmyths.registry.ModBlocks;

/** Vanilla-swamp only additions. Preflight precedes all tree writes. */
public final class SwampNatureFeature extends Feature<SwampNatureFeature.Config>{
 public record Config(String kind) implements FeatureConfiguration{public static final Codec<Config> CODEC=Codec.STRING.fieldOf("kind").xmap(Config::new,Config::kind).codec();}
 public SwampNatureFeature(){super(Config.CODEC);}
 @Override public boolean place(FeaturePlaceContext<Config> c){
  var w=c.level();var origin=c.origin();if(!w.getBiome(origin).is(net.minecraft.world.level.biome.Biomes.SWAMP))return false;
  int x=origin.getX(),z=origin.getZ();BlockPos p=new BlockPos(x,w.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z),z);
  if(SwampStructures.occupied(w,p))return false;
  if(c.config().kind().equals("detail"))return detail(w,p,c.random());
  int water=w.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z)-p.getY();if(water>3||water<0)return false;
  String kind=c.config().kind();if(kind.equals("pine")&&water>0)return false;
  if(!soil(w.getBlockState(p.below())))return false;
  SwampTreeShape shape=new SwampTreeShape(kind,c.random().nextLong());
  for(var cell:shape.logs.keySet())if(!fits(w,p,cell,true))return false;
  for(var cell:shape.leaves.keySet())if(!fits(w,p,cell,false))return false;
  for(var cell:shape.hanging)if(!fits(w,p,cell,false))return false;
  BlockState log=kind.equals("willow")?Woodlands.SETS.get("willow").get("log").defaultBlockState():kind.equals("pine")?Woodlands.SETS.get("pine").get("log").defaultBlockState():Blocks.OAK_LOG.defaultBlockState();
  BlockState leaf=kind.equals("willow")?Woodlands.SETS.get("willow").get("leaves").defaultBlockState():kind.equals("pine")?Woodlands.SETS.get("pine").get("leaves").defaultBlockState():Blocks.OAK_LEAVES.defaultBlockState();
  shape.logs.forEach((q,axis)->w.setBlock(pos(p,q),log.setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[axis]),2));
  shape.leaves.forEach((q,d)->w.setBlock(pos(p,q),leaf.setValue(LeavesBlock.DISTANCE,d).setValue(LeavesBlock.PERSISTENT,false),2));
  shape.hanging.stream().sorted(java.util.Comparator.comparingInt(SwampTreeShape.Cell::y).reversed()).forEach(q->w.setBlock(pos(p,q),Woodlands.HANGING.get().defaultBlockState(),2));return true;
 }
 private static boolean soil(BlockState s){return s.is(BlockTags.DIRT)||s.is(Blocks.MUD)||s.is(Blocks.CLAY)||s.is(Blocks.SAND);}
 private static BlockPos pos(BlockPos p,SwampTreeShape.Cell q){return p.offset(q.x(),q.y(),q.z());}
 private static boolean fits(WorldGenLevel w,BlockPos p,SwampTreeShape.Cell c,boolean root){BlockPos q=pos(p,c);if(q.getY()<w.getMinBuildHeight()||q.getY()>=w.getMaxBuildHeight())return false;BlockState s=w.getBlockState(q);
  return s.isAir()||s.is(Blocks.SHORT_GRASS)||s.is(Blocks.FERN)||s.is(Blocks.VINE)||root&&(w.getFluidState(q).is(FluidTags.WATER)||c.y()<=0&&soil(s));}
 private static boolean detail(WorldGenLevel w,BlockPos p,net.minecraft.util.RandomSource r){boolean changed=false;
  for(int i=0;i<12;i++){int x=p.getX()+r.nextInt(9)-4,z=p.getZ()+r.nextInt(9)-4;BlockPos top=new BlockPos(x,w.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z),z);if(!w.getBiome(top).is(net.minecraft.world.level.biome.Biomes.SWAMP))continue;
   BlockState state=w.getFluidState(top.below()).is(FluidTags.WATER)?ModBlocks.WHITE_LILY.get().defaultBlockState():ModBlocks.REED.get().defaultBlockState();
   if(w.isEmptyBlock(top)&&state.canSurvive(w,top)){w.setBlock(top,state,2);changed=true;continue;}
   if(w.isEmptyBlock(top)&&soil(w.getBlockState(top.below()))){BlockState moss=Blocks.MOSS_CARPET.defaultBlockState();if(moss.canSurvive(w,top)){w.setBlock(top,moss,2);changed=true;}}
  }
  if(r.nextInt(5)==0&&soil(w.getBlockState(p.below()))){int axis=r.nextBoolean()?0:2;boolean ok=true;for(int i=0;i<4;i++){BlockPos q=p.offset(axis==0?i:0,0,axis==2?i:0);if(!w.isEmptyBlock(q)||!soil(w.getBlockState(q.below())))ok=false;}
   if(ok){for(int i=0;i<4;i++)w.setBlock(p.offset(axis==0?i:0,0,axis==2?i:0),Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[axis]),2);changed=true;}}
  return changed;
 }
}
