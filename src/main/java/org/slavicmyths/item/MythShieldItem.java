package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;

/** Forge 1.16.5 defaults isShield to Items.SHIELD, even for ShieldItem subclasses. */
public final class MythShieldItem extends ShieldItem {
    public MythShieldItem(Properties properties) { super(properties); }
    @Override public boolean isShield(ItemStack stack, LivingEntity entity) { return true; }
}
