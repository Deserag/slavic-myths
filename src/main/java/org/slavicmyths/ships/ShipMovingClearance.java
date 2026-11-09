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

    private static boolean harmless(BlockState state) {
        Block block = state.getBlock();
        return state.isAir() || state.getFluidState().is(FluidTags.WATER)
                || block == Blocks.LILY_PAD || block == Blocks.SEAGRASS
                || block == Blocks.TALL_SEAGRASS || block == Blocks.KELP
                || block == Blocks.KELP_PLANT || block == Blocks.SEA_PICKLE
                || block == Blocks.TUBE_CORAL || block == Blocks.BRAIN_CORAL
                || block == Blocks.BUBBLE_CORAL || block == Blocks.FIRE_CORAL
                || block == Blocks.HORN_CORAL || block == Blocks.TUBE_CORAL_FAN
                || block == Blocks.BRAIN_CORAL_FAN || block == Blocks.BUBBLE_CORAL_FAN
                || block == Blocks.FIRE_CORAL_FAN || block == Blocks.HORN_CORAL_FAN
                || block == Blocks.TUBE_CORAL_BLOCK || block == Blocks.BRAIN_CORAL_BLOCK
                || block == Blocks.BUBBLE_CORAL_BLOCK || block == Blocks.FIRE_CORAL_BLOCK
                || block == Blocks.HORN_CORAL_BLOCK;
    }

    private static boolean check(ServerLevel level, ShipRigidMotion.Pose pose,
            int surfaceY, int localX, int relativeY, int localZ,
            Set<BlockPos> visited, boolean waterline) {
        // Sample the four corners. A rotation can otherwise sweep the tip of
        // a full wood block through stone even if its center was clear.
        for (double sampleX : SAMPLES)
            for (double sampleZ : SAMPLES) {
                FirstShipHull.Point point = FirstShipHull.toWorld(
                        localX + sampleX, localZ + sampleZ,
                        pose.x(), pose.z(), pose.yaw());
                BlockPos target = BlockPos.containing(point.x(),
                        surfaceY + relativeY + 0.5D, point.z());
                if (!visited.add(target)) continue;
                if (!level.hasChunkAt(target)) return false;
                BlockState obstacle = level.getBlockState(target);
                if (!harmless(obstacle) || level.getBlockEntity(target) != null) return false;
                if (waterline) {
                    BlockPos below = target.below();
                    if (!level.hasChunkAt(below)
                            || !level.getFluidState(below).is(FluidTags.WATER)) return false;
                }
            }
        return true;
    }

    public static boolean clearPose(ServerLevel level, ShipRigidMotion.Pose pose,
            int waterlineY, Map<BlockPos, BlockState> palette) {
        if (palette == null || palette.isEmpty()) return false;
        Set<BlockPos> visited = new HashSet<>();
        // Full hull, sails, mast, stern rudder and optional chest blocks.
        for (BlockPos p : palette.keySet())
            if (!check(level, pose, waterlineY, p.getX(), p.getY(), p.getZ(),
                    visited, p.getY() == 0)) return false;
        // Unoccupied air within the cabin is part of the ship's swept
        // collision volume too; never phase a person through a cliff.
        for (int z = -5; z <= 5; z++)
            for (int x = -FirstShipBlockHull.halfWidth(z);
                 x <= FirstShipBlockHull.halfWidth(z); x++)
                for (int y = 1; y <= 6; y++)
                    if (!check(level, pose, waterlineY, x, y, z, visited, false)) return false;
        return true;
    }

    private ShipMovingClearance() {}
}
