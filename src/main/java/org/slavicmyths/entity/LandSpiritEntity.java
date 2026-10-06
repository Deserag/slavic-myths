package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;

import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;

/** Shared persistence and bounded encounter plumbing, not shared spirit AI. */
public abstract class LandSpiritEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(LandSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(LandSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_PHASE = SynchedEntityData.defineId(LandSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE_CLOCK = SynchedEntityData.defineId(LandSpiritEntity.class, EntityDataSerializers.INT);
    public BlockPos home;
    protected long nextPower, angryUntil, friendUntil;
    protected UUID friend;
    protected int windup;
    protected LandSpiritEntity(EntityType<? extends LandSpiritEntity> type, Level world) {
        super(type,world); entityData.set(VARIANT,random.nextInt(8));
    }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){ super.defineSynchedData(builder);builder.define(STATE,0);builder.define(VARIANT,0);builder.define(ATTACK_PHASE,0);builder.define(PHASE_CLOCK,0); }
    public AttackTimeline.Phase attackPhase(){return AttackTimeline.Phase.values()[entityData.get(ATTACK_PHASE)];}
    public float attackVisualTime(float age){float partial=net.minecraft.util.Mth.clamp(age-tickCount,0,1);return Math.max(0,(int)level().getGameTime()-entityData.get(PHASE_CLOCK)+partial);}
    protected AttackTimeline attackTimeline(){return null;}
    public int state() { return entityData.get(STATE); }
    public int variant() { return entityData.get(VARIANT); }
    protected void state(int n) { entityData.set(STATE,n); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.1,false) {
            @Override public boolean canUse() { return meleeReady() && super.canUse(); }
            @Override public boolean canContinueToUse() { return meleeReady() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(4,new LookAtPlayerGoal(this,Player.class,18));
        goalSelector.addGoal(5,new RandomLookAroundGoal(this));
    }
    protected Player nearby(double radius) {
        Player p=level().getNearestPlayer(this,radius);
        return p!=null && !p.isCreative() && !p.isSpectator() && p.isAlive()?p:null;
    }
    public void provoke(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        friend=null;friendUntil=0;angryUntil=level().getGameTime()+600;setTarget(player);state(3);voice("angry",.7F);
    }
    protected boolean friendly(Player p) { return friend!=null && friend.equals(p.getUUID()) && friendUntil>level().getGameTime(); }
    public boolean permits(Player p) { return friendly(p); }
    protected void wander() {
        if (random.nextInt(4)==0) getNavigation().moveTo(getX()+random.nextInt(9)-4,getY(),getZ()+random.nextInt(9)-4,.6);
    }
    protected void particles(ParticleOptions particle,int count) {
        if(level() instanceof ServerLevel)((ServerLevel)level()).sendParticles(particle,getX(),getY()+1,getZ(),count,.6,.7,.6,.02);
    }
    protected boolean attackable(LivingEntity target,double range){return target!=null&&target.isAlive()
        &&(!(target instanceof Player p)||!p.isCreative()&&!p.isSpectator())
        &&distanceToSqr(target)<=range*range&&hasLineOfSight(target);}
    protected void faceTarget(){if(getTarget()==null)return;var delta=getTarget().position().subtract(position());float yaw=(float)(Math.atan2(delta.z,delta.x)*180/Math.PI)-90;setYRot(yaw);setYHeadRot(yaw);getLookControl().setLookAt(getTarget(),30,30);}
    protected boolean strikeTarget(float damage,double range,float knockback){var target=getTarget();if(!attackable(target,range))return false;
        var direction=target.position().subtract(position()).multiply(1,0,1).normalize();if(direction.dot(getLookAngle().multiply(1,0,1).normalize())<.25)return false;
        boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(hit&&knockback>0)target.knockback(knockback,getX()-target.getX(),getZ()-target.getZ());return hit;}
    protected void chase(double speed){if(getTarget()!=null&&(tickCount+getId())%8==0)getNavigation().moveTo(getTarget(),speed);}
    public void voice(String kind,float volume) { SoundEvent s=sound(kind);if(s!=null)playSound(s,volume,.9F+random.nextFloat()*.2F); }
    protected SoundEvent sound(String kind) {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(getType()).getPath()+"_"+kind));
    }
    @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return sound("hurt");}
    @Override protected SoundEvent getDeathSound(){return sound("death");}
    @Override public int getAmbientSoundInterval(){return 360;}
    @Override public int getMaxSpawnClusterSize(){return 1;}
    @Override protected boolean shouldDespawnInPeaceful(){return true;}
    @Override public boolean removeWhenFarAway(double d){return home==null;}
    @Override public boolean hurt(DamageSource source,float amount) {
        boolean hit=super.hurt(source,amount);
        if(hit && !level().isClientSide && source.getEntity() instanceof Player)provoke((Player)source.getEntity());
        return hit;
    }
    @Override public void aiStep() {
        super.aiStep();if(level().isClientSide||!usesSpiritSchedule())return;
        if(getTarget()!=null && (!getTarget().isAlive() || level().getGameTime()>angryUntil || distanceToSqr(getTarget())>32*32)) {setTarget(null);state(0);cancelAttack();}
        if(home!=null && distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>24*24) {setTarget(null);state(0);cancelAttack();getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.9);return;}
        abilityTick();
        if((tickCount+getId())%20==0)think();
        syncAttackPhase();
    }
    protected abstract void think();
    private void syncAttackPhase(){var clock=attackTimeline();if(clock!=null&&entityData.get(ATTACK_PHASE)!=clock.phase().ordinal()){entityData.set(PHASE_CLOCK,(int)level().getGameTime()-clock.elapsed());entityData.set(ATTACK_PHASE,clock.phase().ordinal());}}
    private void cancelAttack(){var clock=attackTimeline();if(clock!=null)clock.cancel();syncAttackPhase();}
    protected boolean usesSpiritSchedule(){return true;}
    protected boolean meleeReady() { return windup==0; }
    protected void abilityTick() { }
    @Override public void addAdditionalSaveData(CompoundTag n) {
        super.addAdditionalSaveData(n);n.putInt("SpiritState",state());n.putInt("VisualVariant",variant());n.putLong("NextPower",nextPower);
        n.putLong("AngryUntil",angryUntil);n.putLong("FriendUntil",friendUntil);if(friend!=null)n.putUUID("Friend",friend);if(home!=null)n.putLong("Home",home.asLong());
    }
    @Override public void readAdditionalSaveData(CompoundTag n) {
        super.readAdditionalSaveData(n);state(n.getInt("SpiritState"));entityData.set(VARIANT,n.getInt("VisualVariant")&7);nextPower=n.getLong("NextPower");
        angryUntil=n.getLong("AngryUntil");friendUntil=n.getLong("FriendUntil");friend=n.hasUUID("Friend")?n.getUUID("Friend"):null;home=n.contains("Home")?BlockPos.of(n.getLong("Home")):null;
        windup=0;cancelAttack();if(state()>=3)state(0);
    }
}
