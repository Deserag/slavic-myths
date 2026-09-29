package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.vector.Vector3d;

/** Single-hit melee weapons; no projectile or global reach changes. */
public final class HeavyWeaponItem extends SwordItem {
    private final double push;
    public HeavyWeaponItem(IItemTier tier, int damage, float speed, double push, Properties properties) {
        super(tier, damage, speed, properties);
        this.push = push;
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level.isClientSide && push > 0) {
            Vector3d direction = attacker.getLookAngle();
            target.knockback((float) push, -direction.x, -direction.z);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
