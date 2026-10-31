package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Eight-tick windup followed by a physical ten-block chain projectile; never steals coins. */
public final class ShackledSickleItem extends SwordItem {
    private static final int WINDUP_TICKS = 8;
    private static final int COOLDOWN_TICKS = 240;

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
        if (!level.isClientSide && level instanceof ServerLevel server && !player.getCooldowns().isOnCooldown(this)) {
            MytarSickleProjectile hook = new MytarSickleProjectile(server, player);
            Vec3 look = player.getLookAngle().normalize();
            hook.setPos(player.getX(), player.getEyeY() - .18D, player.getZ());
            hook.shoot(look.x, look.y, look.z, 1.1F, 0F);
            server.addFreshEntity(hook);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            weapon.hurtAndBreak(3, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            player.playSound(SoundEvents.CHAIN_PLACE, 1F, .85F);
        }
        player.stopUsingItem();
    }
}
