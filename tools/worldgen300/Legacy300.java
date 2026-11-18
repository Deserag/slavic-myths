package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.chud.*;

/** Actual upgrade of a copied baseline world: inspect every original regional candidate. */
@EventBusSubscriber(modid="slavicmyths")
public final class Legacy300 {
 static ServerLevel world; static int index; static boolean done; static long began;
 static final List<ChunkPos> candidates=new ArrayList<>(),held=new ArrayList<>();
 static final List<Map<String,Object>> roots=new ArrayList<>();
 static final TicketType<Long> TICKET=TicketType.create("qa300_legacy",Long::compare);
 @SubscribeEvent public static void start(net.neoforged.neoforge.event.server.ServerStartedEvent e){
  if(!"legacy".equals(System.getProperty("slavicmyths.qa300")))return;
  world=e.getServer().overworld();began=System.nanoTime();var c=context();
  var preserved=ChudRegionPlanner.select(c).orElseThrow();
  if(!preserved.persisted()||!preserved.owner().equals(new ChunkPos(82,30))||preserved.access()!=null)throw new IllegalStateException("baseline root was not preserved");
  candidates.addAll(ChudRegionPlanner.placement(c).candidates(world.getSeed(),0,0));
 }
 static Structure.GenerationContext context(){var g=world.getChunkSource().getGenerator();return new Structure.GenerationContext(world.registryAccess(),g,g.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),new ChunkPos(0,0),world,b->true);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){if(world==null||done)return;try{
  if(System.nanoTime()-began>600_000_000_000L)throw new IllegalStateException("10min upgrade bound");
  if(index<candidates.size()){var at=candidates.get(index);if(!held.contains(at)){held.add(at);world.getChunkSource().addRegionTicket(TICKET,at,0,300L);return;}var chunk=world.getChunkSource().getChunkNow(at.x,at.z);if(chunk==null)return;
   for(var entry:chunk.getAllStarts().entrySet())if(entry.getValue().isValid()&&(entry.getKey() instanceof ChudStructure||entry.getKey() instanceof AbandonedStructure)){
    var start=entry.getValue();roots.add(Map.of("owner",List.of(at.x,at.z),"id",world.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(entry.getKey()).toString(),"pieces",start.getPieces().size()));
   }index++;return;
  }
  if(roots.size()!=1||!roots.getFirst().get("owner").equals(List.of(82,30)))throw new IllegalStateException("upgrade duplicate/moved root: "+roots);finish(null);
 }catch(Throwable x){finish(x);}}
 static void finish(Throwable error){done=true;try{var out=new LinkedHashMap<String,Object>();out.put("status",error==null?"PASS_SAVED_ROOT_NO_SECOND_ROOT":"FAIL");out.put("seed",world.getSeed());out.put("roots",roots);out.put("original_candidate_chunks_checked",index);out.put("error",error==null?null:error.toString());Files.writeString(Path.of("legacy300.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(out));}catch(Exception x){x.printStackTrace();}for(var p:held)world.getChunkSource().removeRegionTicket(TICKET,p,0,300L);world.getServer().saveEverything(false,true,true);world.getServer().halt(false);}
}
