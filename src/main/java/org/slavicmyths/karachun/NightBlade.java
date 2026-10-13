package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.phys.*;
import net.minecraft.server.level.*;
import net.minecraft.nbt.CompoundTag;
public final class NightBlade extends ThrowableProjectile {
 private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> HUNGER=net.minecraft.network.syncher.SynchedEntityData.defineId(NightBlade.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
 public UUID session,cast;private int life;private net.minecraft.world.item.ItemStack weapon=net.minecraft.world.item.ItemStack.EMPTY;
 public NightBlade(EntityType<? extends NightBlade> t,net.minecraft.world.level.Level w){super(t,w);setNoGravity(true);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){b.define(HUNGER,false);}
 public boolean hunger(){return entityData.get(HUNGER);}
 public static void hunger(Karachun boss,Vec3 at){var b=NightContent.BLADE.get().create(boss.level());if(b==null)return;b.entityData.set(HUNGER,true);b.session=boss.session;b.setOwner(boss);b.setPos(boss.getEyePosition().add(boss.getLookAngle().scale(1.2)));b.shoot(at.x-b.getX(),at.y-b.getY(),at.z-b.getZ(),.65f,0);boss.level().addFreshEntity(b);}
 public static void launch(ServerPlayer p,UUID cast,double yaw){var b=NightContent.BLADE.get().create(p.level());if(b==null)return;b.cast=cast;b.weapon=p.getMainHandItem().copy();b.setOwner(p);b.setPos(p.getEyePosition());Vec3 dir=p.getLookAngle().yRot((float)yaw);b.shoot(dir.x,dir.y,dir.z,.65f,0);p.level().addFreshEntity(b);}
 @Override public void tick(){super.tick();if(!level().isClientSide){var owner=getOwner();if(++life>(hunger()?35:16)||owner==null||!owner.isAlive()||owner instanceof Karachun b&&!b.fighting())discard();if(tickCount%2==0)((ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,getX(),getY(),getZ(),1,0,0,0,0);}}
 @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&e instanceof LivingEntity l&&(getOwner() instanceof Karachun b?b.candidate(l):getOwner() instanceof ServerPlayer p&&NightCombat.valid(l,p));}
 @Override protected void onHitEntity(EntityHitResult h){if(level().isClientSide)return;if(h.getEntity() instanceof LivingEntity t){if(getOwner() instanceof Karachun b&&b.candidate(t)){NightEffects.add(t,session,net.minecraft.world.effect.MobEffects.HUNGER,240,3);NightStatus.add(t,session,0,6,false);b.credit(NightOwners.owner(t),0,true);}else if(getOwner() instanceof ServerPlayer p&&cast!=null&&NightCombat.claim(p,cast,t.getUUID()))NightCombat.damage(p,t,weapon,1.1f);}discard();}
 @Override protected void onHitBlock(BlockHitResult h){discard();}
 @Override protected double getDefaultGravity(){return 0;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);discard();}
}
