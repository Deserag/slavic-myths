package org.slavicmyths.entity;

import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

/** Shared persistence and bounded encounter plumbing, not shared spirit AI. */
public abstract class LandSpiritEntity extends CreatureEntity {
    private static final DataParameter<Integer> STATE = EntityDataManager.defineId(LandSpiritEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.defineId(LandSpiritEntity.class, DataSerializers.INT);
    public BlockPos home;
    protected long nextPower, angryUntil, friendUntil;
    protected UUID friend;
    protected int windup;
    protected LandSpiritEntity(EntityType<? extends LandSpiritEntity> type, World world) {
        super(type,world); entityData.set(VARIANT,random.nextInt(8));
    }
    @Override protected void defineSynchedData() { super.defineSynchedData();entityData.define(STATE,0);entityData.define(VARIANT,0); }
    public int state() { return entityData.get(STATE); }
    public int variant() { return entityData.get(VARIANT); }
    protected void state(int n) { entityData.set(STATE,n); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new SwimGoal(this));
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.1,false) {
            @Override public boolean canUse() { return meleeReady() && super.canUse(); }
            @Override public boolean canContinueToUse() { return meleeReady() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(4,new LookAtGoal(this,PlayerEntity.class,18));
        goalSelector.addGoal(5,new LookRandomlyGoal(this));
    }
    protected PlayerEntity nearby(double radius) {
        PlayerEntity p=level.getNearestPlayer(this,radius);
        return p!=null && !p.isCreative() && !p.isSpectator() && p.isAlive()?p:null;
    }
    public void provoke(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) return;
        friend=null;friendUntil=0;angryUntil=level.getGameTime()+600;setTarget(player);state(3);voice("angry",.7F);
    }
    protected boolean friendly(PlayerEntity p) { return friend!=null && friend.equals(p.getUUID()) && friendUntil>level.getGameTime(); }
    public boolean permits(PlayerEntity p) { return friendly(p); }
    protected void wander() {
        if (random.nextInt(4)==0) getNavigation().moveTo(getX()+random.nextInt(9)-4,getY(),getZ()+random.nextInt(9)-4,.6);
    }
    protected void particles(IParticleData particle,int count) {
        if(level instanceof ServerWorld)((ServerWorld)level).sendParticles(particle,getX(),getY()+1,getZ(),count,.6,.7,.6,.02);
    }
    public void voice(String kind,float volume) { SoundEvent s=sound(kind);if(s!=null)playSound(s,volume,.9F+random.nextFloat()*.2F); }
    protected SoundEvent sound(String kind) {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("slavicmyths",getType().getRegistryName().getPath()+"_"+kind));
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
        if(hit && !level.isClientSide && source.getEntity() instanceof PlayerEntity)provoke((PlayerEntity)source.getEntity());
        return hit;
    }
    @Override public void aiStep() {
        super.aiStep();if(level.isClientSide)return;
        if(getTarget()!=null && (!getTarget().isAlive() || level.getGameTime()>angryUntil || distanceToSqr(getTarget())>32*32)) {setTarget(null);state(0);}
        if(home!=null && distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>24*24) {setTarget(null);state(0);getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.9);return;}
        abilityTick();
        if((tickCount+getId())%20==0)think();
    }
    protected abstract void think();
    protected boolean meleeReady() { return windup==0; }
    protected void abilityTick() { }
    @Override public void addAdditionalSaveData(CompoundNBT n) {
        super.addAdditionalSaveData(n);n.putInt("SpiritState",state());n.putInt("VisualVariant",variant());n.putLong("NextPower",nextPower);
        n.putLong("AngryUntil",angryUntil);n.putLong("FriendUntil",friendUntil);if(friend!=null)n.putUUID("Friend",friend);if(home!=null)n.putLong("Home",home.asLong());
    }
    @Override public void readAdditionalSaveData(CompoundNBT n) {
        super.readAdditionalSaveData(n);state(n.getInt("SpiritState"));entityData.set(VARIANT,n.getInt("VisualVariant")&7);nextPower=n.getLong("NextPower");
        angryUntil=n.getLong("AngryUntil");friendUntil=n.getLong("FriendUntil");friend=n.hasUUID("Friend")?n.getUUID("Friend"):null;home=n.contains("Home")?BlockPos.of(n.getLong("Home")):null;
        windup=0;if(state()>=3)state(0);
    }
}
