package org.slavicmyths.morozko;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
public final class FrostShard extends ThrowableProjectile {
 public UUID session;private float damage;private int cold;
 public FrostShard(EntityType<? extends FrostShard> t,net.minecraft.world.level.Level w){super(t,w);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
 @Override protected double getDefaultGravity(){return 0;}
 public static void launch(LivingEntity owner,Vec3 target,float damage,int cold,UUID session){var e=FrostContent.SHARD.get().create(owner.level());if(e==null)return;e.setOwner(owner);e.session=session;e.damage=damage;e.cold=cold;if(owner instanceof Morozko){double yaw=Math.toRadians(owner.getYRot());var side=new Vec3(Math.cos(yaw)*.875,0,Math.sin(yaw)*.875);e.setPos(owner.position().add(side).add(0,2.17,0).add(owner.getLookAngle().multiply(1,0,1).normalize().scale(1)));}else e.setPos(owner.getEyePosition().add(owner.getLookAngle().scale(.8)));var d=target.subtract(e.position()).normalize();e.shoot(d.x,d.y,d.z,1,0);owner.level().addFreshEntity(e);}
 @Override protected boolean canHitEntity(Entity e){if(!(e instanceof LivingEntity l)||!(getOwner() instanceof LivingEntity from))return false;return (from instanceof Morozko b?b.candidate(l):from instanceof net.minecraft.server.level.ServerPlayer?FrostStaffTargeting.canHit(l,from):FrostWorld.hostile(l,from))&&super.canHitEntity(e);}
 @Override protected void onHitEntity(EntityHitResult h){if(level().isClientSide||!(h.getEntity() instanceof LivingEntity t)||!(getOwner() instanceof LivingEntity from))return;if(t.hurt(FrostDamage.projectile("morozko_frost",this,from),damage)){FrostCold.add(t,cold);if(from instanceof Morozko b)b.attacked(t);if(from instanceof net.minecraft.server.level.ServerPlayer p)FrostSkills.remember(p,t);}}
 @Override protected void onHit(HitResult h){super.onHit(h);if(!level().isClientSide){FrostContent.sound(this,"spike_break");discard();}}
 @Override public void tick(){super.tick();if(level().isClientSide)level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,getX(),getY(),getZ(),0,0,0);else if(tickCount>24||getOwner()==null||session!=null&&(!(getOwner() instanceof Morozko b)||!b.fighting()||position().distanceToSqr(b.anchor)>1600))discard();}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);discard();}
}
