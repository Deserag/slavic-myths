package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ShipyardBenchItem extends BlockItem {
    public ShipyardBenchItem(Block block, Properties props) { super(block, props); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> text, net.minecraft.world.item.TooltipFlag flag) {
        text.add(Component.literal("ПКМ — вкладки «Создание корабля» и «Модификации»").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("«Создание корабля»: выберите чертёж и соберите по материалам").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("«Модификации»: поместите судно и установите детали").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public Component getName(ItemStack stack) { return Component.literal("Корабельный верстак"); }
}
