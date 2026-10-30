package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Schema-15 new-generation-only repair for the forge roof/chimney joint.
 * The original R1 roof stair protrudes into the 1x1 flue base. Complete
 * the chimney's four-sided masonry collar without changing roof pitches,
 * campfire, the hollow upper shaft, authored R1 resources or loot.
 */
public final class PosadForgeChimneyRoofRepair {
    public static BlockState voxel(int x, int y, int z) {
        if (x != 16 || y != 9 || z != 47) return null;
        BlockState source = PosadNextBuildings.voxel(x,y,z);
        if (source == null || !source.is(Blocks.NETHER_BRICK_STAIRS) || source.getValue(StairBlock.FACING) != Direction.EAST)
            throw new IllegalStateException("Forge R1 chimney base changed at "+x+","+y+","+z);
        for (Direction side : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}) {
            BlockState masonry = PosadNextBuildings.voxel(x+side.getStepX(),y,z+side.getStepZ());
            if (masonry == null || !masonry.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
                throw new IllegalStateException("Forge R1 chimney collar changed at "+x+","+y+","+z+" side "+side);
        }
        for (int upper=10;upper<=15;upper++)
            if (PosadNextBuildings.voxel(x,upper,z) != null)
                throw new IllegalStateException("Forge R1 chimney flue obstructed at y="+upper);
        BlockState cap = PosadNextBuildings.voxel(x,16,z);
        BlockState smoke = PosadNextBuildings.voxel(x,17,z);
        if (cap == null || !cap.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || smoke == null || !smoke.is(Blocks.CAMPFIRE))
            throw new IllegalStateException("Forge R1 chimney termination changed");
        return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    }

    private PosadForgeChimneyRoofRepair() {}
}
