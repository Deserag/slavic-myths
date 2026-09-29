package org.slavicmyths.item;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class SimpleCharmItem extends Item {
    private final String kind;
    public SimpleCharmItem(String kind, Properties properties) { super(properties); this.kind = kind; }
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> text, ITooltipFlag flag) {
        text.add(new TranslationTextComponent("tooltip.slavicmyths." + kind));
    }
}
