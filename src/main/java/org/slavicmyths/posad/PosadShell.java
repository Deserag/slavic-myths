package org.slavicmyths.posad;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Shared deterministic R1 shell. Coordinates are local; null means no world write. */
public final class PosadShell {
    public static final int HEIGHT = 18;
    private static BlockState brick() { return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(); }
    public static BlockState voxel(int x, int y, int z) {
        if (!PosadGeometry.CITY.contains(x,z) || y < -2 || y >= HEIGHT) return null;
        if (y < 0) return Blocks.BLACKSTONE.defaultBlockState();
        if (y == 0) {
            boolean road = (x >= 53 && x <= 59) || (z >= 40 && z <= 43)
                || (z >= 70 && z <= 74) || PosadGeometry.Slot.VECHE_SQUARE.local().contains(x,z);
            return (road ? Blocks.POLISHED_BLACKSTONE : Blocks.BLACKSTONE).defaultBlockState();
        }
        // Tower ownership precedes gate and wall ownership, including corner joints.
        for (int tx : new int[]{0,102}) for (int tz : new int[]{0,102})
            if (x >= tx && x <= tx+10 && z >= tz && z <= tz+10)
                return tower(x-tx,y,z-tz,11,11);
        for (int tx : new int[]{42,62})
            if (x >= tx && x <= tx+8 && z >= 102)
                return tower(x-tx,y,z-102,9,11);
        if (x >= 51 && x <= 61 && z >= 108) {
            if (x >= 53 && x <= 59 && y <= 6) return Blocks.AIR.defaultBlockState();
            if (y <= 8) return brick();
            return parapet(x,y,z);
        }
        if (x < 5 || x > 107 || z < 5 || z > 107) {
            if (y <= 8) return brick();
            return parapet(x,y,z);
        }
        return null;
    }
    private static BlockState parapet(int x,int y,int z) {
        if (y > 10) return null;
        boolean outer=x==0||x==112||z==0||z==112;
        boolean inner=x==4||x==108||z==4||z==108;
        if (inner) return brick();
        if (outer) {
            int along=(x==0||x==112)?z:x;
            if (y==9 || Math.floorMod(along,5)<3) return brick();
        }
        return Blocks.AIR.defaultBlockState();
    }
    private static BlockState tower(int x,int y,int z,int width,int depth) {
        boolean edge=x==0||x==width-1||z==0||z==depth-1;
        // A supported ladder reaches both the patrol opening and the upper room.
        if (x==2 && z==1 && y<=13)
            return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH);
        if (y==8 || y==14) {
            if(x==2 && z==1) return Blocks.AIR.defaultBlockState();
            return brick();
        }
        if (y<=14) {
            // Doorways on all four faces connect adjacent wall walks and city streets.
            boolean door=(x>=width/2-1&&x<=width/2+1&&(z==0||z==depth-1))
                ||(z>=depth/2-1&&z<=depth/2+1&&(x==0||x==width-1));
            boolean patrolDoor = ((x>=1&&x<=3||x>=width-4&&x<=width-2)&&(z==0||z==depth-1))
                || ((z>=1&&z<=3||z>=depth-4&&z<=depth-2)&&(x==0||x==width-1));
            if((door && y<=3) || (patrolDoor && (y==9||y==10))) return Blocks.AIR.defaultBlockState();
            return edge?brick():Blocks.AIR.defaultBlockState();
        }
        int inset=y-15;
        if(x<inset||x>=width-inset||z<inset||z>=depth-inset) return null;
        return Blocks.NETHER_BRICKS.defaultBlockState();
    }
    private PosadShell() {}
}
