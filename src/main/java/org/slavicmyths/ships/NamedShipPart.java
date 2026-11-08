package org.slavicmyths.ships;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A Slavic shipyard part, with a human-readable Creative name before localization. */
public final class NamedShipPart extends Item {
    private final String name;
    public NamedShipPart(String name, Properties props) { super(props); this.name = name; }
    @Override public Component getName(ItemStack stack) { return Component.literal(name); }
}
