package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import static org.slavicmyths.desert.StormIdolRules.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class StormIdolGameTests {
 @GameTest(template="empty") public static void stormIdolRegistration(GameTestHelper h){
  var q=StormIdolContent.BOSS.get().create(h.getLevel());h.assertTrue(q!=null,"registered");
  h.assertTrue(q.getMaxHealth()==INTERNAL_HP,"health");h.assertTrue(q.getParts().length==6,"body, mask and four fists");
  h.assertTrue(StormIdolSounds.get("launch")!=null,"launch sound");q.discard();h.succeed();
 }
 @GameTest(template="empty") public static void stormIdolFistTimeline(GameTestHelper h){
  Vec3 target=new Vec3(0,1,9);float time=10;
  h.assertTrue(fist(LAUNCH,0,25,time,target).distanceTo(target)<.3,"fist flies to aim");
  h.assertTrue(fist(LAUNCH,2,25,time,target).equals(rest(2,time)),"lower fist stays attached");
  h.assertTrue(fist(LAUNCH,0,72,time,target).equals(rest(0,time)),"fist returns");
  h.assertTrue(!active(LAUNCH,0,7)&&active(LAUNCH,0,16)&&!active(LAUNCH,0,50),"active window");
  h.assertTrue(phase(.71F)==1&&phase(.5F)==2&&phase(.34F)==3,"phases");h.succeed();
 }
 @GameTest(template="empty") public static void stormIdolRetaliationLock(GameTestHelper h){
  var w=h.getLevel();Vec3 p=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));var q=StormIdolContent.BOSS.get().create(w);var a=EntityType.SKELETON.create(w);var b=EntityType.ZOMBIE.create(w);
  h.assertTrue(q!=null&&a!=null&&b!=null,"spawn");q.moveTo(p);a.moveTo(p.add(0,0,6));b.moveTo(p.add(6,0,0));q.setNoAi(true);a.setNoAi(true);b.setNoAi(true);w.addFreshEntity(q);w.addFreshEntity(a);w.addFreshEntity(b);
  h.assertTrue(q.hurt(w.damageSources().mobAttack(a),20),"damage");h.assertTrue(q.getTarget()==a,"retaliation");
  q.begin(LAUNCH,a);q.setTarget(b);h.assertTrue(q.getTarget()==a&&q.threatAttackLocked(),"attack target lock");
  q.discard();a.discard();b.discard();h.succeed();
 }
}
