package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;
import org.slavicmyths.furniture.FurnitureBlock;

/**
 * Schema-12 Veche quality pass, deliberately applied only to NEW cities.
 * Repairs unsupported shelves, clears circulation around the upper gallery,
 * adds supported light near the staircase and rooms, and replaces the ore-like
 * facade trim. The original R1, saved schemas and reward chests remain intact.
 */
public final class PosadHallComfort {
    private static final Map<Long, BlockState> PATCH = new HashMap<>();
    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }
    private static BlockState original(int x,int y,int z) { return PosadBuildings.voxel(x,y,z); }
    private static BlockState oldFinish(int x,int y,int z) {
        BlockState b=PosadHallRefinement.voxel(x,y,z);
        return b!=null?b:PosadFinishes.voxel(x,y,z);
    }
    private static void put(int u,int y,int v,BlockState state) {
        int x=43+u,z=13+v;
        if(PATCH.putIfAbsent(key(x,y,z),state)!=null)
            throw new IllegalStateException("Duplicate Veche comfort patch "+x+","+y+","+z);
    }
    private static void clear(int u,int y,int v,String name) {
        int x=43+u,z=13+v;
        if(original(x,y,z)!=null || oldFinish(x,y,z)==null || !oldFinish(x,y,z).is(Furniture.get(name)))
            throw new IllegalStateException("Veche circulation source changed at "+x+","+y+","+z);
        put(u,y,v,Blocks.AIR.defaultBlockState());
    }
    private static void place(int u,int y,int v,String name,Direction facing) {
        int x=43+u,z=13+v;
        if(original(x,y,z)!=null || oldFinish(x,y,z)!=null || original(x,y-1,z)==null
                || original(x,y+1,z)!=null || original(x,y+2,z)!=null)
            throw new IllegalStateException("Veche furniture would obstruct R1 at "+x+","+y+","+z);
        BlockState state=Furniture.get(name).defaultBlockState().setValue(FurnitureBlock.FACING,facing);
        if(name.endsWith("_shelf"))checkShelfSupport(x,y,z,facing);
        put(u,y,v,state);
    }
    private static void checkShelfSupport(int x,int y,int z,Direction front) {
        Direction back=front.getOpposite();
        BlockState wall=original(x+back.getStepX(),y,z+back.getStepZ());
        if(wall==null || !(wall.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || wall.is(Blocks.NETHER_BRICKS)
                || wall.is(Blocks.CRIMSON_PLANKS) || wall.is(Blocks.BOOKSHELF)))
            throw new IllegalStateException("Veche shelf lacks original wall support at "+x+","+y+","+z);
    }
    private static void refaceShelf(int u,int y,int v,String name,Direction facing) {
        int x=43+u,z=13+v;
        if(original(x,y,z)!=null || oldFinish(x,y,z)==null || !oldFinish(x,y,z).is(Furniture.get(name)))
            throw new IllegalStateException("Veche original shelf changed at "+x+","+y+","+z);
        checkShelfSupport(x,y,z,facing);
        put(u,y,v,Furniture.get(name).defaultBlockState().setValue(FurnitureBlock.FACING,facing));
    }
    private static void floorLamp(int u,int y,int v) {
        int x=43+u,z=13+v;
        if(original(x,y-1,z)==null || original(x,y,z)!=null || oldFinish(x,y,z)!=null)
            throw new IllegalStateException("Veche lamp is not on an empty floor at "+x+","+y+","+z);
        put(u,y,v,Blocks.LANTERN.defaultBlockState());
    }
    private static void wallLight(int u,int y,int v,Direction front) {
        int x=43+u,z=13+v;
        if(original(x,y,z)!=null || oldFinish(x,y,z)!=null)
            throw new IllegalStateException("Veche wall light would overwrite an existing block at "+x+","+y+","+z);
        Direction back=front.getOpposite();
        BlockState wall=original(x+back.getStepX(),y,z+back.getStepZ());
        if(wall==null || !wall.is(Blocks.NETHER_BRICKS))
            throw new IllegalStateException("Veche wall torch has no original masonry support at "+x+","+y+","+z);
        put(u,y,v,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,front));
    }
    private static void facade(int x,int y,int z) {
        BlockState original=original(x,y,z),old=oldFinish(x,y,z);
        if(original==null || !original.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
                || old==null || !old.is(Blocks.GILDED_BLACKSTONE))
            throw new IllegalStateException("Veche ore-like facade source changed at "+x+","+y+","+z);
        put(x-43,y,z-13,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
    }
    private static void roofRib(int u,int y,int v,boolean rib) {
        int x=43+u,z=13+v;
        BlockState base=original(x,y,z);
        if(base==null || !base.is(Blocks.NETHER_BRICK_STAIRS))
            throw new IllegalStateException("Veche roof slope R1 mismatch at "+x+","+y+","+z);
        BlockState state=rib
                ? Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,base.getValue(StairBlock.FACING))
                : base;
        put(u,y,v,state);
    }
    static {
        // Do not put freestanding shelves where they cannot survive. These
        // seven approved-room relocations retain the ORIGINAL furniture IDs.
        clear(22,1,2,"linden_shelf"); place(22,1,1,"linden_shelf",Direction.SOUTH);
        clear(24,1,12,"pine_shelf"); place(25,1,11,"pine_shelf",Direction.WEST);
        clear(21,1,4,"linden_shelf"); place(21,1,5,"linden_shelf",Direction.NORTH);
        clear(24,1,13,"linden_shelf"); place(25,1,13,"linden_shelf",Direction.WEST);
        clear(22,1,17,"pine_shelf"); place(22,1,19,"pine_shelf",Direction.NORTH);
        clear(2,7,4,"linden_shelf"); place(1,7,4,"linden_shelf",Direction.EAST);
        clear(22,7,18,"pine_shelf"); place(22,7,19,"pine_shelf",Direction.NORTH);
        // Six remaining shelves: their default NORTH facing had no guaranteed
        // support. Aim each toward the free room and back against real masonry.
        refaceShelf(5,1,2,"linden_shelf",Direction.WEST);
        refaceShelf(2,1,4,"linden_shelf",Direction.EAST);
        refaceShelf(19,1,1,"linden_shelf",Direction.SOUTH);
        refaceShelf(2,1,16,"linden_shelf",Direction.SOUTH);
        refaceShelf(22,7,2,"linden_shelf",Direction.WEST);
        refaceShelf(2,7,18,"linden_shelf",Direction.SOUTH);

        // Keep the two-block gallery edge free of loose furniture. R1
        // trapdoor rails and the approved floor opening are untouched.
        clear(8,7,7,"linden_bench"); clear(18,7,7,"linden_bench");
        clear(8,7,8,"linden_table"); clear(18,7,8,"linden_table");
        // Hall R05 already includes built-in R1 benches. Remove unrelated
        // clutter along their northern approach and central council lanes.
        clear(8,1,8,"pine_table"); clear(18,1,8,"pine_table");
        clear(9,1,8,"pine_bench"); clear(17,1,8,"pine_bench");
        // Move two writers' tables from the middle of the southern upper room
        // toward its solid north wall; do not pinch the central passage.
        clear(10,7,18,"linden_table"); clear(16,7,18,"linden_table");
        place(10,7,16,"linden_table",Direction.SOUTH);
        place(16,7,16,"linden_table",Direction.SOUTH);

        // Light 15 from warm floor lanterns. Both ends of the staircase and
        // corners of upper council/scribes are covered, without putting lamps
        // in the open gallery shaft or blocking the stairs.
        for(int[] p:new int[][]{{1,1,8},{5,1,14},{1,7,8},{5,7,14},
                {8,7,1},{18,7,1},{8,7,19},{18,7,19}})floorLamp(p[0],p[1],p[2]);
        // Brighter wall torches in the otherwise unlit vertical run, gallery,
        // lower hall. All eight mount onto ACTUAL R1 nether-brick partitions.
        wallLight(5,8,10,Direction.WEST);wallLight(5,8,12,Direction.WEST);
        wallLight(7,8,12,Direction.EAST);wallLight(19,8,12,Direction.WEST);
        wallLight(10,8,7,Direction.SOUTH);wallLight(10,8,14,Direction.NORTH);
        wallLight(7,2,10,Direction.EAST);wallLight(19,2,12,Direction.WEST);

        // Remove 16 ore-like gilded patches from four approved facade edges.
        for(int z:new int[]{13,33})for(int y:new int[]{3,9})for(int x:new int[]{45,53,59,67})facade(x,y,z);

        // Correct the asymmetric schema-8 decorative ribs on the original
        // complete R1 double-pitch roof: five paired cross-slope positions,
        // symmetric about u=13. Do not change the slopes, ridge or roof height.
        for(int y=12;y<=17;y++)for(int v=-1;v<=21;v++) {
            for(int u:new int[]{7,14,21}) {
                BlockState base=original(43+u,y,13+v);
                if(base!=null && base.is(Blocks.NETHER_BRICK_STAIRS))roofRib(u,y,v,false);
            }
            for(int u:new int[]{0,6,13,20,26}) {
                BlockState base=original(43+u,y,13+v);
                if(base!=null && base.is(Blocks.NETHER_BRICK_STAIRS))roofRib(u,y,v,true);
            }
        }
    }

    public static BlockState voxel(int cityX,int y,int cityZ) {
        if(cityX<42||cityX>70||cityZ<12||cityZ>34||y<1||y>17)return null;
        return PATCH.get(key(cityX,y,cityZ));
    }
    private PosadHallComfort() {}
}
