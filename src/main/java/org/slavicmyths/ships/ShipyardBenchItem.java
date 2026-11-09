package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ShipyardBenchItem extends BlockItem {
    public ShipyardBenchItem(Block block, Properties props) { super(block, props); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> text, net.minecraft.world.item.TooltipFlag flag) {
        text.add(Component.literal("ПКМ — вкладки «Суда», «Оснащение», «Облик»").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("«Суда»: выберите чертёж и соберите по списку материалов").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("«Оснащение»: поместите судно и установите детали").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public Component getName(ItemStack stack) { return Component.literal("Корабельный верстак"); }
}
