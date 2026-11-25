package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class HalaGameTests {
 private static <T extends Mob> T mob(GameTestHelper h,EntityType<T> type,Vec3 p){T m=type.create(h.getLevel());if(m==null)throw new IllegalStateException("spawn failed");m.moveTo(p);m.setNoAi(true);m.setNoGravity(true);m.setPersistenceRequired();h.assertTrue(h.getLevel().addFreshEntity(m),"spawn rejected");return m;}
 private static Vec3 origin(GameTestHelper h){return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));}
 @GameTest(template="empty") public static void halaV2Registration(GameTestHelper h){
  var q=HalaContent.BOSS.get().create(h.getLevel());h.assertTrue(q!=null,"Hala not registered");h.assertTrue(q.getMaxHealth()==800,"HP");h.assertTrue(q.getParts().length>=12,"multipart missing");h.assertTrue(q.pose(0).length==2781*24,"V2 geometry lost");q.discard();h.succeed();
 }
 @GameTest(template="empty") public static void halaProjectileRetaliationAndLock(GameTestHelper h){
  var pos=origin(h);var q=mob(h,HalaContent.BOSS.get(),pos);var a=mob(h,EntityType.SKELETON,pos.add(0,0,6));var b=mob(h,EntityType.ZOMBIE,pos.add(6,0,0));var arrow=EntityType.ARROW.create(h.getLevel());
  h.assertTrue(arrow!=null,"arrow");h.assertTrue(q.getParts()[0].hurt(h.getLevel().damageSources().arrow(arrow,a),20),"part damage");h.assertTrue(q.getTarget()==a,"retaliation before combat");
  q.action(HalaRules.BEAM);q.invulnerableTime=0;q.hurt(h.getLevel().damageSources().mobAttack(b),50);q.setTarget(b);h.assertTrue(q.getTarget()==a,"target lock failed");arrow.discard();q.discard();a.discard();b.discard();h.succeed();
 }
 @GameTest(template="empty") public static void halaBurnStages(GameTestHelper h){
  var e=mob(h,EntityType.ZOMBIE,origin(h));SolarBurnEffect.apply(e,20);h.assertTrue(SolarBurnEffect.stage(e)==1,"stage I");SolarBurnEffect.apply(e,30);h.assertTrue(SolarBurnEffect.stage(e)==2,"stage II");SolarBurnEffect.apply(e,40);h.assertTrue(SolarBurnEffect.stage(e)==3,"stage III");h.assertTrue(HalaRules.outgoing(3,.1F)==0&&HalaRules.outgoing(2,.1F)<1,"damage rule");h.assertTrue(HalaRules.cool(90,true)<HalaRules.cool(90,false),"water cooling");e.discard();h.succeed();
 }
}
