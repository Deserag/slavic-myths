package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Schema 21 only. Corrects the two documented northbound, two-wide stair
 * flights without altering the approved R1 source data or existing saved
 * schema 7..20 structures. The Minecraft stair faces the LOW side of a tread:
 * an ascending-north flight must face SOUTH, not NORTH.
 */
public final class PosadArchitectureR4 {
    private static final Map<Long,BlockState> PATCH=new HashMap<>();
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}

    private static void stair(int x,int y,int z,BlockState previous){
        if(previous==null || !previous.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
                || previous.getValue(StairBlock.FACING)!=Direction.NORTH
                || previous.getValue(StairBlock.HALF)!=Half.BOTTOM)
            throw new IllegalStateException("Northbound Posad stair source changed at "+x+","+y+","+z+": "+previous);
        BlockState corrected=previous.setValue(StairBlock.FACING,Direction.SOUTH);
        if(PATCH.putIfAbsent(key(x,y,z),corrected)!=null)
            throw new IllegalStateException("Duplicate schema-21 stair "+x+","+y+","+z);
    }

    static {
        // Communal side stairwell: local (7,8), entering from the south.
        for(int step=0;step<6;step++)for(int x:new int[]{20,21}){
            int y=step+1,z=26-step;
            stair(x,y,z,PosadHouseRenovation.voxel(x,y,z));
        }
        // Druzhina interior flight: local (11,12) ascending to the north.
        for(int step=0;step<6;step++)for(int x:new int[]{23,24}){
            int y=step+1,z=84-step;
            stair(x,y,z,PosadNextBuildings.voxel(x,y,z));
        }
        if(PATCH.size()!=24)throw new IllegalStateException("Expected 24 corrected Posad stair treads");
    }

    public static BlockState voxel(int x,int y,int z){
        if(y<1||y>6)return null;
        return PATCH.get(key(x,y,z));
    }

    public static int size(){return PATCH.size();}
    private PosadArchitectureR4(){}
}
