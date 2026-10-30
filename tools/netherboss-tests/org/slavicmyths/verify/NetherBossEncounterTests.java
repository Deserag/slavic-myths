package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.slavicmyths.netherboss.*;
import org.slavicmyths.registry.ModEntities;

@GameTestHolder("slavicmyths_netherboss") @PrefixGameTestTemplate(false)
public final class NetherBossEncounterTests {
 @net.minecraft.gametest.framework.AfterBatch(batch="defaultBatch")
 public static void clearFixtureTickets(net.minecraft.server.level.ServerLevel world){for(var level:world.getServer().getAllLevels())for(long packed:level.getForcedChunks().toLongArray()){var cp=new net.minecraft.world.level.ChunkPos(packed);level.setChunkForced(cp.x,cp.z,false);}}

 private static void platform(net.minecraft.server.level.ServerLevel w,BlockPos a){
  for(int cx=(a.getX()-6)>>4;cx<=(a.getX()+6)>>4;cx++)for(int cz=(a.getZ()-6)>>4;cz<=(a.getZ()+6)>>4;cz++){var cp=new net.minecraft.world.level.ChunkPos(cx,cz);w.setChunkForced(cp.x,cp.z,true);}
  for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){
   w.getChunk((a.getX()+x)>>4,(a.getZ()+z)>>4);w.setBlock(a.offset(x,0,z),Blocks.BLACKSTONE.defaultBlockState(),2);
   for(int y=1;y<=7;y++)w.setBlock(a.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
  }
 }
 @GameTest(template="empty",timeoutTicks=800)
 public static void ritualAndConcurrentRejection(GameTestHelper t){
  var w=t.getLevel().getServer().getLevel(Level.NETHER);w.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);var base=t.absolutePos(new BlockPos(0,0,0)).atY(90);
  var pending=new ArrayList<NetherSummonBoss>();var actors=new ArrayList<net.minecraft.server.level.ServerPlayer>();
  for(boolean morova:new boolean[]{true,false}){
   var a=base.offset(morova?0:256,0,0);platform(w,a);
   if(morova){w.setBlock(a,Blocks.SOUL_SAND.defaultBlockState(),2);for(var d:List.of(new BlockPos(2,0,0),new BlockPos(-2,0,0),new BlockPos(0,0,2),new BlockPos(0,0,-2)))w.setBlock(a.offset(d).above(),Blocks.CANDLE.defaultBlockState(),2);}
   else w.setBlock(a,Blocks.CAMPFIRE.defaultBlockState(),2);
   var p=FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),morova?"MorovaRiteQA":"TriznaRiteQA"));
   p.setPos(a.getX()+5,a.getY()+1,a.getZ());p.setHealth(20);p.setInvulnerable(true);
   p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(NetherBossItems.item(morova?"amulet_of_the_faded":"last_watch_horn")));
   t.assertTrue(NetherRituals.begin(p,a,net.minecraft.world.InteractionHand.MAIN_HAND,morova),"Valid rite rejected");
   t.assertTrue(!NetherRituals.begin(p,a,net.minecraft.world.InteractionHand.MAIN_HAND,morova),"Parallel rite accepted");
   t.assertTrue(p.getMainHandItem().getCount()==1,"Premature token consumption");actors.add(p);
  }
  t.runAtTickTime(350,()->{
   for(int i=0;i<actors.size();i++){
    var p=actors.get(i);var a=base.offset(i==0?0:256,0,0);
    var bosses=w.getEntitiesOfClass(NetherSummonBoss.class,new net.minecraft.world.phys.AABB(a).inflate(16),e->e.isAlive());
    t.assertTrue(bosses.size()==1,"Rite did not produce exactly one boss "+i+" count="+bosses.size());
    t.assertTrue(p.getMainHandItem().isEmpty(),"Token not consumed exactly once");bosses.forEach(Entity::discard);
   }t.succeed();
  });
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void rewardClaimExactlyOnce(GameTestHelper t){
  var w=t.getLevel();var a=t.absolutePos(new BlockPos(40,1,40));platform(w,a);
  var boss=ModEntities.MOROVA_DEVA.get().create(w);boss.moveTo(a.getX()+.5,a.getY()+1,a.getZ()+.5,0,0);w.addFreshEntity(boss);
  String id=UUID.randomUUID().toString();boss.getPersistentData().putString("RiteEncounter",id);
  var recipient=FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BossRewardQA"));
  var row=new net.minecraft.nbt.CompoundTag();row.putUUID("Owner",recipient.getUUID());row.putUUID("Boss",boss.getUUID());row.putString("State","ACTIVE");NetherBossLedger.get(w).records.put(id,row);
  NetherBossRewards.grant(boss,w,boss.damageSources().generic());NetherBossRewards.grant(boss,w,boss.damageSources().generic());
  org.slavicmyths.losthalls.BossRewardInbox.reconcile(recipient);org.slavicmyths.losthalls.BossRewardInbox.reconcile(recipient);
  int souls=0;for(int slot=0;slot<36;slot++)if(recipient.getInventory().getItem(slot).is(NetherBossItems.item("legendary_soul")))souls+=recipient.getInventory().getItem(slot).getCount();
  t.assertTrue(souls==1,"Repeated reward callback duplicated/lost soul "+souls);boss.discard();t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=1200)
 public static void thirtyControlledCombatScenarios(GameTestHelper t){
  var w=t.getLevel();var a=t.absolutePos(new BlockPos(80,1,80));platform(w,a);t.runAtTickTime(150,()->{int contacts=0;
  for(int fight=0;fight<30;fight++){
   var boss=(fight%2==0?ModEntities.MOROVA_DEVA:ModEntities.TRIZNA_PRINCE).get().create(w);
   var target=net.minecraft.world.entity.EntityType.HUSK.create(w);target.setNoAi(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);
   boss.moveTo(a.getX(),a.getY()+1,a.getZ(),0,0);target.moveTo(a.getX()+3,a.getY()+1,a.getZ(),0,0);w.addFreshEntity(boss);w.addFreshEntity(target);boss.setTarget(target);
   var phases=new HashSet<Integer>();float lost=0;
   for(int tick=0;tick<390;tick++){
    if(tick==130)boss.setHealth(boss.getMaxHealth()*.6F);if(tick==260)boss.setHealth(boss.getMaxHealth()*.25F);
    target.setHealth(1000);target.tick();float before=target.getHealth();boss.tick();lost+=Math.max(0,before-target.getHealth());phases.add(boss.phase());
   }
   t.assertTrue(phases.size()==3,"Missing phases "+fight+" "+phases);
   t.assertTrue(lost>0,"Boss never made a real damage contact "+fight);contacts++;
   boss.hurt(boss.damageSources().mobAttack(target),10000);t.assertTrue(!boss.isAlive(),"Boss failed to die "+fight);boss.discard();target.discard();
  }
  System.out.println("NETHERBOSS_CONTROLLED_COMBAT scenarios="+contacts+"; manually advanced entity ticks/dummy opponent; NOT 30 player fights/MP");t.succeed();});
 }
}
