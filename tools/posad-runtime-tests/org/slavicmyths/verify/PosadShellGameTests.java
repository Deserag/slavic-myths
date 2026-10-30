package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.posad.*;
import net.minecraft.world.level.block.Blocks;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadShellGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void openGate(GameTestHelper t) {
  for(int x=53;x<=59;x++)for(int z=108;z<=112;z++)for(int y=1;y<=6;y++)
   t.assertTrue(PosadShell.voxel(x,y,z).isAir(),"Gate blocked "+x+","+y+","+z);
  for(int x=0;x<113;x++)for(int z=0;z<113;z++)
   t.assertTrue(PosadShell.voxel(x,0,z)!=null&&!PosadShell.voxel(x,0,z).isAir(),"Platform hole");
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void patrolAndSlots(GameTestHelper t) {
  for(int z=11;z<102;z++)for(int x=1;x<=3;x++) {
   t.assertTrue(!PosadShell.voxel(x,8,z).isAir(),"Missing patrol floor");
   for(int y=9;y<=10;y++)t.assertTrue(PosadShell.voxel(x,y,z).isAir(),"Blocked patrol");
  }
  for(var slot:PosadGeometry.Slot.values())for(int x=slot.local().minX();x<=slot.local().maxX();x++)
   for(int z=slot.local().minZ();z<=slot.local().maxZ();z++)for(int y=1;y<18;y++)
    t.assertTrue(PosadShell.voxel(x,y,z)==null,"Shell occupies building slot "+slot);
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void physicalChunkPlacement(GameTestHelper t) {
  var world=t.getLevel();
  for(int turns=0;turns<4;turns++) {
  var anchor=t.absolutePos(new net.minecraft.core.BlockPos(turns*160,140,0));
  var piece=new PosadPiece(anchor,turns);
  for(var patch:PosadGeometry.chunkPatches(anchor.getX(),anchor.getZ())) {
   world.getChunk(patch.chunkX(),patch.chunkZ());
   var r=patch.worldClip();
   var clip=new net.minecraft.world.level.levelgen.structure.BoundingBox(r.minX(),anchor.getY()-2,r.minZ(),r.maxX(),anchor.getY()+17,r.maxZ());
   piece.postProcess(world,world.structureManager(),world.getChunkSource().getGenerator(),world.random,clip,
    new net.minecraft.world.level.ChunkPos(patch.chunkX(),patch.chunkZ()),anchor);
  }
  for(int x=0;x<113;x++)for(int z=0;z<113;z++) {
   var at=anchor.offset(x,0,z);
   t.assertTrue(!world.getBlockState(at).isAir(),"Physical platform hole "+at);
  }
  for(int x=53;x<=59;x++)for(int z=108;z<=112;z++)for(int y=1;y<=6;y++) {
   var p=PosadGeometry.rotate(new PosadGeometry.Cell(x,z),turns);
   t.assertTrue(world.getBlockState(anchor.offset(p.x(),y,p.z())).isAir(),"Physical rotated gate blocked");
  }
  }
  t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void foundationAndBridgeSerialization(GameTestHelper t) {
  var decision=PosadTerrain.inspect((x,y,z)->y>=64?PosadTerrain.Material.AIR:
   (z>=136||y<40?PosadTerrain.Material.NATURAL:PosadTerrain.Material.LAVA),0,64,0,0);
  t.assertTrue(decision.accepted(),"Lava shore plan rejected");
  var piece=new PosadPiece(decision.plan());
  var context=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(t.getLevel());
  var restored=new PosadPiece(piece.createTag(context));
  t.assertTrue(restored.getBoundingBox().equals(piece.getBoundingBox()),"Bounds changed after NBT");
  for(int z=0;z<=136;z++)for(int x=0;x<113;x++)for(int y=-48;y<18;y++)
   t.assertTrue(java.util.Objects.equals(piece.voxel(x,y,z),restored.voxel(x,y,z)),"Lost terrain geometry after NBT");
  t.assertTrue(piece.voxel(0,-20,0).is(Blocks.POLISHED_BASALT),"Missing deep support");
  for(int z=113;z<=136;z++)for(int x=53;x<=59;x++) {
   t.assertTrue(piece.voxel(x,0,z).is(Blocks.POLISHED_BLACKSTONE_BRICKS),"Bridge deck hole");
   t.assertTrue(piece.voxel(x,1,z).isAir(),"Blocked bridge");
  }
  t.succeed();
 }

 @GameTest(template="empty",timeoutTicks=1200)
 public static void nativeTerrainSurveyFiveSeeds(GameTestHelper t) {
  var world=t.getLevel();var registry=world.registryAccess();
  var noise=registry.registryOrThrow(net.minecraft.core.registries.Registries.NOISE_SETTINGS).getHolderOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.NETHER);
  var preset=registry.registryOrThrow(net.minecraft.core.registries.Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
  var generator=new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),noise);
  var biome=registry.registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.world.level.biome.Biomes.NETHER_WASTES);
  var structure=new PosadStructure(new net.minecraft.world.level.levelgen.structure.Structure.StructureSettings(net.minecraft.core.HolderSet.direct(new java.util.ArrayList<>(generator.getBiomeSource().possibleBiomes()))));
  var placement=new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(80,32,net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR,22022026);
  int total=0;
  for(long seed:new long[]{11,29,101,2026,22022026}) {
   var random=net.minecraft.world.level.levelgen.RandomState.create(noise.value(),registry.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);
   int accepted=0;long start=System.nanoTime();
   for(int cell=0;cell<8;cell++) {
    var cp=placement.getPotentialStructureChunk(seed,cell*80,0);
    var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,world.getStructureManager(),seed,cp,world,structure.biomes()::contains);
    if(structure.findGenerationPoint(context).isPresent())accepted++;
   }
   total+=accepted;
   System.out.println("POSAD_NATIVE_SURVEY seed="+seed+" candidates=8 accepted="+accepted+" nanos="+(System.nanoTime()-start));
  }
  System.out.println("POSAD_NATIVE_SURVEY totalAccepted="+total+" candidates=40; NO PHYSICAL NETHER STARTS/FREQUENCY CLAIM");
  t.succeed();
 }


 @GameTest(template="empty",timeoutTicks=200)
 public static void towerLaddersEntrancesAndMasonry(GameTestHelper t) {
  int[][] towers={{0,0},{102,0},{0,102},{102,102},{42,102},{62,102}};
  for(int[] tower:towers) {
   int ox=tower[0],oz=tower[1];
   for(int y=1;y<=13;y++) {
    t.assertTrue(PosadShell.voxel(ox+2,y,oz+2).is(Blocks.LADDER),"Missing ladder "+ox+","+y+","+oz);
    t.assertTrue(!PosadShell.voxel(ox+2,y,oz+1).isAir(),"Unsupported ladder "+ox+","+y+","+oz);
   }
  }
  for(int y=1;y<=3;y++) {
   for(int z=0;z<113;z++) {
    t.assertTrue(!PosadShell.voxel(0,y,z).isAir(),"West exterior exit "+y+","+z);
    t.assertTrue(!PosadShell.voxel(112,y,z).isAir(),"East exterior exit "+y+","+z);
   }
   for(int x=0;x<113;x++) {
    t.assertTrue(!PosadShell.voxel(x,y,0).isAir(),"North exterior exit "+x+","+y);
    if(x<53||x>59)t.assertTrue(!PosadShell.voxel(x,y,112).isAir(),"South exterior exit "+x+","+y);
   }
  }
  // Inner entrances and connected high patrol openings must remain traversable.
  t.assertTrue(PosadShell.voxel(10,2,5).isAir(),"NW inner entry");
  t.assertTrue(PosadShell.voxel(102,2,5).isAir(),"NE inner entry");
  t.assertTrue(PosadShell.voxel(10,2,107).isAir(),"SW inner entry");
  t.assertTrue(PosadShell.voxel(102,2,107).isAir(),"SE inner entry");
  t.assertTrue(PosadShell.voxel(46,2,102).isAir(),"Left gate tower inner entry");
  t.assertTrue(PosadShell.voxel(66,2,102).isAir(),"Right gate tower inner entry");
  t.assertTrue(PosadShell.voxel(10,9,2).isAir(),"NW patrol link");
  t.assertTrue(PosadShell.voxel(102,9,2).isAir(),"NE patrol link");
  t.assertTrue(PosadShell.voxel(10,9,110).isAir(),"SW patrol link");
  t.assertTrue(PosadShell.voxel(102,9,110).isAir(),"SE patrol link");
  t.assertTrue(PosadShell.voxel(42,9,110).isAir(),"West gate patrol link");
  t.assertTrue(PosadShell.voxel(70,9,110).isAir(),"East gate patrol link");
  var roadMaterials=new java.util.HashSet<net.minecraft.world.level.block.Block>();
  var wallMaterials=new java.util.HashSet<net.minecraft.world.level.block.Block>();
  for(int z=12;z<=100;z++)roadMaterials.add(PosadShell.voxel(55,0,z).getBlock());
  for(int x=15;x<=90;x++)for(int y=1;y<=8;y++)wallMaterials.add(PosadShell.voxel(x,y,0).getBlock());
  t.assertTrue(roadMaterials.size()>=3,"Road palette lacks variety");
  t.assertTrue(wallMaterials.size()>=3,"Wall palette lacks variety");
  t.succeed();
 }

}
