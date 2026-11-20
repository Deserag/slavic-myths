package org.slavicmyths.marine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One solid trophy pedestal; all skull details are a real 3D animated block-entity mesh. */
public final class ZhelvaTrophyBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final MapCodec<ZhelvaTrophyBlock> CODEC = simpleCodec(ZhelvaTrophyBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 15, 15);
    public ZhelvaTrophyBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
            .strength(3.5F).sound(SoundType.STONE).noOcclusion());
    }
    public ZhelvaTrophyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected MapCodec<? extends ZhelvaTrophyBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }
    @Override public BlockState rotate(BlockState state, Rotation r) {
        return state.setValue(FACING, r.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
    @Override public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter world,
                                         BlockPos pos, CollisionContext ctx) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Tile(pos, state);
    }
    public static final class Tile extends BlockEntity {
        public Tile(BlockPos pos, BlockState state) {
            super(ZhelvaContent.HEAD_TILE.get(), pos, state);
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
            return saveWithoutMetadata(lookup);
        }
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }
    }
}
