package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Ship-specific 3x3, 3x5, 5x5 and 5x7 placement preserving packed cargo. */
public final class RaftItem extends Item {
    private final RaftSize size;
    public RaftItem(Properties properties, RaftSize size) { super(properties); this.size = size; }
    public RaftSize size() { return size; }

    @Override public Component getName(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        boolean packed = custom != null && custom.copyTag().contains("Raft2511Pack");
        return Component.literal(size.displayName + (packed ? " (с грузом)" : ""));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(held);
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.fail(held);
        int halfW = size.width / 2, halfL = size.length / 2;
        for (int dx = -halfW; dx <= halfW; dx++) for (int dz = -halfL; dz <= halfL; dz++) {
            BlockPos support = pos.offset(dx, 0, dz);
            if (!level.hasChunkAt(support) || !level.getFluidState(support).is(FluidTags.WATER)) {
                if (!level.isClientSide) player.displayClientMessage(Component.literal(
                        "Для плота требуется открытая вода " + size.width + "×" + size.length), true);
                return InteractionResultHolder.fail(held);
            }
        }
        RaftBoatEntity raft = RaftContent.RAFT.get().create(level);
        if (raft == null) return InteractionResultHolder.fail(held);
        raft.setRaftSize(size);
        raft.setPos(pos.getX() + .5D, hit.getLocation().y, pos.getZ() + .5D);
        raft.setWaterSurface(hit.getLocation().y);
        raft.setYRot(player.getYRot());
        if (!level.noCollision(raft, raft.getBoundingBox())) return InteractionResultHolder.fail(held);
        if (!level.isClientSide) {
            if (!raft.loadPackedItem(held)) {
                player.displayClientMessage(Component.literal("Повреждённые данные плота: установка отменена"), true);
                return InteractionResultHolder.fail(held);
            }
            // Re-evaluate after restoring the packed size.
            if (!level.noCollision(raft, raft.getBoundingBox()) || !level.addFreshEntity(raft)) return InteractionResultHolder.fail(held);
            if (!player.getAbilities().instabuild) held.shrink(1);
            else if (held.has(DataComponents.CUSTOM_DATA)) held.remove(DataComponents.CUSTOM_DATA);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
