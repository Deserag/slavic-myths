package org.slavicmyths.infernalfurnace;
import net.minecraft.core.BlockPos;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Inventory;import net.minecraft.world.inventory.AbstractContainerMenu;import net.minecraft.world.item.ItemStack;import net.minecraft.world.item.crafting.RecipeType;import net.minecraft.world.level.Level;import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;import net.minecraft.world.level.block.state.BlockState;
/** Vanilla owns all inventories, recipe accounting/XP, NBT and progress. No duplicate smelting engine. */
public final class InfernalFurnaceTile extends AbstractFurnaceBlockEntity {
 public InfernalFurnaceTile(BlockPos p,BlockState s){super(InfernalFurnace.TILE.get(),p,s,RecipeType.SMELTING);}
 @Override protected Component getDefaultName(){return Component.translatable("block.slavicmyths.infernal_furnace");}
 @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new InfernalFurnaceMenu(id,inv,this,dataAccess);}
 @Override protected int getBurnDuration(ItemStack s){return InfernalFurnace.fuel(s)?InfernalFurnace.FUEL_INTERNAL_TICKS:0;}
 @Override public boolean canPlaceItem(int slot,ItemStack s){return slot==1?InfernalFurnace.fuel(s):super.canPlaceItem(slot,s);}
 public static void tick(Level w,BlockPos p,BlockState s,InfernalFurnaceTile tile){
  // Reject injected/legacy foreign fuel even when a third-party writer bypassed mayPlace.
  // It remains removable, never consumed as fuel. Native tick decrements any existing burn normally.
  for(int i=0;i<InfernalFurnace.TICKS_PER_TICK;i++)AbstractFurnaceBlockEntity.serverTick(w,p,w.getBlockState(p),tile);
 }
}
