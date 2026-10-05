package org.slavicmyths.block;
import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.slavicmyths.ritual.AltarTileEntity;

public final class AltarBlock extends Block implements EntityBlock {
    public AltarBlock(Properties properties) { super(properties); }
    public boolean hasTileEntity(BlockState state) { return true; }
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state) { return new AltarTileEntity(pos,state); }
    @Override public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) { return Block.box(1, 0, 1, 15, 12, 15); }
    @Override public InteractionResult useWithoutItem(BlockState state,Level world,BlockPos pos,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),state,world,pos,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        BlockEntity tile = world.getBlockEntity(pos);
        if (!world.isClientSide && tile instanceof AltarTileEntity) ((AltarTileEntity) tile).offer(player, hand);
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
    }
}
