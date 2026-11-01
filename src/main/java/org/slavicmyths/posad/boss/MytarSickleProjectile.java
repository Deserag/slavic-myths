package org.slavicmyths.posad.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Physical chain hook for the boss and the player; client renderer draws an unbroken tether. */
public final class MytarSickleProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> OFFHAND = SynchedEntityData.defineId(MytarSickleProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HOOK_PHASE = SynchedEntityData.defineId(MytarSickleProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOOK_TARGET = SynchedEntityData.defineId(MytarSickleProjectile.class, EntityDataSerializers.INT);
    private int hookedEntityId = -1;
    private int linkedTicks;
    private double pulledDistance;
    private boolean returning;

    public MytarSickleProjectile(EntityType<? extends MytarSickleProjectile> type, Level level) { super(type, level); setNoGravity(true); }
    public MytarSickleProjectile(Level level, LivingEntity owner) { super(ModEntities.MYTAR_CHAIN_PROJECTILE.get(), owner, level); setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OFFHAND, false);
        builder.define(HOOK_PHASE, 0);
        builder.define(HOOK_TARGET, -1);
    }
    public void setOffhand(boolean offhand) { entityData.set(OFFHAND, offhand); }
    public boolean offhand() { return entityData.get(OFFHAND); }
    /** Animated weapon tether and initial projectile share exactly the same hand-space origin. */
    public static Vec3 handAnchor(LivingEntity owner, boolean offhand, float partialTick) {
        Vec3 base = owner.getPosition(partialTick);
        double yaw = Math.toRadians(owner.getYRot());
        Vec3 front = new Vec3(-Math.sin(yaw), 0D, Math.cos(yaw));
        Vec3 right = new Vec3(-front.z, 0D, front.x);
        double lateral = owner instanceof PosadBoss ? .6875D : .36D;
        double height = owner instanceof PosadBoss ? .6D : owner.getEyeHeight() - .46D;
        return base.add(right.scale(offhand ? -lateral : lateral)).add(front.scale(.22D)).add(0D, height, 0D);
    }
    @Override protected Item getDefaultItem() { return ModItems.OKOVNY_SICKLE.get(); }

    /** Phases: 0 flying, 1 latched, 2 returning; only flying projectiles perform hit detection. */
    @Override public void tick() {
        if (entityData.get(HOOK_PHASE) == 0) super.tick();
        else baseTick();
        Entity owner = getOwner();
        if (!(owner instanceof LivingEntity living) || !living.isAlive() || owner.level() != level()) { discard(); return; }
        if (level().isClientSide) {
            if (entityData.get(HOOK_PHASE) == 1) {
                Entity target = level().getEntity(entityData.get(HOOK_TARGET));
                if (target instanceof LivingEntity victim) setPos(victim.getX(), victim.getY() + victim.getBbHeight() * .6D, victim.getZ());
            }
            return;
        }
        if (linkedTicks > 0) {
            if (!(level() instanceof ServerLevel server)) { startReturn(); return; }
            Entity target = server.getEntity(hookedEntityId);
            if (!(target instanceof LivingEntity victim) || !victim.isAlive() || victim.level() != level() || !level().hasChunkAt(victim.blockPosition()) || owner.distanceToSqr(victim) > 196D) { startReturn(); return; }
            setDeltaMovement(Vec3.ZERO);
            setPos(victim.getX(), victim.getY() + victim.getBbHeight() * .6D, victim.getZ());
            if (owner instanceof PosadBoss boss && (!(victim instanceof ServerPlayer player) || !boss.pullChainedVictim(player))) { startReturn(); return; }
            if (owner instanceof Player player) pullForPlayer(player, victim);
            if (returning) return;
            if (--linkedTicks == 0) {
                if (owner instanceof PosadBoss boss) boss.finishChainedVictim();
                startReturn();
            }
            return;
        }
        if (returning) {
            Vec3 toHand = handAnchor(living, offhand(), 1F).subtract(position());
            if (toHand.lengthSqr() <= .32D || tickCount > 85) { discard(); return; }
            Vec3 step = toHand.normalize().scale(Math.min(1.5D, toHand.length()));
            setPos(position().add(step));
            setDeltaMovement(Vec3.ZERO);
            hasImpulse = true;
            return;
        }
        if (owner instanceof Player && (tickCount >= 10 || owner.distanceToSqr(this) > 100D)) startReturn();
        if (owner instanceof PosadBoss && (tickCount > 20 || owner.distanceToSqr(this) > 196D)) startReturn();
    }

    private void pullForPlayer(Player player, LivingEntity victim) {
        if (!(victim instanceof Monster) || victim instanceof PosadBoss || victim.getBbWidth() > 2.2F || victim.getBbHeight() > 3F) { startReturn(); return; }
        if (!player.hasLineOfSight(victim) || pulledDistance >= 5D) { startReturn(); return; }
        Vec3 toward = player.position().subtract(victim.position()).multiply(1D, 0D, 1D);
        double distance = toward.length();
        if (distance <= 2D) return;
        double amount = Math.min(1.3D, Math.min(5D - pulledDistance, distance - 2D));
        if (amount <= 0D) return;
        Vec3 step = toward.normalize().scale(amount);
        BlockPos destination = BlockPos.containing(victim.position().add(step));
        if (!level().hasChunkAt(destination) || level().getFluidState(destination).is(FluidTags.LAVA) || level().getFluidState(destination.below()).is(FluidTags.LAVA) || !level().getBlockState(destination.below()).isSolid() || !level().noCollision(victim, victim.getBoundingBox().move(step))) { startReturn(); return; }
        victim.setDeltaMovement(step.x, victim.getDeltaMovement().y, step.z);
        victim.hurtMarked = true;
        victim.hasImpulse = true;
        pulledDistance += amount;
    }

    private void startReturn() {
        if (returning) return;
        if (getOwner() instanceof PosadBoss boss) boss.releaseChainedVictim();
        linkedTicks = 0;
        returning = true;
        entityData.set(HOOK_PHASE, 2);
        entityData.set(HOOK_TARGET, -1);
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
    }

    private void latch(LivingEntity victim, int duration) {
        hookedEntityId = victim.getId();
        linkedTicks = duration;
        entityData.set(HOOK_TARGET, hookedEntityId);
        entityData.set(HOOK_PHASE, 1);
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
    }
    @Override protected boolean canHitEntity(Entity target) { return entityData.get(HOOK_PHASE) == 0 && target != getOwner() && super.canHitEntity(target); }
    @Override public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide && getOwner() instanceof PosadBoss boss) { boss.releaseChainedVictim(); boss.setChainWeaponAway(false); }
        super.remove(reason);
    }

    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide || returning || linkedTicks > 0) return;
        if (getOwner() instanceof PosadBoss boss && result.getEntity() instanceof ServerPlayer victim) {
            if (boss.catchChain(victim)) { latch(victim, 14); }
        } else if (getOwner() instanceof Player player && result.getEntity() instanceof Monster victim && victim.isAlive() && !victim.isAlliedTo(player) && player.hasLineOfSight(victim)) {
            if (victim.hurt(player.damageSources().playerAttack(player), 4F)) {
                if (victim instanceof PosadBoss || victim.getBbWidth() > 2.2F || victim.getBbHeight() > 3F) victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                else { latch(victim, 10); }
            }
        }
    }

    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || linkedTicks > 0 || returning) return;
        startReturn();
    }
}
