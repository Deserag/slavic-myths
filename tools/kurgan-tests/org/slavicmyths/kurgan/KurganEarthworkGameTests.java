package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.flat.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class KurganEarthworkGameTests {
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void thinSuperflatPlans(GameTestHelper test){
  var world=test.getLevel();
  var settings=new FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(Biomes.PLAINS),List.of());
  settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new FlatLayerInfo(2,Blocks.DIRT));settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.GRASS_BLOCK));settings.updateLayers();
  var generator=new net.minecraft.world.level.levelgen.FlatLevelSource(settings);
  long largest=0;int checked=0;
  for(int tier=0;tier<3;tier++)for(int seed=0;seed<10;seed++){
   var plan=KurganPlan.create(tier,seed);var original=new BlockPos(8,world.getMinBuildHeight()+3,8);
   test.assertTrue(!KurganStructure.terrainValid(generator,plan,original,world,world.getChunkSource().randomState()),"Thin terrain unexpectedly covers dungeon");
   var site=KurganEarthwork.plan(generator,world.getChunkSource().randomState(),world,plan,original);
   test.assertTrue(site!=null&&site.earthwork()!=null,"Budget rejects thin-flat tier="+tier+" seed="+seed);
   test.assertTrue(site.origin().getY()+plan.bounds().y0>=world.getMinBuildHeight()+2,"Dungeon pierces bottom");
   var piece=new KurganDungeonPiece(plan,site.origin(),UUID.randomUUID(),site.earthwork());
   test.assertTrue(org.slavicmyths.worldgen.StructureCoverageService.referencesFit(new net.minecraft.world.level.ChunkPos(0,0),List.of(piece)),"Earthworks escape native eight-chunk references tier="+tier+" seed="+seed);
   var restored=KurganEarthwork.load(site.earthwork().save());
   test.assertTrue(restored.volume==site.earthwork().volume&&restored.save().equals(site.earthwork().save()),"Soil plan changed after reload");
   largest=Math.max(largest,restored.volume);checked++;
  }
  int accepted=0;
  for(int tier=0;tier<3;tier++){
   var structure=world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",KurganStructures.id(tier)));
   boolean found=false;
   for(int attempt=0;attempt<24;attempt++){
    var start=structure.generate(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),attempt,new net.minecraft.world.level.ChunkPos(8192+attempt*17,8192+attempt*23),0,world,h->h.is(Biomes.PLAINS));
    if(start.isValid()){test.assertTrue(start.getPieces().size()==1,"Partial thin-flat start");found=true;accepted++;break;}
   }
   test.assertTrue(found,"No actual accepted thin-superflat worldgen start tier="+tier);
  }
  System.out.println("KURGAN_THIN_SUPERFLAT_NATIVE_START_PASS tiers="+accepted);
  System.out.println("KURGAN_THIN_SUPERFLAT_PLAN_PASS plans="+checked+" tiers=3 largestFill="+largest+" saved=true");test.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=100)
 public static void approachHasHeadroom(GameTestHelper test){
  var world=test.getLevel();var settings=new FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(Biomes.PLAINS),List.of());
  settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new FlatLayerInfo(2,Blocks.DIRT));settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.GRASS_BLOCK));settings.updateLayers();
  var generator=new net.minecraft.world.level.levelgen.FlatLevelSource(settings);
  for(int tier=0;tier<3;tier++){
  var plan=KurganPlan.create(tier,0);
  var site=KurganEarthwork.plan(generator,world.getChunkSource().randomState(),world,plan,new BlockPos(24576+tier*512,world.getMinBuildHeight()+3,24576));
  var soil=site.earthwork();var origin=site.origin();var bounds=soil.bounds(origin);
  var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(origin.getX()-1,world.getMinBuildHeight(),origin.getZ()+plan.radius+1,origin.getX()+1,world.getMaxBuildHeight()-1,bounds.maxZ());
  for(int cx=clip.minX()>>4;cx<=clip.maxX()>>4;cx++)for(int cz=clip.minZ()>>4;cz<=clip.maxZ()>>4;cz++)world.getChunk(cx,cz);
  // Explicit thin-flat fixture in the tested approach strip, independent of the server world's generator.
  for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++)for(int y=world.getMinBuildHeight()+4;y<=bounds.maxY()+5;y++)world.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
  soil.place(world,origin,clip);
  var piece=new KurganDungeonPiece(plan,origin,UUID.randomUUID(),soil);new KurganArchitecture(piece,world,clip,new HashSet<>()).place();
  Map<BlockPos,net.minecraft.world.level.block.state.BlockState> protectedMasonry=new HashMap<>();
  for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++)for(int y=world.getMinBuildHeight()+1;y<=bounds.maxY();y++){
   var pos=new BlockPos(x,y,z);var state=world.getBlockState(pos);if(!state.isAir()&&!state.is(Blocks.DIRT)&&!state.is(Blocks.GRASS_BLOCK)&&!state.is(Blocks.DIRT_PATH)&&!state.is(Blocks.SNOW)&&!state.canBeReplaced())protectedMasonry.put(pos,state);
  }
  test.assertTrue(!protectedMasonry.isEmpty(),"Approach fixture does not cross underground masonry tier="+tier);
  soil.approach(world,origin,clip,plan.radius);
  for(var entry:protectedMasonry.entrySet())test.assertTrue(world.getBlockState(entry.getKey()).equals(entry.getValue()),"Entrance approach breaches protected masonry tier="+tier+" at "+entry.getKey());
  for(int lane=-1;lane<=1;lane++){
  int previous=origin.getY();
  for(int z=clip.minZ();z<=clip.maxZ();z++){
   int floor=Integer.MIN_VALUE;
   for(int candidate=previous-1;candidate<=previous+1;candidate++){
    var foot=new BlockPos(origin.getX()+lane,candidate,z);var support=world.getBlockState(foot).getCollisionShape(world,foot);if(support.isEmpty()||support.max(net.minecraft.core.Direction.Axis.Y)<0.875)continue;
    boolean clear=true;for(int y=1;y<=4;y++)if(!world.getBlockState(foot.above(y)).isAir())clear=false;
    if(clear&&(floor==Integer.MIN_VALUE||Math.abs(candidate-previous)<Math.abs(floor-previous)))floor=candidate;
   }
   final int diagnosticY=previous,diagnosticZ=z,diagnosticX=origin.getX()+lane;test.assertTrue(floor!=Integer.MIN_VALUE,"No walkable one-block step / four-block headroom tier="+tier+" lane="+lane+" z="+z+" previous="+previous+" blocks="+java.util.stream.IntStream.rangeClosed(-1,5).mapToObj(d->world.getBlockState(new BlockPos(diagnosticX,diagnosticY+d,diagnosticZ)).toString()).toList());previous=floor;
  }
  }
  }
  System.out.println("KURGAN_RAISED_APPROACH_PASS tiers=3 lanes=3 headroom=4 maxStep=1 protectedMasonryPreserved=true");test.succeed();
 }
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void raisedDungeonChunkReload(GameTestHelper test){
  var world=test.getLevel();var settings=new FlatLevelGeneratorSettings(Optional.empty(),world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(Biomes.PLAINS),List.of());
  settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.BEDROCK));settings.getLayersInfo().add(new FlatLayerInfo(2,Blocks.DIRT));settings.getLayersInfo().add(new FlatLayerInfo(1,Blocks.GRASS_BLOCK));settings.updateLayers();
  var generator=new net.minecraft.world.level.levelgen.FlatLevelSource(settings);var plan=KurganPlan.create(0,0);
  var site=KurganEarthwork.plan(generator,world.getChunkSource().randomState(),world,plan,new BlockPos(32768,world.getMinBuildHeight()+3,32768));
  test.assertTrue(site!=null&&site.earthwork()!=null,"Missing complete raised site");
  var piece=new KurganDungeonPiece(plan,site.origin(),UUID.randomUUID(),site.earthwork());var bounds=piece.getBoundingBox();
  for(int cx=bounds.minX()>>4;cx<=bounds.maxX()>>4;cx++)for(int cz=bounds.minZ()>>4;cz<=bounds.maxZ()>>4;cz++)world.getChunk(cx,cz);
  for(int x=bounds.minX();x<=bounds.maxX();x++)for(int z=bounds.minZ();z<=bounds.maxZ();z++)for(int y=world.getMinBuildHeight();y<=bounds.maxY()+1;y++)world.setBlock(new BlockPos(x,y,z),(y==world.getMinBuildHeight()?Blocks.BEDROCK:y<world.getMinBuildHeight()+3?Blocks.DIRT:y==world.getMinBuildHeight()+3?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);
  Map<BlockPos,net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity> rewards=new HashMap<>();
  var serialization=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(world);
  for(int pass=0;pass<2;pass++){
   for(int cx=bounds.minX()>>4;cx<=bounds.maxX()>>4;cx++)for(int cz=bounds.minZ()>>4;cz<=bounds.maxZ()>>4;cz++){
    var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(cx*16,world.getMinBuildHeight(),cz*16,cx*16+15,world.getMaxBuildHeight()-1,cz*16+15);
    piece.postProcess(world,world.structureManager(),generator,net.minecraft.util.RandomSource.create(0),clip,new net.minecraft.world.level.ChunkPos(cx,cz),site.origin());
   }
   if(pass==0){
    for(int x=bounds.minX();x<=bounds.maxX();x++)for(int z=bounds.minZ();z<=bounds.maxZ();z++)for(int y=bounds.minY();y<=bounds.maxY();y++){
     var pos=new BlockPos(x,y,z);if(world.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity container&&container.getLootTable()!=null){container.unpackLootTable(null);container.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3));rewards.put(pos,container);}
    }
    var tag=piece.createTag(serialization);test.assertTrue(tag.getLongArray("EarthworkChunks").length>1,"Chunk earthwork completion not persisted");
    piece=new KurganDungeonPiece(world.getStructureManager(),tag);test.assertTrue(piece.getBoundingBox().equals(bounds)&&piece.origin.equals(site.origin()),"Raised bounds/origin changed on piece reload");
   }
  }
  test.assertTrue(!rewards.isEmpty(),"Complete raised dungeon contains no rewards");
  for(var reward:rewards.entrySet())test.assertTrue(world.getBlockEntity(reward.getKey())==reward.getValue()&&reward.getValue().getItem(0).getCount()==3&&reward.getValue().getLootTable()==null,"Reloaded raised dungeon replenishes or buries loot");
  var arrival=piece.arrival();test.assertTrue(world.getBlockState(arrival.below()).isSolid()&&world.getBlockState(arrival).isAir()&&world.getBlockState(arrival.above()).isAir(),"Raised entrance inaccessible after complete placement");
  System.out.println("KURGAN_RAISED_DUNGEON_RELOAD_PASS completeChunkPlacement=true containers="+rewards.size()+" pieceNbtReload=true inventoryPreserved=true");test.succeed();
 }
}
