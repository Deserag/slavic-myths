package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadNativeSurveyGameTests {
@GameTest(template="empty",timeoutTicks=1200)
 public static void nativeTerrainSurveyFiveSeeds(GameTestHelper t) {
  var world=t.getLevel();var registry=world.registryAccess();
  var noise=registry.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.NETHER);
  var preset=registry.registryOrThrow(net.minecraft.core.registries.Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
  var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
  var biome=registry.registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.NETHER_WASTES);
  var structure=new PosadStructure(new net.minecraft.world.level.levelgen.structure.Structure.StructureSettings(net.minecraft.core.HolderSet.direct(new java.util.ArrayList<>(generator.getBiomeSource().possibleBiomes()))));
  var structureSets=registry.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);
  var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)
   structureSets.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","nether_posad")).placement();
  int total=0;
  for(long seed:new long[]{11,29,101,2026,22022026}) {
   var random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),registry.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);
   int accepted=0;long start=System.nanoTime();
   for(int cell=0;cell<8;cell++) {
    var cp=placement.getPotentialStructureChunk(seed,cell*placement.spacing(),0);
    var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,cp,world,structure.biomes()::contains);
    if(structure.findGenerationPoint(context).isPresent())accepted++;
   }
   total+=accepted;
   System.out.println("POSAD_NATIVE_SURVEY seed="+seed+" candidates=8 accepted="+accepted+" nanos="+(System.nanoTime()-start));
  }
  System.out.println("POSAD_NATIVE_SURVEY totalAccepted="+total+" candidates=40; NO PHYSICAL NETHER STARTS/FREQUENCY CLAIM");
  t.succeed();
 }
}
