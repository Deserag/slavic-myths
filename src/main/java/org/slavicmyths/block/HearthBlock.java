package org.slavicmyths.block;

import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.slavicmyths.entity.DomovoyEntity;

public final class HearthBlock extends Block {
    public HearthBlock(Properties properties) { super(properties); }
    @Override public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            List<DomovoyEntity> spirits = world.getEntitiesOfClass(DomovoyEntity.class, new AxisAlignedBB(pos).inflate(8),
                    spirit -> spirit.canBind(player));
            spirits.sort(Comparator.comparingDouble(spirit -> spirit.distanceToSqr(player)));
            if (spirits.isEmpty()) player.displayClientMessage(new TranslationTextComponent("domovoy.slavicmyths.no_friend"), true);
            else spirits.get(0).bind(player, pos);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }
}
