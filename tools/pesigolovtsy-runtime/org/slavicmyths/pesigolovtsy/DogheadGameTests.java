package org.slavicmyths.pesigolovtsy;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.pesigolovtsy.DogheadRules.*;
@GameTestHolder("slavicmyths_pesigolovtsy") @PrefixGameTestTemplate(false)
public final class DogheadGameTests {
 static DogheadEntity actor(GameTestHelper t,Role role){
  t.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
  var chunk=new net.minecraft.world.level.ChunkPos(t.absolutePos(new BlockPos(4,1,4)));
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)t.getLevel().setChunkForced(chunk.x+x,chunk.z+z,true);
  for(int x=0;x<10;x++)for(int z=0;z<10;z++)t.setBlock(new BlockPos(x,0,z),Blocks.STONE);
  var m=t.spawn(DogheadContent.ENTITY.get(),4,1,4);m.role(role,true);m.setPersistenceRequired();
  m.goalSelector.removeAllGoals(g->true);m.targetSelector.removeAllGoals(g->true);return m;
 }
 static void begin(DogheadEntity m,Clip c){try{var f=DogheadEntity.class.getDeclaredMethod("begin",Clip.class);f.setAccessible(true);f.invoke(m,c);}catch(Exception e){throw new RuntimeException(e);}}
 @GameTest(template="empty",timeoutTicks=100) public static void roleAttributes(GameTestHelper t){
  for(var r:Role.values()){var m=actor(t,r);t.assertTrue(m.getHealth()==r.hp,"role HP "+r);t.assertTrue(Math.abs(m.getBbHeight()-r.height)<.001,"role height "+r);m.discard();}t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100) public static void acceptedDamageInterrupt(GameTestHelper t){
  var m=actor(t,Role.LEADER);var victim=t.spawnWithNoFreeWill(EntityType.COW,4,1,6);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);victim.setHealth(100);m.setTarget(victim);begin(m,Clip.CHOP);m.hurt(m.damageSources().generic(),3);
  t.assertTrue(m.clip()==Clip.CHOP&&m.getHealth()==47,"3 actual HP must preserve windup");
  m.invulnerableTime=0;m.hurt(m.damageSources().generic(),4);
  t.assertTrue(m.clip()==Clip.LEADER_HURT&&m.getHealth()==43,"4 actual HP must interrupt early windup");
  begin(m,Clip.CHOP);
  t.runAtTickTime(18,()->{m.invulnerableTime=0;m.hurt(m.damageSources().generic(),4);t.assertTrue(m.clip()==Clip.CHOP,"committed chop must survive ordinary 4 HP hit: "+m.clip()+" age="+m.clipAge(0)+" alive="+m.isAlive()+" removed="+m.isRemoved()+" ticks="+m.tickCount);t.succeed();});
 }
 @GameTest(template="empty",timeoutTicks=150) public static void autonomousHunterHit(GameTestHelper t){
  var m=actor(t,Role.HUNTER);var pig=t.spawnWithNoFreeWill(EntityType.COW,4,1,6);
  pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);pig.setHealth(100);m.setYRot(0);m.yBodyRot=0;m.setTarget(pig);
  t.runAtTickTime(9,()->t.assertTrue(pig.getHealth()==100,"no immediate invisible damage"));
  t.succeedWhen(()->t.assertTrue(pig.getHealth()<=94,"autonomous hunter must actually hit live target: clip="+m.clip()+" ticks="+m.tickCount+" target="+m.getTarget()+" hp="+pig.getHealth()+" distance="+m.distanceTo(pig)));
 }
 @GameTest(template="empty",timeoutTicks=150) public static void lowPreyThrust(GameTestHelper t){
  var m=actor(t,Role.HUNTER);var p=t.spawnWithNoFreeWill(EntityType.PIG,4,1,6);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);m.setYRot(0);m.setTarget(p);
  t.succeedWhen(()->t.assertTrue(p.getHealth()<=94,"aimed spear must hit low prey"));
 }
 @GameTest(template="empty",timeoutTicks=160) public static void actualScoutProjectile(GameTestHelper t){
  var m=actor(t,Role.SCOUT);var p=t.spawnWithNoFreeWill(EntityType.COW,4,1,12);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);m.setYRot(0);m.setTarget(p);
  t.succeedWhen(()->t.assertTrue(p.getHealth()<100,"real arrow must eventually damage target"));
 }
 @GameTest(template="empty",timeoutTicks=100) public static void packAlertAndRemoval(GameTestHelper t){
  var m=actor(t,Role.HUNTER);var mate=DogheadContent.ENTITY.get().create(t.getLevel());mate.pack=m.pack;mate.moveTo(m.position().add(3,0,0));t.getLevel().addFreshEntity(mate);
  var stranger=actor(t,Role.LEADER);var p=t.spawnWithNoFreeWill(EntityType.COW,4,1,7);
  t.assertTrue(DogheadPacks.alert(m,p,false)==1&&mate.getTarget()==p&&stranger.getTarget()==null,"alert must stay in original pack");
  mate.discard();t.assertTrue(DogheadPacks.members(m).size()==1,"removed member must leave loaded index");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=100) public static void runeEquipmentExclusion(GameTestHelper t){
  var m=actor(t,Role.HUNTER);var p=t.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
  var a=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET);var b=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
  org.slavicmyths.rpg.Runes.write(a,1,java.util.List.of("ominous_aura"));org.slavicmyths.rpg.Runes.write(b,1,java.util.List.of("ominous_aura"));
  p.getInventory().add(a.copy());p.getInventory().add(b.copy());t.assertTrue(m.valid(p),"inventory runes must not provide immunity");
  p.setItemSlot(EquipmentSlot.HEAD,a);p.setItemSlot(EquipmentSlot.CHEST,b);t.assertTrue(!m.valid(p),"two equipped runes must suppress aggression");t.succeed();
 }
}
