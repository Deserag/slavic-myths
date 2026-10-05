package org.slavicmyths.kitchen;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.rpg.RpgMenu;
public final class KitchenMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<KitchenMenu>> TYPE=RpgMenu.MENUS.register("kitchen",()->IMenuTypeExtension.create((id,inv,b)->new KitchenMenu(id,inv,b.readBlockPos())));
 public static void register(){}
 public final BlockPos pos;public final SimpleContainer input=new SimpleContainer(5);
 public KitchenMenu(int id,Inventory inv,BlockPos pos){super(TYPE.get(),id);this.pos=pos;
  for(int i=0;i<4;i++)addSlot(new Slot(input,i,18+i*34,30));
  addSlot(new Slot(input,4,204,30){@Override public boolean mayPlace(ItemStack s){return false;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,39+col*18,150+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inv,col,39+col*18,208));
 }
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).getBlock()==ModBlocks.KITCHEN_TABLE.get();}
 @Override public ItemStack quickMoveStack(Player p,int index){Slot s=slots.get(index);if(!s.hasItem())return ItemStack.EMPTY;ItemStack stack=s.getItem(),copy=stack.copy();if(index<5){if(!moveItemStackTo(stack,5,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,4,false))return ItemStack.EMPTY;if(stack.isEmpty())s.set(ItemStack.EMPTY);else s.setChanged();s.onTake(p,stack);return copy;}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide)clearContainer(p,input);}
 @Override public boolean clickMenuButton(Player p,int i){if(p.level().isClientSide||!stillValid(p)||i<0||i>=KitchenRecipes.COUNT)return false;
  Item[] ingredients=KitchenRecipes.ingredients(i);for(int n=0;n<3;n++)if(input.getItem(n).getItem()!=ingredients[n]||input.getItem(n).getCount()<KitchenRecipes.count(i,n))return false;
  ItemStack tool=input.getItem(3),out=input.getItem(4),result=new ItemStack(KitchenRecipes.output(i),KitchenRecipes.amount(i));
  if(tool.getItem()!=KitchenRecipes.tool(i)||!out.isEmpty()&&(!ItemStack.isSameItemSameComponents(out,result)||out.getCount()+result.getCount()>out.getMaxStackSize()))return false;
  for(int n=0;n<3;n++){ItemStack used=input.getItem(n);ItemStack remainder=used.getCraftingRemainingItem();used.shrink(KitchenRecipes.count(i,n));if(!remainder.isEmpty()){if(used.isEmpty())input.setItem(n,remainder);else if(!p.getInventory().add(remainder))p.drop(remainder,false);}}
  // The old low-level hurt() consumed internal tools even in creative mode.
  int before=tool.getDamageValue();ItemStack previous=tool.copy();
  tool.hurtAndBreak(1,(net.minecraft.server.level.ServerLevel)p.level(),(net.minecraft.world.entity.LivingEntity)null,broken->{});
  if(p instanceof ServerPlayer sp&&(tool.isEmpty()||tool.getDamageValue()!=before)){
   net.minecraft.advancements.CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(sp,previous,tool.isEmpty()?previous.getMaxDamage():tool.getDamageValue());
  }
  if(out.isEmpty())input.setItem(4,result);else out.grow(result.getCount());input.setChanged();broadcastChanges();org.slavicmyths.progression.Knowledge.award(p,"kitchen_meal");return true;
 }
}
