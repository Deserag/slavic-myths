package org.slavicmyths.armorer;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Keeps the complete grid: vanilla CraftingInput trims empty borders. */
public record ArmorerInput(int width, int height, List<ItemStack> items) implements RecipeInput {
    public ArmorerInput {
        if(width<1 || width>4 || height<1 || height>4 || items.size()!=width*height)
            throw new IllegalArgumentException("Invalid armorer grid");
        items=List.copyOf(items);
    }
    @Override public ItemStack getItem(int index) { return items.get(index); }
    @Override public int size() { return items.size(); }
}
