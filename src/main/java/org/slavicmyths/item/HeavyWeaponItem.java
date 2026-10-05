package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** Single-hit melee weapons; no projectile or global reach changes. */
public final class HeavyWeaponItem extends SwordItem {
    private final double push;
    public HeavyWeaponItem(Tier tier, int damage, float speed, double push, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, damage, speed)));
        this.push = push;
    }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && push > 0) {
            Vec3 direction = attacker.getLookAngle();
            target.knockback((float) push, -direction.x, -direction.z);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
