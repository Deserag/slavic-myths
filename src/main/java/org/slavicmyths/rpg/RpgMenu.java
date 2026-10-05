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
 public final BlockPos pos;public final boolean anvil;public final SimpleContainer input=new SimpleContainer(3);
 public RpgMenu(int id,Inventory inv,BlockPos pos,boolean anvil){
  super(TYPE.get(),id);this.pos=pos;this.anvil=anvil;
  if(anvil){for(int i=0;i<3;i++){final int index=i;addSlot(new Slot(input,i,54+i*70,48){@Override public int getMaxStackSize(){return index==0?1:64;}});}
   for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,54+col*18,154+row*18));
   for(int col=0;col<9;col++)addSlot(new Slot(inv,col,54+col*18,212));}
 }
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).getBlock()==(anvil?ModBlocks.RUNIC_ANVIL.get():ModBlocks.PATH_STONE.get());}
 @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide)clearContainer(p,input);}
 @Override public boolean clickMenuButton(Player p,int action){
  if(!(p instanceof ServerPlayer)||!stillValid(p))return false;
  if(anvil){if(action>=0&&action<=4)Runes.operation((ServerPlayer)p,input,action);}
  else if(action>=0&&action<4)PathData.choose((ServerPlayer)p,action);
  else if(action>=10&&action<34)PathData.buy((ServerPlayer)p,action-10);
  else if(action>=50&&action<74&&PathData.has(p,PathData.SKILLS[action-50])&&Abilities.active(action-50))PathData.data(p).putInt("Active",action-50+1);
  broadcastChanges();RpgNetwork.sync((ServerPlayer)p);return true;
 }
}
