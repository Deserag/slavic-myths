package org.slavicmyths.trophy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Invisible occupied cells above the 5 x 5 Nine-Mor trophy base.
 * One anchor owns all loot and all client rendering.
 */
public final class TrophyHeadReserveBlock extends Block {
    public static final MapCodec<TrophyHeadReserveBlock> CODEC = simpleCodec(TrophyHeadReserveBlock::new);
    public static final EnumProperty<TrophyHeadBlock.Cell> CELL = TrophyHeadBlock.CELL;
    public static final IntegerProperty Y_OFFSET = IntegerProperty.create("y_offset", 1, 5);
    private static final VoxelShape CELL_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public TrophyHeadReserveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CELL, TrophyHeadBlock.Cell.A).setValue(Y_OFFSET, 1));
    }

    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(CELL, Y_OFFSET); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return CELL_SHAPE; }

    private BlockPos anchor(BlockPos pos, BlockState state) {
        TrophyHeadBlock.Cell cell = state.getValue(CELL);
        return pos.offset(-cell.dx, -state.getValue(Y_OFFSET), -cell.dz);
    }

    @Override public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide && player.isCreative()) {
            BlockPos anchor = anchor(pos, state);
            if (world.hasChunkAt(anchor)) {
                BlockState core = world.getBlockState(anchor);
                if (core.is(TrophyHeads.BLOCKS.get(TrophyHeads.Kind.NINE_MOR_SERPENT).get()) && core.getValue(TrophyHeadBlock.CELL) == TrophyHeadBlock.Cell.A) world.destroyBlock(anchor, false);
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState next, boolean moving) {
        super.onRemove(state, world, pos, next, moving);
        if (world.isClientSide || next.is(this)) return;
        BlockPos anchor = anchor(pos, state);
        if (!world.hasChunkAt(anchor)) return;
        BlockState core = world.getBlockState(anchor);
        if (core.is(TrophyHeads.BLOCKS.get(TrophyHeads.Kind.NINE_MOR_SERPENT).get()) && core.getValue(TrophyHeadBlock.CELL) == TrophyHeadBlock.Cell.A) world.destroyBlock(anchor, true);
    }
}
