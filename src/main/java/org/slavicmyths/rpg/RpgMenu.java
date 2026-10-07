package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.ModBlocks;
public final class RpgMenu extends AbstractContainerMenu {
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"slavicmyths");
 public static final DeferredHolder<MenuType<?>,MenuType<RpgMenu>> TYPE=MENUS.register("rpg",()->IMenuTypeExtension.create((id,inv,b)->new RpgMenu(id,inv,b.readBlockPos(),b.readBoolean())));
 public final BlockPos pos;public final boolean anvil;public final SimpleContainer input=new SimpleContainer(2);
 public int mode;
 public RpgMenu(int id,Inventory inv,BlockPos pos,boolean anvil){
  super(TYPE.get(),id);this.pos=pos;this.anvil=anvil;
  if(anvil){
   addSlot(new Slot(input,0,70,53){@Override public int getMaxStackSize(){return 1;}@Override public boolean mayPlace(ItemStack s){return Runes.max(s)>0;}});
   addSlot(new Slot(input,1,170,53){@Override public boolean isActive(){return mode!=2;}@Override public boolean mayPlace(ItemStack s){return mode==0&&(s.is(net.minecraft.world.item.Items.IRON_INGOT)||s.is(org.slavicmyths.registry.ModItems.SILVER_INGOT.get())||s.is(org.slavicmyths.registry.ModItems.PERUNITE.get()))||mode==1&&!Runes.rune(s).isEmpty();}});
   for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,98+col*18,244+row*18));
   for(int col=0;col<9;col++)addSlot(new Slot(inv,col,98+col*18,302));}

 }
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).getBlock()==(anvil?ModBlocks.RUNIC_ANVIL.get():ModBlocks.PATH_STONE.get());}
 @Override public ItemStack quickMoveStack(Player p,int i){
  if(!anvil||i<0||i>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;
  ItemStack stack=slot.getItem(),copy=stack.copy();
  if(i<2){if(!moveItemStackTo(stack,2,slots.size(),true))return ItemStack.EMPTY;}
  else if(Runes.max(stack)>0){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
  else if(!slots.get(1).mayPlace(stack)||!moveItemStackTo(stack,1,2,false))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
 }
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide)clearContainer(p,input);}
 @Override public boolean clickMenuButton(Player p,int action){
  if(!(p instanceof ServerPlayer)||!stillValid(p))return false;
  if(anvil){if(action>=100&&action<=102){mode=action-100;}else if(action>=0&&action<=4)Runes.operation((ServerPlayer)p,input,action);}
  else if(action>=0&&action<4)PathData.choose((ServerPlayer)p,action);
  else if(action>=10&&action<34)PathData.buy((ServerPlayer)p,action-10);
  else if(action>=50&&action<74&&PathData.has(p,PathData.SKILLS[action-50])&&Abilities.active(action-50))PathData.data(p).putInt("Active",action-50+1);
  broadcastChanges();RpgNetwork.sync((ServerPlayer)p);return true;
 }
}
