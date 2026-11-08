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

    @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
        return net.minecraft.network.chat.Component.translatableWithFallback("item.slavicmyths.raft_2511_test", "Плот");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(held);
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.fail(held);
        RaftBoatEntity raft = RaftContent.RAFT.get().create(level);
        if (raft == null) return InteractionResultHolder.fail(held);
        // IMPORTANT: boats are positioned at the fluid SURFACE, not near the bottom of a water block.
        // The former +0.15 spawned the entire 0.65-high raft underwater and triggered boat sinking.
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            BlockPos support = pos.offset(dx, 0, dz);
            if (!level.hasChunkAt(support) || !level.getFluidState(support).is(FluidTags.WATER)) {
                if (!level.isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.literal("Для плота требуется свободная вода 3×3"), true);
                return InteractionResultHolder.fail(held);
            }
        }
        raft.setPos(pos.getX() + 0.5D, hit.getLocation().y, pos.getZ() + 0.5D);
        raft.setWaterSurface(hit.getLocation().y);
        raft.setYRot(player.getYRot());
        if (!level.noCollision(raft, raft.getBoundingBox())) return InteractionResultHolder.fail(held);
        if (!level.isClientSide) {
            level.addFreshEntity(raft);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
