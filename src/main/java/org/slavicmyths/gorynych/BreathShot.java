package org.slavicmyths.gorynych;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
public final class BreathShot extends Projectile {
 private static final EntityDataAccessor<Integer> TYPE=SynchedEntityData.defineId(BreathShot.class,EntityDataSerializers.INT);
 public BreathShot(EntityType<? extends BreathShot> t,Level w){super(t,w);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(TYPE,0);}
 public int type(){return entityData.get(TYPE);}
 public void configure(Gorynych owner,int type,Vec3 from,Vec3 aim){setOwner(owner);entityData.set(TYPE,type);setPos(from);setDeltaMovement(aim.subtract(from).normalize().scale(.65));}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();if(!(getOwner() instanceof Gorynych boss)||!boss.fighting()||tickCount>90||!w.hasChunkAt(blockPosition())){discard();return;}var from=position();var to=from.add(getDeltaMovement());var block=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));var entity=ProjectileUtil.getEntityHitResult(level(),this,from,to,getBoundingBox().expandTowards(getDeltaMovement()).inflate(.6),e->e instanceof LivingEntity l&&boss.candidate(l));HitResult hit=entity!=null&&(block.getType()==HitResult.Type.MISS||from.distanceToSqr(entity.getLocation())<from.distanceToSqr(block.getLocation()))?entity:block;if(hit.getType()!=HitResult.Type.MISS){var at=hit.getLocation();for(var t:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.subtract(2,2,2),at.add(2,2,2)),boss::candidate))if(boss.hasLineOfSight(t)){t.hurt(org.slavicmyths.combat.MythDamageSources.caused(type()==0?"gorynych_mor":"gorynych_fire",boss),type()==0?6:8);if(type()==1)t.igniteForSeconds(3);}boss.cloud(type(),at,3);boss.sound(type()==0?"mor_cloud":"head_fireball");discard();return;}setPos(to);w.sendParticles(BreathCloud.particle(type()),getX(),getY(),getZ(),5,.12,.12,.12,0);}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
}
