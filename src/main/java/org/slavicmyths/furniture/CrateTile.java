package org.slavicmyths.furniture;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A local, persistent inventory shared by wooden crates and bedside cabinets. */
public final class CrateTile extends BlockEntity implements Container, MenuProvider {
 private final NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
 public CrateTile(BlockPos p,BlockState s){super(Furniture.CRATE.get(),p,s);}
 public int getContainerSize(){return items.size();}
 public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
 public ItemStack getItem(int slot){return items.get(slot);}
 public ItemStack removeItem(int slot,int count){ItemStack s=ContainerHelper.removeItem(items,slot,count);if(!s.isEmpty())setChanged();return s;}
 public ItemStack removeItemNoUpdate(int slot){ItemStack s=ContainerHelper.takeItem(items,slot);if(!s.isEmpty())setChanged();return s;}
 public void setItem(int slot,ItemStack s){items.set(slot,s);s.limitSize(getMaxStackSize(s));setChanged();}
 public void clearContent(){items.clear();setChanged();}
 public boolean stillValid(Player p){return Container.stillValidBlockEntity(this,p);}
 public Component getDisplayName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return ChestMenu.threeRows(id,inv,this);}
 @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);ContainerHelper.saveAllItems(tag,items,lookup);}
 @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);items.clear();ContainerHelper.loadAllItems(tag,items,lookup);}
}
