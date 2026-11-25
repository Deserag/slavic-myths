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
}
