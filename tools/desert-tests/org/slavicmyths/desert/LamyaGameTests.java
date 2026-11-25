package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import static org.slavicmyths.desert.LamyaRules.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class LamyaGameTests {
 @GameTest(template="empty") public static void lamyaRegistration(GameTestHelper h){
  var q=LamyaContent.BOSS.get().create(h.getLevel());h.assertTrue(q!=null,"registered");h.assertTrue(q.getMaxHealth()==INTERNAL_HP,"health");h.assertTrue(q.getParts().length==20,"collision groups");h.assertTrue(LamyaSounds.get("ultimate")!=null,"sound");q.discard();h.succeed();
 }
 @GameTest(template="empty") public static void lamyaTimelines(GameTestHelper h){
  for(int a=FIRE;a<=ULTIMATE;a++){h.assertTrue(!active(a,0)&&active(a,release(a))&&!active(a,LENGTH[a]),"attack timing "+a);}
  h.assertTrue(mask(FIRE_WIND)==5&&mask(ULTIMATE)==7,"head combinations");h.assertTrue(phase(.71F)==1&&phase(.5F)==2&&phase(.34F)==3,"phase thresholds");
  var g=LamyaGeometry.INSTANCE;float[] p=g.sample(WALK,.5F);h.assertTrue(p[19*6+2]<-5,"straight tail");h.succeed();
 }
 @GameTest(template="empty") public static void lamyaTargetLock(GameTestHelper h){
  var w=h.getLevel();Vec3 p=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));var q=LamyaContent.BOSS.get().create(w);var a=EntityType.SKELETON.create(w);var b=EntityType.ZOMBIE.create(w);
  h.assertTrue(q!=null&&a!=null&&b!=null,"spawn");q.moveTo(p);a.moveTo(p.add(0,0,5));b.moveTo(p.add(5,0,0));q.setNoAi(true);a.setNoAi(true);b.setNoAi(true);w.addFreshEntity(q);w.addFreshEntity(a);w.addFreshEntity(b);
  h.assertTrue(q.hurt(w.damageSources().mobAttack(a),20),"damage");h.assertTrue(q.getTarget()==a,"retaliation");q.begin(FIRE,a);q.setTarget(b);h.assertTrue(q.getTarget()==a&&q.threatAttackLocked(),"target lock");q.discard();a.discard();b.discard();h.succeed();
 }

 @GameTest(template="empty") public static void lamyaDroughtHealing(GameTestHelper h){
  var w=h.getLevel();var q=EntityType.HUSK.create(w);h.assertTrue(q!=null,"husk");q.moveTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2))));q.setNoAi(true);w.addFreshEntity(q);
  q.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);q.setHealth(50);
  LamyaDroughtEffect.apply(q);LamyaDroughtEffect.apply(q);LamyaDroughtEffect.apply(q);
  h.assertTrue(LamyaDroughtEffect.stage(q)==3,"three drought stages");
  q.heal(10);h.assertTrue(Math.abs(q.getHealth()-54)<.1,"drought healing reduction");
  h.assertTrue(Math.abs(LamyaDroughtEffect.fireMultiplier(q)-1.45F)<.001,"fire vulnerability");q.discard();h.succeed();
 }
 @GameTest(template="empty",timeoutTicks=120) public static void lamyaNativeFire(GameTestHelper h){
  var w=h.getLevel();Vec3 p=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2))).add(0,24,0);
  var q=LamyaContent.BOSS.get().create(w);var t=EntityType.HUSK.create(w);h.assertTrue(q!=null&&t!=null,"spawn");
  q.moveTo(p);q.setNoGravity(true);t.moveTo(p.add(0,0,8));t.setNoAi(true);t.setNoGravity(true);
  w.addFreshEntity(q);w.addFreshEntity(t);
  t.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);t.setHealth(100);
  h.assertTrue(q.hurt(w.damageSources().mobAttack(t),20),"retaliation damage");h.assertTrue(q.getTarget()==t,"retaliation target");q.begin(FIRE,t);
  h.runAfterDelay(72,()->{h.assertTrue(t.getHealth()<100,"native fire must hit stationary hostile");q.discard();t.discard();h.succeed();});
 }
}
