package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A Slavic shipyard part, with a human-readable Creative name before localization. */
public final class NamedShipPart extends Item {
    private final String name;
    public NamedShipPart(String name, Properties props) { super(props); this.name = name; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<Component> text, net.minecraft.world.item.TooltipFlag flag) {
        if (name.equals("Палубная секция")) text.add(Component.literal("Материал для постройки и расширения на корабельном верстаке").withStyle(net.minecraft.ChatFormatting.GRAY));
        else if (name.contains("парус") || name.contains("Парус") || name.contains("ткань")
                || name.contains("мачты")) text.add(Component.literal(
                "Корабль: ПКМ по мачте — монтаж/замена; Shift+ПКМ без предмета — снять")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        else text.add(Component.literal("Установить на плот: Shift + ПКМ").withStyle(net.minecraft.ChatFormatting.GRAY));
        if (name.equals("Малый парус")) text.add(Component.literal("Самостоятельная тяга при рулевом управлении; 5×7 — два паруса").withStyle(net.minecraft.ChatFormatting.GRAY));
        if (name.equals("Судовой руль")) text.add(Component.literal("Рулевой направляет судно с поднятым парусом").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override public Component getName(ItemStack stack) { return Component.literal(name); }
}
