package org.slavicmyths.husbandry;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** No ticking: inventory changes synchronise the visible count immediately. */
public final class YardStorage extends BlockEntity {
    private final boolean nest;
    private final NonNullList<ItemStack> items;
    public YardStorage(BlockPos p,BlockState s){super(((YardStorageBlock)s.getBlock()).nest?Husbandry.NEST_TILE.get():Husbandry.FEEDER_TILE.get(),p,s);nest=((YardStorageBlock)s.getBlock()).nest;items=NonNullList.withSize(nest?3:1,ItemStack.EMPTY);}
    public boolean accepts(ItemStack s){return nest?s.is(Husbandry.GOOSE_EGG.get())||s.is(Husbandry.DUCK_EGG.get())||s.is(Items.EGG):s.is(Husbandry.feed("all_valid"));}
    public ItemStack food(){return nest?ItemStack.EMPTY:items.getFirst().copy();}
    public int count(){return items.stream().mapToInt(ItemStack::getCount).sum();}
    public boolean hasRoom(){return count()<(nest?3:16);}
    public int insert(ItemStack s,int requested){
        if(!accepts(s))return 0;int n=Math.min(Math.min(requested,s.getCount()),(nest?3:16)-count());
        if(n<=0)return 0;
        if(nest){int left=n;for(int i=0;i<3&&left>0;i++)if(items.get(i).isEmpty()){items.set(i,s.copyWithCount(1));left--;}}
        else {ItemStack existing=items.getFirst();if(!existing.isEmpty()&&!ItemStack.isSameItemSameComponents(existing,s))return 0;items.set(0,s.copyWithCount(count()+n));}
        changed();return n;
    }
    public List<ItemStack> extract(boolean all){
        List<ItemStack> out=new ArrayList<>();
        for(int i=0;i<items.size();i++){ItemStack s=items.get(i);if(!s.isEmpty()){out.add(s.split(all?s.getCount():1));if(!all)break;}}
        changed();return out;
    }
    private void changed(){setChanged();syncState();}
    private void syncState(){if(level!=null&&!level.isClientSide){int c=count();var property=nest?YardStorageBlock.EGG_COUNT:YardStorageBlock.FILL_LEVEL;int visual=nest?c:c==0?0:c<=5?1:c<=10?2:3;BlockState state=level.getBlockState(worldPosition);if(state.getBlock() instanceof YardStorageBlock&&state.getValue(property)!=visual)level.setBlock(worldPosition,state.setValue(property,visual),3);}}
    @Override public void onLoad(){super.onLoad();syncState();}
    @Override protected void saveAdditional(CompoundTag n,HolderLookup.Provider r){super.saveAdditional(n,r);ContainerHelper.saveAllItems(n,items,r);}
    @Override protected void loadAdditional(CompoundTag n,HolderLookup.Provider r){super.loadAdditional(n,r);items.clear();ContainerHelper.loadAllItems(n,items,r);for(int i=0;i<items.size();i++){ItemStack s=items.get(i);if(!s.isEmpty()){if(!accepts(s))items.set(i,ItemStack.EMPTY);else s.setCount(Math.min(s.getCount(),nest?1:16));}}}
}
