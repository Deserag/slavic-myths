package org.slavicmyths.combat;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;

/** Only hostile alternatives are eligible; villagers, pets and players are protected. */
public final class DisorientationEffect extends MobEffect {
    public DisorientationEffect(){super(MobEffectCategory.HARMFUL,0x602b72);}
    @Override public void onEffectStarted(LivingEntity e,int amplifier){
        if(e instanceof Mob mob&&!e.level().isClientSide&&mob.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof StunGoal))
            mob.goalSelector.addGoal(-1,new StunGoal(mob));
    }
    private static final class StunGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final Mob mob;
        StunGoal(Mob mob){this.mob=mob;setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
        @Override public boolean canUse(){return stunned(mob);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){mob.getNavigation().stop();mob.setDeltaMovement(0,Math.min(0,mob.getDeltaMovement().y),0);}
    }
    public static boolean stunned(LivingEntity e){return e.hasEffect(org.slavicmyths.registry.ModEffects.DISORIENTATION)&&RareCombat.now(e)<e.getPersistentData().getLong("RareStunUntil");}
    public static boolean allowed(Mob mob,LivingEntity target){return target!=null&&target!=mob&&target.isAlive()&&target instanceof Enemy&&!mob.isAlliedTo(target)
        &&(!mob.getPersistentData().hasUUID("RareMorokOwner")||!target.getUUID().equals(mob.getPersistentData().getUUID("RareMorokOwner")));}
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
    @Override public boolean applyEffectTick(LivingEntity e,int amplifier){
        if(e.level().isClientSide||!(e instanceof Mob mob))return true;
        if(stunned(e)){mob.setTarget(null);mob.getNavigation().stop();e.setDeltaMovement(0,Math.min(0,e.getDeltaMovement().y),0);return true;}
        if(!allowed(mob,mob.getTarget()))mob.setTarget(null);
        if(mob.getTarget()==null&&e.tickCount%20==0){
            LivingEntity nearest=null;double best=36;
            for(var candidate:e.level().getEntitiesOfClass(LivingEntity.class,e.getBoundingBox().inflate(6),x->allowed(mob,x))){
                double distance=e.distanceToSqr(candidate);if(distance<best&&mob.getSensing().hasLineOfSight(candidate)){nearest=candidate;best=distance;}
            }mob.setTarget(nearest);
        }return true;
    }
}
