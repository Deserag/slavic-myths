package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;

/** Small server-side effect, never a LightningBoltEntity. */
public final class ThunderAxeItem extends AxeItem {
    public ThunderAxeItem(Tier tier, Properties properties) { super(tier, properties.attributes(AxeItem.createAttributes(tier, 5.0F, -3.1F))); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player && attacker.level() instanceof ServerLevel) {
            Player player = (Player) attacker;
            if (!player.getCooldowns().isOnCooldown(this) && player.getRandom().nextFloat() < 0.2F) {
                target.hurt(target.damageSources().magic(), 1.0F);
                ((ServerLevel) attacker.level()).sendParticles(ParticleTypes.ENCHANT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 8, 0.25, 0.3, 0.25, 0.02);
                attacker.level().playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
                        SoundSource.PLAYERS, 0.25F, 1.4F);
                player.getCooldowns().addCooldown(this, 60);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
