package org.slavicmyths.ninemor;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

/** A real, visible projectile; impact and debuffs always execute on the server. */
public final class NineMorShot extends ThrowableItemProjectile {
    public NineMorShot(EntityType<? extends NineMorShot> type, Level level) { super(type, level); }

    public NineMorShot(Level level, LivingEntity owner, int element) {
        super(NineMorContent.SHOT.get(), owner, level);
        setItem(new ItemStack(visualItem(element)));
    }

    private static Item visualItem(int element) {
        return switch (element) {
            case 1 -> Items.LIME_DYE;
            case 2 -> Items.PURPLE_DYE;
            case 3 -> Items.CYAN_DYE;
            case 4 -> Items.ORANGE_DYE;
            case 5 -> Items.YELLOW_DYE;
            case 6 -> Items.GREEN_DYE;
            case 7 -> Items.BROWN_DYE;
            case 8 -> Items.BLUE_DYE;
            default -> Items.RED_DYE;
        };
    }

    private int type() {
        Item stackItem = getItem().getItem();
        for (int i = 1; i <= 8; i++) if (stackItem == visualItem(i)) return i;
        return 0;
    }

    @Override protected Item getDefaultItem() { return Items.RED_DYE; }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide && hit.getEntity() instanceof LivingEntity living
                && hit.getEntity() != getOwner() && !(living instanceof NineMorSerpent)) {
            LivingEntity attacker = getOwner() instanceof LivingEntity other ? other : null;
            living.hurt(attacker != null ? damageSources().mobAttack(attacker) : damageSources().magic(), 8F);
            NineMorSerpent.applyElement(living, type());
        }
        impact();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        impact();
    }

    private void impact() {
        if (level() instanceof ServerLevel server) {
            int color = NineMorSerpent.COLORS[type()];
            server.sendParticles(new DustParticleOptions(
                new Vector3f(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F), 1.4F),
                getX(), getY(), getZ(), 23, .35D, .35D, .35D, .05D);
            discard();
        }
    }

    @Override public void tick() { super.tick(); if (!level().isClientSide && tickCount > 100) discard(); }
}
