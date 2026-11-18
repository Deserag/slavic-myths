package org.slavicmyths.posad.boss;

import java.util.Comparator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Actual line-of-sight chain hook. This player weapon never steals currency or heals the bearer. */
public final class ShackledSickleItem extends SwordItem {
    public ShackledSickleItem(Tier tier, Properties properties) { super(tier, properties); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(weapon);
        Vec3 origin = player.getEyePosition();
        Vec3 heading = player.getLookAngle().normalize();
        Vec3 end = origin.add(heading.scale(10D));
        AABB search = new AABB(origin, end).inflate(1.25D);
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, search, entity -> entity != player && entity.isAlive() && entity instanceof Monster && !entity.isAlliedTo(player) && player.hasLineOfSight(entity)).stream()
            .filter(entity -> entity.getBoundingBox().inflate(.5D).clip(origin, end).isPresent())
            .min(Comparator.comparingDouble(entity -> origin.distanceToSqr(entity.position()))).orElse(null);
        if (target == null) return InteractionResultHolder.pass(weapon);
        if (!level.isClientSide) {
            boolean hit = target.hurt(player.damageSources().playerAttack(player), 4F);
            if (hit && !(target instanceof PosadBoss) && target.getBbWidth() <= 2.2F && target.getBbHeight() <= 3F) {
                Vec3 motion = player.position().subtract(target.position()).multiply(1D, 0D, 1D).normalize().scale(.9D);
                if (level.noCollision(target, target.getBoundingBox().move(motion))) target.setDeltaMovement(target.getDeltaMovement().add(motion));
            }
            player.getCooldowns().addCooldown(this, 240);
            weapon.hurtAndBreak(3, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            player.playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE, 1F, .9F);
        }
        return InteractionResultHolder.sidedSuccess(weapon, level.isClientSide);
    }
}
