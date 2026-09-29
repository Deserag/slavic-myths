package org.slavicmyths.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.slavicmyths.SlavicMyths;

public final class ModItemGroup {
    public static final ItemGroup TAB = new ItemGroup(SlavicMyths.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.BIRCH_BARK_SCROLL.get());
        }
    };

    private ModItemGroup() { }
}
