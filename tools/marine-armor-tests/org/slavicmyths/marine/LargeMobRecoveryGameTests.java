package org.slavicmyths.marine;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_marine_armor") @PrefixGameTestTemplate(false)
public final class LargeMobRecoveryGameTests {
 @GameTest(template="empty",timeoutTicks=60)
 public static void embeddedBossesEscapeWithoutBreakingTrees(GameTestHelper t){
  var world=t.getLevel();
  for(String id:new String[]{"zmey_gorynych","ocean_zmey","deep_sea_serpent","morovoy_fire_zmey","morovoy_raven","stratim_bird","zhelva","tsar_pike","pharaohka"})for(boolean central:new boolean[]{false,true}){
   var m=BossRetaliationGameTests.boss(t,id);m.moveTo(t.absoluteVec(new Vec3(8,30,8)));
   var original=m.position();var box=m.getBoundingBox();
   var log=BlockPos.containing(central?m.getX():box.maxX-.1,box.minY+.2,m.getZ());var old=world.getBlockState(log);
   world.setBlock(log,Blocks.OAK_LOG.defaultBlockState(),3);
   t.assertTrue(!world.noCollision(m,m.getBoundingBox()),id+" fixture is not embedded");
   t.assertTrue(org.slavicmyths.combat.LargeMobClearance.steer(m,new Vec3(0,0,1)).lengthSqr()==0,id+" did not reproduce old stuck state");
   for(int n=0;n<64&&!world.noCollision(m,m.getBoundingBox().deflate(.001));n++){
    var before=m.position();t.assertTrue(org.slavicmyths.combat.LargeMobRecovery.recover(m),id+" cannot recover central="+central);
    t.assertTrue(m.position().distanceToSqr(before)<=.063,"Recovery jumped farther than a quarter block");
   }
   t.assertTrue(world.noCollision(m,m.getBoundingBox().deflate(.001)),id+" remains embedded");
   t.assertTrue(m.getY()==original.y,"Recovery changed height");t.assertTrue(world.getBlockState(log).is(Blocks.OAK_LOG),"Recovery destroyed the tree");
   var free=m.position();t.assertTrue(!org.slavicmyths.combat.LargeMobRecovery.recover(m)&&m.position().equals(free),"Unobstructed boss was moved");
   world.setBlock(log,old,3);m.discard();
  }t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void sealedBossCannotPassThroughWalls(GameTestHelper t){
  var m=BossRetaliationGameTests.boss(t,"tsar_pike");m.moveTo(t.absoluteVec(new Vec3(8,30,8)));var start=m.position();
  var center=BlockPos.containing(start);for(int x=-5;x<=5;x++)for(int y=-1;y<=5;y++)for(int z=-5;z<=5;z++)t.getLevel().setBlock(center.offset(x,y,z),Blocks.STONE.defaultBlockState(),3);
  t.assertTrue(!org.slavicmyths.combat.LargeMobRecovery.recover(m),"Boss tunneled through sealed stone");
  t.assertTrue(m.position().equals(start),"Failed recovery moved boss");
  t.assertTrue(t.getLevel().getBlockState(center).is(Blocks.STONE),"Failed recovery broke stone");m.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=60)
 public static void recoveryTickFreesUnderwaterBossAndHonorsNoAi(GameTestHelper t){
  var m=BossRetaliationGameTests.boss(t,"zhelva");m.moveTo(t.absoluteVec(new Vec3(8,30,8)));var center=BlockPos.containing(m.position());var w=t.getLevel();
  for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)for(int y=0;y<=6;y++)w.setBlock(center.offset(x,y,z),Blocks.WATER.defaultBlockState(),3);
  var log=center.above();w.setBlock(log,Blocks.OAK_LOG.defaultBlockState(),3);m.baseTick();
  t.assertTrue(m.isInWaterOrBubble(),"Fixture is not underwater");m.tickCount=10;m.setNoAi(true);var start=m.position();
  org.slavicmyths.combat.LargeMobRecovery.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(m));
  t.assertTrue(m.position().equals(start),"Recovery moved a no-AI entity");m.setNoAi(false);
  for(int i=0;i<64&&!w.noCollision(m,m.getBoundingBox().deflate(.001));i++)org.slavicmyths.combat.LargeMobRecovery.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(m));
  t.assertTrue(w.noCollision(m,m.getBoundingBox().deflate(.001)),"Tick hook did not free underwater turtle");
  t.assertTrue(w.getFluidState(BlockPos.containing(m.position())).is(net.minecraft.tags.FluidTags.WATER),"Recovery moved aquatic boss out of water");
  t.assertTrue(w.getBlockState(log).is(Blocks.OAK_LOG),"Recovery destroyed underwater tree");m.discard();t.succeed();
 }}