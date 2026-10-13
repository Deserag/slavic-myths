package org.slavicmyths.water;
import java.util.UUID;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.registry.*;
public final class FishingNetTile extends BlockEntity {
 public final NonNullList<ItemStack> catchItems=NonNullList.withSize(4,ItemStack.EMPTY);public int wear;public UUID owner;
 public FishingNetTile(net.minecraft.core.BlockPos pos,BlockState state){super(ModTiles.FISHING_NET.get(),pos,state);}
 protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);ContainerHelper.saveAllItems(n,catchItems,registries);n.putInt("Wear",wear);if(owner!=null)n.putUUID("Owner",owner);}
 protected void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);catchItems.clear();ContainerHelper.loadAllItems(n,catchItems,registries);wear=Math.max(0,Math.min(32,n.getInt("Wear")));owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)level.scheduleTick(worldPosition,ModBlocks.FISHING_NET.get(),6000);}
}
