package org.slavicmyths.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class AncientSignItem extends Item {
    public AncientSignItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.slavicmyths.ancient_sign"));
    }
}
