package org.slavicmyths.sisters;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Dark-blue single projectile summoned by Neveya's raised hand. */
public final class NeveyaBolt extends ThrowableProjectile{
    private float damage=20.8F;
    public NeveyaBolt(EntityType<? extends NeveyaBolt> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){}
    @Override protected double getDefaultGravity(){return 0;}
    public void configure(NeveyaBoss boss,float amount){setOwner(boss);damage=amount;}
    @Override protected boolean canHitEntity(Entity entity){
        return entity instanceof Player p&&!p.isCreative()&&!p.isSpectator()&&super.canHitEntity(entity);
    }
    @Override protected void onHitEntity(EntityHitResult result){
        if(!level().isClientSide&&result.getEntity() instanceof Player p&&getOwner() instanceof NeveyaBoss boss)
            p.hurt(damageSources().mobProjectile(this,boss),damage);
    }
    @Override protected void onHit(HitResult result){super.onHit(result);if(!level().isClientSide)discard();}
    @Override public void tick(){
        super.tick();
        level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,getX(),getY(),getZ(),0,.01,0);
        if(!level().isClientSide&&tickCount>65)discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putFloat("Damage",damage);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);damage=tag.getFloat("Damage");}
}
