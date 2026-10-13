package org.slavicmyths.item;

import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class AncientCoinItem extends Item {
    public AncientCoinItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> text, TooltipFlag flag) {
        text.add(Component.translatable("tooltip.slavicmyths.ancient_coin"));
    }
}
