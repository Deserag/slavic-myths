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

    @Override public Component getName(ItemStack stack) {
        return Component.translatableWithFallback("item.slavicmyths.shipyard_key", "Верфяной ключ");
    }
}
