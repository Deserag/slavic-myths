package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;

/**
 * Communal-house R1 correction, gated to new schema-7 cities.
 *
 * The old 2-wide stairs stood directly behind the south entrance (X23/24)
 * and blocked both central lanes. A 2-wide flight now runs up the WEST
 * stairwell. The house's original structural NBT remains versioned and intact;
 * standalone placement applies this deterministic overlay after placing NBT.
 */
public final class PosadHouseRenovation {
    private static final int ORIGIN_X=13, ORIGIN_Z=15;
    private static final Map<Long, BlockState> PATCH = new HashMap<>();

    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }
    private static void put(int x,int y,int z,BlockState state) {
        if(PATCH.putIfAbsent(key(x,y,z),state)!=null)throw new IllegalStateException("Duplicate communal-house renovation at "+x+","+y+","+z);
    }
    private static void furniture(int x,int y,int z,String name) { put(x,y,z,Furniture.get(name).defaultBlockState()); }
    private static void lantern(int x,int y,int z) { put(x,y,z,Blocks.SOUL_LANTERN.defaultBlockState()); }

    static {
        // Remove the old central march and seal its abandoned second-floor hole.
        for(int step=0;step<6;step++) {
            int y=step+1, z=11-step;
            for(int x:new int[]{10,11})put(x,y,z,y==6?Blocks.POLISHED_BLACKSTONE.defaultBlockState():Blocks.AIR.defaultBlockState());

            // Two supported stair lanes: from west bedroom towards upstairs,
            // with verified two-block clearance at every ascending tread.
            for(int x:new int[]{7,8}) {
                for(int under=1;under<y;under++)put(x,under,z,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                put(x,y,z,Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
                for(int head=y+1;head<=y+2;head++)put(x,head,z,Blocks.AIR.defaultBlockState());
            }
        }
        for(int x:new int[]{10,11})for(int z:new int[]{7,8})put(x,6,z,Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        // Lower access from central entry corridor into the new side stairwell.
        for(int y:new int[]{1,2})put(9,y,11,Blocks.AIR.defaultBlockState());
        // Upper stair exit back into the public corridor, head height 2.
        for(int y:new int[]{7,8})put(9,y,5,Blocks.AIR.defaultBlockState());

        // Relocate the chest previously at the center of the stair mouth to
        // the western outside wall; never create a duplicate reward container.
        put(2,1,11,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));

        // Original entrance was 3 blocks tall above the two door leaves.
        for(int x:new int[]{10,11})put(x,3,13,Blocks.CRIMSON_PLANKS.defaultBlockState());
        put(6,7,8,Blocks.CRIMSON_FENCE.defaultBlockState());

        // Eight rooms: tables and chairs from the existing furniture system.
        furniture(6,1,3,"linden_table"); furniture(7,1,3,"linden_chair");
        furniture(16,1,3,"pine_table"); furniture(17,1,3,"pine_chair");
        furniture(4,1,8,"linden_table"); furniture(3,1,8,"linden_chair");
        furniture(17,1,8,"pine_table"); furniture(16,1,8,"pine_chair");
        furniture(4,7,4,"linden_table"); furniture(5,7,4,"linden_chair");
        furniture(16,7,3,"pine_table"); furniture(17,7,3,"pine_chair");
        furniture(4,7,8,"linden_table"); furniture(5,7,8,"linden_chair");
        furniture(17,7,8,"pine_table"); furniture(16,7,8,"pine_chair");

        // Solid floors support these lanterns, unlike unmounted wall torches.
        for(int[] p:new int[][]{{2,1,2},{19,1,2},{2,1,8},{19,1,8},
                {2,7,2},{19,7,2},{2,7,8},{19,7,8}})lantern(p[0],p[1],p[2]);
    }

    public static BlockState voxel(int cityX,int y,int cityZ) {
        if(cityX<ORIGIN_X||cityX>35||cityZ<ORIGIN_Z||cityZ>29||y<1||y>11)return null;
        int x=cityX-ORIGIN_X,z=cityZ-ORIGIN_Z;
        BlockState explicit=PATCH.get(key(x,y,z));
        if(explicit!=null)return explicit;
        BlockState old=PosadBuildings.voxel(cityX,y,cityZ);
        if(old!=null&&old.is(Blocks.NETHER_BRICKS)&&y>=2&&y<=10
                &&(cityX==14||cityX==34)&&Math.floorMod(cityZ,5)==0)
            return Blocks.CRIMSON_PLANKS.defaultBlockState();
        return null;
    }

    public static String chestLoot(int cityX,int y,int cityZ) {
        return cityX==15&&y==1&&cityZ==26?"chests/nether_communal_personal":null;
    }

    private PosadHouseRenovation() {}
}
