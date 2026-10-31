package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;
import org.slavicmyths.furniture.FurnitureBlock;

/**
 * R3 additions for newly created schema-20 cities only. Existing schema-19
 * cities, original R1 templates, loot, NBT and the approved A+ residence stay
 * untouched. All coordinates are exact city-local Minecraft blocks.
 */
public final class PosadArchitectureR3 {
    private static final Map<Long,BlockState> EDITS=new HashMap<>();
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}

    /** Exact effective old state, including every old roof/lighting overlay. */
    private static BlockState old(int x,int y,int z){
        BlockState b=PosadnikTerem.voxel(x,y,z);
        if(b!=null)return b;
        b=PosadLightingR3.voxel(x,y,z);if(b!=null)return b;
        b=PosadHallR2.voxel(x,y,z);if(b!=null)return b;
        b=PosadEasternRoofRepair.voxel(x,y,z);if(b!=null)return b;
        b=PosadForgeChimneyRoofRepair.voxel(x,y,z);if(b!=null)return b;
        b=PosadDistrictRoofRepair.voxel(x,y,z);if(b!=null)return b;
        b=PosadHallRoofRepair.voxel(x,y,z);if(b!=null)return b;
        b=PosadHallComfort.voxel(x,y,z);if(b!=null)return b;
        b=PosadDruzhinaEntrances.voxel(x,y,z);if(b!=null)return b;
        b=PosadCourtyardFinishes.voxel(x,y,z);if(b!=null)return b;
        b=PosadMarketRenovation.voxel(x,y,z);if(b!=null)return b;
        b=PosadHallRefinement.voxel(x,y,z);if(b!=null)return b;
        b=PosadHouseRenovation.voxel(x,y,z);if(b!=null)return b;
        b=PosadFinishes.voxel(x,y,z);if(b!=null)return b;
        b=PosadMarketBuildings.voxel(x,y,z);if(b!=null)return b;
        b=PosadAdditionalBuildings.voxel(x,y,z);if(b!=null)return b;
        b=PosadNextBuildings.voxel(x,y,z);if(b!=null)return b;
        b=PosadBuildings.voxel(x,y,z);if(b!=null)return b;
        return PosadShell.voxel(x,y,z);
    }

    private static boolean empty(BlockState state){return state==null||state.isAir();}
    private static void put(int x,int y,int z,BlockState state){
        if(!PosadGeometry.CITY.contains(x,z)||y<1||y>17)throw new IllegalStateException("R3 outside city "+x+","+y+","+z);
        if(EDITS.putIfAbsent(key(x,y,z),state)!=null)throw new IllegalStateException("Duplicate R3 coordinate "+x+","+y+","+z);
    }
    private static void emptyAt(int x,int y,int z,BlockState state){
        BlockState previous=old(x,y,z);
        if(!empty(previous))throw new IllegalStateException("R3 new voxel collides with existing "+x+","+y+","+z+" "+previous);
        put(x,y,z,state);
    }
    private static void roof(int x,int y,int z){
        BlockState previous=old(x,y,z);
        if(previous==null||!(previous.is(Blocks.NETHER_BRICK_STAIRS)||previous.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)))
            throw new IllegalStateException("R3 facade eave changed "+x+","+y+","+z+" "+previous);
        put(x,y,z,Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
    }
    private static void extraFurniture(int x,int y,int z,String id,Direction face){
        // Optional named-room improvements never erase a wall, chest, doorway,
        // stair or existing functional furniture.
        if(!PosadGeometry.CITY.contains(x,z)||!empty(old(x,y,z))||!empty(old(x,y+1,z)))return;
        BlockState below=old(x,y-1,z);
        if(empty(below)||below.getBlock() instanceof net.minecraft.world.level.block.StairBlock)return;
        BlockState furniture=Furniture.get(id).defaultBlockState();
        if(furniture.hasProperty(FurnitureBlock.FACING))furniture=furniture.setValue(FurnitureBlock.FACING,face);
        put(x,y,z,furniture);
    }
    private static void streetPost(int x,int z,int xOffset,int zOffset){
        BlockState source=old(x,1,z);
        if(source==null||!source.is(Blocks.LANTERN))throw new IllegalStateException("R3 street light changed at "+x+","+z);
        // Height 1..2 on the original curb, light hanging over the public
        // street at height 3, leaving a two-block-high unobstructed passage.
        put(x,1,z,Blocks.POLISHED_BASALT.defaultBlockState());
        for(int y:new int[]{2,3})emptyAt(x,y,z,Blocks.POLISHED_BASALT.defaultBlockState());
        emptyAt(x,4,z,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        int armX=x+xOffset,armZ=z+zOffset;
        emptyAt(armX,4,armZ,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        emptyAt(armX,3,armZ,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    static {
        // Monumental, genuinely stepped south portico fronton in existing
        // X42..70/Z12..34 plot. R2 pillars at X50/62 carry the Y11 soffit.
        for(int x=51;x<=61;x++)roof(x,12,34);
        for(int y=13;y<=17;y++){
            int halfWidth=17-y;
            for(int x=56-halfWidth;x<=56+halfWidth;x++){
                BlockState material=x==56&&y%2==0?Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState():
                    (Math.abs(x-56)==halfWidth?Blocks.CRIMSON_PLANKS.defaultBlockState():Blocks.NETHER_BRICKS.defaultBlockState());
                emptyAt(x,y,34,material);
            }
        }
        // Gable lamps mounted from actual R2 Y11 soffit, not hanging in air.
        for(int x:new int[]{52,60}){
            BlockState support=old(x,11,34);
            if(support==null||support.isAir())throw new IllegalStateException("R3 portico lacks soffit "+x);
            emptyAt(x,10,34,Blocks.CHAIN.defaultBlockState());
            emptyAt(x,9,34,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        }

        // Existing R1 raised dais at U11..15/V2 is preserved. Add a throne
        // (masonry seat + red back) on it, on the R02 ceremonial axis U13.
        emptyAt(56,2,15,Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));
        for(int x:new int[]{55,57})emptyAt(x,2,15,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        for(int x:new int[]{55,57})emptyAt(x,2,14,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        emptyAt(56,2,14,Blocks.CRIMSON_PLANKS.defaultBlockState());
        emptyAt(56,3,14,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        // The actual council aisle U12..14/V6..19 remains empty and >=3 wide.
        extraFurniture(59,1,17,"blackwood_table",Direction.SOUTH);
        extraFurniture(60,1,17,"blackwood_chair",Direction.WEST);
        for(int x:new int[]{50,62})extraFurniture(x,1,24,"blackwood_bench",Direction.SOUTH);

        // Existing functional furniture: targeted room upgrades away from
        // door mouths and the stairwells. No nether-explosive vanilla beds.
        for(int[] p:new int[][]{{16,1,20},{31,1,20},{16,7,19},{31,7,19}})
            extraFurniture(p[0],p[1],p[2],"blackwood_bedside_cabinet",Direction.SOUTH);
        for(int[] p:new int[][]{{17,1,81},{34,1,81},{17,7,81},{34,7,81}})
            extraFurniture(p[0],p[1],p[2],"blackwood_bedside_cabinet",Direction.SOUTH);
        for(int[] p:new int[][]{{18,1,52},{31,1,51},{19,1,63}})
            extraFurniture(p[0],p[1],p[2],"blackwood_table",Direction.WEST);

        // Eighteen old floor lamps are upgraded to supported basalt lamp-posts.
        // None projects into the city at player head height (Y1/Y2).
        for(int z=37;z<=97;z+=12)streetPost(52,z,1,0);
        for(int z=43;z<=103;z+=12)streetPost(60,z,-1,0);
        for(int z:new int[]{39,75})for(int x:new int[]{43,69})streetPost(x,z,0,z==39?1:-1);
        streetPost(40,56,1,0);
        streetPost(72,56,-1,0);
    }
    public static BlockState voxel(int x,int y,int z){
        if(y<1||y>17||!PosadGeometry.CITY.contains(x,z))return null;
        return EDITS.get(key(x,y,z));
    }
    public static int size(){return EDITS.size();}
    private PosadArchitectureR3(){}
}
