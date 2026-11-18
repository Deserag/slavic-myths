package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;
import org.slavicmyths.furniture.FurnitureBlock;

/**
 * Schema-10 new-city-only furnishing for the R1 military and forge districts.
 * The immutable R1 geometry, doors, stairs, roofs and five loot chests per
 * district are never rewritten. Used by native and standalone placement.
 */
public final class PosadCourtyardFinishes {
    private static final Map<Long, BlockState> DECOR = new HashMap<>();
    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }

    private static void put(int x,int y,int z,BlockState state) {
        if (DECOR.putIfAbsent(key(x,y,z),state)!=null) throw new IllegalStateException("Duplicate courtyard finish at "+x+","+y+","+z);
    }
    private static void furniture(int ox,int oz,int u,int y,int v,String name) {
        int x=ox+u,z=oz+v;
        BlockState floor=PosadNextBuildings.voxel(x,y-1,z);
        if (floor==null || floor.isAir() || PosadNextBuildings.voxel(x,y,z)!=null || PosadNextBuildings.voxel(x,y+1,z)!=null || PosadNextBuildings.voxel(x,y+2,z)!=null)
            throw new IllegalStateException("Courtyard furniture obstructs approved R1 at "+x+","+y+","+z);
        BlockState state=Furniture.get(name).defaultBlockState();
        if (name.endsWith("_shelf")) {
            Direction facing=null;
            // Shelves require a sturdy R1 wall at their back. Match the
            // registered FurnitureBlock.canSurvive rule and rotate the facing.
            for (Direction front : new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}) {
                Direction back=front.getOpposite();
                BlockState wall=PosadNextBuildings.voxel(x+back.getStepX(),y,z+back.getStepZ());
                if (wall!=null && (wall.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || wall.is(Blocks.NETHER_BRICKS) || wall.is(Blocks.CRIMSON_PLANKS))) {
                    facing=front;
                    break;
                }
            }
            if (facing==null) throw new IllegalStateException("Unsupported courtyard shelf at "+x+","+y+","+z);
            state=state.setValue(FurnitureBlock.FACING,facing);
        }
        put(x,y,z,state);
    }
    private static void light(int ox,int oz,int u,int y,int v) {
        int x=ox+u,z=oz+v;
        BlockState floor=PosadNextBuildings.voxel(x,y-1,z);
        if (floor==null || floor.isAir() || PosadNextBuildings.voxel(x,y,z)!=null || PosadNextBuildings.voxel(x,y+1,z)!=null || PosadNextBuildings.voxel(x,y+2,z)!=null)
            throw new IllegalStateException("Courtyard lamp obstructs approved R1 at "+x+","+y+","+z);
        put(x,y,z,Blocks.SOUL_LANTERN.defaultBlockState());
    }
    private static void accent(int ox,int oz,int u,int y,int v,BlockState newState) {
        int x=ox+u,z=oz+v;
        BlockState original=PosadNextBuildings.voxel(x,y,z);
        if (original==null || !original.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
            throw new IllegalStateException("Courtyard facade requires original solid R1 wall at "+x+","+y+","+z);
        put(x,y,z,newState);
    }
    private static void military(int u,int y,int v,String name) { furniture(12,75,u,y,v,name); }
    private static void forge(int u,int y,int v,String name) { furniture(12,44,u,y,v,name); }
    private static void militaryLight(int u,int y,int v) { light(12,75,u,y,v); }
    private static void forgeLight(int u,int y,int v) { light(12,44,u,y,v); }
    private static void militaryAccent(int u,int y,int v,BlockState state) { accent(12,75,u,y,v,state); }
    private static void forgeAccent(int u,int y,int v,BlockState state) { accent(12,44,u,y,v,state); }

    static {
        // R01 armory, R02 mess, R03 barracks, R04 supply.
        military(4,1,3,"pine_weapon_rack"); military(3,1,5,"pine_shelf");
        military(20,1,2,"linden_shelf"); military(21,1,4,"pine_table"); military(21,1,5,"pine_chair");
        military(4,1,7,"pine_bedside_cabinet"); military(6,1,10,"pine_bench");
        military(20,1,8,"pine_shelf"); military(21,1,10,"linden_table");
        // R05 council, R06 captain, R07 upper barracks, R08 equipment.
        military(4,7,2,"pine_weapon_rack"); military(6,7,4,"pine_table"); military(7,7,4,"pine_chair");
        military(20,7,2,"linden_shelf"); military(20,7,4,"linden_table"); military(21,7,4,"linden_chair");
        military(4,7,7,"pine_bedside_cabinet"); military(6,7,10,"pine_bench");
        military(20,7,7,"pine_weapon_rack"); military(21,7,10,"pine_shelf"); military(23,7,10,"pine_table");
        militaryLight(15,1,3); militaryLight(15,1,9); militaryLight(15,7,3); militaryLight(15,7,9);

        // Forge: one open working hall, storehouse, yard and roofed work bay.
        forge(4,1,3,"pine_shelf"); forge(11,1,3,"linden_shelf");
        forge(12,1,8,"pine_table"); forge(13,1,9,"pine_stool"); forge(4,1,10,"pine_weapon_rack");
        forge(5,1,19,"pine_shelf"); forge(6,1,20,"linden_table"); forge(10,1,19,"linden_shelf");
        forge(16,1,4,"pine_weapon_rack"); forge(21,1,9,"pine_table"); forge(20,1,15,"pine_bench");
        forge(18,1,22,"linden_table"); forge(21,1,22,"pine_bench");
        forgeLight(5,1,6); forgeLight(9,1,7); forgeLight(7,1,21);
        forgeLight(18,1,20); forgeLight(23,1,16);

        // Solid outer masonry only: leave roof slopes, doors and windows R1.
        for (int x : new int[]{4,9,21,24}) for (int v : new int[]{1,11})
            militaryAccent(x,4,v,Blocks.CRIMSON_PLANKS.defaultBlockState());
        for (int x : new int[]{2,25}) for (int v : new int[]{3,9})
            militaryAccent(x,4,v,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        for (int v : new int[]{4,6,10,12}) {
            forgeAccent(2,4,v,Blocks.CRIMSON_PLANKS.defaultBlockState());
            forgeAccent(15,4,v,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        }
        for (int x : new int[]{2,11}) for (int v : new int[]{19,22})
            forgeAccent(x,4,v,Blocks.CRIMSON_PLANKS.defaultBlockState());
    }

    public static BlockState voxel(int cityX,int y,int cityZ) {
        if (y<1 || y>17 || !(PosadGeometry.Slot.DRUZHINNY_DVOR.local().contains(cityX,cityZ) || PosadGeometry.Slot.KUZNECHNY_DVOR.local().contains(cityX,cityZ))) return null;
        return DECOR.get(key(cityX,y,cityZ));
    }

    private PosadCourtyardFinishes() {}
}
