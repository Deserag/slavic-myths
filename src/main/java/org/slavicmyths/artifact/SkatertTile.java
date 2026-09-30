package org.slavicmyths.artifact;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import org.slavicmyths.registry.*;
public final class SkatertTile extends TileEntity {
 public ItemStack cloth=new ItemStack(ModItems.SKATERT.get());public long expires;
 public SkatertTile(){super(ModTiles.SKATERT.get());}
 public CompoundNBT save(CompoundNBT n){super.save(n);n.put("Cloth",cloth.save(new CompoundNBT()));n.putLong("Expires",expires);return n;}
 public void load(BlockState s,CompoundNBT n){super.load(s,n);cloth=ItemStack.of(n.getCompound("Cloth"));expires=n.getLong("Expires");}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)level.getBlockTicks().scheduleTick(worldPosition,ModBlocks.SKATERT.get(),(int)Math.max(1,Math.min(2400,expires-ArtifactEvents.now(level))));}
}
