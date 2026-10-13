package org.slavicmyths.sanctuary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
public final class SanctuaryChestBlock extends ChestBlock {
 public SanctuaryChestBlock(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST),()->SanctuaryContent.CHEST.get());}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SanctuaryChest(p,s);}
}
