package org.slavicmyths.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;

/** Five real produce slots; server Slot policy also governs quick move. */
public final class SackMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<SackMenu>> TYPE=RpgMenu.MENUS.register("storage_sack",()->IMenuTypeExtension.create((id,inv,b)->new SackMenu(id,inv,b.readBlockPos(),new SimpleContainer(5))));
 public static void register(){}
 public final Container storage;public final BlockPos pos;
 public SackMenu(int id,Inventory inv,BlockPos pos,Container storage){super(TYPE.get(),id);this.storage=storage;this.pos=pos;
  for(int i=0;i<5;i++)addSlot(new Slot(storage,i,44+i*18,22){public boolean mayPlace(ItemStack s){return storage instanceof StorageTile t?t.canPlaceItem(getContainerSlot(),s):s.is(org.slavicmyths.kitchen.KitchenII.tag("storage/sack_items"));}});
  for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,9+r*9+c,8+c*18,61+r*18));
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,119));
 }
 public boolean stillValid(Player p){return storage instanceof StorageTile t?t.stillValid(p):p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&p.level().getBlockState(pos).is(Household.BLOCKS.get(Household.Kind.SACK).get());}
 public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(!(index<5?moveItemStackTo(stack,5,slots.size(),true):moveItemStackTo(stack,0,5,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
}
