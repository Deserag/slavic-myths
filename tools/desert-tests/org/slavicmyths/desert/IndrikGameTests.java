package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class IndrikGameTests {
 @GameTest(template="empty") public static void indrikRegistration(GameTestHelper h){var q=IndrikContent.BOSS.get().create(h.getLevel());h.assertTrue(q!=null,"Indrik not registered");h.assertTrue(q.getMaxHealth()==IndrikRules.INTERNAL_HP,"internal HP");h.assertTrue(IndrikRules.logicalHealth(q.getMaxHealth(),q.getMaxHealth())==1500,"logical HP");h.assertTrue(q.getParts().length==23,"multipart");h.assertTrue(IndrikGeometry.INSTANCE.count==3669,"geometry");q.discard();h.succeed();}
 @GameTest(template="empty") public static void indrikRetaliationLock(GameTestHelper h){var w=h.getLevel();Vec3 p=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,2,2)));var q=IndrikContent.BOSS.get().create(w);var a=EntityType.SKELETON.create(w);var b=EntityType.ZOMBIE.create(w);h.assertTrue(q!=null&&a!=null&&b!=null,"spawn");q.moveTo(p);a.moveTo(p.add(0,0,6));b.moveTo(p.add(6,0,0));q.setNoAi(true);a.setNoAi(true);b.setNoAi(true);w.addFreshEntity(q);w.addFreshEntity(a);w.addFreshEntity(b);h.assertTrue(q.hurt(w.damageSources().mobAttack(a),20),"damage");h.assertTrue(q.getTarget()==a,"retaliation");q.begin(IndrikRules.CHARGE,a);q.setTarget(b);h.assertTrue(q.getTarget()==a&&q.threatAttackLocked(),"charge target lock");q.discard();a.discard();b.discard();h.succeed();}
}
