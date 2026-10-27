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
public final class CinderClimate {
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var lookup=VanillaRegistries.createLookup();var original=MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.NETHER);var values=new ArrayList<>(original.values());
  var navya=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","navya_grove"));var cinder=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","black_cinder"));
  values.add(Pair.of(Climate.parameters(-.3F,.4F,0,0,0,0,.175F),navya));values.add(Pair.of(Climate.parameters(.7F,-.4F,0,0,0,0,.175F),cinder));var candidate=new Climate.ParameterList<>(values);var rows=new JsonArray();
  for(long seed:new long[]{0,1,-8675309,20261024,987654321}){
   var state=RandomState.create(lookup.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.NETHER).value(),lookup.lookupOrThrow(Registries.NOISE),seed);var counts=new TreeMap<String,Integer>();
   for(int x=-128;x<128;x++)for(int z=-128;z<128;z++){var climate=state.sampler().sample(x*16,16,z*16);var key=candidate.findValue(climate);counts.merge(key.location().toString(),1,Integer::sum);}
   if(counts.size()!=7)throw new AssertionError("all seven climate classifications must remain represented: "+counts);
   var row=new JsonObject();row.addProperty("seed",seed);row.add("counts",new Gson().toJsonTree(counts));rows.add(row);
  }
  var report=new JsonObject();report.addProperty("scope","Offline classification only, vanilla native noise settings, step64 across 16k area; not actual hell worldgen/arena/spawn");report.add("seeds",rows);Files.writeString(Path.of("docs/implementation/black-cinder-production/offline-climate.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));System.out.println("PASS offline native climate: five seeds, all five vanilla + Navya + Black Cinder represented. No worlds.");
 }
}
