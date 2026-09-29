package org.slavicmyths.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public final class BerryMorsItem extends Item {
    public BerryMorsItem(Properties properties) { super(properties); }
    @Override public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, world, user);
        if (!world.isClientSide && user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            if (!player.abilities.instabuild) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (result.isEmpty()) return bottle;
                if (!player.inventory.add(bottle)) player.drop(bottle, false);
            }
        }
        return result;
    }
}
