package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Localizable ship parts. Visual models are authored under assets/slavicmyths. */
public final class ShipPartItem extends Item {
    private final String title;
    public ShipPartItem(Properties properties, String title) {
        super(properties);
        this.title = title;
    }
    @Override public Component getName(ItemStack stack) { return Component.literal(title); }
}
