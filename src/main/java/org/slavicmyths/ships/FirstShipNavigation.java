package org.slavicmyths.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

/**
 * Server-side swept-footprint safety gate. A ship's native Boat collision is
 * deliberately small to avoid deck-player jitter, so the full 7x11 rotated
 * footprint must reject shore/rocks, overhead beams and too-shallow water.
 *
 * This is NOT vanilla block collision and does not prove multiplayer smoothness.
 */
public final class FirstShipNavigation {
    public static boolean clear(Level level, double centerX, double centerZ,
            double surfaceY, double yaw) {
        for (int sx = 0; sx < FirstShipHull.WIDTH * 2; sx++) {
            for (int sz = 0; sz < FirstShipHull.LENGTH * 2; sz++) {
                double lx = -FirstShipHull.WIDTH / 2D + (sx + .5D) * .5D;
                double lz = -FirstShipHull.LENGTH / 2D + (sz + .5D) * .5D;
                if (!FirstShipHull.withinHull(lx, lz, .10D)) continue;
                FirstShipHull.Point pos = FirstShipHull.toWorld(lx, lz, centerX, centerZ, yaw);
                BlockPos water = BlockPos.containing(pos.x(), surfaceY - .14D, pos.z());
                BlockPos deep = water.below();
                if (!level.hasChunkAt(water) || !level.hasChunkAt(deep)
                        || !level.getFluidState(water).is(FluidTags.WATER)
                        || !level.getFluidState(water).isSource()
                        || !level.getFluidState(deep).is(FluidTags.WATER)) return false;
                // Check the occupied lower hold and the upper deck, not only
                // the tiny native Boat hitbox.
                for (int dy = 1; dy <= 4; dy++) {
                    BlockPos solid = water.above(dy);
                    if (!level.hasChunkAt(solid) ||
                            !level.getBlockState(solid).getCollisionShape(level, solid).isEmpty()) return false;
                }
            }
        }
        // Mast and broad sail must have clearance over the full ship, not just
        // directly at the vessel center.
        for (int xi = -2; xi <= 2; xi++) {
            for (int zi = -1; zi <= 1; zi++) {
                FirstShipHull.Point pos = FirstShipHull.toWorld(
                        xi, zi + FirstShipHull.MAST_Z, centerX, centerZ, yaw);
                for (int dy = 5; dy <= 10; dy++) {
                    BlockPos upper = BlockPos.containing(pos.x(), surfaceY + dy - .14D, pos.z());
                    if (!level.hasChunkAt(upper) ||
                            !level.getBlockState(upper).getCollisionShape(level, upper).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    private FirstShipNavigation() {}
}
