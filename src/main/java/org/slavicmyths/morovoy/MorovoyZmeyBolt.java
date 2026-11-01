package org.slavicmyths.morovoy;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

/** Genuine collision-tested projectile with a bounded temporary impact field. */
public final class MorovoyZmeyBolt extends ThrowableProjectile {
 private static final EntityDataAccessor<Integer> ELEMENT=SynchedEntityData.defineId(MorovoyZmeyBolt.class,EntityDataSerializers.INT);
 public MorovoyZmeyBolt(EntityType<? extends MorovoyZmeyBolt> type,Level level){super(type,level);}
 public void configure(MorovoyFireZmey source,int element){setOwner(source);entityData.set(ELEMENT,element==0?0:1);setNoGravity(true);}
 public int element(){return entityData.get(ELEMENT);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ELEMENT,0);}
 @Override protected double getDefaultGravity(){return 0;}
 @Override protected boolean canHitEntity(Entity target){return target instanceof LivingEntity l&&l.isAlive()&&!(target instanceof MorovoyFireZmey)&&getOwner()!=target&&super.canHitEntity(target);}
 @Override protected void onHitEntity(EntityHitResult hit){if(!level().isClientSide&&hit.getEntity() instanceof LivingEntity target&&getOwner() instanceof MorovoyFireZmey boss&&boss.canDamage(target)){if(target.hurt(damageSources().mobProjectile(this,boss),element()==0?8:10)&&element()==0)target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));}}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide){if(getOwner() instanceof MorovoyFireZmey boss&&level() instanceof net.minecraft.server.level.ServerLevel server)MorovoyZmeyField.spawn(server,boss,element(),position(),80);discard();}}
 @Override public void tick(){super.tick();if(tickCount>90&&!level().isClientSide)discard();if(level().isClientSide){Vector3f color=element()==0?new Vector3f(.14f,.84f,.18f):new Vector3f(.65f,.08f,.98f);level().addParticle(new DustParticleOptions(color,1.3f),getX(),getY(),getZ(),0,0,0);if(element()==1&&tickCount%2==0)level().addParticle(new DustParticleOptions(new Vector3f(1f,1f,1f),.55f),getX(),getY(),getZ(),0,0,0);}}
 @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Element",element());}
 @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(ELEMENT,tag.getInt("Element")==0?0:1);}
}
