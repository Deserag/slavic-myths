package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import org.slavicmyths.losthalls.*;
/** Native noise classification only; never creates a world/server/chunk. */
public final class HallsNativeAudit {
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var lookup=VanillaRegistries.createLookup();var original=MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.NETHER);var values=new ArrayList<>(original.values());
  values.add(Pair.of(Climate.parameters(-.3F,.4F,0,0,0,0,.175F),key("navya_grove")));values.add(Pair.of(Climate.parameters(.7F,-.4F,0,0,0,0,.175F),key("black_cinder")));values.add(Pair.of(HallsClimate.POINT,key("lost_halls")));var candidate=new Climate.ParameterList<>(values);var selection=HallsClimate.selection(candidate,k->k.equals(key("lost_halls")));
  JsonArray rows=new JsonArray();Set<String> all=new HashSet<>();int complete=0;
  for(long seed:new long[]{42041,42042,42043,42044,42045}){var noise=RandomState.create(lookup.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.NETHER).value(),lookup.lookupOrThrow(Registries.NOISE),seed);
   for(int[] window:new int[][]{{0,0},{16384,0},{-16384,8192},{8192,-16384}}){Map<String,Integer> counts=new TreeMap<>();int full=0;
    for(int x=-16;x<16;x++)for(int z=-16;z<16;z++){int bx=window[0]+x*64,bz=window[1]+z*64;var biome=HallsClimate.choose(selection,candidate.findValue(noise.sampler().sample(Math.floorDiv(bx,4),19,Math.floorDiv(bz,4))),Math.floorDiv(bx,4),19,Math.floorDiv(bz,4),noise.sampler());counts.merge(biome.location().toString(),1,Integer::sum);all.add(biome.location().toString());}
    for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){int rx=Math.floorDiv(window[0],192)+x,rz=Math.floorDiv(window[1],192)+z;var p=LostHallsRegionTopology.plan(seed,rx,rz,76);boolean eligible=true;var anchors=new ArrayList<net.minecraft.core.BlockPos>();p.rooms().forEach(n->anchors.add(n.anchor()));anchors.add(p.arena());for(var at:anchors)if(!HallsClimate.choose(selection,candidate.findValue(noise.sampler().sample(Math.floorDiv(at.getX(),4),at.getY()/4,Math.floorDiv(at.getZ(),4))),Math.floorDiv(at.getX(),4),at.getY()/4,Math.floorDiv(at.getZ(),4),noise.sampler()).equals(key("lost_halls"))){eligible=false;break;}if(eligible)full++;}
    complete+=full;Map<String,Object> row=new LinkedHashMap<>();row.put("seed",seed);row.put("center",window);row.put("sampleSpanBlocks",2048);row.put("stepBlocks",64);row.put("counts",counts);row.put("allSevenAnchorsClassifiedHalls",full);rows.add(new Gson().toJsonTree(row));
   }
  }
  if(all.size()!=8||complete==0)throw new AssertionError("Missing biome classification or complete candidate region: "+all+" full="+complete);
  Map<String,Object> report=new LinkedHashMap<>();report.put("scope","20 windows of native noise classification plus seven-anchor biome checks; not terrain/arena/route/spawn proof");report.put("status","PASS_OFFLINE_CLASSIFICATION_ONLY");report.put("runtime_launches",0);report.put("biomes",all);report.put("completeCandidateRegions",complete);report.put("windows",rows);Files.writeString(Path.of("docs/implementation/p0-bosses-327/native-climate.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));System.out.println("Native climate: eight classifications, 20 windows, complete candidate regions="+complete+". Generated-world QA NOT_RUN.");
 }
 private static ResourceKey<Biome> key(String id){return ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
}
