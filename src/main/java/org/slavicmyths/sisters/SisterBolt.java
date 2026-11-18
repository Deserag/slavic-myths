package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Actual swept Minecraft projectile, damage only on collision. */
public final class SisterBolt extends ThrowableProjectile {
    private float damage = 6;
    public SisterBolt(EntityType<? extends SisterBolt> type,Level world) { super(type,world); }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    @Override protected double getDefaultGravity() { return 0; }
    public void configure(LivingEntity boss,float hitDamage) { setOwner(boss);damage=hitDamage; }
    @Override protected boolean canHitEntity(Entity entity) { return entity instanceof Player p && !p.isSpectator() && !p.isCreative() && super.canHitEntity(entity); }
    @Override protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide && result.getEntity() instanceof Player player && getOwner() instanceof LivingEntity boss && (boss instanceof SisterBoss || boss instanceof LedeyaBoss) && player.hurt(damageSources().mobProjectile(this,boss),damage)) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,45,0));
        }
    }
    @Override protected void onHit(HitResult result) { super.onHit(result); if (!level().isClientSide) discard(); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide && tickCount%2==0) level().addParticle(getType() == SistersContent.FROST_BOLT.get() ? net.minecraft.core.particles.ParticleTypes.SNOWFLAKE : net.minecraft.core.particles.ParticleTypes.WITCH,getX(),getY(),getZ(),0,.015,0);
        if (!level().isClientSide && tickCount>=70) discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putFloat("ImpactDamage",damage); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);damage=tag.getFloat("ImpactDamage"); }
}
