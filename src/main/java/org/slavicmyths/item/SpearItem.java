package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** A measured melee push distinguishes the spear without changing global reach. */
public final class SpearItem extends SwordItem {
    public SpearItem(Tier tier, Properties properties) { super(tier, properties.attributes(SwordItem.createAttributes(tier, 3, -2.8F))); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            Vec3 look = attacker.getLookAngle();
            target.push(look.x * 0.18, 0.03, look.z * 0.18);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
