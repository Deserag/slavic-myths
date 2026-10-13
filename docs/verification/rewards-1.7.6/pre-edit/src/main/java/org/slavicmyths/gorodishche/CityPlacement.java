package org.slavicmyths.gorodishche;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.slavicmyths.worldgen.LandFoundationPiece;

/** Shared strict assets/bounds gate and native pieces. Never creates substitute buildings. */
public final class CityPlacement {
 public static void validate(CityPlan.Plan plan,StructureTemplateManager manager,LevelHeightAccessor height,ResourceManager resources){
  if(resources==null){var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server!=null)resources=server.getResourceManager();}
  if(resources==null||resources.getResource(ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/processor_list/village_foundation.json")).isEmpty())throw new IllegalStateException("MISSING_PROCESSOR slavicmyths:village_foundation");
  Set<ResourceLocation> checked=new HashSet<>();
  for(var p:plan.placements()){
   var b=p.building();var template=manager.get(b.id()).orElseThrow(()->new IllegalStateException("MISSING_TEMPLATE "+b.id()));var size=template.getSize();if(size.getX()!=b.width()||size.getY()!=b.height()||size.getZ()!=b.depth())throw new IllegalStateException("TEMPLATE_SIZE_MISMATCH "+b.id());
   if(p.box().minY()<height.getMinBuildHeight()||p.box().maxY()>=height.getMaxBuildHeight())throw new IllegalStateException("OUT_OF_WORLD_HEIGHT "+b.id()+" "+p.box());
   if(!checked.add(b.id()))continue;
   CompoundTag saved;var resource=resources.getResource(ResourceLocation.fromNamespaceAndPath(b.id().getNamespace(),"structure/"+b.id().getPath()+".nbt")).orElseThrow(()->new IllegalStateException("MISSING_TEMPLATE_RESOURCE "+b.id()));try(var in=resource.open()){saved=net.minecraft.nbt.NbtIo.readCompressed(in,net.minecraft.nbt.NbtAccounter.unlimitedHeap());}catch(java.io.IOException ex){throw new IllegalStateException("MALFORMED_TEMPLATE "+b.id(),ex);}var palette=saved.getList("palette",10);
   for(var raw:palette){var n=(CompoundTag)raw;var id=ResourceLocation.parse(n.getString("Name"));if(!BuiltInRegistries.BLOCK.containsKey(id))throw new IllegalStateException("BLOCK_ID_MISSING "+id+" in "+b.id());var block=BuiltInRegistries.BLOCK.get(id);var props=n.getCompound("Properties");for(String name:props.getAllKeys()){var prop=block.getStateDefinition().getProperty(name);if(prop==null||prop.getValue(props.getString(name)).isEmpty())throw new IllegalStateException("BLOCK_PROPERTY_INVALID "+id+"/"+name+" in "+b.id());}}
   for(var raw:saved.getList("blocks",10)){var n=((CompoundTag)raw).getCompound("nbt");if(n.contains("LootTable")){var id=ResourceLocation.parse(n.getString("LootTable"));if(resources.getResource(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),"loot_table/"+id.getPath()+".json")).isEmpty())throw new IllegalStateException("MISSING_LOOT_TABLE "+id);}}
   for(var job:b.jobs())if(!BuiltInRegistries.BLOCK.containsKey(job.block())||!BuiltInRegistries.VILLAGER_PROFESSION.containsKey(job.role()))throw new IllegalStateException("WORKSTATION_ID_MISSING "+job);
  }
 }
 public static void validateDestination(ServerLevel level,CityPlan.Plan plan){
  Set<BlockPos> roadVolume=new HashSet<>();for(var road:plan.roads())for(int y=-6;y<=4;y++)roadVolume.add(road.pos().offset(0,y,0));
  for(var lamp:CityRoadPiece.lamps(plan))for(int y=0;y<=4;y++){roadVolume.add(lamp.base().above(y));roadVolume.add(lamp.base().relative(lamp.inward()).above(y));}
  for(int cx=plan.bounds().minX()>>4;cx<=plan.bounds().maxX()>>4;cx++)for(int cz=plan.bounds().minZ()>>4;cz<=plan.bounds().maxZ()>>4;cz++)for(var pos:level.getChunk(cx,cz).getBlockEntitiesPos()){
   boolean affected=roadVolume.contains(pos);if(!affected)for(var p:plan.placements()){var b=p.box();if(pos.getX()>=b.minX()&&pos.getX()<=b.maxX()&&pos.getZ()>=b.minZ()&&pos.getZ()<=b.maxZ()&&pos.getY()>=b.minY()-6&&pos.getY()<=b.maxY()){affected=true;break;}}
   if(affected)throw new IllegalStateException("BLOCK_ENTITY_COLLISION "+pos.toShortString());
  }
 }
 public static List<StructurePiece> pieces(CityPlan.Plan plan,StructureTemplateManager manager,long seed,int minY,int maxY){
  var result=new ArrayList<StructurePiece>();
  for(var p:plan.placements()){var box=p.box();int floor=p.origin().getY()+p.building().surface();result.add(new LandFoundationPiece(box,floor,0,Math.max(minY,floor-6),Math.min(maxY-1,box.maxY())));}
  result.add(new CityRoadPiece(plan,minY));for(var p:plan.placements())result.add(new CityBuildingPiece(manager,p,seed));return result;
 }
 public record Population(int villagers,int guards){}
 public static Population place(ServerLevel level,CityPlan.Plan plan,long seed){
  var pieces=pieces(plan,level.getStructureManager(),seed,level.getMinBuildHeight(),level.getMaxBuildHeight());var buildings=pieces.stream().filter(p->p instanceof CityBuildingPiece).map(p->(CityBuildingPiece)p).toList();buildings.forEach(p->p.deferResidents(true));
  for(var piece:pieces){var b=piece.getBoundingBox();for(int cx=b.minX()>>4;cx<=b.maxX()>>4;cx++)for(int cz=b.minZ()>>4;cz<=b.maxZ()>>4;cz++){var clip=new BoundingBox(cx*16,level.getMinBuildHeight(),cz*16,cx*16+15,level.getMaxBuildHeight()-1,cz*16+15);if(b.intersects(clip))piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(seed),clip,new ChunkPos(cx,cz),plan.origin());}}
  int villagers=0,guards=0;
  for(var piece:buildings){piece.deferResidents(false);villagers+=piece.spawnResidents(level,plan.bounds(),RandomSource.create(seed));guards+=piece.spawnedGuards();}
  return new Population(villagers,guards);
 }
 private CityPlacement(){}
}
