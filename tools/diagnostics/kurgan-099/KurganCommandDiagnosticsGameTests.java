package org.slavicmyths.kurgan;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.StructureCoverageService;
/** Diagnosis of the reported bug; success means reproduction, NOT a fixed command. */
@GameTestHolder("slavicmyths") @PrefixGameTestTemplate(false)
public final class KurganCommandDiagnosticsGameTests {
 @GameTest(template="port_empty",timeoutTicks=200)
 public static void reproduceUnloadedHeightCommandFailure(GameTestHelper test) throws Exception {
  var world=test.getLevel();int x=12000000,z=12000000;
  for(int[] offset:new int[][]{{192,0},{-192,0},{0,192},{0,-192}}){
   var center=new BlockPos(x+offset[0],172,z+offset[1]);var sample=center.offset(-8,0,-8);
   world.getChunk(sample.getX()>>4,sample.getZ()>>4);world.setBlock(sample,Blocks.GRASS_BLOCK.defaultBlockState(),2);
   test.assertTrue(!world.hasChunk(center.getX()>>4,center.getZ()>>4),"Diagnosis requires an unloaded center chunk");
   int missing=world.getHeight(Heightmap.Types.OCEAN_FLOOR,center.getX(),center.getZ())-1;
   int actual=world.getHeight(Heightmap.Types.OCEAN_FLOOR,sample.getX(),sample.getZ())-1;
   test.assertTrue(missing==-65&&actual==172,"Unexpected height fixture: "+missing+" / "+actual);
  }
  var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(x,172,z));
  long begin=System.nanoTime();int result=world.getServer().getCommands().getDispatcher().execute("slavicmyths kurgan generate small",source);
  var diagnostics=StructureCoverageService.lastPlacement();
  test.assertTrue(result==0&&diagnostics!=null&&diagnostics.lastReason()==StructureCoverageService.Rejection.EXTREME_TERRAIN,"Command did not reproduce the reported rejection: "+diagnostics);
  test.assertTrue(diagnostics.lastMeasurements().getOrDefault("moundRelief",0L)==237L,"Expected false 237-block relief: "+diagnostics);
  System.out.println("KURGAN_COMMAND_BUG_REPRODUCED actualDispatcher=true result=0 unloadedHeight=-65 realHeight=172 falseRelief=237 elapsedMs="+(System.nanoTime()-begin)/1000000);test.succeed();
 }
}
