package org.slavicmyths.worldgen;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
/** Explicit nearby construction. No vanilla locate, distance search, or world access from workers. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class LargeSiteJobs {
 private static final Map<ServerLevel,Job> JOBS=new HashMap<>();
 public static int locate(CommandSourceStack source,String id){
  var w=source.getLevel();if(w.dimension()!=Level.OVERWORLD){source.sendFailure(Component.literal("Эта постройка находится в обычном мире."));return 0;}
  var at=BlockPos.containing(source.getPosition());
  var known=ManualStructureRecords.get(w).entries().stream().filter(e->e.family().equals(id)&&e.arrival().distSqr(at)<=1024.*1024).min(Comparator.comparingDouble(e->e.arrival().distSqr(at)));
  if(known.isPresent()){source.sendSuccess(()->Component.literal(id+": "+known.get().arrival().toShortString()),false);return 1;}
  if(JOBS.containsKey(w)){source.sendFailure(Component.literal("Уже создаётся большая постройка; дождись завершения."));return 0;}
  JOBS.put(w,new Job(source,id));source.sendSuccess(()->Component.literal("Создаётся "+id+" поблизости; координаты будут выданы после завершения."),false);return 1;
 }
 public static int control(CommandSourceStack source,boolean cancel){
  var job=JOBS.get(source.getLevel());if(job==null)return 0;
  if(cancel){boolean partial=job.reserved;job.finish();source.sendSuccess(()->Component.literal(partial?"Большая постройка остановлена; незавершённая область зарезервирована.":"Подготовка большой постройки отменена; блоки не изменялись."),false);}
  else source.sendSuccess(()->Component.literal(job.id+": этап "+job.phase+", чанки "+job.chunk+"/"+job.footprint.size()),false);
  return 1;
 }
 private static final class Job {
  final CommandSourceStack source;final ServerLevel w;final String id;final UUID token=UUID.randomUUID();final BlockPos player;
  SiteChunks chunks;BlockPos origin,arrival;final List<BlockPos> candidates=new ArrayList<>();List<StructurePiece> pieces=List.of();List<ChunkPos> footprint=new ArrayList<>();BoundingBox box;Structure structure;
  int attempt,phase,chunk,piece,y=Integer.MIN_VALUE,index;long notice;boolean reserved;
  Job(CommandSourceStack s,String id){source=s;w=s.getLevel();this.id=id;player=BlockPos.containing(s.getPosition());chunks=new SiteChunks(w);var fallback=new ArrayList<BlockPos>();
   for(int radius:new int[]{256,384,512,640,768})for(int i=0;i<8;i++){double angle=i*Math.PI/4;var at=new ChunkPos(player.offset((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius))).getMiddleBlockPosition(64);var biome=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(at.getX()>>2,16,at.getZ()>>2,w.getChunkSource().randomState().sampler());if(biome.is(net.minecraft.tags.BiomeTags.IS_OCEAN)||biome.is(net.minecraft.tags.BiomeTags.IS_RIVER))continue;if(biome.is(net.minecraft.tags.BiomeTags.IS_FOREST)||biome.is(net.minecraft.tags.BiomeTags.IS_TAIGA))fallback.add(at);else candidates.add(at);}
   candidates.addAll(fallback);if(candidates.isEmpty())candidates.add(player);select();}
  void select(){origin=candidates.get(attempt);}

  void plan(){
   int surface=org.slavicmyths.yaga.YagaPlacement.ground(w,origin.getX(),origin.getZ());if(surface<w.getMinBuildHeight()+16)throw new IllegalStateException("Нет сухой площадки");
   boolean spring=id.equals("abode_pure_spring");origin=origin.atY(surface-(spring?20:0));var manager=w.getStructureManager();var list=new ArrayList<StructurePiece>();
   if(id.equals("gorodishche")){
    String climate=w.getBiome(origin).value().getBaseTemperature()<.25?"cold":w.getBiome(origin).value().getBaseTemperature()>1?"warm":"temperate";
    var plan=org.slavicmyths.gorodishche.CityPlan.make(climate,w.getSeed()^origin.asLong(),origin.getX(),origin.getZ(),(x,z)->surface,true);
    org.slavicmyths.gorodishche.CityPlacement.validate(plan,manager,w,null);list.addAll(org.slavicmyths.gorodishche.CityPlacement.pieces(plan,manager,w.getSeed(),w.getMinBuildHeight(),w.getMaxBuildHeight()));arrival=plan.origin().offset(87,1,175);
   }else if(id.equals("serpent_hill")){
    origin=origin.below(12);list.add(new org.slavicmyths.serpenthill.SerpentHillPiece(origin,net.minecraft.world.level.block.Rotation.NONE,w.getSeed()^origin.asLong(),UUID.randomUUID()));arrival=origin.offset(97,81,82);
   }else{
    int width=org.slavicmyths.sanctuary.SanctuaryGeometry.width(spring),depth=org.slavicmyths.sanctuary.SanctuaryGeometry.depth(spring);var instance=UUID.randomUUID();
    for(int x=0;x<width;x+=16)for(int z=0;z<depth;z+=16)list.add(new org.slavicmyths.sanctuary.SanctuaryPiece(manager,(spring?"spring":"fire")+"/"+x+"_"+z,origin.offset(x,0,z),origin,net.minecraft.world.level.block.Rotation.NONE,instance,spring));
    arrival=origin.offset(spring?48:40,spring?19:2,spring?82:70);
   }
   pieces=List.copyOf(list);var shaped=list.stream().filter(p->!(p instanceof LandFoundationPiece)).toList();var b=shaped.getFirst().getBoundingBox();box=new BoundingBox(b.minX(),b.minY()-(id.equals("gorodishche")||id.equals("serpent_hill")?0:8),b.minZ(),b.maxX(),b.maxY(),b.maxZ());for(var p:shaped)box.encapsulate(p.getBoundingBox());
   if(box.minY()<w.getMinBuildHeight()||box.maxY()>=w.getMaxBuildHeight()||!w.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),0,box.minZ()))||!w.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),0,box.maxZ()))||!ManualStructureRecords.get(w).overlapping(box).isEmpty())throw new IllegalStateException("Место занято или выходит за границы мира");
   structure=w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));if(structure==null)throw new IllegalStateException("Нет типа постройки "+id);
   for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++)footprint.add(new ChunkPos(x,z));
  }
  void step(){try{
   if(w.getGameTime()>=notice){notice=w.getGameTime()+100;source.sendSuccess(()->Component.literal(id+": этап "+phase+", чанки "+chunk+"/"+footprint.size()),false);}
   if(phase==0){if(!chunks.ready(new ChunkPos(origin)))return;plan();phase=1;return;}
   if(phase==1){chunks.prefetch(footprint,chunk);if(!chunks.ready(footprint.get(chunk)))return;phase=2;return;}
   if(phase==2){long until=System.nanoTime()+2_000_000;int ops=0;
    while(chunk<footprint.size()&&ops++<16384&&System.nanoTime()<until){var cp=footprint.get(chunk);if(y==Integer.MIN_VALUE)y=box.minY();int x=cp.getMinBlockX()+(index&15),z=cp.getMinBlockZ()+(index>>4);var p=new BlockPos(x,y,z);var old=w.getBlockState(p);
     if(box.isInside(p)&&(w.getBlockEntity(p)!=null||org.slavicmyths.rpg.tools.ToolLogOrigins.get(w).blocked(p)||!ManualStructureJobs.natural(old)&&old.getFluidState().isEmpty()))throw new IllegalStateException("Защищённая постройка: "+p.toShortString()+" / "+old);
     if(++index==256){index=0;if(++y>box.maxY()){chunk++;y=Integer.MIN_VALUE;break;}}
    }
    if(chunk==footprint.size()){if(!ManualStructureRecords.get(w).overlapping(box).isEmpty())throw new IllegalStateException("Площадка занята другим заданием во время подготовки");ManualStructureRecords.get(w).add(token,"construction",0,arrival,box);reserved=true;phase=3;chunk=piece=0;y=box.minY();}else if(y==Integer.MIN_VALUE)phase=1;return;
   }
   if(phase==3){long until=System.nanoTime()+chunks.budgetNanos();
    while(chunk<footprint.size()&&System.nanoTime()<until){var cp=footprint.get(chunk);var clip=new BoundingBox(cp.getMinBlockX(),y,cp.getMinBlockZ(),cp.getMaxBlockX(),Math.min(y+3,box.maxY()),cp.getMaxBlockZ());var p=pieces.get(piece++);if(p.getBoundingBox().intersects(clip))p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),w.random,clip,cp,origin);
     if(piece==pieces.size()){piece=0;y+=4;if(y>box.maxY()){chunk++;y=box.minY();}}
    }
    if(chunk<footprint.size())return;
    var owner=new ChunkPos(new BlockPos((box.minX()+box.maxX())/2,0,(box.minZ()+box.maxZ())/2));var start=new StructureStart(structure,owner,0,new PiecesContainer(pieces));w.getChunkSource().getChunkNow(owner.x,owner.z).setStartForStructure(structure,start);
    for(var cp:footprint){var c=w.getChunkSource().getChunkNow(cp.x,cp.z);c.addReferenceForStructure(structure,owner.toLong());c.setUnsaved(true);}
    ManualStructureRecords.get(w).replace(token,id,0,arrival,box);source.sendSuccess(()->Component.literal(id+" создано: "+arrival.toShortString()),false);finish();
   }
  }catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().warn("Nearby site {} phase {} attempt {} at {}: {}",id,phase,attempt,origin,e.toString());if(!reserved&&attempt+1<candidates.size()){attempt++;chunks.close();chunks=new SiteChunks(w);footprint.clear();pieces=List.of();phase=chunk=piece=index=0;y=Integer.MIN_VALUE;select();return;}source.sendFailure(Component.literal(id+" не создано: "+e.getMessage()));com.mojang.logging.LogUtils.getLogger().warn("Nearby site construction failed: "+id,e);finish();}}
  void finish(){chunks.close();JOBS.remove(w);}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){for(var j:List.copyOf(JOBS.values()))if(j.w.getServer()==e.getServer())j.step();}
 @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel world){var j=JOBS.get(world);if(j!=null)j.finish();}}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var j:List.copyOf(JOBS.values()))if(j.w.getServer()==e.getServer())j.finish();}
 private LargeSiteJobs(){}
}
