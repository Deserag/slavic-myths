package org.slavicmyths.finist;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
/** Native arrow collision/ballistics/enchantment pipeline with bounded turn steering. */
public final class FinistArrow extends AbstractArrow {
 private UUID guide,originalGuide;private int trails,age;
 public FinistArrow(EntityType<? extends FinistArrow> type,Level level){super(type,level);}
 public FinistArrow(LivingEntity owner,Level world,ItemStack bow){super(org.slavicmyths.registry.ModEntities.FINIST_ARROW.get(),owner,world,new ItemStack(Items.ARROW),bow);}
 public void guide(UUID id){guide=originalGuide=id;}public UUID guide(){return guide;}
 public static Vec3 steer(Vec3 velocity,Vec3 desired){return steer(velocity,desired,6);}
 /** Keep the turn radius proportional to target distance rather than launch speed.
  * A fixed six-degree turn lets full-draw arrows orbit past a nearby marked enemy. */
 private static Vec3 steer(Vec3 velocity,Vec3 desired,double degrees){double speed=velocity.length();if(speed<.001||desired.lengthSqr()<.001)return velocity;var from=velocity.normalize();var to=desired.normalize();double angle=Math.acos(Math.clamp(from.dot(to),-1,1));if(angle<1E-6)return velocity;double fraction=Math.min(1,Math.toRadians(degrees)/angle);if(angle>Math.PI-1E-4){double turn=Math.min(angle,Math.toRadians(degrees));Vec3 basis=Math.abs(from.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0);Vec3 tangent=basis.subtract(from.scale(basis.dot(from))).normalize();return from.scale(Math.cos(turn)).add(tangent.scale(Math.sin(turn))).scale(speed);}return from.scale(Math.sin((1-fraction)*angle)/Math.sin(angle)).add(to.scale(Math.sin(fraction*angle)/Math.sin(angle))).normalize().scale(speed);}
 public static Vec3 guideVelocity(Vec3 velocity,Vec3 desired){
  double turn=Math.clamp(Math.toDegrees(Math.atan2(velocity.length()*4,Math.max(1,desired.length()))),20,45);
  double horizontalDot=velocity.x*desired.x+velocity.z*desired.z;
  if(horizontalDot<0){
   // Turn back in the horizontal plane first. The shortest 3D arc toward a
   // lower enemy behind the shooter otherwise drives the arrow into the floor.
   double angle=Math.atan2(velocity.x*desired.z-velocity.z*desired.x,horizontalDot);
   double yaw=Math.clamp(angle,-Math.toRadians(turn),Math.toRadians(turn));
   var heading=new Vec3(velocity.x*Math.cos(yaw)-velocity.z*Math.sin(yaw),Math.max(0,velocity.y),velocity.x*Math.sin(yaw)+velocity.z*Math.cos(yaw));
   return steer(velocity,heading,turn);
  }
  return steer(velocity,desired,turn);
 }
 @Override public void tick(){if(level() instanceof ServerLevel w&&!inGround){if(guide!=null){var e=w.getEntity(guide);var p=getOwner() instanceof ServerPlayer s?s:null;if(p==null||e==null||!e.isAlive()||!(e instanceof LivingEntity target)||!GreyPackRing.enemy(p,target)||distanceToSqr(target)>48*48||w.clip(new net.minecraft.world.level.ClipContext(position(),target.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS){guide=null;}else{var proposed=guideVelocity(getDeltaMovement(),target.getBoundingBox().getCenter().subtract(position()));setDeltaMovement(proposed);}}
   if(tickCount%4==0&&trails<15){trails++;w.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.8F,.65F,.35F),.45F),getX(),getY(),getZ(),1,0,0,0,0);}}
  super.tick();if(++age>1200)discard();}
 @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(Items.ARROW);}
 @Override protected void onHitEntity(EntityHitResult hit){var p=getOwner() instanceof ServerPlayer s?s:null;var target=hit.getEntity() instanceof LivingEntity l?l:null;boolean enhanced=p!=null&&GreyPackRing.enemy(p,target)&&p.hasLineOfSight(target)&&(originalGuide==null||originalGuide.equals(target.getUUID()));getPersistentData().putBoolean("FinistEnhanced",enhanced);super.onHitEntity(hit);getPersistentData().remove("FinistEnhanced");}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(guide!=null)n.putUUID("FinistGuide",guide);if(originalGuide!=null)n.putUUID("FinistOriginalGuide",originalGuide);n.putInt("FinistAge",age);n.putInt("FinistTrails",trails);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);guide=n.hasUUID("FinistGuide")?n.getUUID("FinistGuide"):null;originalGuide=n.hasUUID("FinistOriginalGuide")?n.getUUID("FinistOriginalGuide"):guide;age=Math.clamp(n.getInt("FinistAge"),0,1200);trails=Math.clamp(n.getInt("FinistTrails"),0,15);}
}
