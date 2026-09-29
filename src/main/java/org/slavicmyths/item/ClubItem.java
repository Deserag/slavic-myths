package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.vector.Vector3d;

public final class ClubItem extends SwordItem {
    public ClubItem(Properties properties) { super(ItemTier.WOOD, 4, -3.1F, properties); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level.isClientSide) {
            Vector3d look = attacker.getLookAngle();
            target.push(look.x * 0.35, 0.08, look.z * 0.35);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
