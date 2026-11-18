package org.slavicmyths.morovoy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

/** Individually synchronized, collision-tested projectile; no block destruction. */
public final class RavenBlightBolt extends ThrowableProjectile {
    private float damage = 8;
    public RavenBlightBolt(EntityType<? extends RavenBlightBolt> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}
    public void configure(MorovoyRaven boss, float hitDamage) { setOwner(boss); damage = hitDamage; }
    @Override protected double getDefaultGravity() { return 0; }
    @Override protected boolean canHitEntity(Entity candidate) {
        return candidate instanceof LivingEntity victim && !(victim instanceof MorovoyRaven) && super.canHitEntity(candidate) && (getOwner() == null || !getOwner().isAlliedTo(candidate));
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && hit.getEntity() instanceof LivingEntity victim && getOwner() instanceof MorovoyRaven boss && victim.hurt(damageSources().mobProjectile(this, boss), damage)) {
            victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 65, 0));
        }
    }
    @Override protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel world && getOwner() instanceof MorovoyRaven boss && boss.isAlive()) {
            RavenMiasma zone = MorovoyRavenContent.MIASMA.get().create(world);
            if (zone != null && world.hasChunkAt(BlockPos.containing(hit.getLocation()))) {
                zone.setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
                zone.configure(boss, 2.8F, 75);
                world.addFreshEntity(zone);
            }
        }
        if (!level().isClientSide) discard();
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) level().addParticle(new DustParticleOptions(new Vector3f(.3F, 1F, .19F), 1.5F), getX(), getY(), getZ(), 0, .015, 0);
        else if (tickCount >= 85) discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putFloat("Damage", damage); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); damage = tag.getFloat("Damage"); }
}
