package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.furniture.Furniture;

/**
 * Schema-8 visual/interior pass on the immutable approved Veche R1 footprint.
 * All additional furniture coordinates are proven empty in BOTH R1 and schema-6.
 * No doors, circulation axes, support piers, windows or loot containers move.
 */
public final class PosadHallRefinement {
    private static final Map<Long,BlockState> DECOR = new HashMap<>();

    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }

    private static void add(int u,int y,int v,BlockState block) {
        int x=u+43,z=v+13;
        if(PosadBuildings.voxel(x,y,z)!=null || PosadFinishes.voxel(x,y,z)!=null)
            throw new IllegalStateException("Approved Veche furniture would overwrite R1: "+x+","+y+","+z);
        if(DECOR.putIfAbsent(key(x,y,z),block)!=null)
            throw new IllegalStateException("Duplicate Veche interior coordinate: "+x+","+y+","+z);
    }
    private static void furniture(int u,int y,int v,String name) { add(u,y,v,Furniture.get(name).defaultBlockState()); }
    private static void lantern(int u,int y,int v) { add(u,y,v,Blocks.SOUL_LANTERN.defaultBlockState()); }

    static {
        // 1F: archive, elder's audience hall, treasury, council chamber and guards.
        furniture(2,1,4,"linden_shelf"); furniture(5,1,5,"linden_stool");
        furniture(11,1,4,"pine_bench"); furniture(15,1,4,"pine_bench");
        furniture(19,1,1,"linden_shelf");
        furniture(21,1,4,"linden_shelf"); furniture(24,1,5,"linden_stool");
        furniture(9,1,8,"pine_bench"); furniture(17,1,8,"pine_bench");
        furniture(23,1,9,"linden_chair"); furniture(24,1,13,"linden_shelf");
        furniture(2,1,16,"linden_shelf"); furniture(5,1,17,"linden_stool");
        furniture(8,1,17,"pine_bench"); furniture(18,1,17,"pine_bench");
        furniture(22,1,17,"pine_shelf"); furniture(24,1,16,"pine_weapon_rack");

        // 2F: archives, meeting room, relic chamber, gallery, watch and scribes.
        furniture(2,7,4,"linden_shelf"); furniture(5,7,4,"linden_stool");
        furniture(11,7,4,"pine_bench"); furniture(15,7,4,"pine_bench");
        furniture(22,7,2,"linden_shelf"); furniture(24,7,5,"linden_stool");
        furniture(8,7,7,"linden_bench"); furniture(18,7,7,"linden_bench");
        furniture(23,7,9,"pine_table"); furniture(25,7,9,"pine_chair");
        furniture(2,7,18,"linden_shelf"); furniture(5,7,18,"linden_stool");
        furniture(8,7,18,"linden_table"); furniture(18,7,18,"linden_table");
        furniture(22,7,18,"pine_shelf"); furniture(24,7,17,"pine_weapon_rack");

        lantern(6,1,18); lantern(6,7,18); lantern(21,1,8); lantern(21,7,8);
    }

    public static BlockState voxel(int x,int y,int z) {
        if(x<42||x>70||z<12||z>34||y<1||y>17)return null;
        BlockState decor=DECOR.get(key(x,y,z));
        if(decor!=null)return decor;

        BlockState base=PosadBuildings.voxel(x,y,z);
        if(base==null)return null;

        // Deliberate facade rhythm, rather than isolated random stone patches.
        boolean front=(z==13||z==33)&&(x==45||x==53||x==59||x==67);
        boolean flank=(x==43||x==69)&&(z==16||z==20||z==24||z==28||z==32);
        if((y==3||y==9)&&front&&base.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
            return Blocks.GILDED_BLACKSTONE.defaultBlockState();
        if((y==3||y==9)&&flank&&base.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
            return Blocks.CRIMSON_PLANKS.defaultBlockState();

        // Dark structural ribs on the existing north/south roof stairs.
        if(y>=12&&base.is(Blocks.NETHER_BRICK_STAIRS)&&Math.floorMod(x-43,7)==0)
            return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,base.getValue(StairBlock.FACING));
        return null;
    }

    private PosadHallRefinement() {}
}
