package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.ManualStructureJobs;
import org.slavicmyths.kurgan.BurialRecords;
@GameTestHolder("slavicmyths_placement") @PrefixGameTestTemplate(false)
public final class ManualPlacementGameTests {
 @GameTest(template="port_empty",timeoutTicks=100000)
 public static void sixRealManualCommandsOnThinFlat(GameTestHelper test){
  var world=test.getLevel();test.assertTrue(!world.getServer().getWorldData().worldGenOptions().generateStructures(),"Manual fixture must have natural structures disabled");System.out.println("MANUAL_EXISTING_RECORDS_FROM_PREVIOUS_PROCESS count="+org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().size());String[] commands={"slavicmyths generate kurgan small 0","slavicmyths generate kurgan warrior 0","slavicmyths generate kurgan great 0","slavicmyths generate bandit_camp small 0","slavicmyths generate bandit_camp fortified 0","slavicmyths generate bandit_camp large 0","slavicmyths generate bandit_camp fortified 0","slavicmyths generate kurgan small 0","slavicmyths generate kurgan great 0","slavicmyths generate kurgan small 0"};
  int baseX=test.absolutePos(BlockPos.ZERO).getX()+8192,baseZ=test.absolutePos(BlockPos.ZERO).getZ()+8192;
  int[] stage={0};UUID[] job={null};long[] max={0};
  test.onEachTick(()->{
   java.util.concurrent.locks.LockSupport.parkNanos(1_000_000L); // GameTest ticks run without the normal 50 ms pacing; allow async chunk workers to advance.
   if(stage[0]==commands.length){System.out.println("MANUAL_STRUCTURE_THIN_FLAT_PASS actualCommands=10 realStructures=10 accessibleEntrances=10 hillFixtures=2 waterRelocation=1 protectedConstruction=1 maxOperations="+max[0]+" repeatedRequestGuard=true");try{var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(baseX,0,baseZ));var dispatcher=world.getServer().getCommands().getDispatcher();test.assertTrue(dispatcher.execute("slavicmyths generate kurgan small 0",source)==1,"Cancellation fixture not accepted");var cancelled=ManualStructureJobs.lastJob;test.assertTrue(dispatcher.execute("slavicmyths generation status",source)==1,"Status did not expose active job");test.assertTrue(dispatcher.execute("slavicmyths generation cancel",source)==1,"Cancel failed");test.assertTrue(!ManualStructureJobs.active(world)&&!ManualStructureJobs.result(cancelled).success(),"Cancel retained an active job");test.assertTrue(dispatcher.execute("slavicmyths search status",source)==0,"Unexpected active search");System.out.println("MANUAL_JOB_STATUS_CANCEL_PASS releasedActiveJob=true");}catch(Exception e){throw new RuntimeException(e);}test.succeed();return;}
   if(job[0]==null){try{
    int sourceX=baseX+stage[0]*4096;int surface=world.getChunkSource().getGenerator().getBaseHeight(sourceX,baseZ,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState())-1;
    if(stage[0]>=6&&stage[0]<=8){int radius=stage[0]==8?org.slavicmyths.kurgan.KurganPlan.create(2,0).radius:stage[0]==7?org.slavicmyths.kurgan.KurganPlan.create(0,0).radius:0;int az=baseZ-(stage[0]==6?140:128+radius);
     if(stage[0]==7){for(int x=sourceX-40;x<=sourceX+40;x++)for(int z=az-40;z<=az+40;z++){world.getChunk(x>>4,z>>4);world.setBlock(new BlockPos(x,surface,z),net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),2);}}
     else {for(int x=sourceX;x<sourceX+24;x++)for(int z=az;z<az+24;z++){world.getChunk(x>>4,z>>4);int rise=1+Math.min(18,(x-sourceX)/2+(z-az)/2);for(int y=surface+1;y<=surface+rise;y++)world.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState(),2);world.setBlock(new BlockPos(x,surface+rise,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState(),2);world.setBlock(new BlockPos(x,surface+rise+1,z),net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState(),2);}}
    }
    if(stage[0]==8){int az=baseZ-128-org.slavicmyths.kurgan.KurganPlan.create(2,0).radius;world.getChunk(sourceX>>4,az>>4);world.setBlock(new BlockPos(sourceX,world.getMinBuildHeight()+1,az),net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(),2);((net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(new BlockPos(sourceX,world.getMinBuildHeight()+1,az))).setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));}
    if(stage[0]==9){var plan=org.slavicmyths.kurgan.KurganPlan.create(0,0);var room=plan.rooms.get(1);int target=Math.max(surface,world.getMinBuildHeight()+2-plan.bounds().y0);var pos=new BlockPos(sourceX+room.x,target+room.y+1,baseZ-128-plan.radius+room.z);world.getChunk(pos.getX()>>4,pos.getZ()>>4);world.setBlock(pos,net.minecraft.world.level.block.Blocks.BRICKS.defaultBlockState(),2);}
    var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(baseX+stage[0]*4096,world.getChunkSource().getGenerator().getBaseHeight(baseX+stage[0]*4096,baseZ,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState()),baseZ));int accepted=world.getServer().getCommands().getDispatcher().execute(commands[stage[0]],source);test.assertTrue(accepted==1,"Command not accepted: "+commands[stage[0]]);job[0]=ManualStructureJobs.lastJob;
    test.assertTrue(world.getServer().getCommands().getDispatcher().execute(commands[stage[0]],source)==0,"Concurrent generation was accepted");
   }catch(Exception e){throw new RuntimeException(e);}return;}
   var result=ManualStructureJobs.result(job[0]);if(result==null)return;

   test.assertTrue(result.success(),"Command failed: "+commands[stage[0]]+" / "+result.message());
   if(stage[0]==9){var plan=org.slavicmyths.kurgan.KurganPlan.create(0,0);var room=plan.rooms.get(1);int sourceX=baseX+stage[0]*4096;int original=world.getChunkSource().getGenerator().getBaseHeight(sourceX,baseZ,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState())-1;int target=Math.max(original,world.getMinBuildHeight()+2-plan.bounds().y0);var protectedPos=new BlockPos(sourceX+room.x,target+room.y+1,baseZ-128-plan.radius+room.z);test.assertTrue(world.getBlockState(protectedPos).is(net.minecraft.world.level.block.Blocks.BRICKS),"Protected underground construction was changed");System.out.println("MANUAL_FINAL_VOLUME_PROTECTION_PASS originalBrickPreserved=true localRelocation=true");}
   var arrival=result.entrance();test.assertTrue(arrival!=null&&world.getBlockState(arrival).isAir()&&world.getBlockState(arrival.above()).isAir()&&!world.getBlockState(arrival.below()).getCollisionShape(world,arrival.below()).isEmpty(),"Entrance inaccessible: "+commands[stage[0]]+" "+arrival);
   test.assertTrue(result.peakOperations()<=65536,"Operation budget exceeded: "+result.peakOperations());max[0]=Math.max(max[0],result.peakOperations());
   if(stage[0]<3||stage[0]>=7){var instance=BurialRecords.get(world).instances.get(job[0]);test.assertTrue(instance!=null,"Actual kurgan instance missing");test.assertTrue(instance.origin.getY()+instance.plan.bounds().y0>=world.getMinBuildHeight()+2,"Dungeon intersects bedrock");
    if(stage[0]==7){int expectedZ=baseZ-128-instance.plan.radius;int dx=instance.origin.getX()-(baseX+stage[0]*4096),dz=instance.origin.getZ()-expectedZ;test.assertTrue(dx*dx+dz*dz>0&&dx*dx+dz*dz<=256*256,"Water relocation exceeded local radius or stayed in water");}
    if(stage[0]==8){var buried=new BlockPos(baseX+stage[0]*4096,world.getMinBuildHeight()+1,baseZ-128-instance.plan.radius);world.getChunk(buried.getX()>>4,buried.getZ()>>4);test.assertTrue(world.getBlockEntity(buried) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest&&chest.getItem(0).is(net.minecraft.world.item.Items.DIAMOND),"Container outside FINAL placement volume was altered or wrongly rejected");}
    walkApproach(test,arrival);}
   else {var record=org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().stream().filter(e->e.id().equals(job[0])).findFirst().orElseThrow();var box=record.bounds();int containers=0;for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++){var chunk=world.getChunk(x,z);for(var tile:chunk.getBlockEntities().values())if(box.isInside(tile.getBlockPos())&&tile instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)containers++;}test.assertTrue(containers>0,"Camp template loot containers were not actually placed");}
   test.assertTrue(org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().stream().anyMatch(e->e.id().equals(job[0])),"Completed structure was not recorded");
   try{var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(new Vec3(baseX+stage[0]*4096,0,baseZ));String locate=commands[stage[0]].replace("generate","locate").replace("fortified","medium").replace(" 0","");test.assertTrue(world.getServer().getCommands().getDispatcher().execute(locate,source)==1,"Locating manually generated structure failed: "+locate);test.assertTrue(!org.slavicmyths.worldgen.BoundedStructureSearch.active(world),"Known structure lookup unexpectedly started remote chunk generation");}catch(Exception e){throw new RuntimeException(e);}
   System.out.println("MANUAL_STRUCTURE_COMMAND_PASS "+commands[stage[0]]+" entrance="+arrival+" constructionY="+result.surfaceY()+" changedBlocks="+result.changedBlocks()+" peakOperations="+result.peakOperations()+" peakJobMs="+result.peakNanos()/1000000.0+" slowestPhase="+result.slowestPhase()+" ticks="+result.ticksElapsed());stage[0]++;job[0]=null;
  });
 }
 private static void walkApproach(GameTestHelper test,BlockPos arrival){
  var world=test.getLevel();BlockPos current=arrival;int steps=0;
  for(int z=1;z<=128;z++){BlockPos next=null;for(int dy=1;dy>=-1;dy--){var p=current.offset(0,dy,1);world.getChunk(p.getX()>>4,p.getZ()>>4); // Verification reads persisted chunks after the job releases its own tickets.
if(world.getBlockState(p).isAir()&&world.getBlockState(p.above()).isAir()&&!world.getBlockState(p.below()).getCollisionShape(world,p.below()).isEmpty()){next=p;break;}}
   if(next==null){for(int y=current.getY()-2;y<=current.getY()+4;y++)System.out.println("APPROACH_BLOCK "+current.offset(0,y-current.getY(),1)+" "+world.getBlockState(current.offset(0,y-current.getY(),1)));}test.assertTrue(next!=null,"Kurgan approach has an impassable step after "+steps+" blocks: "+current);current=next;steps++;
   int original=world.getChunkSource().getGenerator().getBaseHeight(current.getX(),current.getZ(),net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,world,world.getChunkSource().randomState());if(steps>=12&&current.getY()==original)return;
  }
  test.fail("Kurgan approach did not reach original ground in 128 steps");
 }
}
