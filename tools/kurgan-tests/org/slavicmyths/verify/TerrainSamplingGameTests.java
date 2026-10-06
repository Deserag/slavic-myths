package org.slavicmyths.verify;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.StructureCoverageService;
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class TerrainSamplingGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void rawNoiseHeightParity(GameTestHelper test){
  var world=test.getLevel();var noise=world.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
  var preset=world.registryAccess().registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
  var generator=new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(preset),noise);int comparisons=0;
  for(long seed:new long[]{0,17,731269}){
   var random=RandomState.create(noise.value(),world.registryAccess().lookupOrThrow(Registries.NOISE),seed);
   var context=new Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,new ChunkPos(0,0),world,h->true);
   try(var scope=StructureCoverageService.sampleTerrain(context)){
    for(int[] position:new int[][]{{0,0},{-127,64},{511,-255},{2048,2048},{-3072,-2048},{4096,-4096}})for(var type:new Heightmap.Types[]{Heightmap.Types.OCEAN_FLOOR_WG,Heightmap.Types.WORLD_SURFACE_WG}){
     int expected=generator.getBaseHeight(position[0],position[1],type,world,random),actual=StructureCoverageService.baseHeight(generator,position[0],position[1],type,world,random);
     test.assertTrue(actual==expected,"Noise-column height differs from vanilla seed="+seed+" type="+type+" expected="+expected+" actual="+actual);comparisons++;
    }
   }
  }
  System.out.println("TERRAIN_RAW_NOISE_PARITY_PASS comparisons="+comparisons+" seeds=3 bothHeightmaps=true");test.succeed();
 }
}
