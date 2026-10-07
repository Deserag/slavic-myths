package org.slavicmyths.kitchen;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
public record KitchenInput(List<ItemStack> stacks) implements RecipeInput {
 public int size(){return stacks.size();}public ItemStack getItem(int i){return stacks.get(i);}
}
