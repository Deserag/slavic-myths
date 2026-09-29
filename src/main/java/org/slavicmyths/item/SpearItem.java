package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.vector.Vector3d;

/** A measured melee push distinguishes the spear without changing global reach. */
public final class SpearItem extends SwordItem {
    public SpearItem(IItemTier tier, Properties properties) { super(tier, 3, -2.8F, properties); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level.isClientSide) {
            Vector3d look = attacker.getLookAngle();
            target.push(look.x * 0.18, 0.03, look.z * 0.18);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
