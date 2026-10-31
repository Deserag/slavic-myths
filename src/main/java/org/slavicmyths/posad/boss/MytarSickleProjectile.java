package org.slavicmyths.posad.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Real colliding thrown sickle. The boss owns damage, pull and finisher state. */
public final class MytarSickleProjectile extends ThrowableItemProjectile {
    public MytarSickleProjectile(EntityType<? extends MytarSickleProjectile> type, Level level) { super(type, level); }
    public MytarSickleProjectile(Level level, LivingEntity owner) { super(ModEntities.MYTAR_CHAIN_PROJECTILE.get(), owner, level); }
    @Override protected Item getDefaultItem() { return ModItems.MYTAR_SHACKLED_SICKLE.get(); }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && (tickCount > 20 || !(getOwner() instanceof PosadBoss) || distanceToSqr(getOwner()) > 14D * 14D)) discard();
    }
    @Override protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide && getOwner() instanceof PosadBoss boss && result.getEntity() instanceof net.minecraft.server.level.ServerPlayer victim) boss.catchChain(victim);
    }
    @Override protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) discard();
    }
}
