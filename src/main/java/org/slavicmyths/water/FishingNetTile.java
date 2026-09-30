package org.slavicmyths.water;
import java.util.UUID;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.block.BlockState;
import org.slavicmyths.registry.*;
public final class FishingNetTile extends TileEntity {
 public final NonNullList<ItemStack> catchItems=NonNullList.withSize(4,ItemStack.EMPTY);public int wear;public UUID owner;
 public FishingNetTile(){super(ModTiles.FISHING_NET.get());}
 public CompoundNBT save(CompoundNBT n){super.save(n);ItemStackHelper.saveAllItems(n,catchItems);n.putInt("Wear",wear);if(owner!=null)n.putUUID("Owner",owner);return n;}
 public void load(BlockState s,CompoundNBT n){super.load(s,n);catchItems.clear();ItemStackHelper.loadAllItems(n,catchItems);wear=Math.max(0,Math.min(32,n.getInt("Wear")));owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)level.getBlockTicks().scheduleTick(worldPosition,ModBlocks.FISHING_NET.get(),6000);}
}
