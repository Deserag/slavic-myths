package org.slavicmyths.furniture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
public final class RackTile extends BlockEntity {
 public ItemStack weapon=ItemStack.EMPTY;
 public RackTile(net.minecraft.core.BlockPos pos,BlockState state){super(Furniture.RACK.get(),pos,state);}
 public static boolean accepts(ItemStack s){Item i=s.getItem();return i instanceof TieredItem||i instanceof ProjectileWeaponItem||i instanceof ShieldItem||i instanceof TridentItem||i==org.slavicmyths.registry.ModItems.VELES_STAFF.get()||i==org.slavicmyths.registry.ModItems.POOL_SPEAR.get();}
 public void interact(Player p,InteractionHand hand){
  if(!weapon.isEmpty()){ItemStack returned=weapon;weapon=ItemStack.EMPTY;if(!p.addItem(returned))p.drop(returned,false);}
  else {ItemStack held=p.getItemInHand(hand);if(!accepts(held))return;weapon=held.copy();weapon.setCount(1);held.shrink(1);}
  setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
 }
 @Override protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);n.put("Weapon",weapon.saveOptional(registries));}
 @Override protected void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);weapon=ItemStack.parseOptional(registries,n.getCompound("Weapon"));if(!weapon.isEmpty())weapon.setCount(1);}
 @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }
