package org.slavicmyths.armorer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.rpg.RpgMenu;
public final class ArmorerMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<ArmorerMenu>> TYPE=RpgMenu.MENUS.register("armorer",()->IMenuTypeExtension.create((id,inv,b)->new ArmorerMenu(id,inv,b.readBlockPos())));
    public static void register(){}
    public final TransientCraftingContainer input=new TransientCraftingContainer(this,4,4);
    private final ResultContainer result=new ResultContainer();private final Player owner;public final BlockPos pos;private boolean consuming;
    public ArmorerMenu(int id,Inventory inventory,BlockPos pos) {
        super(TYPE.get(),id);this.owner=inventory.player;this.pos=pos;
        addSlot(new Slot(result,0,142,48){
            public boolean mayPlace(ItemStack stack){return false;}
            public boolean mayPickup(Player player){if(!stillValid(player))return false;if(owner.level().isClientSide)return hasItem();ArmorerRecipe r=recipe();return r!=null&&ItemStack.matches(r.assemble(recipeInput(),owner.registryAccess()),getItem());}
            public void onTake(Player player,ItemStack stack){craft(stack);}
        });
        for(int y=0;y<4;y++)for(int x=0;x<4;x++)addSlot(new Slot(input,x+y*4,26+x*18,21+y*18));
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inventory,x+y*9+9,8+x*18,111+y*18));
        for(int x=0;x<9;x++)addSlot(new Slot(inventory,x,8+x*18,169));
    }
    private ArmorerInput recipeInput(){return new ArmorerInput(4,4,input.getItems());}
    private RecipeHolder<ArmorerRecipe> recipeHolder(){return owner.level().getRecipeManager().getRecipeFor(ArmorerRecipe.TYPE.get(),recipeInput(),owner.level()).orElse(null);}
    private ArmorerRecipe recipe(){var holder=recipeHolder();return holder==null?null:holder.value();}
    @Override public void slotsChanged(Container inventory) {
        if(consuming||owner.level().isClientSide)return;ArmorerRecipe r=recipe();result.setItem(0,r==null?ItemStack.EMPTY:r.assemble(recipeInput(),owner.registryAccess()));broadcastChanges();
    }
    private void craft(ItemStack crafted) {
        if(owner.level().isClientSide)return;RecipeHolder<ArmorerRecipe> holder=recipeHolder();ArmorerRecipe r=holder==null?null:holder.value();if(r==null||!stillValid(owner)){slotsChanged(input);return;}
        NonNullList<ItemStack> remainder=r.getRemainingItems(recipeInput());consuming=true;
        for(int i=0;i<16;i++) {
            if(!input.getItem(i).isEmpty())input.removeItem(i,1);
            ItemStack left=remainder.get(i);
            if(!left.isEmpty()) {if(input.getItem(i).isEmpty())input.setItem(i,left);else if(!owner.getInventory().add(left))owner.drop(left,false);}
        }
        consuming=false;crafted.onCraftedBy(owner.level(),owner,crafted.getCount());owner.awardRecipes(java.util.Collections.singleton(holder));
        org.slavicmyths.progression.Knowledge.award(owner,"armorer_craft");slotsChanged(input);
    }
    @Override public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).is(ModBlocks.ARMORER_TABLE.get());}
    @Override public boolean canTakeItemForPickAll(ItemStack s,Slot slot){return slot.container!=result&&super.canTakeItemForPickAll(s,slot);}
    @Override public ItemStack quickMoveStack(Player p,int index) {
        if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index==0) {
            int room=0;for(int i=17;i<53;i++){ItemStack s=slots.get(i).getItem();if(s.isEmpty())room+=copy.getMaxStackSize();else if(ItemStack.isSameItemSameComponents(s,copy))room+=s.getMaxStackSize()-s.getCount();}
            if(room<copy.getCount()||!moveItemStackTo(stack,17,53,true))return ItemStack.EMPTY;
            slot.onTake(p,copy);return copy;
        }
        if(!moveItemStackTo(stack,index<17?17:1,index<17?53:17,index<17))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
    }
    @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide){consuming=true;clearContainer(p,input);result.clearContent();}}
}
