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
 public static Vec3 steer(Vec3 velocity,Vec3 desired){double speed=velocity.length();if(speed<.001||desired.lengthSqr()<.001)return velocity;var from=velocity.normalize();var to=desired.normalize();double angle=Math.acos(Math.clamp(from.dot(to),-1,1));if(angle<1E-6)return velocity;double fraction=Math.min(1,Math.toRadians(6)/angle);if(angle>Math.PI-1E-4)return from.scale(1-fraction).add(to.scale(fraction)).normalize().scale(speed);return from.scale(Math.sin((1-fraction)*angle)/Math.sin(angle)).add(to.scale(Math.sin(fraction*angle)/Math.sin(angle))).normalize().scale(speed);}
 @Override public void tick(){if(level() instanceof ServerLevel w&&!inGround){if(guide!=null){var e=w.getEntity(guide);var p=getOwner() instanceof ServerPlayer s?s:null;if(p==null||e==null||!e.isAlive()||!(e instanceof LivingEntity target)||!GreyPackRing.enemy(p,target)||distanceToSqr(target)>48*48){guide=null;}else{var proposed=steer(getDeltaMovement(),target.getBoundingBox().getCenter().subtract(position()));if(w.clip(new net.minecraft.world.level.ClipContext(position(),position().add(proposed),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS)setDeltaMovement(proposed);}}
   if(tickCount%4==0&&trails<15){trails++;w.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.8F,.65F,.35F),.45F),getX(),getY(),getZ(),1,0,0,0,0);}}
  super.tick();if(++age>1200)discard();}
 @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(Items.ARROW);}
 @Override protected void onHitEntity(EntityHitResult hit){var p=getOwner() instanceof ServerPlayer s?s:null;var target=hit.getEntity() instanceof LivingEntity l?l:null;boolean enhanced=p!=null&&GreyPackRing.enemy(p,target)&&p.hasLineOfSight(target)&&(originalGuide==null||originalGuide.equals(target.getUUID()));getPersistentData().putBoolean("FinistEnhanced",enhanced);super.onHitEntity(hit);getPersistentData().remove("FinistEnhanced");}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(guide!=null)n.putUUID("FinistGuide",guide);if(originalGuide!=null)n.putUUID("FinistOriginalGuide",originalGuide);n.putInt("FinistAge",age);n.putInt("FinistTrails",trails);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);guide=n.hasUUID("FinistGuide")?n.getUUID("FinistGuide"):null;originalGuide=n.hasUUID("FinistOriginalGuide")?n.getUUID("FinistOriginalGuide"):guide;age=Math.clamp(n.getInt("FinistAge"),0,1200);trails=Math.clamp(n.getInt("FinistTrails"),0,15);}
}
