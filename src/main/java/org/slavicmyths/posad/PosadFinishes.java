package org.slavicmyths.posad;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;

/** New-city-only hall improvements; no retrofit of existing structures. */
public final class PosadFinishes {
    private static final Map<Long,BlockState> DECOR=new HashMap<>();
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}
    private static void item(int u,int y,int v,String id){DECOR.put(key(u+43,y,v+13),Furniture.get(id).defaultBlockState());}
    private static void lamp(int u,int y,int v){DECOR.put(key(u+43,y,v+13),Blocks.SOUL_LANTERN.defaultBlockState());}
    static {
        item(4,1,4,"linden_table");item(3,1,4,"linden_chair");item(5,1,2,"linden_shelf");
        item(9,1,4,"pine_table");item(8,1,4,"pine_chair");item(17,1,4,"pine_bench");
        item(22,1,2,"linden_shelf");item(23,1,3,"linden_table");
        item(8,1,8,"pine_table");item(18,1,8,"pine_table");
        item(23,1,8,"linden_table");item(24,1,12,"pine_shelf");
        // Treasury and upper relic room: both chests are beside the east wall.
        DECOR.put(key(68,1,16),Blocks.CHEST.defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING,net.minecraft.core.Direction.WEST));
        DECOR.put(key(68,7,16),Blocks.CHEST.defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING,net.minecraft.core.Direction.WEST));
        item(23,1,18,"pine_weapon_rack");item(25,1,18,"pine_bench");
        item(3,1,18,"linden_table");item(4,1,18,"linden_chair");
        item(4,7,4,"linden_table");item(3,7,4,"linden_chair");
        item(9,7,4,"pine_table");item(17,7,4,"linden_table");
        item(8,7,8,"linden_table");item(18,7,8,"linden_table");
        item(23,7,12,"pine_table");item(25,7,12,"pine_chair");
        item(3,7,18,"linden_table");item(10,7,18,"linden_table");item(16,7,18,"linden_table");
        item(23,7,18,"pine_weapon_rack");item(25,7,18,"pine_bench");
        for(int[] a:new int[][]{{1,1,1},{25,1,1},{1,1,19},{25,1,19},{7,1,7},{19,1,7},{7,1,16},{19,1,16},{1,7,1},{25,7,1},{1,7,19},{25,7,19},{7,7,7},{19,7,7}})lamp(a[0],a[1],a[2]);
    }
    public static BlockState voxel(int x,int y,int z) {
        if(x<42||x>70||z<12||z>34||y<1||y>17)return null;
        BlockState base=PosadBuildings.voxel(x,y,z);
        // R1 supplied a three-high OPEN front entrance but no actual door.
        // Make it a true two-leaf entrance, close the third side with a frame
        // and provide a solid lintel above both door leaves.
        if(z==33 && y<=3 && base==null) {
            if(x==57)return Blocks.CRIMSON_PLANKS.defaultBlockState();
            if((x==55||x==56) && y==3)return Blocks.CRIMSON_PLANKS.defaultBlockState();
            if((x==55||x==56) && (y==1||y==2))
                return Blocks.CRIMSON_DOOR.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.DoorBlock.FACING,net.minecraft.core.Direction.SOUTH)
                    .setValue(net.minecraft.world.level.block.DoorBlock.HINGE,x==55?net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT:net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT)
                    .setValue(net.minecraft.world.level.block.DoorBlock.HALF,y==1?net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER:net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        }
        if(y==12&&x>=43&&x<=69&&z>=13&&z<=33&&base==null)return Blocks.CRIMSON_PLANKS.defaultBlockState();
        if(y>=12&&base!=null&&base.is(Blocks.NETHER_BRICK_STAIRS)&&Math.floorMod(x+z,5)==0)
            return Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairBlock.FACING,base.getValue(StairBlock.FACING));
        if(base!=null&&base.is(Blocks.POLISHED_BLACKSTONE_BRICKS)&&y>=2&&y<=5
                &&(((z==13||z==33)&&(x==47||x==51||x==61||x==65))
                    ||((x==43||x==69)&&(z==17||z==21||z==27||z==30))))
            return Blocks.CRIMSON_PLANKS.defaultBlockState();
        BlockState finish=DECOR.get(key(x,y,z));
        return base==null?finish:null;
    }
    public static String chestLoot(int x,int y,int z){
        if(x==68&&z==16&&y==1)return "chests/nether_veche_treasury";
        if(x==68&&z==16&&y==7)return "chests/nether_veche_relics";
        return null;
    }
    public static String barrelLoot(int x,int z){return PosadGeometry.Slot.MENOVYE_RYADY.local().contains(x,z)?"chests/posad_barrel_market":"chests/posad_barrel_supplies";}
    private PosadFinishes(){}
}
