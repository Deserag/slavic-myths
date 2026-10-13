package org.slavicmyths.industry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;
public final class MachineMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<MachineMenu>> BLOOMERY=RpgMenu.MENUS.register("chud_bloomery",()->IMenuTypeExtension.create((id,inv,b)->new MachineMenu(id,inv,b.readBlockPos(),false))),HEARTH=RpgMenu.MENUS.register("refining_hearth",()->IMenuTypeExtension.create((id,inv,b)->new MachineMenu(id,inv,b.readBlockPos(),true)));
    public static void init(){}
    public final boolean refining;public final BlockPos pos;public final ContainerData data;private final Container inventory;private final int count;
    public MachineMenu(int id,Inventory inv,BlockPos pos,boolean refining){this(id,inv,new SimpleContainer(refining?4:3),new SimpleContainerData(4),pos,refining);}
    public MachineMenu(int id,Inventory inv,Container inventory,ContainerData data,BlockPos pos,boolean refining){
        super(refining?HEARTH.get():BLOOMERY.get(),id);this.refining=refining;this.pos=pos;this.inventory=inventory;this.data=data;count=refining?4:3;
        addSlot(new Slot(inventory,0,refining?35:56,17){public boolean mayPlace(ItemStack s){return refining?s.is(Items.IRON_INGOT)||s.is(Items.COPPER_INGOT):s.is(Industry.LUMP.get());}});
        if(refining)addSlot(new Slot(inventory,1,58,17){public boolean mayPlace(ItemStack s){return s.is(Industry.FLUX.get());}});
        addSlot(new Slot(inventory,refining?2:1,refining?46:56,53){public boolean mayPlace(ItemStack s){return s.getBurnTime(refining?MachineRecipe.HEARTH.get():MachineRecipe.BLOOMERY.get())>0;}});
        addSlot(new Slot(inventory,count-1,116,35){public boolean mayPlace(ItemStack s){return false;}public void onTake(Player p,ItemStack s){if(inventory instanceof MachineTile t)t.claimExperience(p);super.onTake(p,s);}});
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,84+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,142));addDataSlots(data);
    }
    public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&p.level().getBlockState(pos).is(refining?Industry.HEARTH.get():Industry.BLOOMERY.get());}
    public ItemStack quickMoveStack(Player p,int index){if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();
        if(index<count){if(!moveItemStackTo(stack,count,count+36,index==count-1))return ItemStack.EMPTY;}
        else if((refining?stack.is(Items.IRON_INGOT)||stack.is(Items.COPPER_INGOT):stack.is(Industry.LUMP.get()))){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
        else if(refining&&stack.is(Industry.FLUX.get())){if(!moveItemStackTo(stack,1,2,false))return ItemStack.EMPTY;}
        else if(stack.getBurnTime(refining?MachineRecipe.HEARTH.get():MachineRecipe.BLOOMERY.get())>0){int fuel=refining?2:1;if(!moveItemStackTo(stack,fuel,fuel+1,false))return ItemStack.EMPTY;}
        else if(index<count+27){if(!moveItemStackTo(stack,count+27,count+36,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,count,count+27,false))return ItemStack.EMPTY;
        if(stack.getCount()==original.getCount())return ItemStack.EMPTY;if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,original.copyWithCount(original.getCount()-stack.getCount()));return original;
    }
}
