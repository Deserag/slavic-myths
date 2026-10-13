package org.slavicmyths.underground;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.*;
public final class ThrownStone extends ThrowableItemProjectile {
 public ThrownStone(EntityType<? extends ThrowableItemProjectile> t,Level l){super(t,l);}public ThrownStone(Level l,LivingEntity owner){super(UndergroundMobs.STONE.get(),owner,l);}
 protected Item getDefaultItem(){return Items.COBBLESTONE;}
 public boolean shouldBeSaved(){return false;}
 protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&!(e instanceof EncounterMob)&&!(e instanceof org.slavicmyths.industry.ChudPerson);}
 protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);hit.getEntity().hurt(damageSources().thrown(this,getOwner()),EncounterRules.STONE_DAMAGE);}
 protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide){if(level() instanceof net.minecraft.server.level.ServerLevel w)w.sendParticles(ParticleTypes.POOF,getX(),getY(),getZ(),6,.1,.1,.1,.03);playSound(net.minecraft.sounds.SoundEvents.STONE_BREAK,.5F,1);discard();}}
 public void tick(){super.tick();if(!level().isClientSide&&(tickCount>40||getOwner()!=null&&!getOwner().isAlive()))discard();}
}
