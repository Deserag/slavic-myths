package org.slavicmyths.mor;

import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** Collision handled by vanilla projectile raycasts, with one server impact and no fire blocks. */
public final class MorBolt extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> THREAD=SynchedEntityData.defineId(MorBolt.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROOT=SynchedEntityData.defineId(MorBolt.class,EntityDataSerializers.BOOLEAN);
    private float damage=3;
    public MorBolt(EntityType<? extends MorBolt> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(THREAD,false);b.define(ROOT,false);}
    public boolean root(){return entityData.get(ROOT);}
    public void configureRoot(Mob owner){setOwner(owner);entityData.set(ROOT,true);damage=0;setNoGravity(true);}
    public boolean thread(){return entityData.get(THREAD);}
    public void configure(Mob owner,boolean thread,float damage){setOwner(owner);entityData.set(THREAD,thread);this.damage=damage;}
    @Override protected double getDefaultGravity(){return 0;}
    @Override protected boolean canHitEntity(Entity e){return e instanceof LivingEntity l&&MorController.valid(l)&&(getOwner()==null||!getOwner().isAlliedTo(e))&&super.canHitEntity(e);}
    @Override protected void onHitEntity(EntityHitResult hit){if(!level().isClientSide&&hit.getEntity() instanceof LivingEntity e&&MorController.valid(e)&&getOwner() instanceof LivingEntity owner){if(e.getLastHurtByMob()==owner)e.invulnerableTime=Math.min(e.invulnerableTime,10);if(e.hurt(damageSources().mobProjectile(this,owner),damage)&&thread())MorController.hold(e,20);}}
    @Override protected void onHit(HitResult hit){if(thread()&&hit instanceof BlockHitResult block&&level().getBlockState(block.getBlockPos()).is(net.minecraft.tags.BlockTags.LEAVES))return;super.onHit(hit);if(!level().isClientSide)discard();}
    @Override public void tick(){if(root()){tickCount++;if(!level().isClientSide&&tickCount>=10)discard();return;}super.tick();if(level().isClientSide){level().addParticle(new DustParticleOptions(new org.joml.Vector3f(.9F,.12F,.18F),thread()?.5F:1.2F),getX(),getY(),getZ(),0,0,0);}else if(tickCount>=(thread()?14:24))discard();}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("Thread",thread());n.putBoolean("Root",root());n.putFloat("Damage",damage);n.putInt("Age",tickCount);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);entityData.set(THREAD,n.getBoolean("Thread"));entityData.set(ROOT,n.getBoolean("Root"));damage=n.getFloat("Damage");tickCount=n.getInt("Age");}
}
