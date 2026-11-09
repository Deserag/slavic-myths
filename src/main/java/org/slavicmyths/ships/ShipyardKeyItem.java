package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A ship-specific packing tool. Right-click on a stopped empty-of-passengers raft.
 * The server writes all on-board inventories to the returned raft item first.
 */
public final class ShipyardKeyItem extends Item {
    public ShipyardKeyItem(Properties properties) { super(properties); }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> text, net.minecraft.world.item.TooltipFlag flag) {
        text.add(Component.literal("ПКМ по остановленному кораблю — швартовка в блоки мира").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("Shift + ПКМ по пустому судну — упаковка вместе с грузом").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("Для плотов действуют прежние правила упаковки").withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(Component.literal("Средняя кнопка мыши не сохраняет груз!").withStyle(net.minecraft.ChatFormatting.YELLOW));
    }

    @Override public Component getName(ItemStack stack) {
        return Component.translatableWithFallback("item.slavicmyths.shipyard_key", "Верфяной ключ");
    }
}
