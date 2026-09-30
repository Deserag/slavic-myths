package org.slavicmyths.rpg;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.ModBlocks;
public final class RpgMenu extends Container {
 public static final DeferredRegister<ContainerType<?>> MENUS=DeferredRegister.create(ForgeRegistries.CONTAINERS,"slavicmyths");
 public static final RegistryObject<ContainerType<RpgMenu>> TYPE=MENUS.register("rpg",()->IForgeContainerType.create((id,inv,b)->new RpgMenu(id,inv,b.readBlockPos(),b.readBoolean())));
 public final BlockPos pos;public final boolean anvil;public final Inventory input=new Inventory(3);
 public RpgMenu(int id,PlayerInventory inv,BlockPos pos,boolean anvil){
  super(TYPE.get(),id);this.pos=pos;this.anvil=anvil;
  if(anvil){for(int i=0;i<3;i++){final int index=i;addSlot(new Slot(input,i,54+i*70,48){@Override public int getMaxStackSize(){return index==0?1:64;}});}
   for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,54+col*18,154+row*18));
   for(int col=0;col<9;col++)addSlot(new Slot(inv,col,54+col*18,212));}
 }
 @Override public boolean stillValid(PlayerEntity p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level.getBlockState(pos).getBlock()==(anvil?ModBlocks.RUNIC_ANVIL.get():ModBlocks.PATH_STONE.get());}
 @Override public ItemStack quickMoveStack(PlayerEntity p,int i){return ItemStack.EMPTY;}
 @Override public void removed(PlayerEntity p){super.removed(p);if(!p.level.isClientSide)clearContainer(p,p.level,input);}
 @Override public boolean clickMenuButton(PlayerEntity p,int action){
  if(!(p instanceof ServerPlayerEntity)||!stillValid(p))return false;
  if(anvil){if(action>=0&&action<=4)Runes.operation((ServerPlayerEntity)p,input,action);}
  else if(action>=0&&action<4)PathData.choose((ServerPlayerEntity)p,action);
  else if(action>=10&&action<34)PathData.buy((ServerPlayerEntity)p,action-10);
  else if(action>=50&&action<74&&PathData.has(p,PathData.SKILLS[action-50])&&Abilities.active(action-50))PathData.data(p).putInt("Active",action-50+1);
  broadcastChanges();RpgNetwork.sync((ServerPlayerEntity)p);return true;
 }
}
