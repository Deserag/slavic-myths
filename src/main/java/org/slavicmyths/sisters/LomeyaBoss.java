package org.slavicmyths.sisters;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureHealthBalance;

/** Version 2.5.1 boss. Separate real attack windows and phase-II two-handed warhammer. */
public final class LomeyaBoss extends Monster {
    public static final int IDLE=0, COMBO=1, GRAB=2, SLAM=3, GIANT_WINDUP=4, GIANT_STRIKE=5;
    private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(LomeyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(LomeyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO=SynchedEntityData.defineId(LomeyaBoss.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bossBar=new ServerBossEvent(Component.literal("Ломея"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),BossEvent.BossBarColor.RED),BossEvent.BossBarOverlay.NOTCHED_10);
    private final Set<UUID> fracturedThisAction=new HashSet<>();
    private int cooldown=35, cycle;
    private Vec3 lockedPosition=Vec3.ZERO;

    public LomeyaBoss(EntityType<? extends LomeyaBoss> type,Level level) {
        super(type,level);
        setPersistenceRequired();
        setCustomName(Component.literal("Ломея"));
        setCustomNameVisible(false);
        xpReward=0;
        setHealth(getMaxHealth());
    }
    @Override public float getMaxHealth() {
        return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,2000)
            .add(Attributes.ATTACK_DAMAGE,26).add(Attributes.MOVEMENT_SPEED,.245)
            .add(Attributes.ARMOR,14).add(Attributes.KNOCKBACK_RESISTANCE,.85)
            .add(Attributes.FOLLOW_RANGE,40);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(MOVE,IDLE);b.define(FRAME,0);b.define(PHASE_TWO,false);
    }
    public int action() { return entityData.get(MOVE); }
    public int actionFrame() { return entityData.get(FRAME); }
    public boolean phaseTwo() { return entityData.get(PHASE_TWO); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bossBar.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bossBar.removePlayer(p); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new HuntGoal());
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,24));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean canHit(Player p) { return p.isAlive() && !p.isCreative() && !p.isSpectator(); }
    private void begin(int action,Player target) {
        fracturedThisAction.clear();
        Vec3 forward=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        lockedPosition=position().add(forward.scale(3.2));
        entityData.set(MOVE,action);entityData.set(FRAME,0);
        getNavigation().stop();
    }
    private int duration(int action) {
        return switch(action) {case COMBO -> 46;case GRAB -> 38;case SLAM -> 46;case GIANT_WINDUP -> 40;case GIANT_STRIKE -> 58;default -> 0;};
    }
    private void damage(Player p,float amount,boolean fracture) {
        if (!canHit(p) || !getSensing().hasLineOfSight(p) || !p.hurt(damageSources().mobAttack(this),amount)) return;
        if (fracture && fracturedThisAction.add(p.getUUID())) FractureHooks.apply(p);
    }
    private boolean forward(Player p,double reach) {
        Vec3 delta=p.position().subtract(position()).multiply(1,0,1);
        Vec3 facing=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        return delta.lengthSqr()<=reach*reach && delta.lengthSqr()>.0001 && delta.normalize().dot(facing)>.22;
    }
    private void sweep(ServerLevel world,float amount,double radius) {
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius,2.5,radius),this::canHit))
            if (forward(p,radius)) damage(p,amount,true);
    }
    private void area(ServerLevel world,float amount,double radius) {
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius+3,3,radius+3),this::canHit))
            if (p.position().multiply(1,0,1).distanceToSqr(lockedPosition.multiply(1,0,1))<=radius*radius)
                damage(p,amount,true);
    }
    private void updateMove(ServerLevel world,Player target) {
        int frame=actionFrame()+1,action=action();
        entityData.set(FRAME,frame);
        if (frame%5==0 && frame<duration(action)-8) {
            world.sendParticles(ParticleTypes.CRIT,getX(),getY()+2.6,getZ(),4,.45,.25,.45,.02);
            if (action==SLAM || action==GIANT_STRIKE)
                world.sendParticles(ParticleTypes.CLOUD,lockedPosition.x,lockedPosition.y+.15,lockedPosition.z,8,1.5,.05,1.5,.01);
        }
        switch(action) {
            case COMBO -> {
                // Four timed hits, but only one Fracture increase per combo.
                if(frame==12 || frame==21 || frame==29 || frame==37) sweep(world,16.9F,3.8);
            }
            case GRAB -> {
                if(frame==22 && canHit(target) && forward(target,3.6)) {
                    Vec3 pull=position().subtract(target.position()).multiply(1,0,1).normalize().scale(.65);
                    target.push(pull.x,.18,pull.z);
                    damage(target,18.2F,true);
                }
            }
            case SLAM -> { if(frame==27) area(world,18.2F,3.5); }
            case GIANT_STRIKE -> { if(frame==36) { area(world,35.1F,5); world.sendParticles(ParticleTypes.POOF,lockedPosition.x,lockedPosition.y+.2,lockedPosition.z,30,2,.2,2,.15); } }
            default -> {}
        }
        if(frame>=duration(action)) {
            entityData.set(MOVE,IDLE);entityData.set(FRAME,0);
            cooldown=phaseTwo()?27:22;
        }
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel world) || !isAlive()) return;
        bossBar.setProgress(Math.clamp(getHealth()/Math.max(1,getMaxHealth()),0,1));
        if (!phaseTwo() && getHealth()<=getMaxHealth()*.5F) {
            entityData.set(PHASE_TWO,true);
            entityData.set(MOVE,IDLE);entityData.set(FRAME,0);
            cooldown=45;
            fracturedThisAction.clear();
            playSound(SoundEvents.IRON_GOLEM_REPAIR,1.5F,.55F);
        }
        Player target=getTarget() instanceof Player p && canHit(p)?p:null;
        if (target==null) { if(action()!=IDLE) {entityData.set(MOVE,IDLE);entityData.set(FRAME,0);} return; }
        getLookControl().setLookAt(target,25,25);
        if(action()!=IDLE) {getNavigation().stop();updateMove(world,target);return;}
        if(cooldown>0) {cooldown--;return;}
        if(distanceToSqr(target)>50) {getNavigation().moveTo(target,phaseTwo()?1.02:1.1);return;}
        int choice=cycle++%3;
        begin(phaseTwo()?(choice==2?GRAB:GIANT_STRIKE):(choice==0?COMBO:choice==1?GRAB:SLAM),target);
    }
    private final class HuntGoal extends Goal {
        HuntGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse() {return getTarget()!=null && getTarget().isAlive();}
        @Override public boolean canContinueToUse() {return canUse();}
        @Override public boolean requiresUpdateEveryTick() {return true;}
        @Override public void tick() {
            LivingEntity target=getTarget();
            if(target!=null && action()==IDLE && distanceToSqr(target)>25)
                getNavigation().moveTo(target,1.0);
        }
    }
}
