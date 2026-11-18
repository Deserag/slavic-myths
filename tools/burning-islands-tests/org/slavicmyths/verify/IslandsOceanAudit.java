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

/** Native base columns, no world/server or chunks. Counterexample to climate-only ocean selection. */
public final class IslandsOceanAudit {
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var lookup=VanillaRegistries.createLookup();var preset=MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.NETHER);
  var values=new ArrayList<>(preset.values());
  values.add(Pair.of(Climate.parameters(-.3F,.4F,0,0,0,0,.175F),key("navya_grove")));values.add(Pair.of(Climate.parameters(.7F,-.4F,0,0,0,0,.175F),key("black_cinder")));
  var islands=key("burning_islands");values.add(Pair.of(Climate.parameters(.9F,.9F,0,0,0,0,.175F),islands));
  var candidate=new Climate.ParameterList<>(values);var settings=lookup.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.NETHER);
  var generator=new NoiseBasedChunkGenerator(new FixedBiomeSource(lookup.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES)),settings);
  var report=new JsonObject();var rows=new JsonArray();int total=0,land=0;
  for(long seed:new long[]{0,1,-8675309,20261024,987654321}){
   var state=RandomState.create(settings.value(),lookup.lookupOrThrow(Registries.NOISE),seed);var examples=new JsonArray();int selected=0,solid=0;
   for(int window=0;window<3;window++)for(int x=-32;x<32;x++)for(int z=-32;z<32;z++){
    int bx=x*64+window*16384,bz=z*64-window*8192;
    // All three vertical quart samples are classified by climate; no base column enters this decision.
    for(int y:new int[]{32,48,80})if(candidate.findValue(state.sampler().sample(Math.floorDiv(bx,4),y/4,Math.floorDiv(bz,4))).equals(islands)){
     selected++;total++;
     var column=generator.getBaseColumn(bx,bz,net.minecraft.world.level.LevelHeightAccessor.create(0,256),state);
     if(!column.getBlock(generator.getSeaLevel()-1).is(net.minecraft.world.level.block.Blocks.LAVA)){
      solid++;land++;if(examples.size()<3){var e=new JsonObject();e.addProperty("x",bx);e.addProperty("y",y);e.addProperty("z",bz);e.addProperty("baseSeaBlock",column.getBlock(generator.getSeaLevel()-1).toString());examples.add(e);}
     }
    }
   }
   var row=new JsonObject();row.addProperty("seed",seed);row.addProperty("windows",3);row.addProperty("selectedQuartSamples",selected);row.addProperty("notLavaAtSea",solid);row.add("counterexamples",examples);rows.add(row);
  }
  if(total==0||land==0)throw new AssertionError("Audit needs real counterexamples");
  report.addProperty("scope","Offline native base columns + hypothetical climate append, NOT actual generated hell worlds or ocean coverage");report.addProperty("status","BLOCKED_OCEAN_BIOME_ARCHITECTURE");report.addProperty("productionSelectorChanged",false);report.add("seeds",rows);
  Files.writeString(Path.of("docs/implementation/burning-islands-205/ocean-architecture-evidence.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
  System.out.println("Climate-only candidate: "+land+" of "+total+" samples lack lava at sea; five native noise seeds, zero game launches. NOT ocean-only.");
 }
 private static ResourceKey<Biome> key(String id){return ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
}
