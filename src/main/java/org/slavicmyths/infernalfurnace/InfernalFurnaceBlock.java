package org.slavicmyths.infernalfurnace;
import com.mojang.serialization.MapCodec;import net.minecraft.core.BlockPos;import net.minecraft.world.level.Level;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.entity.player.Player;
public final class InfernalFurnaceBlock extends FurnaceBlock {
 public static final MapCodec<FurnaceBlock> CODEC=simpleCodec(InfernalFurnaceBlock::new);
 public InfernalFurnaceBlock(BlockBehaviour.Properties p){super(p);}
 @Override public MapCodec<FurnaceBlock> codec(){return CODEC;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new InfernalFurnaceTile(p,s);}
 @Override protected void openContainer(Level w,BlockPos p,Player player){if(w.getBlockEntity(p) instanceof InfernalFurnaceTile tile){player.openMenu(tile);player.awardStat(net.minecraft.stats.Stats.INTERACT_WITH_FURNACE);}}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> type){return w.isClientSide?null:createTickerHelper(type,InfernalFurnace.TILE.get(),InfernalFurnaceTile::tick);}
}
