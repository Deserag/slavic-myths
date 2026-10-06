package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class UnderwaterPlacementGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void bedAnchoredDeepWater(GameTestHelper test){
  var world=test.getLevel();var structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths","underwater_ruins"));int accepted=0;
  for(int depth:new int[]{12,24,48}){
   var settings=new FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.SWAMP),List.of());
   settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new FlatLayerInfo(32,Blocks.STONE));settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.CLAY));settings.getLayersInfo().add(new FlatLayerInfo(depth,Blocks.WATER));settings.updateLayers();
   var generator=new net.minecraft.world.level.levelgen.FlatLevelSource(settings);Set<String> variants=new HashSet<>();
   for(int seed=0;seed<32&&variants.size()<3;seed++){
    var start=structure.generate(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),seed,new ChunkPos(seed*20,0),0,world,h->h.is(Biomes.SWAMP));
    test.assertTrue(start.isValid()&&start.getPieces().size()==1,"Valid deep-water bed rejects ruins depth="+depth+" seed="+seed);
    var piece=start.getPieces().getFirst();var tag=piece.createTag(StructurePieceSerializationContext.fromLevel(world));variants.add(tag.getString("SwampTemplate"));
    test.assertTrue(piece.getBoundingBox().maxY()<world.getMinBuildHeight()+34+depth,"Bed-anchored ruin floats above water surface");accepted++;
   }
   test.assertTrue(variants.size()==3,"Did not exercise all preserved ruin variants");
  }
  System.out.println("UNDERWATER_NATIVE_BED_PLACEMENT_PASS depths=12,24,48 variants=3 accepted="+accepted+" negativeBedY=true");test.succeed();
 }
}
