package org.slavicmyths.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class BerryMorsItem extends Item {
    public BerryMorsItem(Properties properties) { super(properties); }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, world, user);
        if (!world.isClientSide && user instanceof Player) {
            Player player = (Player) user;
            if (!player.getAbilities().instabuild) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (result.isEmpty()) return bottle;
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            }
        }
        return result;
    }
}
