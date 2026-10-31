package org.slavicmyths.furniture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WardrobeTile extends BlockEntity implements Container, MenuProvider {
 private final NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
 public WardrobeTile(BlockPos p,BlockState s){super(Furniture.WARDROBE.get(),p,s);}
 @Override public int getContainerSize(){return items.size();}
 @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
 @Override public ItemStack getItem(int slot){return items.get(slot);}
 @Override public ItemStack removeItem(int slot,int count){ItemStack result=ContainerHelper.removeItem(items,slot,count);if(!result.isEmpty())setChanged();return result;}
 @Override public ItemStack removeItemNoUpdate(int slot){ItemStack result=ContainerHelper.takeItem(items,slot);if(!result.isEmpty())setChanged();return result;}
 @Override public void setItem(int slot,ItemStack stack){items.set(slot,stack);stack.limitSize(getMaxStackSize(stack));setChanged();}
 @Override public void clearContent(){items.clear();setChanged();}
 @Override public boolean stillValid(Player player){return Container.stillValidBlockEntity(this,player);}
 @Override public Component getDisplayName(){return Component.translatable("container.slavicmyths.wardrobe");}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return ChestMenu.threeRows(id,inventory,this);}
 @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);ContainerHelper.saveAllItems(tag,items,lookup);}
 @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);items.clear();ContainerHelper.loadAllItems(tag,items,lookup);}
}
