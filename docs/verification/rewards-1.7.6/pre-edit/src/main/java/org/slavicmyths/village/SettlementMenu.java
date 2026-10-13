package org.slavicmyths.village;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;

public final class SettlementMenu extends AbstractContainerMenu {
    // 16 x 4 fits Minecraft's minimum 320 x 240 scaled viewport with the player inventory.
    public static final int COLUMNS=16;
    public static final DeferredHolder<MenuType<?>,MenuType<SettlementMenu>> TYPE=RpgMenu.MENUS.register("settlement_chest",()->IMenuTypeExtension.create((id,inv,b)->new SettlementMenu(id,inv,b.readBlockPos(),new SimpleContainer(WorkLimits.STORAGE_SLOTS))));
    public static void register(){}
    public final Container storage;public final BlockPos pos;
    public static int rows(){return (WorkLimits.STORAGE_SLOTS+COLUMNS-1)/COLUMNS;}
    public SettlementMenu(int id,Inventory inv,BlockPos pos,Container storage){super(TYPE.get(),id);this.storage=storage;this.pos=pos;
        for(int i=0;i<storage.getContainerSize();i++)addSlot(new Slot(storage,i,8+i%COLUMNS*18,20+i/COLUMNS*18));
        int y=rows()*18+42;
        int playerX=(COLUMNS*18+16-162)/2;
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,9+r*9+c,playerX+c*18,y+r*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,playerX+c*18,y+58));
    }
    public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&p.level().getBlockState(pos).is(SettlementStorage.BLOCK.get());}
    public ItemStack quickMoveStack(Player p,int index){Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();int n=storage.getContainerSize();if(!(index<n?moveItemStackTo(s,n,slots.size(),true):moveItemStackTo(s,0,n,false)))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
