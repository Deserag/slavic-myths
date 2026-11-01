package org.slavicmyths.posad.boss;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.rpg.Runes;

/** Real eight-tick right-click windup; integrates the existing persistent Rune Anvil data. */
public final class ShackledSickleItem extends SwordItem {
    public static final int WINDUP_TICKS = 8;
    public static final int COOLDOWN_TICKS = 240;
    public static final int MAX_RUNE_SOCKETS = 3;

    public ShackledSickleItem(Tier tier, Properties properties) { super(tier, properties); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(weapon);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(weapon);
    }

    @Override public void onUseTick(Level level, LivingEntity user, ItemStack weapon, int remainingUseDuration) {
        if (!(user instanceof Player player) || getUseDuration(weapon, player) - remainingUseDuration < WINDUP_TICKS) return;
        if (level instanceof ServerLevel server && !player.getCooldowns().isOnCooldown(this)) {
            InteractionHand hand = player.getUsedItemHand();
            MytarSickleProjectile hook = new MytarSickleProjectile(server, player);
            Vec3 launch = MytarSickleProjectile.handAnchor(player, hand == InteractionHand.OFF_HAND, 1F);
            Vec3 look = player.getLookAngle().normalize();
            hook.setPos(launch.x, launch.y, launch.z);
            hook.setOffhand(hand == InteractionHand.OFF_HAND);
            hook.shoot(look.x, look.y, look.z, 1.1F, 0F);
            if (server.addFreshEntity(hook)) {
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                weapon.hurtAndBreak(3, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                player.playSound(SoundEvents.CHAIN_PLACE, 1F, .85F);
            }
        }
        player.stopUsingItem();
    }

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.lore").withStyle(ChatFormatting.DARK_RED));
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.ability").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.stats").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.limit").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.sockets", Runes.occupied(stack), MAX_RUNE_SOCKETS).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.slavicmyths.okovny_sickle.runes", Runes.slots(stack), MAX_RUNE_SOCKETS).withStyle(ChatFormatting.GRAY));
    }
}
