package org.slavicmyths.infernalfurnace;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Inventory;import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;import net.minecraft.world.item.crafting.RecipeType;
public final class InfernalFurnaceMenu extends AbstractFurnaceMenu {
 public InfernalFurnaceMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(3),new SimpleContainerData(4));}
 public InfernalFurnaceMenu(int id,Inventory inv,Container items,ContainerData data){super(InfernalFurnace.MENU.get(),RecipeType.SMELTING,RecipeBookType.FURNACE,id,inv,items,data);
  // Vanilla FurnaceFuelSlot also accepts empty buckets; replace that exception explicitly.
  Slot fuel=new Slot(items,1,56,53){@Override public boolean mayPlace(ItemStack s){return InfernalFurnace.fuel(s);}};fuel.index=1;slots.set(1,fuel);
 }
 @Override protected boolean isFuel(ItemStack s){return InfernalFurnace.fuel(s);}
}
