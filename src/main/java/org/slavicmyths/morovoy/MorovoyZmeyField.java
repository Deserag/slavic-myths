package org.slavicmyths.morovoy;

import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** No fire blocks or permanent Mor world edits. Safe lifetime even after reload. */
public final class MorovoyZmeyField extends Entity {
 private static final EntityDataAccessor<Integer> ELEMENT=SynchedEntityData.defineId(MorovoyZmeyField.class,EntityDataSerializers.INT);
 private UUID owner;private int remaining=80;
 public MorovoyZmeyField(EntityType<? extends MorovoyZmeyField> type,Level level){super(type,level);noPhysics=true;}
 public int element(){return entityData.get(ELEMENT);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(ELEMENT,0);}
 public static void spawn(ServerLevel world,MorovoyFireZmey boss,int element,Vec3 at,int lifetime){MorovoyZmeyField field=MorovoyFireZmeyContent.FIELD.get().create(world);if(field==null)return;field.owner=boss.getUUID();field.remaining=Math.min(120,Math.max(20,lifetime));field.entityData.set(ELEMENT,element==0?0:1);field.setPos(at);world.addFreshEntity(field);}
 @Override public void tick(){super.tick();if(level().isClientSide){if(tickCount%2==0)for(int i=0;i<3;i++){double r=1.8*Math.sqrt(random.nextDouble()),a=random.nextDouble()*Math.PI*2;float f=random.nextFloat();Vector3f shade=element()==0?new Vector3f(.05f,.55f+f*.45f,.1f):f<.2f?new Vector3f(1f,1f,1f):f<.6f?new Vector3f(.65f,.12f,1f):new Vector3f(.2f,.02f,.45f);level().addParticle(new DustParticleOptions(shade,.7f+f*.5f),getX()+Math.cos(a)*r,getY()+random.nextFloat()*.65,getZ()+Math.sin(a)*r,0,.015,0);}return;}
  if(--remaining<=0){discard();return;}
  if(!(level() instanceof ServerLevel world)||owner==null||!(world.getEntity(owner) instanceof MorovoyFireZmey boss)||!boss.isAlive()||boss.state()>=MorovoyFireZmey.DYING){discard();return;}
  if(tickCount%10!=0)return;
  for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.2,1.5,2.2),e->boss.canDamage(e)&&e.distanceToSqr(position())<=5.0)){if(element()==0)victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45,0));else victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,0));if(boss.hasLineOfSight(victim))victim.hurt(world.damageSources().mobAttack(boss),element()==0?2:3);}
 }
 @Override protected void addAdditionalSaveData(CompoundTag tag){tag.putInt("Element",element());tag.putInt("Remaining",remaining);if(owner!=null)tag.putUUID("Owner",owner);}
 @Override protected void readAdditionalSaveData(CompoundTag tag){entityData.set(ELEMENT,tag.getInt("Element")==0?0:1);remaining=Math.clamp(tag.getInt("Remaining"),0,120);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;}
}
