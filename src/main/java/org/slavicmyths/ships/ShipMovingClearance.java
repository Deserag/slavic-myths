package org.slavicmyths.ships;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Continuous-contraption collision on the SERVER. The 2.95-wide Boat marker
 * is intentionally not trusted as the collision hull of a 7x11 ship.
 *
 * The assembled hull has already been removed from ordinary chunks: its
 * physical blocks live in a local palette. Every intermediate pose is checked
 * against loaded world chunks and the waterline. No destructive mutation is
 * ever performed by a clearance query (unlike the former voxel move).
 *
 * Costs are intentionally conservative for the first implementation. Future
 * optimization should cache section collision shapes, never skip sweep steps.
 */
public final class ShipMovingClearance {
    private static final double[] SAMPLES = {-0.42D, 0.42D};

    private static boolean harmless(ServerLevel level, BlockPos pos, BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN)
                || FirstShipBlockHull.softWaterObstacle(level, pos, state);
    }

    private static boolean check(ServerLevel level, ShipRigidMotion.Pose pose,
            int surfaceY, int localX, int relativeY, int localZ,
            LongSet visited, boolean waterline, double cos, double sin) {
        // Sample the four corners. A rotation can otherwise sweep the tip of
        // a full wood block through stone even if its center was clear.
        for (double sampleX : SAMPLES)
            for (double sampleZ : SAMPLES) {
                double x = localX + sampleX, z = localZ + sampleZ;
                int worldX = (int)Math.floor(pose.x() + x * cos - z * sin);
                int worldY = surfaceY + relativeY;
                int worldZ = (int)Math.floor(pose.z() + x * sin + z * cos);
                // Most neighboring source voxels map to the same target cell.
                // Probe the primitive packed coordinate before allocating a BlockPos.
                if (visited.add(BlockPos.asLong(worldX, worldY, worldZ))) {
                    BlockPos target = new BlockPos(worldX, worldY, worldZ);
                    if (!level.hasChunkAt(target)) return false;
                    BlockState obstacle = level.getBlockState(target);
                    if (!harmless(level, target, obstacle)
                            || level.getBlockEntity(target) != null) return false;
                    // Once per unique sample, not four times for every
                    // overlapping bottom voxel; protect the whole bilge.
                    if (waterline) {
                        BlockPos below = target.below();
                        if (!level.hasChunkAt(below)
                                || !level.getFluidState(below).is(FluidTags.WATER))
                            return false;
                    }
                }
            }
        return true;
    }

    public static boolean clearPose(ServerLevel level, ShipRigidMotion.Pose pose,
            int waterlineY, Map<BlockPos, BlockState> palette) {
        if (palette == null || palette.isEmpty()) return false;
        LongSet visited = new LongOpenHashSet();
        double yaw = Math.toRadians(pose.yaw());
        double cos = Math.cos(yaw), sin = Math.sin(yaw);
        // Full hull, sails, mast, stern rudder and optional chest blocks.
        for (BlockPos p : palette.keySet())
            if (!check(level, pose, waterlineY, p.getX(), p.getY(), p.getZ(),
                    visited, p.getY() == 0 || p.getY() == 1, cos, sin)) return false;
        // Unoccupied air within the cabin is part of the ship's swept
        // collision volume too; never phase a person through a cliff.
        for (BlockPos floor : palette.keySet()) {
            if (floor.getY() != 0) continue;
            for (int y = 1; y <= 6; y++)
                if (!check(level, pose, waterlineY, floor.getX(), y,
                        floor.getZ(), visited, false, cos, sin)) return false;
        }
        return true;
    }

    /**
     * Clear the same water plants and coral types as the stationary hull.
     * All actual voxel cells and the swept interior are covered, including
     * lily pads one block above the waterline. No normal solid block or
     * BlockEntity can be removed. Only called AFTER the full pose is accepted.
     */
    private static void clearCell(ServerLevel level, ShipRigidMotion.Pose pose,
            int waterlineY, int localX, int relativeY, int localZ,
            LongSet visited, double cos, double sin) {
        for (double sx : SAMPLES) for (double sz : SAMPLES) {
            double x = localX + sx, z = localZ + sz;
            int worldX = (int)Math.floor(pose.x() + x * cos - z * sin);
            int worldY = waterlineY + relativeY;
            int worldZ = (int)Math.floor(pose.z() + x * sin + z * cos);
            if (!visited.add(BlockPos.asLong(worldX, worldY, worldZ))) continue;
            BlockPos target = new BlockPos(worldX, worldY, worldZ);
            if (!level.hasChunkAt(target)) continue;
            BlockState state = level.getBlockState(target);
            if (!FirstShipBlockHull.softWaterObstacle(level, target, state)) continue;
            BlockState replacement = target.getY() <= waterlineY
                    || state.getFluidState().is(FluidTags.WATER)
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            level.setBlock(target, replacement, 3);
        }
    }

    public static void clearSoftObstacles(ServerLevel level, ShipRigidMotion.Pose pose,
            int waterlineY, Map<BlockPos, BlockState> palette) {
        if (palette == null || palette.isEmpty()) return;
        LongSet visited = new LongOpenHashSet();
        double yaw = Math.toRadians(pose.yaw());
        double cos = Math.cos(yaw), sin = Math.sin(yaw);
        for (BlockPos cell : palette.keySet()) {
            clearCell(level, pose, waterlineY, cell.getX(), cell.getY(),
                    cell.getZ(), visited, cos, sin);
            if (cell.getY() == 0)
                for (int y = 1; y <= 6; y++)
                    clearCell(level, pose, waterlineY, cell.getX(), y,
                            cell.getZ(), visited, cos, sin);
        }
    }

    private ShipMovingClearance() {}
}
