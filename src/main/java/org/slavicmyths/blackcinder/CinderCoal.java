package org.slavicmyths.blackcinder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
/** Visible slow ember; cannot place fire or damage blocks. */
public final class CinderCoal extends Projectile {
 public CinderCoal(EntityType<? extends CinderCoal> t,Level l){super(t,l);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&!(e instanceof CinderMob)&&!(e instanceof CinderSpike);}
 @Override public void tick(){super.tick();var v=getDeltaMovement();var next=position().add(v);if(!level().isClientSide){if(tickCount>80||!level().hasChunkAt(net.minecraft.core.BlockPos.containing(next))){discard();return;}var sweep=new AABB(position(),next).inflate(.4);for(int cx=net.minecraft.util.Mth.floor(sweep.minX)>>4;cx<=(net.minecraft.util.Mth.floor(sweep.maxX)>>4);cx++)for(int cz=net.minecraft.util.Mth.floor(sweep.minZ)>>4;cz<=(net.minecraft.util.Mth.floor(sweep.maxZ)>>4);cz++)if(!level().hasChunk(cx,cz)){discard();return;}var hit=ProjectileUtil.getHitResultOnMoveVector(this,this::canHitEntity);if(hit.getType()!=HitResult.Type.MISS){if(hit instanceof EntityHitResult e&&e.getEntity() instanceof LivingEntity living)living.hurt(CinderDamage.heat(this,getOwner()),8);discard();return;}}setPos(next);updateRotation();if(level().isClientSide)level().addParticle(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,getX(),getY(),getZ(),0,0,0);}
 @Override protected void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Age",tickCount);}
 @Override protected void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);tickCount=Math.clamp(t.getInt("Age"),0,81);}
}
