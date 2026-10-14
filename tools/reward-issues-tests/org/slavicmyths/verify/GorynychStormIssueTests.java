package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.gorynych.*;

@GameTestHolder("slavicmyths_rewards") @PrefixGameTestTemplate(false)
public final class GorynychStormIssueTests {
    private static net.minecraft.world.entity.monster.Zombie mob(GameTestHelper t,Vec3 at){
        var mob=EntityType.ZOMBIE.create(t.getLevel());mob.moveTo(t.absoluteVec(at));mob.setNoAi(true);mob.setNoGravity(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.setHealth(100);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
        mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);t.getLevel().addFreshEntity(mob);return mob;
    }
    @GameTest(template="empty") public static void oneAndThreeTargets(GameTestHelper t){
        var owner=mob(t,new Vec3(1,2,1));var at=t.absoluteVec(new Vec3(1,2,6));
        var targets=List.of(mob(t,new Vec3(1,2,6)),mob(t,new Vec3(2,2,6)),mob(t,new Vec3(1,2,7)));
        StormCrossbow.burst(owner,at);StormCrossbow.burst(owner,at);
        for(var target:targets){t.assertTrue(target.getHealth()==97,"storm deals 3 once, never repeats immediate volley AoE");var slow=target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);t.assertTrue(slow!=null&&slow.getDuration()==40&&slow.getAmplifier()==1,"40 ticks Slowness II, no stun");}
        System.out.println("STORM_AFTER targets3 HP100->97 each, repeated volley burst0, slowII40");t.succeed();
    }
    @GameTest(template="empty") public static void wallBlocksStorm(GameTestHelper t){
        var owner=mob(t,new Vec3(1,2,1));var target=mob(t,new Vec3(1,2,7));
        for(int x=0;x<=2;x++)for(int y=1;y<=5;y++)t.setBlock(x,y,6,Blocks.STONE);
        StormCrossbow.burst(owner,t.absoluteVec(new Vec3(1,2,5)));
        t.assertTrue(target.getHealth()==100&&!target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"solid wall blocks damage and control");t.succeed();
    }
    private static final class HitArrow extends Arrow {
        HitArrow(GameTestHelper t,LivingEntity owner){super(t.getLevel(),owner,new ItemStack(Items.ARROW),new ItemStack(GorynychContent.item("three_breath_crossbow")));setBaseDamage(2);setDeltaMovement(0,0,3);setCritArrow(false);getPersistentData().putInt("GorynychSpecial",3);}
        void hit(LivingEntity target){onHitEntity(new EntityHitResult(target));}
    }
    @GameTest(template="empty") public static void stormAfterNativeArrow(GameTestHelper t){
        var owner=mob(t,new Vec3(1,2,1));var target=mob(t,new Vec3(1,2,6));var arrow=new HitArrow(t,owner);arrow.hit(target);
        t.assertTrue(target.getHealth()==83,"14 arrow +3 storm, native i-frames must not swallow extra");
        t.assertTrue(arrow.getPersistentData().getInt("GorynychSpecial")==0,"special consumed once");
        StormCrossbow.afterHit(arrow,target.position());t.assertTrue(target.getHealth()==83,"special not processed twice");
        System.out.println("STORM_ARROW native14+storm3 HP100->83, no duplicate");t.succeed();
    }
}
