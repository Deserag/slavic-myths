package org.slavicmyths.farming;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

public final class FieldHoeItem extends Item {
    public FieldHoeItem(Properties properties) { super(properties.durability(350)); }
    @Override public int getEnchantmentValue() { return 14; }
    @Override public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility ability) { return ability == ItemAbilities.HOE_TILL; }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer(); var level = context.getLevel(); var center = context.getClickedPos();
        if (player == null || context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;
        var sideways = player.getDirection().getClockWise(); int radius = player.isShiftKeyDown() ? 0 : 1; int changed = 0;
        for (int offset = -radius; offset <= radius && !context.getItemInHand().isEmpty(); offset++) {
            var pos = center.relative(sideways, offset);
            if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || !level.getBlockState(pos.above()).isAir()) continue;
            var local = new UseOnContext(player, context.getHand(), new BlockHitResult(Vec3.atCenterOf(pos), context.getClickedFace(), pos, false));
            var state = level.getBlockState(pos); var result = state.getToolModifiedState(local, ItemAbilities.HOE_TILL, true);
            // Vanilla coarse/rooted dirt are first loosened to dirt; this farm tool completes that step in one use.
            if (result != null && result.is(Blocks.DIRT) && (state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT))) result = Blocks.FARMLAND.defaultBlockState();
            if (result == null || !result.is(Blocks.FARMLAND)) continue;
            if (!level.isClientSide) {
                if (!level.setBlock(pos, result, 11)) continue;
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            changed++;
        }
        if (changed == 0) return InteractionResult.PASS;
        if (!level.isClientSide) level.playSound(null, center, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
