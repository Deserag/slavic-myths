package org.slavicmyths.village;

import java.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Simulate on copies, then commit on the single server thread. Never silently overflow. */
public final class InventoryTransactions {
    public static List<ItemStack> snapshot(Container c){List<ItemStack> out=new ArrayList<>();for(int i=0;i<c.getContainerSize();i++)out.add(c.getItem(i).copy());return out;}
    public static boolean insert(List<ItemStack> slots,ItemStack incoming,int max){ItemStack left=incoming.copy();if(left.isEmpty())return true;
        for(int pass=0;pass<2;pass++)for(int i=0;i<slots.size();i++){ItemStack old=slots.get(i);if(pass==0?old.isEmpty()||!ItemStack.isSameItemSameComponents(old,left):!old.isEmpty())continue;int n=Math.min(left.getCount(),Math.min(max,left.getMaxStackSize())-old.getCount());if(n<=0)continue;if(old.isEmpty())slots.set(i,left.copyWithCount(n));else old.grow(n);left.shrink(n);if(left.isEmpty())return true;}return false;}
    public static boolean canInsert(Container c,ItemStack s){return insert(snapshot(c),s,c.getMaxStackSize());}
    public static void commit(Container c,List<ItemStack> copy){for(int i=0;i<copy.size();i++)c.setItem(i,copy.get(i));c.setChanged();}
    public static boolean transfer(Container from,int slot,Container to,int count){if(from==to)return false;ItemStack actual=from.getItem(slot);if(count<=0||actual.getCount()<count)return false;List<ItemStack> copy=snapshot(to);if(!insert(copy,actual.copyWithCount(count),to.getMaxStackSize()))return false;from.removeItem(slot,count);commit(to,copy);return true;}
    private InventoryTransactions(){}
}
