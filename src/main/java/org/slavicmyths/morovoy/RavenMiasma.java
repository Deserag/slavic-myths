package org.slavicmyths.morovoy;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Recoverable plague zone: client particles + server damage, zero terrain mutations. */
public final class RavenMiasma extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(RavenMiasma.class, EntityDataSerializers.FLOAT);
    private UUID owner;
    private int elapsed, lifetime = 100;
    public RavenMiasma(EntityType<? extends RavenMiasma> type, Level level) { super(type, level); noPhysics = true; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(RADIUS, 3.4F); }
    public float radius() { return entityData.get(RADIUS); }
    public void configure(MorovoyRaven bird, float size, int lifespan) {
        owner = bird.getUUID(); lifetime = Math.clamp(lifespan, 20, 300);
        entityData.set(RADIUS, Math.clamp(size, 1F, 6F));
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount % 3 == 0) for (int i = 0; i < 5; i++) {
                double angle = (tickCount * .14) + (i * Math.PI * 2 / 5);
                double x = getX() + Math.cos(angle) * radius(), z = getZ() + Math.sin(angle) * radius();
                level().addParticle(org.slavicmyths.mor.MorContent.SPARK.get(), x, getY() + .18, z, 0, .018, 0);
            }
            return;
        }
        if (!(level() instanceof ServerLevel world) || ++elapsed >= lifetime || owner == null || !(world.getEntity(owner) instanceof MorovoyRaven boss) || !boss.isAlive()) { discard(); return; }
        if (elapsed % 10 != 0) return;
        for (Player player : world.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius(), 2.2, radius()), p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
            if (player.distanceToSqr(getX(), player.getY(), getZ()) > radius() * radius() || Math.abs(player.getY() - getY()) > 2.2) continue;
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 35, 0));
            if (elapsed % 20 == 0) player.hurt(damageSources().mobAttack(boss), 2);
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", elapsed); tag.putInt("Life", lifetime); tag.putFloat("Radius", radius());
        if (owner != null) tag.putUUID("Owner", owner);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        elapsed = Math.clamp(tag.getInt("Age"), 0, 200);
        lifetime = Math.clamp(tag.getInt("Life"), 1, 300);
        entityData.set(RADIUS, Math.clamp(tag.getFloat("Radius"), 1F, 6F));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
