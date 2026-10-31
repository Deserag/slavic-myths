package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Schema-11 military entrance repair. R1 keeps the immutable, historical
 * opposite-facing door leaves and 3-block-high gaps; only new city instances
 * receive corrected paired doors and solid lintels.
 */
public final class PosadDruzhinaEntrances {
    private static final Map<Long, BlockState> PATCH = new HashMap<>();
    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }
    private static void put(int u,int y,int v,BlockState block,boolean originalDoor) {
        int x=12+u,z=75+v;
        BlockState original=PosadNextBuildings.voxel(x,y,z);
        if (originalDoor ? original==null || !original.is(Blocks.CRIMSON_DOOR) : original!=null)
            throw new IllegalStateException("Military R1 entrance source changed at "+x+","+y+","+z);
        if (PATCH.putIfAbsent(key(x,y,z),block)!=null)
            throw new IllegalStateException("Duplicate military entrance patch "+x+","+y+","+z);
    }
    private static void doorway(int v,Direction facing) {
        for (int y=1;y<=2;y++) {
            DoubleBlockHalf half=y==1?DoubleBlockHalf.LOWER:DoubleBlockHalf.UPPER;
            for (int u=13;u<=14;u++) {
                boolean leftLeaf=u==13;
                // Hinges are outside the central passage: both leaves swing
                // toward their nearest masonry jamb, not into each other.
                DoorHingeSide hinge=facing==Direction.NORTH
                    ? (leftLeaf?DoorHingeSide.LEFT:DoorHingeSide.RIGHT)
                    : (leftLeaf?DoorHingeSide.RIGHT:DoorHingeSide.LEFT);
                BlockState door=Blocks.CRIMSON_DOOR.defaultBlockState()
                    .setValue(DoorBlock.FACING,facing)
                    .setValue(DoorBlock.HINGE,hinge)
                    .setValue(DoorBlock.HALF,half);
                put(u,y,v,door,true);
            }
        }
        for (int u=13;u<=14;u++)
            put(u,3,v,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),false);
    }
    static {
        doorway(1,Direction.NORTH);
        doorway(11,Direction.SOUTH);
        if (PATCH.size()!=12) throw new IllegalStateException("Expected 8 door halves and 4 lintel blocks");
    }
    public static BlockState voxel(int cityX,int y,int cityZ) {
        if (y<1||y>3||cityX<25||cityX>26||(cityZ!=76&&cityZ!=86)) return null;
        return PATCH.get(key(cityX,y,cityZ));
    }
    private PosadDruzhinaEntrances() {}
}
