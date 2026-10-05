package org.slavicmyths.kurgan;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.rpg.RpgMenu;
public final class BurialCoffinMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<BurialCoffinMenu>> TYPE=RpgMenu.MENUS.register("burial_coffin",()->IMenuTypeExtension.create((id,inv,b)->new BurialCoffinMenu(id,inv,new SimpleContainer(5))));
 public final Container inventory;
 public BurialCoffinMenu(int id,Inventory inv,Container contents){super(TYPE.get(),id);inventory=contents;checkContainerSize(contents,5);contents.startOpen(inv.player);for(int i=0;i<5;i++)addSlot(new Slot(contents,i,44+i*18,29));for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,72+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,130));}
 public static void register(){}
 @Override public boolean stillValid(Player p){return inventory.stillValid(p);}
 @Override public void removed(Player p){super.removed(p);inventory.stopOpen(p);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(index<5?!moveItemStackTo(s,5,slots.size(),true):!moveItemStackTo(s,0,5,false))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
