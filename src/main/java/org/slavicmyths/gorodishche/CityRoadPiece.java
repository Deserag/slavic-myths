package org.slavicmyths.gorodishche;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slavicmyths.worldgen.LandTerrain;

/** Saved, chunk-indexed road raster. Only placement-time local corrections, never ticking. */
public final class CityRoadPiece extends StructurePiece {
 public record Lamp(BlockPos base,Direction inward){}
 private record LightBlock(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){}
 private final List<CityPlan.Road>roads;private final List<Lamp>lamps;private final Map<Long,List<CityPlan.Road>>byChunk=new HashMap<>();private final Map<Long,List<LightBlock>>lightsByChunk=new HashMap<>();
 public CityRoadPiece(CityPlan.Plan plan,int minY){super(GorodishcheStructures.ROADS.get(),0,new BoundingBox(plan.bounds().minX(),Math.max(minY,plan.bounds().minY()-6),plan.bounds().minZ(),plan.bounds().maxX(),plan.bounds().maxY(),plan.bounds().maxZ()));roads=plan.roads();lamps=lamps(plan);index();}
 public CityRoadPiece(CompoundTag n){super(GorodishcheStructures.ROADS.get(),n);int[] a=n.getIntArray("Roads");var list=new ArrayList<CityPlan.Road>();for(int i=0;i+4<a.length;i+=5)list.add(new CityPlan.Road(new BlockPos(a[i],a[i+1],a[i+2]),a[i+3]<0?null:Direction.from3DDataValue(a[i+3]),a[i+4]==0?"MAIN":a[i+4]==1?"SECONDARY":"ALLEY"));roads=List.copyOf(list);a=n.getIntArray("StreetLights");var saved=new ArrayList<Lamp>();for(int i=0;i+3<a.length;i+=4)saved.add(new Lamp(new BlockPos(a[i],a[i+1],a[i+2]),Direction.from3DDataValue(a[i+3])));lamps=List.copyOf(saved);index();}
 public static List<Lamp> lamps(CityPlan.Plan plan){
  Map<Long,Integer>height=new HashMap<>();for(var r:plan.roads())height.put(BlockPos.asLong(r.pos().getX(),0,r.pos().getZ()),r.pos().getY());List<Lamp>result=new ArrayList<>();Set<Long>occupied=new HashSet<>();
  for(var s:plan.streets()){boolean alongZ=s.x0()==s.x1();int length=Math.max(s.x1()-s.x0(),s.z1()-s.z0());for(int i=6;i<length-5;i+=12)for(int side:new int[]{-1,1}){
   int cx=s.x0()+(alongZ?0:i),cz=s.z0()+(alongZ?i:0),offset=side*(s.width()/2+1);var base=new BlockPos(cx+(alongZ?offset:0),height.getOrDefault(BlockPos.asLong(cx,0,cz),plan.origin().getY()),cz+(alongZ?0:offset));Direction inward=alongZ?(side<0?Direction.EAST:Direction.WEST):(side<0?Direction.SOUTH:Direction.NORTH);var arm=base.relative(inward);boolean fits=plan.bounds().isInside(base)&&plan.bounds().isInside(arm);
   for(var p:plan.placements())if(p.box().isInside(base)||p.box().isInside(arm)||base.getX()>=p.box().minX()&&base.getX()<=p.box().maxX()&&base.getZ()>=p.box().minZ()&&base.getZ()<=p.box().maxZ()||arm.getX()>=p.box().minX()&&arm.getX()<=p.box().maxX()&&arm.getZ()>=p.box().minZ()&&arm.getZ()<=p.box().maxZ())fits=false;
   if(fits&&occupied.add(base.asLong()))result.add(new Lamp(base,inward));
  }}return List.copyOf(result);
 }
 private void lightBlock(BlockPos p,net.minecraft.world.level.block.state.BlockState state){lightsByChunk.computeIfAbsent(new ChunkPos(p).toLong(),k->new ArrayList<>()).add(new LightBlock(p,state));}
 private void index(){for(var r:roads)byChunk.computeIfAbsent(new ChunkPos(r.pos()).toLong(),k->new ArrayList<>()).add(r);for(var lamp:lamps){var p=lamp.base();lightBlock(p,Blocks.STONE_BRICKS.defaultBlockState());for(int y=1;y<=3;y++)lightBlock(p.above(y),Blocks.SPRUCE_LOG.defaultBlockState());var axis=lamp.inward().getAxis();lightBlock(p.above(4),Blocks.SPRUCE_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS,axis));lightBlock(p.relative(lamp.inward()).above(4),Blocks.SPRUCE_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS,axis));lightBlock(p.relative(lamp.inward()).above(3),Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING,true));}}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){int[] a=new int[roads.size()*5];int i=0;for(var r:roads){a[i++]=r.pos().getX();a[i++]=r.pos().getY();a[i++]=r.pos().getZ();a[i++]=r.up()==null?-1:r.up().get3DDataValue();a[i++]=r.roadClass().equals("MAIN")?0:r.roadClass().equals("SECONDARY")?1:2;}n.putIntArray("Roads",a);a=new int[lamps.size()*4];i=0;for(var lamp:lamps){a[i++]=lamp.base().getX();a[i++]=lamp.base().getY();a[i++]=lamp.base().getZ();a[i++]=lamp.inward().get3DDataValue();}n.putIntArray("StreetLights",a);}
 @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  for(var r:byChunk.getOrDefault(chunk.toLong(),List.of())){var p=r.pos();if(!clip.isInside(p))continue;
   for(int y=1;y<=4;y++){var q=p.above(y);if(q.getY()>=world.getMaxBuildHeight())break;var old=world.getBlockState(q);if(!old.is(Blocks.BEDROCK)&&!old.hasBlockEntity())world.setBlock(q,Blocks.AIR.defaultBlockState(),2);}
   if(world.getBlockState(p).is(Blocks.BEDROCK)||world.getBlockState(p).hasBlockEntity())continue;
   var state=r.up()!=null?Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,r.up()):r.roadClass().equals("MAIN")?(Math.floorMod(p.getX()/4+p.getZ()/5,3)==0?Blocks.COARSE_DIRT:Blocks.COBBLESTONE).defaultBlockState():Blocks.GRAVEL.defaultBlockState();world.setBlock(p,state,2);
   for(int depth=1;depth<=6;depth++){var q=p.below(depth);if(q.getY()<world.getMinBuildHeight()||!clip.isInside(q))break;var old=world.getBlockState(q);if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()||!LandTerrain.vegetation(old)&&old.getFluidState().isEmpty())break;world.setBlock(q,Blocks.COBBLESTONE.defaultBlockState(),2);}
  }
  for(var light:lightsByChunk.getOrDefault(chunk.toLong(),List.of()))if(clip.isInside(light.pos())&&!world.getBlockState(light.pos()).is(Blocks.BEDROCK)&&!world.getBlockState(light.pos()).hasBlockEntity()){
   world.setBlock(light.pos(),light.state(),2);
   if(light.state().is(Blocks.STONE_BRICKS))for(int depth=1;depth<=6;depth++){var q=light.pos().below(depth);if(q.getY()<world.getMinBuildHeight()||!clip.isInside(q))break;var old=world.getBlockState(q);if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()||!LandTerrain.vegetation(old)&&old.getFluidState().isEmpty())break;world.setBlock(q,Blocks.COBBLESTONE.defaultBlockState(),2);}
  }
 }
}
