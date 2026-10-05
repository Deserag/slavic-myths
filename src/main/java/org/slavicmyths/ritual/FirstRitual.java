package org.slavicmyths.ritual;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.registry.ModItems;

/** Ordered definition, independent from block interaction and persistence. */
public final class FirstRitual {
    public static final int STEPS = 3;
    public static Item ingredient(int step) {
        switch (step) {
            case 0: return ModItems.WORMWOOD.get();
            case 1: return ModItems.BIRCH_BARK_SCROLL.get();
            default: return ModItems.WARDING_CHARM.get();
        }
    }
    public static ItemStack result() { return new ItemStack(ModItems.ANCIENT_SIGN.get()); }
    private FirstRitual() { }
}
