package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Measures actual native accepted starts, not placement-cell jitter alone. */
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class CoverageGameTests {
 record Point(double x,double z,StructureCoverageService.Tier tier){}
 static Map<String,Double> metrics(List<Double> distances){
  if(distances.isEmpty())return Map.of();distances.sort(Double::compare);
  int n=distances.size();double sum=distances.stream().mapToDouble(Double::doubleValue).sum();
  return Map.of("min",distances.getFirst(),"mean",sum/n,"median",n%2==0?(distances.get(n/2-1)+distances.get(n/2))/2:distances.get(n/2),"p95",distances.get(Math.max(0,(int)Math.ceil(n*.95)-1)),"max",distances.getLast());
 }
 @GameTest(template="port_empty",timeoutTicks=2400)
 public static void acceptedFlatNetworks(GameTestHelper test)throws Exception{measure(test,false);}
 static void measure(GameTestHelper test,boolean natural)throws Exception{
  long measurementStarted=System.nanoTime();
  var world=test.getLevel();var report=new com.google.gson.JsonArray();
  var acceptedBoxes=new HashMap<String,List<net.minecraft.world.level.levelgen.structure.BoundingBox>>();
  for(long seed:new long[]{0,17,731269})for(String family:List.of("kurgan","bandit","swamp","water")){
   var biome=family.equals("swamp")||family.equals("water")?Biomes.SWAMP:Biomes.PLAINS;
   var settings=new FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome),List.of());
   settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new FlatLayerInfo(122,Blocks.STONE));settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.CLAY));if(family.equals("water"))settings.getLayersInfo().add(new FlatLayerInfo(6,Blocks.WATER));settings.updateLayers();
   net.minecraft.world.level.chunk.ChunkGenerator generator=new net.minecraft.world.level.levelgen.FlatLevelSource(settings);
   var random=world.getChunkSource().randomState();
   if(natural){
    var noise=world.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);
    var preset=world.registryAccess().registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.OVERWORLD);
    generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
    random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),world.registryAccess().lookupOrThrow(Registries.NOISE),seed);
   }
   var eligibleByTier=new EnumMap<StructureCoverageService.Tier,Integer>(StructureCoverageService.Tier.class);var points=new ArrayList<Point>();var counts=new com.google.gson.JsonObject();int attempted=0,rejected=0,invalidBiomeCenters=0,unclassified=0;var rejectionReasons=new EnumMap<StructureCoverageService.Rejection,Integer>(StructureCoverageService.Rejection.class);
   for(var profile:StructureCoverageService.profiles())if(profile.family().equals(family)){
    var structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",profile.id()));int accepted=0;
    var placement=profile.placement();int cells=(int)Math.ceil(4096.0/(profile.spacing()*16));
    for(int cx=-cells;cx<=cells;cx++)for(int cz=-cells;cz<=cells;cz++){
     ChunkPos chunk=placement.getPotentialStructureChunk(seed,cx*profile.spacing(),cz*profile.spacing());attempted++;
     var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,chunk,world,structure.biomes()::contains);
     // Share exact noise columns between eligibility and actual generation, within this cell only.
     try(var samples=StructureCoverageService.sampleTerrain(context)){
      if(natural&&!StructureCoverageService.localCandidates(context,structure).iterator().hasNext()){invalidBiomeCenters++;continue;}
     eligibleByTier.merge(profile.tier(),1,Integer::sum);
     var start=structure.generate(world.registryAccess(),generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,chunk,0,world,h->natural?structure.biomes().contains(h):h.is(biome));
     if(!start.isValid()){rejected++;var diagnostics=StructureCoverageService.lastPlacement();if(diagnostics==null||diagnostics.lastReason()==null||diagnostics.lastReason()==StructureCoverageService.Rejection.ACCEPTED)unclassified++;else rejectionReasons.merge(diagnostics.lastReason(),1,Integer::sum);continue;}
     var box=start.getBoundingBox();if(!natural&&(family.equals("kurgan")||family.equals("bandit")))acceptedBoxes.computeIfAbsent(seed+":"+family,k->new ArrayList<>()).add(box);points.add(new Point((box.minX()+box.maxX())*.5,(box.minZ()+box.maxZ())*.5,profile.tier()));accepted++;
     }
    }
    if(!natural)test.assertTrue(accepted>0,"No actual accepted starts "+profile.id()+" seed="+seed+" diagnostics="+StructureCoverageService.lastPlacement());counts.addProperty(profile.id(),accepted);
   }
   var row=new com.google.gson.JsonObject();row.addProperty("seed",seed);row.addProperty("family",family);row.addProperty("scenario",natural?"native overworld noise + biomes; starts only; no block placement":"eligible deep flat; starts only; no block placement");row.addProperty("attempted",attempted);row.addProperty("noEligibleLocalCandidates",invalidBiomeCenters);row.addProperty("rejected",rejected);row.addProperty("rejectedUnclassified",unclassified);row.add("rejectionReasons",new com.google.gson.Gson().toJsonTree(rejectionReasons));row.add("acceptedById",counts);
   for(var tier:StructureCoverageService.Tier.values()){
    var distances=new ArrayList<Double>();int unmatched=0;
    for(var point:points)if(point.tier==tier&&Math.abs(point.x)<=3072&&Math.abs(point.z)<=3072){
     double nearest=Double.POSITIVE_INFINITY;
     for(var other:points)if(other!=point&&(tier==StructureCoverageService.Tier.SMALL?other.tier==StructureCoverageService.Tier.SMALL:tier==StructureCoverageService.Tier.MEDIUM?other.tier==StructureCoverageService.Tier.SMALL:other.tier!=StructureCoverageService.Tier.LARGE))nearest=Math.min(nearest,Math.hypot(point.x-other.x,point.z-other.z));
     if(Double.isFinite(nearest))distances.add(nearest);else unmatched++;
    }
    var values=metrics(distances);row.add(tier.name(),new com.google.gson.Gson().toJsonTree(values));
    // A failure is reported honestly; coverage on arbitrary natural terrain is a separate gate.
    int limit=tier==StructureCoverageService.Tier.SMALL?1000:tier==StructureCoverageService.Tier.MEDIUM?2000:4000;
    boolean applicable=StructureCoverageService.profiles().stream().anyMatch(p->p.family().equals(family)&&p.tier()==tier);
    int eligible=eligibleByTier.getOrDefault(tier,0);boolean measured=!values.isEmpty();
    row.addProperty(tier.name()+"EligibleCandidates",eligible);row.addProperty(tier.name()+"UnmatchedStarts",unmatched);
    row.addProperty(tier.name()+"Status",!applicable?"NOT_APPLICABLE":eligible==0?"NO_ELIGIBLE_CANDIDATES":!measured?"NOT_MEASURED":"MEASURED");
    row.addProperty(tier.name()+"Pass",!applicable||(measured&&unmatched==0&&values.get("max")<=limit));
   }
   report.add(row);
   Path partial=Path.of(System.getProperty("slavicmyths.portRoot"),"docs/verification/mob-worldgen-0.9.9/accepted-"+(natural?"natural":"flat")+"-networks.partial.json");Files.createDirectories(partial.getParent());Files.writeString(partial,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
   System.out.println("COVERAGE_ACCEPTED_"+(natural?"NATURAL":"FLAT")+"_MEASURED family="+family+" seed="+seed+" accepted="+points.size()+" rejected="+rejected);
  }
  Path path=Path.of(System.getProperty("slavicmyths.portRoot"),"docs/verification/mob-worldgen-0.9.9/accepted-"+(natural?"natural":"flat")+"-networks.json");Files.createDirectories(path.getParent());Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
  for(var element:report){var row=element.getAsJsonObject();for(var tier:StructureCoverageService.Tier.values())test.assertTrue(row.get(tier.name()+"Pass").getAsBoolean(),"Accepted structure coverage exceeds limit "+row.get("family")+" "+tier+" "+row.get(tier.name()));}
  if(!natural){int checked=0;
   for(long seed:new long[]{0,17,731269})for(var kurgan:acceptedBoxes.getOrDefault(seed+":kurgan",List.of()))for(var camp:acceptedBoxes.getOrDefault(seed+":bandit",List.of())){checked++;test.assertTrue(!kurgan.intersects(camp),"Accepted native kurgan/camp footprint overlap seed="+seed+" kurgan="+kurgan+" camp="+camp);}
   System.out.println("COVERAGE_CROSS_FAMILY_PLAN_PROTECTION_PASS pairs="+checked);
  }
  System.out.println("COVERAGE_MEASURE_TIME_MS "+(System.nanoTime()-measurementStarted)/1_000_000+" cache="+StructureCoverageService.footprintStats());test.succeed();
 }
}
