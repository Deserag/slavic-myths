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
}
