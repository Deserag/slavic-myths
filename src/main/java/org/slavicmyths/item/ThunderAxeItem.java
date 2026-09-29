package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.IItemTier;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;

/** Small server-side effect, never a LightningBoltEntity. */
public final class ThunderAxeItem extends AxeItem {
    public ThunderAxeItem(IItemTier tier, Properties properties) { super(tier, 5.0F, -3.1F, properties); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity && attacker.level instanceof ServerWorld) {
            PlayerEntity player = (PlayerEntity) attacker;
            if (!player.getCooldowns().isOnCooldown(this) && player.getRandom().nextFloat() < 0.2F) {
                target.hurt(DamageSource.MAGIC, 1.0F);
                ((ServerWorld) attacker.level).sendParticles(ParticleTypes.ENCHANT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 8, 0.25, 0.3, 0.25, 0.02);
                attacker.level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER,
                        SoundCategory.PLAYERS, 0.25F, 1.4F);
                player.getCooldowns().addCooldown(this, 60);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
