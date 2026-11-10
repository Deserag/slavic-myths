package org.slavicmyths.posad.boss;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Two-block-long animated jaw trophy; one block entity footprint, no chunk loads. */
public final class BaukHeadBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BaukHeadBlock> CODEC = simpleCodec(BaukHeadBlock::new);
    public static final BooleanProperty JAW_OPEN = BooleanProperty.create("jaw_open");
    public BaukHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(JAW_OPEN, false));
    }
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, JAW_OPEN); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos front = context.getClickedPos().relative(facing);
        if (!context.getLevel().hasChunkAt(front) || !context.getLevel().getBlockState(front).canBeReplaced()) return null;
        BlockPos rear = context.getClickedPos().relative(facing.getOpposite());
        if (!context.getLevel().hasChunkAt(rear) || !context.getLevel().getBlockState(rear).canBeReplaced()) return null;
        return defaultBlockState().setValue(FACING, facing);
    }
    @Override protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, world, pos, oldState, moved);
        if (!world.isClientSide) world.scheduleTick(pos, this, 45);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(JAW_OPEN, !state.getValue(JAW_OPEN)), 3);
        level.scheduleTick(pos, this, 35 + random.nextInt(35));
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Block.box(0, 0, -16, 16, 16, 16);
        return switch (state.getValue(FACING)) {
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 32);
            case EAST -> Block.box(0, 0, 0, 32, 16, 16);
            case WEST -> Block.box(-16, 0, 0, 16, 16, 16);
            default -> shape;
        };
    }
}
