package org.slavicmyths.depth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.*;
public final class ThrownNet extends ThrowableItemProjectile {
 public ThrownNet(EntityType<? extends ThrownNet> t,Level w){super(t,w);}public ThrownNet(Level w,LivingEntity e){super(ModEntities.THROWN_NET.get(),e,w);}
 protected Item getDefaultItem(){return ModItems.VODYANOY_NET.get();}

 public void tick(){super.tick();if(!level().isClientSide){if(tickCount>80)discard();if(getPersistentData().contains("ClassNetRange")){var n=getPersistentData();double r=n.getDouble("ClassNetRange");if(position().distanceToSqr(new Vec3(n.getDouble("ClassNetX"),n.getDouble("ClassNetY"),n.getDouble("ClassNetZ")))>r*r)discard();}}}
 protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(level().isClientSide||!(hit.getEntity() instanceof LivingEntity e)||!(getOwner() instanceof LivingEntity owner))return;
  if(!org.slavicmyths.combat.CombatTargeting.canControl(owner,e)||e.getTags().contains("slavic_music_immune")){immune(e);return;}
  if(getPersistentData().contains("ClassNetDuration")){org.slavicmyths.rpg.classes.ClassControl.apply(e,getPersistentData().getInt("ClassNetDuration"),false);return;}
  if(!e.canChangeDimensions(e.level(),e.level())||e.getMaxHealth()>=100){immune(e);return;}
  e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,e.isInWater()?100:60,e.isInWater()?4:3));
 }
 private void immune(LivingEntity e){if(getOwner() instanceof net.minecraft.server.level.ServerPlayer p)p.displayClientMessage(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.net_immune"),true);((net.minecraft.server.level.ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,getX(),getY(),getZ(),3,.1,.1,.1,0);}
 protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide)discard();}
}
