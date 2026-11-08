package org.slavicmyths.ships;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Shipyard's main screen holds a real vessel stack; sneak + empty hand opens crafting. */
public final class ShipyardWorkbenchBlock extends CraftingTableBlock {
    private static final MapCodec<ShipyardWorkbenchBlock> CODEC = simpleCodec(ShipyardWorkbenchBlock::new);

    public ShipyardWorkbenchBlock() { super(Properties.ofFullCopy(Blocks.CRAFTING_TABLE).strength(3F)); }
    private ShipyardWorkbenchBlock(Properties properties) { super(properties); }
    @Override public MapCodec<? extends CraftingTableBlock> codec() { return CODEC; }

    private InteractionResult open(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer server) {
            if (player.isSecondaryUseActive()) {
                // Standard 3x3 crafting, but with the *shipyard* validity check.
                server.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                        new net.minecraft.world.inventory.CraftingMenu(id, inventory,
                                net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos)) {
                            @Override public boolean stillValid(Player viewer) {
                                return viewer.isAlive() &&
                                        viewer.level().getBlockState(pos).is(RaftContent.SHIPYARD_BENCH.get()) &&
                                        viewer.distanceToSqr(pos.getCenter()) <= 64.0D;
                            }
                        }, Component.literal("Корабельная верфь: крафт")));
            } else {
                server.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                        new ShipyardMenu(id, inventory, pos),
                        Component.literal("Корабельная верфь")),
                        buffer -> buffer.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level,
            BlockPos pos, Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (held.getItem() instanceof RaftItem || held.is(RaftContent.DECK_SECTION.get())) {
            // No silent inventory mutation when clicking the station with an expensive vessel.
            // Put the raft into the real slot, inspect then explicitly press Upgrade.
            open(level, pos, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
