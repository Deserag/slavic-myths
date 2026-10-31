package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Schema 18: light-only corrections grounded in 4-rotation physical Minecraft LightLayer.BLOCK measurements.
 * R1, older saved schemas, loot and all existing furniture IDs remain immutable.
 */
public final class PosadLightingR3 {
    private static final Map<Long,BlockState> LIGHTS=new HashMap<>();
    private static int halls=0,roads=0;
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}
    private static void put(int x,int y,int z) {
        if(LIGHTS.putIfAbsent(key(x,y,z),Blocks.LANTERN.defaultBlockState())!=null)
            throw new IllegalStateException("Duplicate city light "+x+","+y+","+z);
    }
    private static BlockState formerHall(int x,int y,int z) {
        BlockState s=PosadHallR2.voxel(x,y,z);
        if(s==null)s=PosadHallRoofRepair.voxel(x,y,z);
        if(s==null)s=PosadHallComfort.voxel(x,y,z);
        if(s==null)s=PosadHallRefinement.voxel(x,y,z);
        if(s==null)s=PosadFinishes.voxel(x,y,z);
        return s==null?PosadBuildings.voxel(x,y,z):s;
    }
    private static void hall(int u,int y,int v) {
        int x=43+u,z=13+v;
        if(!PosadGeometry.Slot.VECHE_HALL.local().contains(x,z) || (y!=1&&y!=7))
            throw new IllegalStateException("Hall light outside plot");
        BlockState old=formerHall(x,y,z);
        BlockState under=PosadBuildings.voxel(x,y-1,z);
        if(old!=null || under==null || under.isAir())
            throw new IllegalStateException("Occupied/unsupported hall light at "+x+","+y+","+z+" old="+old+" under="+under);
        put(x,y,z);halls++;
    }
    private static void road(int x,int z) {
        if(!PosadGeometry.CITY.contains(x,z)||PosadShell.voxel(x,1,z)!=null)
            throw new IllegalStateException("Road light obstructs original shell "+x+","+z);
        BlockState under=PosadShell.voxel(x,0,z);
        if(under==null||under.isAir())throw new IllegalStateException("Unsupported road light "+x+","+z);
        for(var slot:PosadGeometry.Slot.values())
            if(slot!=PosadGeometry.Slot.VECHE_SQUARE && slot.local().contains(x,z))
                throw new IllegalStateException("Road light intrudes "+slot+" "+x+","+z);
        put(x,1,z);roads++;
    }
    static {
        // Actual schema-17 light: hall entry 3, main nave 6, elder 2, upper north 8.
        // Symmetric light sources off the central three-wide ceremonial aisle.
        hall(9,1,18);hall(17,1,18);
        hall(8,1,11);hall(18,1,11);
        hall(10,1,8);hall(16,1,8);
        hall(10,1,5);hall(16,1,5);
        hall(10,7,5);hall(16,7,5);

        // Actual schema-17 square and road light: 0. Leave x53..59 carriageway unobstructed.
        for(int z=37;z<=97;z+=12)road(52,z);
        for(int z=43;z<=103;z+=12)road(60,z);
        for(int z:new int[]{39,75})for(int x:new int[]{43,69})road(x,z);
        for(int x:new int[]{40,72})road(x,56);
        if(halls!=10||roads!=18||LIGHTS.size()!=28)throw new IllegalStateException("Lighting count mismatch");
    }
    public static BlockState voxel(int x,int y,int z){return y==1||y==7?LIGHTS.get(key(x,y,z)):null;}
    public static BlockState hallVoxel(int x,int y,int z){
        return PosadGeometry.Slot.VECHE_HALL.local().contains(x,z)?voxel(x,y,z):null;
    }
    private PosadLightingR3(){}
}
