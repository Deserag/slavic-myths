package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

public final class ClubItem extends SwordItem {
    public ClubItem(Properties properties) { super(Tiers.WOOD, properties.attributes(SwordItem.createAttributes(Tiers.WOOD, 4, -3.1F))); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            Vec3 look = attacker.getLookAngle();
            target.push(look.x * 0.35, 0.08, look.z * 0.35);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
