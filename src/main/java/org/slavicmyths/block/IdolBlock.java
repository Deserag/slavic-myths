package org.slavicmyths.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.slavicmyths.progression.Knowledge;

public final class IdolBlock extends Block {
    public IdolBlock(Properties properties) { super(properties); }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            Knowledge.award(player, "shrine");
            player.displayClientMessage(new TranslationTextComponent("shrine.slavicmyths.discovered"), true);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
}
