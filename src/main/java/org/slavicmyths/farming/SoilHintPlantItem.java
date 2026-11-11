package org.slavicmyths.farming;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Preserves vanilla placement and food use while naming the required Nether soil. */
public final class SoilHintPlantItem extends ItemNameBlockItem {
    private final Block substrate;

    public SoilHintPlantItem(Block crop, Block substrate, Properties properties) {
        super(crop, properties);
        this.substrate = substrate;
    }

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.slavicmyths.nether_plant_soil", substrate.getName()).withStyle(ChatFormatting.GRAY));
    }
}
