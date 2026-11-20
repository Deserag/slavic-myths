package org.slavicmyths.marine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Guaranteed victory reward: a short defensive water blessing on a long cooldown. */
public final class ZhelvaStoneCharm extends Item {
    public ZhelvaStoneCharm(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        return Component.translatableWithFallback(
            "item.slavicmyths.zhelva_stone_charm", "Оберег Каменного Дна");
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this))
            return InteractionResultHolder.fail(stack);
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 150, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 480, 0));
            player.getCooldowns().addCooldown(this, 1600);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
