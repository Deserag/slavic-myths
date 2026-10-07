package org.slavicmyths.kitchen;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class TableTile extends BlockEntity {
 public final NonNullList<ItemStack> food=NonNullList.withSize(4,ItemStack.EMPTY);
 public TableTile(BlockPos p,BlockState s){super(KitchenII.TABLE.get(),p,s);}
 public void interact(Player player,InteractionHand hand,BlockHitResult hit){
  if(player.isSpectator())return;ItemStack held=player.getItemInHand(hand);boolean place=held.is(KitchenII.tag("placeable_table_foods"));if(!place&&!held.isEmpty())return;
  int best=-1;double distance=Double.MAX_VALUE;
  for(int i=0;i<4;i++){if(place?!food.get(i).isEmpty():food.get(i).isEmpty())continue;double x=worldPosition.getX()+(i%2==0?.28:.72),z=worldPosition.getZ()+(i/2==0?.28:.72);double d=Math.pow(hit.getLocation().x-x,2)+Math.pow(hit.getLocation().z-z,2);if(d<distance){distance=d;best=i;}}
  if(best<0||level.isClientSide)return;
  if(place){food.set(best,held.copyWithCount(1));if(!player.getAbilities().instabuild)held.shrink(1);}else if(player.isShiftKeyDown()){
   ItemStack meal=food.get(best);var props=meal.get(DataComponents.FOOD);if(props==null||!player.canEat(props.canAlwaysEat()))return;
   food.set(best,ItemStack.EMPTY);ItemStack rest=meal.finishUsingItem(level,player);KitchenTile.give(player,rest);
  }else {ItemStack meal=food.get(best);food.set(best,ItemStack.EMPTY);KitchenTile.give(player,meal);}
  setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);
 }
 public void drop(){for(int i=0;i<4;i++){ItemStack s=food.get(i);food.set(i,ItemStack.EMPTY);if(!s.isEmpty())net.minecraft.world.level.block.Block.popResource(level,worldPosition,s);}}
 protected void saveAdditional(CompoundTag n,HolderLookup.Provider p){super.saveAdditional(n,p);ContainerHelper.saveAllItems(n,food,p);}
 protected void loadAdditional(CompoundTag n,HolderLookup.Provider p){super.loadAdditional(n,p);food.clear();ContainerHelper.loadAllItems(n,food,p);}
 public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
