package org.slavicmyths.verify;
import java.util.*;import java.nio.file.*;import com.google.gson.*;
import net.minecraft.core.*;import net.minecraft.core.registries.Registries;import net.minecraft.data.registries.VanillaRegistries;import net.minecraft.world.level.*;import net.minecraft.world.level.biome.*;import net.minecraft.world.level.levelgen.*;import net.minecraft.world.level.block.Blocks;
import org.slavicmyths.burningislands.OceanTerrain;
/** Real native columns/settings, no generated chunks, game, server or GameTest. */
public final class OceanTerrainAudit {
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var lookup=VanillaRegistries.createLookup();var ops=net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,lookup);var config=NoiseGeneratorSettings.DIRECT_CODEC.parse(ops,JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/minecraft/worldgen/noise_settings/nether.json")))).getOrThrow();
  var generator=new NoiseBasedChunkGenerator(new FixedBiomeSource(lookup.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES)),Holder.direct(config));JsonArray rows=new JsonArray();int selected=0,checks=0;
  long start=System.nanoTime();
  for(long seed:new long[]{0,1,-8675309,20261024,987654321}){
   var state=RandomState.create(config,lookup.lookupOrThrow(Registries.NOISE),seed);var ocean=new OceanTerrain(generator,state,seed,config);JsonObject row=new JsonObject();JsonArray plans=new JsonArray();int found=0;
   search:for(int rx=-24;rx<=24;rx++)for(int rz=-24;rz<=24;rz++){var p=ocean.plan(rx*512+256,rz*512+256);if(!ocean.region(p))continue;found++;JsonObject plan=new JsonObject();plan.addProperty("x",p.x());plan.addProperty("z",p.z());plan.addProperty("homeY",p.home().getY());int included=0;
    for(int dx=-160;dx<=160;dx+=32)for(int dz=-160;dz<=160;dz+=32){int qx=Math.floorDiv(p.x()+dx,4),qz=Math.floorDiv(p.z()+dz,4);if(!ocean.contains(qx,8,qz))continue;included++;selected++;
     for(int a=0;a<4;a++)for(int b=0;b<4;b++){var full=generator.getBaseColumn(qx*4+a,qz*4+b,LevelHeightAccessor.create(0,224),state);if(!full.getBlock(31).is(Blocks.LAVA))throw new AssertionError("Selected biome over land "+seed+" "+(qx*4+a)+" "+(qz*4+b));checks++;}
     if(ocean.contains(qx,0,qz)||ocean.contains(qx,60,qz))throw new AssertionError("Vertical envelope");
    }
    plan.addProperty("includedQuartSamples",included);plans.add(plan);
    // Fixed reverse-accessible slope/bridges, no missing floor or mandatory 2-block ascent.
    for(int side:new int[]{-1,1}){int previous=OceanTerrain.floor(p,p.x(),p.z());for(int x=1;x<=148;x++){int floor=OceanTerrain.floor(p,p.x()+x*side,p.z());if(floor==Integer.MIN_VALUE||Math.abs(floor-previous)>1||OceanTerrain.craterLava(p,p.x()+x*side,p.z()))throw new AssertionError("Dry reversible approach");previous=floor;checks++;}}
    if(plans.size()>=1)break search;
   }
   if(found==0)throw new AssertionError("No ocean regions for seed "+seed);if(ocean.cachedRegions()>128||ocean.cachedQuarts()>8192)throw new AssertionError("Cache bound");row.addProperty("seed",seed);row.add("sampledRegions",plans);rows.add(row);
  }
  JsonObject report=new JsonObject();report.addProperty("scope","Current224-high Nether settings/native base columns, short-slice equivalence and pure geometry only; no actual chunks/world/structures/spawn/performance QA");report.addProperty("status","OFFLINE_PASS");report.addProperty("selectedQuartSamples",selected);report.addProperty("checks",checks);report.addProperty("wallSeconds",(System.nanoTime()-start)/1e9);report.add("seeds",rows);Files.writeString(Path.of("docs/implementation/2026-10-29-hell-remaining/ocean-native.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));System.out.println("Ocean native: "+selected+" selected quart samples, "+checks+" full-column/path checks; runtime0");
 }
}
