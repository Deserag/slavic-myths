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
            java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag) {
        lines.addAll(RaftCharacteristics.tooltip(RaftCharacteristics.fromItem(stack)));
        lines.add(net.minecraft.network.chat.Component.literal(
                "Верфяной ключ: безопасная упаковка с сохранением груза")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
        RaftCharacteristics.Info info = RaftCharacteristics.fromItem(stack);
        int sails = info == null ? 0 : info.sails();
        return net.minecraft.network.chat.Component.literal(size.title +
                (sails == 2 ? " — два паруса" : sails == 1 ? " — один парус" : "") +
                (sails > 0 && info != null && info.helm() ? " и руль" : "") +
                (info != null && info.rails() ? " • с ограждением" : ""));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(held);
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.fail(held);
        RaftBoatEntity raft = RaftContent.RAFT.get().create(level);
        if (raft == null) return InteractionResultHolder.fail(held);
        // Cover every quarter-cell of the ROTATED deck. Previous world-axis-aligned
        // checks incorrectly allowed long rafts to overlap shores and solid blocks.
        for (int sx = 0; sx < size.width * 2; sx++)
            for (int sz = 0; sz < size.length * 2; sz++) {
                RaftFootprint.Point offset = RaftFootprint.sample(size.width, size.length, sx, sz, player.getYRot());
                BlockPos support = BlockPos.containing(pos.getX() + .5D + offset.x(),
                        pos.getY(), pos.getZ() + .5D + offset.z());
                BlockPos upper = support.above();
                if (!level.hasChunkAt(support) || !level.hasChunkAt(upper) ||
                        !level.getFluidState(support).is(FluidTags.WATER) ||
                        !level.getFluidState(support).isSource() ||
                        !level.getBlockState(upper).getCollisionShape(level, upper).isEmpty()) {
                    if (!level.isClientSide) player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "Для этого плота нужна ровная свободная вода " +
                                            size.width + "×" + size.length), true);
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
