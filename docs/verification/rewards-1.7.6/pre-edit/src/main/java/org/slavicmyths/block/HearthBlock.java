package org.slavicmyths.block;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import org.slavicmyths.entity.DomovoyEntity;

public final class HearthBlock extends Block {
    public HearthBlock(Properties properties) { super(properties); }
    @Override public InteractionResult useWithoutItem(BlockState state,Level world,BlockPos pos,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),state,world,pos,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        if (!world.isClientSide) {
            List<DomovoyEntity> spirits = world.getEntitiesOfClass(DomovoyEntity.class, new AABB(pos).inflate(8),
                    spirit -> spirit.canBind(player));
            spirits.sort(Comparator.comparingDouble(spirit -> spirit.distanceToSqr(player)));
            if (spirits.isEmpty()) player.displayClientMessage(Component.translatable("domovoy.slavicmyths.no_friend"), true);
            else spirits.get(0).bind(player, pos);
        }
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(world.isClientSide);
    }
}
