package org.slavicmyths.kurgan;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.rpg.RpgMenu;
public final class BurialCoffinMenu extends Container {
 public static final RegistryObject<ContainerType<BurialCoffinMenu>> TYPE=RpgMenu.MENUS.register("burial_coffin",()->IForgeContainerType.create((id,inv,b)->new BurialCoffinMenu(id,inv,new Inventory(5))));
 public final IInventory inventory;
 public BurialCoffinMenu(int id,PlayerInventory inv,IInventory contents){super(TYPE.get(),id);inventory=contents;checkContainerSize(contents,5);contents.startOpen(inv.player);for(int i=0;i<5;i++)addSlot(new Slot(contents,i,44+i*18,29));for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,72+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,130));}
 public static void register(){}
 @Override public boolean stillValid(PlayerEntity p){return inventory.stillValid(p);}
 @Override public void removed(PlayerEntity p){super.removed(p);inventory.stopOpen(p);}
 @Override public ItemStack quickMoveStack(PlayerEntity p,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(index<5?!moveItemStackTo(s,5,slots.size(),true):!moveItemStackTo(s,0,5,false))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
