package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Protect the physical interior of parked modular ships from farming blocks.
 * Allowed storage and ordinary decorative blocks remain vanilla-placeable.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class ShipCargoPlacementGuard {
    private ShipCargoPlacementGuard() {}

    /** No agricultural soil, planted crops or harvestable growing plants aboard. */
    public static boolean forbidden(BlockState state) {
        return state.getBlock() instanceof FarmBlock
                || state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof LiquidBlock
                || !state.getFluidState().isEmpty()
                || state.is(BlockTags.CROPS)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.COCOA)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.BAMBOO_SAPLING)
                || state.is(Blocks.NETHER_WART)
                || state.is(Blocks.MELON_STEM)
                || state.is(Blocks.PUMPKIN_STEM)
                || state.is(Blocks.ATTACHED_MELON_STEM)
                || state.is(Blocks.ATTACHED_PUMPKIN_STEM);
    }

    public static boolean onParkedShip(ServerLevel world, BlockPos placed) {
        // The event is rare and only restricted blocks enter this scan.
        // The maximum current vessel length is below 100 blocks.
        for (FirstShipEntity ship : world.getEntitiesOfClass(FirstShipEntity.class,
                new AABB(placed).inflate(95D, 55D, 95D))) {
            if (!ship.isNativeHull() || ship.isMoving()) continue;
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (config == null || config.withdrawn()) continue;
            BlockPos anchor = ship.blockAnchor();
            int y = placed.getY() - anchor.getY();
            if (y < 1 || y > 48) continue;
            int worldX = placed.getX() - anchor.getX();
            int worldZ = placed.getZ() - anchor.getZ();
            int turn = Math.floorMod(ship.quarterTurns(), 4);
            int x = switch (turn) {
                case 0 -> worldX;
                case 1 -> worldZ;
                case 2 -> -worldX;
                default -> -worldZ;
            };
            int z = switch (turn) {
                case 0 -> worldZ;
                case 1 -> -worldX;
                case 2 -> -worldZ;
                default -> worldX;
            };
            if (ship.withinShip(x, z)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        BlockState finalState = event.getFinalState();
        if (!event.isSimulated() && finalState != null
                && event.getLevel() instanceof ServerLevel server
                && forbidden(finalState)
                && onParkedShip(server, event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel server
                && forbidden(event.getPlacedBlock())
                && onParkedShip(server, event.getPos()))
            event.setCanceled(true);
    }
}
