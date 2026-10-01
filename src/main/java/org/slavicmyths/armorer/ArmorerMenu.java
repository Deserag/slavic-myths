package org.slavicmyths.armorer;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.rpg.RpgMenu;

public final class ArmorerMenu extends Container {
    public static final RegistryObject<ContainerType<ArmorerMenu>> TYPE=RpgMenu.MENUS.register("armorer",()->IForgeContainerType.create((id,inv,b)->new ArmorerMenu(id,inv,b.readBlockPos())));
    public static void register(){}
    public final CraftingInventory input=new CraftingInventory(this,4,4);
    private final CraftResultInventory result=new CraftResultInventory();private final PlayerEntity owner;public final BlockPos pos;private boolean consuming;
    public ArmorerMenu(int id,PlayerInventory inventory,BlockPos pos) {
        super(TYPE.get(),id);this.owner=inventory.player;this.pos=pos;
        addSlot(new Slot(result,0,142,48){
            public boolean mayPlace(ItemStack stack){return false;}
            public boolean mayPickup(PlayerEntity player){if(!stillValid(player))return false;if(owner.level.isClientSide)return hasItem();ArmorerRecipe r=recipe();return r!=null&&ItemStack.matches(r.assemble(input),getItem());}
            public ItemStack onTake(PlayerEntity player,ItemStack stack){craft(stack);return stack;}
        });
        for(int y=0;y<4;y++)for(int x=0;x<4;x++)addSlot(new Slot(input,x+y*4,26+x*18,21+y*18));
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inventory,x+y*9+9,8+x*18,111+y*18));
        for(int x=0;x<9;x++)addSlot(new Slot(inventory,x,8+x*18,169));
    }
    private ArmorerRecipe recipe(){return owner.level.getRecipeManager().getRecipeFor(ArmorerRecipe.TYPE,input,owner.level).orElse(null);}
    @Override public void slotsChanged(IInventory inventory) {
        if(consuming||owner.level.isClientSide)return;ArmorerRecipe r=recipe();result.setItem(0,r==null?ItemStack.EMPTY:r.assemble(input));broadcastChanges();
    }
    private void craft(ItemStack crafted) {
        if(owner.level.isClientSide)return;ArmorerRecipe r=recipe();if(r==null||!stillValid(owner)){slotsChanged(input);return;}
        NonNullList<ItemStack> remainder=r.getRemainingItems(input);consuming=true;
        for(int i=0;i<16;i++) {
            if(!input.getItem(i).isEmpty())input.removeItem(i,1);
            ItemStack left=remainder.get(i);
            if(!left.isEmpty()) {if(input.getItem(i).isEmpty())input.setItem(i,left);else if(!owner.inventory.add(left))owner.drop(left,false);}
        }
        consuming=false;crafted.onCraftedBy(owner.level,owner,crafted.getCount());owner.awardRecipes(java.util.Collections.singleton(r));
        org.slavicmyths.progression.Knowledge.award(owner,"armorer_craft");slotsChanged(input);
    }
    @Override public boolean stillValid(PlayerEntity p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level.getBlockState(pos).is(ModBlocks.ARMORER_TABLE.get());}
    @Override public boolean canTakeItemForPickAll(ItemStack s,Slot slot){return slot.container!=result&&super.canTakeItemForPickAll(s,slot);}
    @Override public ItemStack quickMoveStack(PlayerEntity p,int index) {
        if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index==0) {
            int room=0;for(int i=17;i<53;i++){ItemStack s=slots.get(i).getItem();if(s.isEmpty())room+=copy.getMaxStackSize();else if(ItemStack.isSame(s,copy)&&ItemStack.tagMatches(s,copy))room+=s.getMaxStackSize()-s.getCount();}
            if(room<copy.getCount()||!moveItemStackTo(stack,17,53,true))return ItemStack.EMPTY;
            slot.onTake(p,copy);return copy;
        }
        if(!moveItemStackTo(stack,index<17?17:1,index<17?53:17,index<17))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
    }
    @Override public void removed(PlayerEntity p){super.removed(p);if(!p.level.isClientSide){consuming=true;clearContainer(p,p.level,input);result.clearContent();}}
}
