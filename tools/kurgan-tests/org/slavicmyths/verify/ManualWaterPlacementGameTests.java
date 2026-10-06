package org.slavicmyths.verify;
import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.ManualStructureJobs;
@GameTestHolder("slavicmyths_water_placement") @PrefixGameTestTemplate(false)
public final class ManualWaterPlacementGameTests {
 @GameTest(template="port_empty",timeoutTicks=10000)
 public static void oceanDoesNotBecomeAnArtificialIsland(GameTestHelper test){
  var world=test.getLevel();var generator=world.getChunkSource().getGenerator();for(int tier=0;tier<3;tier++){var plan=org.slavicmyths.kurgan.KurganPlan.create(tier,0);int floor=generator.getBaseHeight(65536,65536,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState())-1;test.assertTrue(org.slavicmyths.kurgan.KurganEarthwork.plan(generator,world.getChunkSource().randomState(),world,plan,new BlockPos(65536,floor,65536))==null,"Native earthworks would create an ocean island");}System.out.println("NATIVE_KURGAN_OCEAN_GUARD_PASS tiers=3");String[] commands={"slavicmyths generate kurgan small 0","slavicmyths generate bandit_camp large 0","slavicmyths generate kurgan great 0"};int[] stage={0};UUID[] job={null};var base=test.absolutePos(BlockPos.ZERO).offset(8192,0,8192);
  test.onEachTick(()->{java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
   if(stage[0]==commands.length){System.out.println("MANUAL_OCEAN_RELOCATION_LIMIT_PASS commands=3 noDryLandWithin256=true terrainWrites=0 iceAndLava=true");test.succeed();return;}
   if(job[0]==null){try{int x=base.getX()+stage[0]*4096,z=base.getZ();int radius=stage[0]==0?org.slavicmyths.kurgan.KurganPlan.create(0,0).radius:stage[0]==2?org.slavicmyths.kurgan.KurganPlan.create(2,0).radius:0;int anchorZ=z-(stage[0]==1?180:128+radius);int top=world.getChunkSource().getGenerator().getBaseHeight(x,anchorZ,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,world,world.getChunkSource().randomState())-1;world.getChunk(x>>4,anchorZ>>4);
    if(stage[0]==0)world.setBlock(new BlockPos(x,top,anchorZ),net.minecraft.world.level.block.Blocks.ICE.defaultBlockState(),2);if(stage[0]==2)world.setBlock(new BlockPos(x,top,anchorZ),net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState(),2);
    var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(x,top+1,z));test.assertTrue(world.getServer().getCommands().getDispatcher().execute(commands[stage[0]],source)==1,"Ocean job was not accepted");job[0]=ManualStructureJobs.lastJob;
   }catch(Exception e){throw new RuntimeException(e);}return;}
   var result=ManualStructureJobs.result(job[0]);if(result==null)return;test.assertTrue(!result.success()&&result.message().contains("256"),"Unbounded ocean relocation / unexpected failure: "+result.message());test.assertTrue(result.changedBlocks()==0,"Ocean terraformed into an island");test.assertTrue(!ManualStructureJobs.active(world),"Water failure retained an active job");stage[0]++;job[0]=null;
  });
 }
}
