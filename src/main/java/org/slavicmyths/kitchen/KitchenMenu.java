package org.slavicmyths.kitchen;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.rpg.RpgMenu;
public final class KitchenMenu extends Container {
 public static final RegistryObject<ContainerType<KitchenMenu>> TYPE=RpgMenu.MENUS.register("kitchen",()->IForgeContainerType.create((id,inv,b)->new KitchenMenu(id,inv,b.readBlockPos())));
 public static void register(){}
 public final BlockPos pos;public final Inventory input=new Inventory(5);
 public KitchenMenu(int id,PlayerInventory inv,BlockPos pos){super(TYPE.get(),id);this.pos=pos;
  for(int i=0;i<4;i++)addSlot(new Slot(input,i,18+i*34,30));
  addSlot(new Slot(input,4,204,30){@Override public boolean mayPlace(ItemStack s){return false;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,39+col*18,150+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inv,col,39+col*18,208));
 }
 @Override public boolean stillValid(PlayerEntity p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level.getBlockState(pos).getBlock()==ModBlocks.KITCHEN_TABLE.get();}
 @Override public ItemStack quickMoveStack(PlayerEntity p,int index){Slot s=slots.get(index);if(!s.hasItem())return ItemStack.EMPTY;ItemStack stack=s.getItem(),copy=stack.copy();if(index<5){if(!moveItemStackTo(stack,5,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,4,false))return ItemStack.EMPTY;if(stack.isEmpty())s.set(ItemStack.EMPTY);else s.setChanged();s.onTake(p,stack);return copy;}
 @Override public void removed(PlayerEntity p){super.removed(p);if(!p.level.isClientSide)clearContainer(p,p.level,input);}
 @Override public boolean clickMenuButton(PlayerEntity p,int i){if(p.level.isClientSide||!stillValid(p)||i<0||i>=KitchenRecipes.COUNT)return false;
  Item[] ingredients=KitchenRecipes.ingredients(i);for(int n=0;n<3;n++)if(input.getItem(n).getItem()!=ingredients[n]||input.getItem(n).getCount()<KitchenRecipes.count(i,n))return false;
  ItemStack tool=input.getItem(3),out=input.getItem(4),result=new ItemStack(KitchenRecipes.output(i),KitchenRecipes.amount(i));
  if(tool.getItem()!=KitchenRecipes.tool(i)||!out.isEmpty()&&(!ItemStack.isSame(out,result)||out.hasTag()||out.getCount()+result.getCount()>out.getMaxStackSize()))return false;
  for(int n=0;n<3;n++){ItemStack used=input.getItem(n);ItemStack remainder=used.getContainerItem();used.shrink(KitchenRecipes.count(i,n));if(!remainder.isEmpty()){if(used.isEmpty())input.setItem(n,remainder);else if(!p.inventory.add(remainder))p.drop(remainder,false);}}
  if(tool.hurt(1,p.getRandom(),p instanceof ServerPlayerEntity?(ServerPlayerEntity)p:null)){tool.shrink(1);tool.setDamageValue(0);}
  if(out.isEmpty())input.setItem(4,result);else out.grow(result.getCount());input.setChanged();broadcastChanges();org.slavicmyths.progression.Knowledge.award(p,"kitchen_meal");return true;
 }
}
