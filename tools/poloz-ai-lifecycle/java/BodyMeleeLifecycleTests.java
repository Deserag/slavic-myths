package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.poloz.*;

/** Native installed goal and native GoalSelector fast path, not a replacement AI implementation. */
@GameTestHolder("slavicmyths_poloz") @PrefixGameTestTemplate(false)
public final class BodyMeleeLifecycleTests {
    static final class ObservedZombie extends Zombie {
        int attacks;
        ObservedZombie(ServerLevel level){super(EntityType.ZOMBIE,level);}
        @Override public boolean doHurtTarget(Entity target){attacks++;return super.doHurtTarget(target);}
    }
    static GreatPoloz boss(GameTestHelper test) {
        var boss=PolozContent.BOSS.get().create(test.getLevel());
        boss.moveTo(test.absoluteVec(new Vec3(8,4,8)));boss.initializeBody();
        // Intentionally not added: target validity does not require an active world encounter.
        return boss;
    }
    static Goal isolate(PathfinderMob actor,GreatPoloz boss,GameTestHelper test) {
        actor.moveTo(boss.position().add(1,0,0));actor.tickCount=100;
        test.getLevel().addFreshEntity(actor); // Production EntityJoinLevelEvent installs BodyMelee.
        var goal=actor.goalSelector.getAvailableGoals().stream().map(w->w.getGoal())
                .filter(g->g.getClass().getName().equals("org.slavicmyths.poloz.PolozEvents$BodyMelee"))
                .findFirst().orElseThrow(()->new AssertionError("Production BodyMelee was not installed"));
        actor.goalSelector.removeAllGoals(g->g!=goal);
        actor.targetSelector.removeAllGoals(g->true);
        actor.setTarget(boss);actor.goalSelector.tick();
        test.assertTrue(actor.goalSelector.getAvailableGoals().stream().anyMatch(w->w.isRunning()),"Native selector started BodyMelee");
        return goal;
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void nativeFastTickTargetTransitions(GameTestHelper test) {
        var failures=new ArrayList<String>();
        for(String kind:List.of("heavy_preemption","null_zombie","null_heavy_bandit","replacement","dead_boss")) {
            var boss=boss(test);
            PathfinderMob actor=kind.equals("null_heavy_bandit")||kind.equals("heavy_preemption")
                    ?(PathfinderMob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("slavicmyths:bandit_heavy")).create(test.getLevel())
                    :new ObservedZombie(test.getLevel());
            try {
                if(kind.equals("heavy_preemption")) {
                    actor.moveTo(boss.position().add(1,0,0));actor.tickCount=100;
                    test.getLevel().addFreshEntity(actor);
                    var previous=new Zombie(EntityType.ZOMBIE,test.getLevel());previous.moveTo(actor.position().add(4,0,0));
                    actor.setTarget(previous);actor.goalSelector.tick();
                    test.assertTrue(actor.goalSelector.getAvailableGoals().stream().anyMatch(w->w.isRunning()&&w.getGoal().getClass().getName().endsWith("BanditEntity$CombatGoal")),"Native bandit combat started");
                    actor.setTarget(boss);
                    // Native preemption calls CombatGoal.stop(), which clears the target after canUse.
                    actor.goalSelector.tick();
                    test.assertTrue(actor.getTarget()==null,"Original CombatGoal.stop target clearing preserved");
                    actor.goalSelector.tick();
                    test.assertTrue(actor.goalSelector.getAvailableGoals().stream().noneMatch(w->w.isRunning()&&w.getGoal().getClass().getName().endsWith("PolozEvents$BodyMelee")),"Stale melee goal cleaned up");
                    System.out.println("POLOZ_AI_CASE "+kind+" PASS");continue;
                }
                isolate(actor,boss,test);
                int before=actor instanceof ObservedZombie z?z.attacks:0;
                if(kind.startsWith("null"))actor.setTarget(null);
                else if(kind.equals("replacement"))actor.setTarget(new Zombie(EntityType.ZOMBIE,test.getLevel()));
                else boss.setHealth(0);
                var current=actor.getTarget();actor.tickCount=120;
                // Minecraft calls this path without canContinueToUse revalidation.
                actor.goalSelector.tickRunningGoals(true);
                test.assertTrue(actor.getTarget()==current,"Invalid target was not replaced by the melee goal");
                if(actor instanceof ObservedZombie z)test.assertTrue(z.attacks==before,"No attack after target became invalid: "+kind);
                actor.goalSelector.tick();
                test.assertTrue(actor.goalSelector.getAvailableGoals().stream().noneMatch(w->w.isRunning()),"Next selector cleanup stopped invalid goal");
                System.out.println("POLOZ_AI_CASE "+kind+" PASS");
            } catch(Throwable failure) {
                failures.add(kind+":"+failure.getClass().getSimpleName()+":"+failure.getMessage());
                System.out.println("POLOZ_AI_CASE "+kind+" FAIL "+failure);
            } finally {actor.discard();boss.discard();}
        }
        var boss=boss(test);var actor=new ObservedZombie(test.getLevel());
        try {
            isolate(actor,boss,test);
            test.assertTrue(actor.attacks==1,"Live valid target: original first melee attack");
            actor.tickCount=119;actor.goalSelector.tickRunningGoals(true);
            test.assertTrue(actor.attacks==1,"Original20tick cooldown preserved");
            actor.tickCount=120;actor.goalSelector.tickRunningGoals(true);
            test.assertTrue(actor.attacks==2&&actor.getTarget()==boss,"Live target: original next attack preserved");
            System.out.println("POLOZ_AI_CASE valid_target_cooldown PASS");
        } catch(Throwable failure){failures.add("valid_target_cooldown:"+failure);}
        finally {actor.discard();boss.discard();}
        test.assertTrue(failures.isEmpty(),String.join(" | ",failures));test.succeed();
    }
}
