package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.chud.*;
@GameTestHolder("slavicmyths_abandoned_terrain") @PrefixGameTestTemplate(false)
public final class AbandonedTerrainGameTests {
    @GameTest(template="empty",timeoutTicks=7200)
    public static void naturalTwelveSeedCandidates(GameTestHelper t)throws Exception{
        var w=t.getLevel();var registry=w.registryAccess();var noise=registry.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);
        var preset=registry.registryOrThrow(net.minecraft.core.registries.Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.OVERWORLD);
        var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
        var structure=(AbandonedStructure)registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_abandoned"));
        var set=registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_abandoned"));var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)set.placement();
        var city=registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        var report=new com.google.gson.JsonArray();int totalAccepted=0;
        for(long seed=0;seed<12;seed++){
            var random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),registry.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);int eligible=0,accepted=0;long begin=System.nanoTime();var entries=new com.google.gson.JsonArray();var cities=new com.google.gson.JsonArray();var outers=new ArrayList<net.minecraft.world.level.levelgen.structure.BoundingBox>();var rejects=new HashMap<String,Integer>();
            record Cell(ChunkPos chunk,int surface,boolean mountain){}var cells=new ArrayList<Cell>();
            for(int gx=-32;gx<=32;gx++)for(int gz=-32;gz<=32;gz++){
                var chunk=placement.getPotentialStructureChunk(seed,gx*256,gz*256);var biome=generator.getBiomeSource().getNoiseBiome((chunk.x<<2)+2,24,(chunk.z<<2)+2,random.sampler());if(!structure.biomes().contains(biome))continue;
                int height=generator.getBaseHeight(chunk.getMiddleBlockX(),chunk.getMiddleBlockZ(),net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,w,random);cells.add(new Cell(chunk,height,city.biomes().contains(biome)));
            }
            eligible=cells.size();cells.sort(Comparator.comparingInt(Cell::surface).reversed());var chosen=new ArrayList<Cell>();chosen.addAll(cells.stream().filter(Cell::mountain).limit(32).toList());chosen.addAll(cells.stream().filter(q->!q.mountain()).limit(8).toList());
            for(int index=0;index<chosen.size();index++){
                var chunk=chosen.get(index).chunk();
                var c=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,w.getStructureManager(),seed,chunk,w,structure.biomes()::contains);
                long candidateBegin=System.nanoTime();var start=structure.generate(registry,generator,generator.getBiomeSource(),random,w.getStructureManager(),seed,chunk,0,w,structure.biomes()::contains);
                double candidatePlanningMs=(System.nanoTime()-candidateBegin)/1_000_000.0;
                if(start.isValid()){accepted++;for(var piece:start.getPieces())if(piece instanceof AbandonedPiece p){outers.add(p.plan.envelope());entries.add(new com.google.gson.Gson().toJsonTree(Map.of("anchor",p.plan.anchor.toShortString(),"kind",p.plan.kind.name(),"rotation",p.plan.rotation,"rootComplexId",p.plan.rootId,"entranceFloor",p.plan.entranceFloor,"shaftProfile",p.plan.shaftProfile,"nativePlanningMs",candidatePlanningMs)));}}
                else{
                    var diagnostic=org.slavicmyths.worldgen.StructureCoverageService.lastPlacement();rejects.merge(diagnostic==null?"unknown":diagnostic.lastMeasurements().toString(),1,Integer::sum);
                    if(diagnostic!=null&&diagnostic.lastMeasurements().isEmpty()){
                        var parent=city.generate(registry,generator,generator.getBiomeSource(),random,w.getStructureManager(),seed,chunk,0,w,city.biomes()::contains);
                        if(parent.isValid())for(var piece:parent.getPieces())if(piece instanceof ChudPiece p){outers.add(p.abandoned==null?p.plan.bounds():p.abandoned.envelope());cities.add(new com.google.gson.Gson().toJsonTree(Map.of("cityId",p.plan.cityId(),"anchor",p.plan.anchor.toShortString(),"attached",p.abandoned!=null)));}
                    }
                }
            }
            int pairs=0;long minimum=Long.MAX_VALUE;for(int i=0;i<outers.size();i++)for(int j=i+1;j<outers.size();j++){long distance=ChudPlan.boundaryDistanceSquared(outers.get(i),outers.get(j));t.assertTrue(distance>=4_000_000,"naturally accepted roots under 2000 XZ");minimum=Math.min(minimum,distance);pairs++;}
            var row=new com.google.gson.JsonObject();row.addProperty("seed",seed);row.addProperty("eligibleCells",eligible);row.addProperty("sampling","highest 32 mountain and 8 forest native candidates within 65x65 placement regions");row.addProperty("highestCandidateSurface",cells.isEmpty()?0:cells.getFirst().surface());row.addProperty("sampledCells",chosen.size());row.addProperty("acceptedStarts",accepted);row.addProperty("independentPairsChecked",pairs);if(pairs>0)row.addProperty("minimumBoundaryDistance",Math.sqrt(minimum));row.addProperty("elapsedMilliseconds",(System.nanoTime()-begin)/1_000_000.0);row.add("starts",entries);row.add("cities",cities);row.add("rejections",new com.google.gson.Gson().toJsonTree(rejects));report.add(row);totalAccepted+=accepted;write("natural-candidates.json",report);System.out.println("ABANDONED_NATURAL seed="+seed+" eligible="+eligible+" accepted="+accepted+" reasons="+rejects);
        }
        write("natural-candidates.json",report);
        t.assertTrue(totalAccepted>0,"No naturally accepted abandoned complex in measured mountain candidates");t.succeed();
    }
    private static void write(String name,com.google.gson.JsonElement data)throws Exception{var path=java.nio.file.Path.of(System.getProperty("slavicmyths.chudRoot"),"docs/verification/chud-1.5.2",name);java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(data));}
}
