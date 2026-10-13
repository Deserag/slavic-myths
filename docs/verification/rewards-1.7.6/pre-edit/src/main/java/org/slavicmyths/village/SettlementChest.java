package org.slavicmyths.village;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Real inventory only. No ownership, remote access, ticking or virtual stock. */
public final class SettlementChest extends BlockEntity implements Container,MenuProvider {
    private final NonNullList<ItemStack> items=NonNullList.withSize(WorkLimits.STORAGE_SLOTS,ItemStack.EMPTY);
    public SettlementChest(BlockPos p,BlockState s){super(SettlementStorage.TILE.get(),p,s);}
    public int getContainerSize(){return items.size();}
    public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    public ItemStack getItem(int slot){return items.get(slot);}
    public ItemStack removeItem(int slot,int count){ItemStack s=ContainerHelper.removeItem(items,slot,count);if(!s.isEmpty())setChanged();return s;}
    public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(items,slot);}
    public void setItem(int slot,ItemStack s){items.set(slot,s);s.limitSize(getMaxStackSize(s));setChanged();}
    public void clearContent(){items.clear();setChanged();}
    public boolean stillValid(Player p){return Container.stillValidBlockEntity(this,p);}
    public int usedSlots(){return (int)items.stream().filter(s->!s.isEmpty()).count();}
    public int freeSlots(){return getContainerSize()-usedSlots();}
    public double fillRatio(){double n=0;for(ItemStack s:items)if(!s.isEmpty())n+=(double)s.getCount()/getMaxStackSize(s);return n/getContainerSize();}
    public boolean isFull(){return fillRatio()>=1;}
    public boolean hasSpace(ItemStack stack){return InventoryTransactions.canInsert(this,stack);}
    public Component getDisplayName(){return Component.translatable("block.slavicmyths.settlement_chest");}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SettlementMenu(id,inv,worldPosition,this);}
    @Override public void setChanged(){super.setChanged();if(level!=null&&!level.isClientSide)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);ContainerHelper.saveAllItems(tag,items,lookup);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);items.clear();ContainerHelper.loadAllItems(tag,items,lookup);}
}
