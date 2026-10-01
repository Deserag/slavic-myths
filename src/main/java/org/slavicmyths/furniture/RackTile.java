package org.slavicmyths.furniture;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
public final class RackTile extends TileEntity {
 public ItemStack weapon=ItemStack.EMPTY;
 public RackTile(){super(Furniture.RACK.get());}
 public static boolean accepts(ItemStack s){Item i=s.getItem();return i instanceof TieredItem||i instanceof ShootableItem||i instanceof ShieldItem||i instanceof TridentItem||i==org.slavicmyths.registry.ModItems.VELES_STAFF.get()||i==org.slavicmyths.registry.ModItems.POOL_SPEAR.get();}
 public void interact(PlayerEntity p,Hand hand){
  if(!weapon.isEmpty()){ItemStack returned=weapon;weapon=ItemStack.EMPTY;if(!p.addItem(returned))p.drop(returned,false);}
  else {ItemStack held=p.getItemInHand(hand);if(!accepts(held))return;weapon=held.copy();weapon.setCount(1);held.shrink(1);}
  setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
 }
 @Override public CompoundNBT save(CompoundNBT n){super.save(n);n.put("Weapon",weapon.save(new CompoundNBT()));return n;}
 @Override public void load(BlockState s,CompoundNBT n){super.load(s,n);weapon=ItemStack.of(n.getCompound("Weapon"));if(!weapon.isEmpty())weapon.setCount(1);}
 @Override public CompoundNBT getUpdateTag(){return save(new CompoundNBT());}
 @Override public SUpdateTileEntityPacket getUpdatePacket(){return new SUpdateTileEntityPacket(worldPosition,0,getUpdateTag());}
 @Override public void onDataPacket(NetworkManager net,SUpdateTileEntityPacket packet){load(getBlockState(),packet.getTag());}
}
