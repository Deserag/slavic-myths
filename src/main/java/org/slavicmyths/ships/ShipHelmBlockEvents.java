package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Dedicated world-block helm interaction for real modular ships.
 *
 * A modular helm is a vanilla fence positioned far from its Boat marker:
 * entity interaction and legacy 7x11 hitboxes cannot be relied upon.
 * Only the precise hull-owned helm cell is accepted, and the entity's
 * authoritative tryHelm() still validates rider, position and line of sight.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class ShipHelmBlockEvents {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND
                || event.getEntity().isSecondaryUseActive()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos hit = event.getPos();
        if (!level.getBlockState(hit).is(Blocks.DARK_OAK_FENCE)) return;
        for (FirstShipEntity ship : level.getEntitiesOfClass(FirstShipEntity.class,
                new AABB(hit).inflate(32D, 18D, 32D))) {
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (config == null || !ship.isNativeHull() || ship.isMoving() || ship.isRemoved()) continue;
            int y = config.bridge() ? 10 : 5;
            int z = -config.length() / 2 + (config.bridge() ? 4 : config.cabin() ? 9 : 3);
            BlockPos helm = FirstShipBlockHull.world(
                    ship.blockAnchor(), ship.quarterTurns(), 0, y, z);
            if (!hit.equals(helm)) continue;
            if (ship.tryHelm(player)) {
                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
            return;
        }
    }

    private ShipHelmBlockEvents() {}
}
