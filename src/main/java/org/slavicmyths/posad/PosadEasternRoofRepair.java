package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Schema-16 new-city-only roof repair for the main granary.
 * The R1 two-row flat slab crest (13x2) is replaced with two opposing
 * pitched stair edges, leaving the masonry support, all roof slopes,
 * the separate shed's R1 roof, its chests and every historical template intact.
 */
public final class PosadEasternRoofRepair {
    public static BlockState voxel(int x, int y, int z) {
        if (y != 10 || (x != 83 && x != 84) || z < 13 || z > 25) return null;
        Direction facing = x == 83 ? Direction.WEST : Direction.EAST;
        BlockState source = PosadAdditionalBuildings.voxel(x,y,z);
        BlockState support = PosadAdditionalBuildings.voxel(x,9,z);
        BlockState outward = PosadAdditionalBuildings.voxel(x + (x == 83 ? -1 : 1),8,z);
        if (source == null || !source.is(Blocks.NETHER_BRICK_SLAB) || source.getValue(SlabBlock.TYPE) != SlabType.BOTTOM
                || support == null || !support.is(Blocks.NETHER_BRICKS)
                || outward == null || !outward.is(Blocks.NETHER_BRICK_STAIRS)
                || outward.getValue(StairBlock.FACING) != facing)
            throw new IllegalStateException("Granary R1 roof ridge changed at "+x+","+y+","+z);
        return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    private PosadEasternRoofRepair() {}
}
