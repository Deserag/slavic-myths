package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class AntQueenGameTests {
 private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> type,Vec3 p){
  T mob=type.create(h.getLevel());if(mob==null)throw new IllegalStateException("Entity creation failed");
  mob.moveTo(p);mob.setNoAi(true);mob.setNoGravity(true);mob.setPersistenceRequired();
  h.assertTrue(h.getLevel().addFreshEntity(mob),"Entity spawn failed");return mob;
 }
 private static Vec3 origin(GameTestHelper h){return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));}
 @GameTest(template="empty")
 public static void antQueenRegistriesAndAttributes(GameTestHelper h){
  var q=AntContent.QUEEN.get().create(h.getLevel());var a=AntContent.WORKER.get().create(h.getLevel());var f=AntContent.FLYING.get().create(h.getLevel());
  if(q==null||a==null||f==null)throw new IllegalStateException("Ant registration failed");
  h.assertTrue(q.getMaxHealth()==650&&a.getMaxHealth()==10&&f.getMaxHealth()==8,"wrong health");
  h.assertTrue(q.getParts().length==10,"wrong multipart count");
  h.assertTrue(q.pose(0).size()==546&&a.pose(0).size()==142&&f.pose(0).size()==190,"wrong geometry count");
  q.discard();a.discard();f.discard();h.succeed();
 }
 @GameTest(template="empty")
 public static void antQueenRetaliatesAgainstProjectileOwner(GameTestHelper h){
  var pos=origin(h);var q=mob(h,AntContent.QUEEN.get(),pos);var skeleton=mob(h,EntityType.SKELETON,pos.add(0,0,5));
  var arrow=EntityType.ARROW.create(h.getLevel());if(arrow==null)throw new IllegalStateException("Arrow creation failed");
  h.assertTrue(q.getParts()[0].hurt(h.getLevel().damageSources().arrow(arrow,skeleton),20),"part damage rejected");
  h.assertTrue(q.getTarget()==skeleton,"arrow owner not acquired");
  arrow.discard();q.discard();skeleton.discard();h.succeed();
 }
 @GameTest(template="empty")
 public static void antQueenKeepsTargetDuringAttack(GameTestHelper h){
  var pos=origin(h);var q=mob(h,AntContent.QUEEN.get(),pos);var a=mob(h,EntityType.ZOMBIE,pos.add(0,0,4));var b=mob(h,EntityType.SKELETON,pos.add(4,0,0));
  h.assertTrue(q.hurt(h.getLevel().damageSources().mobAttack(a),20),"first hit rejected");
  h.assertTrue(q.getTarget()==a,"initial threat not acquired");
  q.action(AntRules.SUMMON);q.invulnerableTime=0;
  h.assertTrue(q.hurt(h.getLevel().damageSources().mobAttack(b),50),"second hit rejected");
  q.setTarget(b);h.assertTrue(q.getTarget()==a,"target changed during telegraph");
  q.discard();a.discard();b.discard();h.succeed();
 }
 @GameTest(template="empty")
 public static void antQueenDispersesColonyOnDeath(GameTestHelper h){
  var pos=origin(h);var q=mob(h,AntContent.QUEEN.get(),pos);var a=mob(h,AntContent.WORKER.get(),pos.add(3,0,0));var f=mob(h,AntContent.FLYING.get(),pos.add(-3,0,0));
  a.bind(q.getUUID(),0,null);f.bind(q.getUUID(),1,null);
  q.hurt(h.getLevel().damageSources().genericKill(),10000);
  h.assertTrue(a.dispersing()&&f.dispersing(),"colony did not disperse");
  a.discard();f.discard();q.discard();h.succeed();
 }
 private AntQueenGameTests(){}
}
