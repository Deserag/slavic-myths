package org.slavicmyths.block;
import net.minecraft.network.chat.Component;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import org.slavicmyths.progression.Knowledge;

public final class IdolBlock extends Block {
    public IdolBlock(Properties properties) { super(properties); }
    @Override public InteractionResult useWithoutItem(BlockState state,Level world,BlockPos pos,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),state,world,pos,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        if (!world.isClientSide) {
            Knowledge.award(player, "shrine");
            player.displayClientMessage(Component.translatable("shrine.slavicmyths.discovered"), true);
        }
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
    }
}
