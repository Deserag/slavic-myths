package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/** Existing IDs and measured push, with a native main-hand interaction-range modifier. */
public final class SpearItem extends SwordItem {
    public SpearItem(Tier tier, Properties properties) { super(tier, properties.attributes(SwordItem.createAttributes(tier, 3, -2.8F)
        .withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE,
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","spear_reach"),1.25,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND))); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            Vec3 look = attacker.getLookAngle();
            target.push(look.x * 0.18, 0.03, look.z * 0.18);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
