package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ShipyardBenchItem extends BlockItem {
    public ShipyardBenchItem(Block block, Properties props) { super(block, props); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> text, net.minecraft.world.item.TooltipFlag flag) {
        text.add(Component.literal("ПКМ — крафт корабельных деталей").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("ПКМ с палубной секцией — плот 3×3 (9 секций + вёсла)").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("ПКМ с предметом плота — расширить, сохранив груз").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public Component getName(ItemStack stack) { return Component.literal("Корабельный верстак"); }
}
