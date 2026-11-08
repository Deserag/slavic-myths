package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Places exactly one raft on water. No Creative/Survival recipe is granted yet. */
public final class RaftItem extends Item {
    public RaftItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(held);
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.fail(held);
        RaftBoatEntity raft = RaftContent.RAFT.get().create(level);
        if (raft == null) return InteractionResultHolder.fail(held);
        raft.setPos(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D);
        raft.setYRot(player.getYRot());
        if (!level.noCollision(raft, raft.getBoundingBox())) return InteractionResultHolder.fail(held);
        if (!level.isClientSide) {
            level.addFreshEntity(raft);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
