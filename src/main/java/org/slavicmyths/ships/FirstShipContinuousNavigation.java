package org.slavicmyths.ships;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Swept-volume gate for continuous yaw/translation, not axis-aligned
 * whole-voxel teleportation. Never changes terrain before every probe passes.
 * Only explicitly allowlisted vanilla aquatic decorations can be removed.
 */
public final class FirstShipContinuousNavigation {
    public static Set<BlockPos> preflight(ServerLevel level, double x, double y,
            double z, double yaw, int rig, int fabric, int geometry) {
        Set<BlockPos> removable = new HashSet<>();
        // Sample the navigable footprint at half-block intervals. Checking
        // all occupied columns (not only the tiny Boat hitbox) prevents the
        // keel passing through the shoreline when rotating in place.
        for (int sx = 0; sx < FirstShipHull.WIDTH * 2; sx++) {
            for (int sz = 0; sz < FirstShipHull.LENGTH * 2; sz++) {
                double lx = -FirstShipHull.WIDTH / 2D + (sx + .5D) * .5D;
                double lz = -FirstShipHull.LENGTH / 2D + (sz + .5D) * .5D;
                if (!FirstShipHull.withinHull(lx, lz, .10D)) continue;
                FirstShipHull.Point offset = FirstShipHull.toWorld(lx, lz, x, z, yaw);
                BlockPos water = BlockPos.containing(offset.x(), y, offset.z());
                if (!level.hasChunkAt(water) || !level.hasChunkAt(water.below())
                        || !level.getFluidState(water.below()).is(FluidTags.WATER)) return null;
                if (!waterSourceOrSoft(level, water, removable)) return null;
                for (int dy = 1; dy <= 6; dy++)
                    if (!emptyOrSoft(level, water.above(dy), removable)) return null;
            }
        }
        // The mast/yards/sails also occupy high altitude. Verify the actual
        // palette for this rig tier, with corner samples to include rotation.
        for (Map.Entry<BlockPos, BlockState> entry :
                FirstShipBlockHull.movingPalette(rig, fabric, geometry).entrySet()) {
            BlockPos cell = entry.getKey();
            if (cell.getY() < 7) continue;
            for (double dx : new double[]{-.42D, .42D})
                for (double dz : new double[]{-.42D, .42D}) {
                    FirstShipHull.Point point = FirstShipHull.toWorld(
                            cell.getX() + dx, cell.getZ() + dz, x, z, yaw);
                    BlockPos world = BlockPos.containing(point.x(), y + cell.getY(), point.z());
                    if (!emptyOrSoft(level, world, removable)) return null;
                }
        }
        return removable;
    }

    private static boolean waterSourceOrSoft(ServerLevel level, BlockPos at,
            Set<BlockPos> removable) {
        if (level.getFluidState(at).is(FluidTags.WATER)
                && level.getFluidState(at).isSource()) return true;
        if (FirstShipBlockHull.isSoftWaterObstacle(level, at)) {
            removable.add(at.immutable());
            return true;
        }
        return false;
    }

    private static boolean emptyOrSoft(ServerLevel level, BlockPos at,
            Set<BlockPos> removable) {
        if (!level.hasChunkAt(at)) return false;
        if (level.getBlockState(at).isAir()) return true;
        if (FirstShipBlockHull.isSoftWaterObstacle(level, at)) {
            removable.add(at.immutable());
            return true;
        }
        return false;
    }

    public static void clearAllowed(ServerLevel level, Set<BlockPos> positions, int waterline) {
        for (BlockPos pos : positions) {
            BlockState replacement = pos.getY() <= waterline
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, replacement,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private FirstShipContinuousNavigation() {}
}
