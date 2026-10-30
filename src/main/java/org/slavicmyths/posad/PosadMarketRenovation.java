package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;

/**
 * Schema-9 market furnishings and chest relocation.
 * Original 2.2.6 digital source remains immutable for history/parity checks.
 * Each authored loot table moves with its ONE original chest to a wall-side spot.
 */
public final class PosadMarketRenovation {
    private static final int OX=75, OZ=75;
    private static final Map<Long,BlockState> PATCH=new HashMap<>();
    private static final Map<Long,String> LOOT=new HashMap<>();
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}
    private static void add(int x,int y,int z,BlockState state,boolean requireEmpty){
        BlockState original=PosadMarketBuildings.voxel(OX+x,y,OZ+z);
        if(requireEmpty&&original!=null)throw new IllegalStateException("Market R1 occupied at "+x+","+y+","+z);
        if(PATCH.putIfAbsent(key(OX+x,y,OZ+z),state)!=null)throw new IllegalStateException("Market overlap at "+x+","+y+","+z);
    }
    private static void relocate(int oldX,int oldZ,int newX,int newZ){
        String table=PosadMarketBuildings.chestLoot(OX+oldX,1,OZ+oldZ);
        BlockState original=PosadMarketBuildings.voxel(OX+oldX,1,OZ+oldZ);
        if(table==null||original==null||!original.is(Blocks.CHEST))throw new IllegalStateException("Missing original market loot chest");
        add(oldX,1,oldZ,Blocks.AIR.defaultBlockState(),false);
        BlockState front=Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,newX<13?Direction.EAST:Direction.WEST);
        add(newX,1,newZ,front,true);
        if(LOOT.putIfAbsent(key(OX+newX,1,OZ+newZ),table)!=null)throw new IllegalStateException("Duplicate market reward");
    }
    private static void furnish(int x,int z,String id){add(x,1,z,Furniture.get(id).defaultBlockState(),true);}
    private static void lamp(int x,int z){add(x,1,z,Blocks.SOUL_LANTERN.defaultBlockState(),true);}

    static {
        // Four shop chests, then two chests per warehouse; none remain in the middle.
        relocate(4,3,3,3); relocate(22,3,23,3);
        relocate(4,12,3,12); relocate(22,12,23,12);
        relocate(4,20,3,20); relocate(7,20,3,22);
        relocate(19,20,23,22); relocate(22,20,23,20);

        // Metal trade, food shop, crafting shop, miscellaneous trade.
        furnish(5,3,"pine_weapon_rack"); furnish(7,3,"pine_table");
        furnish(19,3,"linden_table"); furnish(21,3,"linden_shelf");
        furnish(5,12,"pine_table"); furnish(7,15,"pine_stool");
        furnish(21,12,"linden_table"); furnish(19,15,"linden_shelf");
        // Rear raw-material storehouse and high-value merchant warehouse.
        furnish(5,21,"pine_shelf"); furnish(7,21,"pine_bench");
        furnish(19,21,"linden_shelf"); furnish(21,21,"linden_table");

        lamp(7,6);lamp(19,6);lamp(7,14);lamp(19,14);lamp(8,22);lamp(18,22);
        if(LOOT.size()!=8)throw new IllegalStateException("Expected eight market rewards");
    }

    public static BlockState voxel(int cityX,int y,int cityZ){
        if(cityX<75||cityX>101||cityZ<75||cityZ>99||y<1||y>11)return null;
        BlockState patch=PATCH.get(key(cityX,y,cityZ));
        if(patch!=null)return patch;
        // Repeatable crimson accents on the existing wall, not through windows.
        if(y==3&&(cityX==77||cityX==99)
                &&(cityZ==79||cityZ==81||cityZ==87||cityZ==89||cityZ==95||cityZ==97)){
            BlockState original=PosadMarketBuildings.voxel(cityX,y,cityZ);
            if(original!=null&&original.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
                return Blocks.CRIMSON_PLANKS.defaultBlockState();
        }
        return null;
    }
    public static String chestLoot(int cityX,int y,int cityZ){return LOOT.get(key(cityX,y,cityZ));}
    private PosadMarketRenovation(){}
}
