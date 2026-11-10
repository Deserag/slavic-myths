package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Real modular helms are vanilla blocks, so they do not trigger the ship
 * entity's interaction method when clicked. Route an exact block hit to its
 * owning vessel; never treat the ship's rail fences as helms.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class ShipHelmBlockEvents {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getHand() != InteractionHand.MAIN_HAND
                || player.isShiftKeyDown()) return;
        BlockPos clicked = event.getPos();
        if (!player.level().getBlockState(clicked).is(Blocks.DARK_OAK_FENCE)) return;
        for (FirstShipEntity ship : player.level().getEntitiesOfClass(
                FirstShipEntity.class, new AABB(clicked).inflate(34D, 24D, 34D))) {
            if (ship.isRemoved() || !ship.isNativeHull() || ship.isMoving()
                    || ship.modularConfiguration() == null) continue;
            BlockPos expected = FirstShipBlockHull.world(ship.blockAnchor(),
                    ship.quarterTurns(), 0, (int)ship.helmSeatHeight(),
                    (int)ship.helmWheelZ());
            if (expected.equals(clicked) && ship.tryHelmFromBlock(player, clicked)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
    }

    private ShipHelmBlockEvents() {}
}
