package org.slavicmyths.posad.boss;

import net.minecraft.core.BlockPos;
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
    private int hookedEntityId = -1;
    private int linkedTicks;
    private double pulledDistance;
    private boolean returning;

    public MytarSickleProjectile(EntityType<? extends MytarSickleProjectile> type, Level level) { super(type, level); setNoGravity(true); }
    public MytarSickleProjectile(Level level, LivingEntity owner) { super(ModEntities.MYTAR_CHAIN_PROJECTILE.get(), owner, level); setNoGravity(true); }
    @Override protected Item getDefaultItem() { return ModItems.MYTAR_SHACKLED_SICKLE.get(); }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        Entity owner = getOwner();
        if (!(owner instanceof LivingEntity living) || !living.isAlive() || owner.level() != level()) { discard(); return; }
        if (linkedTicks > 0) {
            if (!(level() instanceof ServerLevel server)) { discard(); return; }
            Entity entity = server.getEntity(hookedEntityId);
            if (!(entity instanceof LivingEntity victim) || !victim.isAlive() || !victim.level().equals(level()) || owner.distanceToSqr(victim) > 196D) { startReturn(); return; }
            setDeltaMovement(Vec3.ZERO);
            setPos(victim.getX(), victim.getY() + victim.getBbHeight() * .6D, victim.getZ());
            if (owner instanceof Player player) pullForPlayer(player, victim);
            linkedTicks--;
            if (linkedTicks == 0) startReturn();
            return;
        }
        if (returning) {
            Vec3 toOwner = living.getEyePosition().subtract(position());
            if (toOwner.lengthSqr() <= 2.25D || tickCount > 45) { discard(); return; }
            Vec3 step = toOwner.normalize().scale(Math.min(1.5D, toOwner.length()));
            setPos(position().add(step));
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (owner instanceof Player && (tickCount >= 10 || owner.distanceToSqr(this) > 100D)) startReturn();
        if (owner instanceof PosadBoss && (tickCount > 20 || owner.distanceToSqr(this) > 196D)) discard();
    }

    private void pullForPlayer(Player player, LivingEntity victim) {
        if (!(victim instanceof Monster) || victim instanceof PosadBoss || victim.getBbWidth() > 2.2F || victim.getBbHeight() > 3F) return;
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
        pulledDistance += amount;
    }

    private void startReturn() {
        linkedTicks = 0;
        returning = true;
        setDeltaMovement(Vec3.ZERO);
    }

    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        if (getOwner() instanceof PosadBoss boss && result.getEntity() instanceof ServerPlayer victim) {
            if (boss.catchChain(victim)) { hookedEntityId = victim.getId(); linkedTicks = 12; setDeltaMovement(Vec3.ZERO); }
        } else if (getOwner() instanceof Player player && result.getEntity() instanceof Monster victim && victim.isAlive() && !victim.isAlliedTo(player) && player.hasLineOfSight(victim)) {
            if (victim.hurt(player.damageSources().playerAttack(player), 4F)) {
                if (victim instanceof PosadBoss || victim.getBbWidth() > 2.2F || victim.getBbHeight() > 3F) victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                else { hookedEntityId = victim.getId(); linkedTicks = 10; setDeltaMovement(Vec3.ZERO); }
            }
        }
    }

    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || linkedTicks > 0) return;
        if (getOwner() instanceof Player) startReturn();
        else discard();
    }
}
