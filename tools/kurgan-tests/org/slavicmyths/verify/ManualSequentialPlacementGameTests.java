package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.ManualStructureJobs;
@GameTestHolder("slavicmyths_sequential_placement") @PrefixGameTestTemplate(false)
public final class ManualSequentialPlacementGameTests {
 @GameTest(template="port_empty",timeoutTicks=200000)
 public static void sixCommandsFromOnePositionPreserveEarlierStructures(GameTestHelper test){
  var world=test.getLevel();String[] commands={"slavicmyths generate kurgan small 0","slavicmyths generate kurgan warrior 0","slavicmyths generate kurgan great 0","slavicmyths generate bandit_camp small 0","slavicmyths generate bandit_camp fortified 0","slavicmyths generate bandit_camp large 0"};var sourcePos=test.absolutePos(BlockPos.ZERO).offset(8192,0,8192);var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(sourcePos.getX(),0,sourcePos.getZ()));int[] stage={0};UUID[] job={null};var entrances=new ArrayList<BlockPos>();
  test.onEachTick(()->{java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L);
   if(stage[0]==commands.length){System.out.println("MANUAL_SEQUENTIAL_SAME_SOURCE_PASS commands=6 structures=6 previousEntrancesPreserved=true boundedLocalRelocation=true");test.succeed();return;}
   if(job[0]==null){try{test.assertTrue(world.getServer().getCommands().getDispatcher().execute(commands[stage[0]],source)==1,"Sequential command not accepted");job[0]=ManualStructureJobs.lastJob;}catch(Exception e){throw new RuntimeException(e);}return;}
   var result=ManualStructureJobs.result(job[0]);if(result==null)return;test.assertTrue(result.success(),"Sequential command failed: "+commands[stage[0]]+" / "+result.message());var arrival=result.entrance();int x,z,initialZ;
   if(stage[0]<3){var instance=org.slavicmyths.kurgan.BurialRecords.get(world).instances.get(job[0]);test.assertTrue(instance!=null,"Sequential kurgan missing");x=instance.origin.getX();z=instance.origin.getZ();initialZ=sourcePos.getZ()-128-instance.plan.radius;}
   else {x=arrival.getX()-(stage[0]==5?39:10);z=arrival.getZ()-2;initialZ=sourcePos.getZ()-96-(stage[0]==5?84:44);}
   int dx=x-sourcePos.getX(),dz=z-initialZ;test.assertTrue(dx*dx+dz*dz<=256*256,"Placement escaped local radius");entrances.add(arrival);
   for(var pos:entrances){world.getChunk(pos.getX()>>4,pos.getZ()>>4);test.assertTrue(world.getBlockState(pos).isAir()&&world.getBlockState(pos.above()).isAir()&&!world.getBlockState(pos.below()).getCollisionShape(world,pos.below()).isEmpty(),"Previous entrance damaged by a later placement: "+pos);}
   System.out.println("MANUAL_SEQUENTIAL_COMMAND_PASS "+commands[stage[0]]+" dx="+dx+" dz="+dz+" entrance="+arrival+" ticks="+result.ticksElapsed()+" peakJobMs="+result.peakNanos()/1000000.0);stage[0]++;job[0]=null;
  });
 }
}
