package org.slavicmyths.posad.boss;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Two reserved cells, one visible model and one loot-bearing anchor.
 * The front cell prevents other blocks from occupying the skull's snout.
 */
public final class BaukHeadBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BaukHeadBlock> CODEC = simpleCodec(BaukHeadBlock::new);
    public static final BooleanProperty JAW_OPEN = BooleanProperty.create("jaw_open");
    public static final BooleanProperty FRONT = BooleanProperty.create("front");
    private static final VoxelShape PART_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BaukHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(JAW_OPEN, false).setValue(FRONT, false));
    }

    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, JAW_OPEN, FRONT); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos front = context.getClickedPos().relative(facing);
        var world = context.getLevel();
        if (!world.hasChunkAt(front) || front.getY() >= world.getMaxBuildHeight() || front.getY() < world.getMinBuildHeight() || !world.getWorldBorder().isWithinBounds(front) || !world.getBlockState(front).canBeReplaced(context)) return null;
        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(front, context.getClickedFace(), context.getItemInHand())) return null;
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClientSide && !state.getValue(FRONT)) {
            world.setBlock(pos.relative(state.getValue(FACING)), state.setValue(FRONT, true).setValue(JAW_OPEN, false), 3);
        }
    }

    @Override public RenderShape getRenderShape(BlockState state) { return state.getValue(FRONT) ? RenderShape.INVISIBLE : RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return PART_SHAPE; }

    @Override protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState previous, boolean moved) {
        super.onPlace(state, world, pos, previous, moved);
        if (!world.isClientSide && !state.getValue(FRONT) && !previous.is(this)) world.scheduleTick(pos, this, 45);
    }

    @Override protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(FRONT)) return;
        BlockPos front = pos.relative(state.getValue(FACING));
        BlockState other = world.getBlockState(front);
        if (!other.is(this) || !other.getValue(FRONT) || other.getValue(FACING) != state.getValue(FACING)) return;
        world.setBlock(pos, state.setValue(JAW_OPEN, !state.getValue(JAW_OPEN)), 3);
        world.scheduleTick(pos, this, 35 + random.nextInt(35));
    }

    @Override public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide && player.isCreative() && state.getValue(FRONT)) {
            BlockPos anchor = pos.relative(state.getValue(FACING).getOpposite());
            BlockState other = world.getBlockState(anchor);
            if (other.is(this) && !other.getValue(FRONT) && other.getValue(FACING) == state.getValue(FACING)) world.destroyBlock(anchor, false);
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState next, boolean moving) {
        super.onRemove(state, world, pos, next, moving);
        if (world.isClientSide || next.is(this)) return;
        Direction face = state.getValue(FACING);
        if (state.getValue(FRONT)) {
            BlockPos anchor = pos.relative(face.getOpposite());
            BlockState other = world.getBlockState(anchor);
            if (other.is(this) && !other.getValue(FRONT) && other.getValue(FACING) == face) world.destroyBlock(anchor, true);
        } else {
            BlockPos front = pos.relative(face);
            BlockState other = world.getBlockState(front);
            if (other.is(this) && other.getValue(FRONT) && other.getValue(FACING) == face) world.removeBlock(front, false);
        }
    }
}
