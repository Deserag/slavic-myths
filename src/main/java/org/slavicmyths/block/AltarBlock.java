package org.slavicmyths.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.slavicmyths.ritual.AltarTileEntity;

public final class AltarBlock extends Block {
    public AltarBlock(Properties properties) { super(properties); }
    @Override public boolean hasTileEntity(BlockState state) { return true; }
    @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new AltarTileEntity(); }
    @Override public VoxelShape getShape(BlockState s, IBlockReader w, BlockPos p, ISelectionContext c) { return Block.box(1, 0, 1, 15, 12, 15); }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = world.getBlockEntity(pos);
        if (!world.isClientSide && tile instanceof AltarTileEntity) ((AltarTileEntity) tile).offer(player, hand);
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
}
