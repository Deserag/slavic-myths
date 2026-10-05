package org.slavicmyths.artifact;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.registry.*;
public final class SkatertTile extends BlockEntity {
 public ItemStack cloth=new ItemStack(ModItems.SKATERT.get());public long expires;
 public SkatertTile(net.minecraft.core.BlockPos pos,BlockState state){super(ModTiles.SKATERT.get(),pos,state);}
 protected void saveAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(n,registries);n.put("Cloth",cloth.saveOptional(registries));n.putLong("Expires",expires);}
 protected void loadAdditional(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){super.loadAdditional(n,registries);cloth=ItemStack.parseOptional(registries,n.getCompound("Cloth"));expires=n.getLong("Expires");}
 public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)level.scheduleTick(worldPosition,ModBlocks.SKATERT.get(),(int)Math.max(1,Math.min(2400,expires-ArtifactEvents.now(level))));}
}
