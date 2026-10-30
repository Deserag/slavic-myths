package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Schema-13 roof repair for NEW Veche halls only. R1 and existing structures
 * remain immutable: this overlay removes the schema-6 flat ceiling, repairs
 * the asymmetric ridge and restores uniform pitched roof surfaces.
 *
 * R1's northern slope peaks at v=9/y=17; v=10 is the centre ridge. The R1
 * extra top slab at v=11 incorrectly replaces the mirrored SOUTH stair.
 */
public final class PosadHallRoofRepair {
    private static final int MIN_X=42, MAX_X=70, MIN_Z=12, MAX_Z=34;

    public static BlockState voxel(int x,int y,int z) {
        if(x<MIN_X||x>MAX_X||z<MIN_Z||z>MAX_Z||y<12||y>17)return null;
        BlockState source=PosadBuildings.voxel(x,y,z);

        // Schema-6 added a 27x21 flat crimson ceiling where the R1 y=12
        // source was AIR. These 475 cells hide the authentic vaulted roof.
        // Explicit AIR is essential: null would fall through to schema-6.
        if(y==12&&x>=43&&x<=69&&z>=13&&z<=33&&source==null)
            return Blocks.AIR.defaultBlockState();

        // Centre ridge is v=10 -> city Z=23. Its v=11 neighbour was a
        // second row of slabs, although the mirror of the NORTH stair at
        // v=9 is the missing SOUTH stair at v=11. Keep the one-row top slab.
        if(y==17&&z==24) {
            if(source==null||!source.is(Blocks.NETHER_BRICK_SLAB))
                throw new IllegalStateException("Changed R1 Veche roof ridge at "+x+","+y+","+z);
            return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
        }

        if(source!=null&&source.is(Blocks.NETHER_BRICK_STAIRS)) {
            // No random crimson stairs on the pitch. Retain five deliberate
            // paired dark masonry ribs, preserving the original stair facing.
            int u=x-43;
            boolean rib=u==0||u==6||u==13||u==20||u==26;
            return rib
                ? Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,source.getValue(StairBlock.FACING))
                : source;
        }
        return null;
    }

    private PosadHallRoofRepair() {}
}
