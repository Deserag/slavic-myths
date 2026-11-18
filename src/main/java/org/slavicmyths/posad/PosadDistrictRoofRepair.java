package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Schema-14 repairs the three-block-wide flat crest on the communal house
 * and military hall. Replace the two outer crest rows by pitched stairs,
 * leaving the central full-block ridge and all immutable R1 assets intact.
 * Nothing outside the exact 98 R1 ridge-shoulder cells is changed.
 */
public final class PosadDistrictRoofRepair {
    private static BlockState shoulder(BlockState source, BlockState lowerPitch, BlockState center, Direction facing, int x, int y, int z) {
        if (source == null || !source.is(Blocks.NETHER_BRICKS) || center == null || !center.is(Blocks.NETHER_BRICKS)
                || lowerPitch == null || !lowerPitch.is(Blocks.NETHER_BRICK_STAIRS) || lowerPitch.getValue(StairBlock.FACING) != facing)
            throw new IllegalStateException("Changed R1 district roof at " + x + "," + y + "," + z);
        return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    public static BlockState voxel(int x, int y, int z) {
        if (y == 16 && x >= 13 && x <= 35 && (z == 21 || z == 23)) {
            Direction facing = z == 21 ? Direction.NORTH : Direction.SOUTH;
            return shoulder(PosadBuildings.voxel(x,y,z), PosadBuildings.voxel(x,15,z + (z == 21 ? -1 : 1)),
                    PosadBuildings.voxel(x,16,22), facing, x, y, z);
        }
        if (y == 17 && x >= 13 && x <= 38 && (z == 80 || z == 82)) {
            Direction facing = z == 80 ? Direction.NORTH : Direction.SOUTH;
            return shoulder(PosadNextBuildings.voxel(x,y,z), PosadNextBuildings.voxel(x,16,z + (z == 80 ? -1 : 1)),
                    PosadNextBuildings.voxel(x,17,81), facing, x, y, z);
        }
        return null;
    }

    private PosadDistrictRoofRepair() {}
}
