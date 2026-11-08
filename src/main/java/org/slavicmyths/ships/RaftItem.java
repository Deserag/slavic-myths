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

/** Four sizes of the same native, cargo-carrying raft entity. */
public final class RaftItem extends Item {
    private final RaftSize size;

    public RaftItem(RaftSize size, Properties properties) { super(properties); this.size = size; }
    public RaftSize size() { return size; }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> text, net.minecraft.world.item.TooltipFlag flag) {
        text.add(net.minecraft.network.chat.Component.literal("Палуба " + size.width + "×" + size.length + " • до 4 игроков")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        text.add(net.minecraft.network.chat.Component.literal("Парус и борта устанавливаются: Shift + ПКМ").withStyle(net.minecraft.ChatFormatting.GRAY));
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
            text.add(net.minecraft.network.chat.Component.literal("Возможно, содержит упакованный груз").withStyle(net.minecraft.ChatFormatting.GOLD));
    }

    @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
        int sails = RaftContent.sailPreset(stack);
        return net.minecraft.network.chat.Component.literal(size.title +
                (sails == 2 ? " — два паруса" : sails == 1 ? " — парус и руль" : ""));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(held);
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.fail(held);
        RaftBoatEntity raft = RaftContent.RAFT.get().create(level);
        if (raft == null) return InteractionResultHolder.fail(held);
        for (int dx = -(size.width / 2); dx <= size.width / 2; dx++)
            for (int dz = -(size.length / 2); dz <= size.length / 2; dz++) {
                BlockPos support = pos.offset(dx, 0, dz);
                if (!level.hasChunkAt(support) || !level.getFluidState(support).is(FluidTags.WATER)) {
                    if (!level.isClientSide) player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Необходима свободная вода " + size.width + "×" + size.length), true);
                    return InteractionResultHolder.fail(held);
                }
            }
        raft.configure(size);
        raft.setPos(pos.getX() + .5D, hit.getLocation().y, pos.getZ() + .5D);
        raft.setWaterSurface(hit.getLocation().y);
        raft.setYRot(player.getYRot());
        if (!level.noCollision(raft, raft.getBoundingBox())) return InteractionResultHolder.fail(held);
        if (!level.isClientSide) {
            if (!raft.loadPackedItem(held)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Некорректные данные груза: установка отменена"), true);
                return InteractionResultHolder.fail(held);
            }
            if (!level.addFreshEntity(raft)) return InteractionResultHolder.fail(held);
            if (!player.getAbilities().instabuild) held.shrink(1);
            else if (held.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA))
                held.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
