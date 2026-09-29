package org.slavicmyths.item;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class PerunCharmItem extends Item {
    public PerunCharmItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> text, ITooltipFlag flag) {
        text.add(new TranslationTextComponent("tooltip.slavicmyths.perun_charm"));
    }
}
