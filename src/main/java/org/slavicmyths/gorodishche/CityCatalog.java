package org.slavicmyths.gorodishche;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** Immutable data snapshot, atomically replaced on server resource reload. */
@EventBusSubscriber(modid="slavicmyths")
public final class CityCatalog {
 public record Job(BlockPos pos,ResourceLocation role,ResourceLocation block){}
 public record Building(ResourceLocation id,String name,String climate,String category,String family,String roof,int width,int height,int depth,int floors,int surface,int weight,BlockPos entrance,List<BlockPos>beds,List<Job>jobs){}
 public record Settings(int size,int terrainDelta,int correction){}
 private record Data(List<Building>buildings,Settings settings){}
 private static volatile Data snapshot;
 private static JsonObject read(ResourceManager rm,String path)throws IOException{try(var r=new InputStreamReader(rm.getResource(ResourceLocation.fromNamespaceAndPath("slavicmyths","gorodishche/"+path+".json")).orElseThrow(()->new IOException("MISSING_CATALOG "+path)).open(),java.nio.charset.StandardCharsets.UTF_8)){return JsonParser.parseReader(r).getAsJsonObject();}}
 public static BlockPos pos(JsonArray a){return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
 public static synchronized void reload(ResourceManager rm){try{
  List<Building> list=new ArrayList<>();
  for(var row:read(rm,"catalog").getAsJsonArray("buildings")){var j=row.getAsJsonObject();var size=j.getAsJsonArray("size");List<BlockPos>beds=new ArrayList<>();List<Job>jobs=new ArrayList<>();for(var bed:j.getAsJsonArray("beds"))beds.add(pos(bed.getAsJsonArray()));for(var job:j.getAsJsonArray("workstations")){var q=job.getAsJsonObject();jobs.add(new Job(pos(q.getAsJsonArray("pos")),ResourceLocation.parse(q.get("role").getAsString()),ResourceLocation.parse(q.get("block").getAsString())));}
   list.add(new Building(ResourceLocation.parse(j.get("id").getAsString()),j.get("name").getAsString(),j.get("climate").getAsString(),j.get("category").getAsString(),j.get("family").getAsString(),j.get("roof_variant").getAsString(),size.get(0).getAsInt(),size.get(1).getAsInt(),size.get(2).getAsInt(),j.get("floors").getAsInt(),j.get("surface").getAsInt(),j.get("weight").getAsInt(),pos(j.getAsJsonArray("entrance_offset")),List.copyOf(beds),List.copyOf(jobs)));
  }
  var s=read(rm,"layouts");if(list.isEmpty())throw new IOException("EMPTY_CATALOG");if(s.get("size").getAsInt()!=176||s.get("local_correction").getAsInt()<4||s.get("local_correction").getAsInt()>6||s.get("terrain_max_delta").getAsInt()<10||s.get("terrain_max_delta").getAsInt()>16)throw new IOException("INVALID_LAYOUT_LIMITS");snapshot=new Data(List.copyOf(list),new Settings(s.get("size").getAsInt(),s.get("terrain_max_delta").getAsInt(),s.get("local_correction").getAsInt()));
 }catch(IOException|RuntimeException ex){throw new IllegalStateException("Городище: "+ex.getMessage(),ex);}}
 @SubscribeEvent public static void listeners(AddReloadListenerEvent e){e.addListener((ResourceManagerReloadListener)CityCatalog::reload);}
 private static Data data(){var d=snapshot;if(d==null){var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)throw new IllegalStateException("CITY_DATA_NOT_READY");reload(server.getResourceManager());d=snapshot;}return d;}
 public static List<Building> buildings(){return data().buildings();}public static Settings settings(){return data().settings();}
 private CityCatalog(){}
}
