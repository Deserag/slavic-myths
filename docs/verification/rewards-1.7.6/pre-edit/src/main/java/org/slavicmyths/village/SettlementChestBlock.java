package org.slavicmyths.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class SettlementChestBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<SettlementChestBlock> CODEC=simpleCodec(p->new SettlementChestBlock());
    public SettlementChestBlock(){super(Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return Block.box(1,0,1,15,15,15);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SettlementChest(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof SettlementChest tile)p.openMenu(tile,pos);return InteractionResult.sidedSuccess(w.isClientSide);}
    @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
    @Override protected int getAnalogOutputSignal(BlockState s,Level w,BlockPos p){return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(w.getBlockEntity(p));}
    @Override protected void onRemove(BlockState old,Level w,BlockPos p,BlockState next,boolean moving){if(!old.is(next.getBlock())&&w.getBlockEntity(p) instanceof SettlementChest tile){Containers.dropContents(w,p,tile);w.updateNeighbourForOutputSignal(p,this);}super.onRemove(old,w,p,next,moving);}
}
