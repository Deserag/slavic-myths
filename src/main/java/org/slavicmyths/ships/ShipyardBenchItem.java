package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ShipyardBenchItem extends BlockItem {
    public ShipyardBenchItem(Block block, Properties props) { super(block, props); }
    @Override public Component getName(ItemStack stack) { return Component.literal("Корабельный верстак"); }
}
