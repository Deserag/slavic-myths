package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * New-city schema-17 Veche R2 architectural shell. The R1 source, all 18
 * functional zones and loot NBT are immutable. R05 and R08 become a shared
 * 13x13 ceremonial nave with an open south entry bay (R08), rather than two
 * rooms divided by a narrow arch. The second floor and staircase stay fixed.
 * Each edit validates the exact historic R1 voxel before being registered.
 *
 * One source of truth: called by both PosadPiece and standalone PosadCommands.
 */
public final class PosadHallR2 {
    private static final Map<Long, BlockState> PATCH = new HashMap<>();

    private static long key(int x,int y,int z) { return ((long)x<<32)|((long)y<<16)|z; }

    private static void edit(int u,int y,int v,Block expected,BlockState replacement) {
        int x=43+u,z=13+v;
        if(!PosadGeometry.Slot.VECHE_HALL.local().contains(x,z) || y<0 || y>16)
            throw new IllegalStateException("R2 outside reserved Veche plot: "+x+","+y+","+z);
        BlockState source=PosadBuildings.voxel(x,y,z);
        if(expected==null ? source!=null : source==null || !source.is(expected))
            throw new IllegalStateException("Veche R1 changed at "+x+","+y+","+z+": "+source);
        if(PATCH.putIfAbsent(key(x,y,z),replacement)!=null)
            throw new IllegalStateException("Duplicate Veche R2 voxel "+x+","+y+","+z);
    }

    /** Only these 24 historic partitions are permitted to clear natural blocks. */
    public static boolean isOpening(int x,int y,int z) {
        return z==28 && y>=1 && y<=4 && ((x>=51 && x<=53) || (x>=59 && x<=61));
    }

    static {
        // Keep both masonry end piers and the complete Y5 lintel/upper floor.
        // R05 + the R08 south bay share 13x13 interior without changing stairs.
        for(int y=1;y<=4;y++) {
            for(int u:new int[]{8,9,10,16,17,18})edit(u,y,15,Blocks.NETHER_BRICKS,Blocks.AIR.defaultBlockState());
            for(int u:new int[]{7,19})edit(u,y,15,Blocks.NETHER_BRICKS,Blocks.POLISHED_BASALT.defaultBlockState());
        }
        for(int u:new int[]{7,13,19})edit(u,5,15,Blocks.NETHER_BRICKS,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());

        // Two paired, genuine 2x2 windows on each floor of the south facade.
        for(int y:new int[]{2,3,8,9})
            for(int u:new int[]{9,10,16,17})
                edit(u,y,20,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.IRON_BARS.defaultBlockState());

        // A supported monumental entrance portico exactly inside v21 of the
        // reserved plot; the existing Y0 city deck supports both uprights.
        for(int u:new int[]{7,19}) {
            BlockState deck=PosadShell.voxel(43+u,0,34);
            if(deck==null || deck.isAir())throw new IllegalStateException("Veche portico lacks city deck at u="+u);
            edit(u,0,21,null,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            for(int y=1;y<=10;y++) {
                BlockState column=y==1 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                    : y==10 ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState()
                    : Blocks.POLISHED_BASALT.defaultBlockState();
                edit(u,y,21,null,column);
            }
        }

        // The original double door is untouched. Solid jambs and a carved
        // three-stone lintel make its surrounding entrance legible at scale.
        for(int u:new int[]{11,15})
            for(int y=1;y<=5;y++)
                edit(u,y,20,Blocks.POLISHED_BLACKSTONE_BRICKS,y==5
                    ? Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState()
                    : Blocks.POLISHED_BASALT.defaultBlockState());
        for(int u:new int[]{12,13,14})
            edit(u,4,20,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());

        // Solid south/north roof soffits. These slabs lie directly beneath
        // the existing uninterrupted R1 eave stairs, not on arbitrary rows.
        for(int u=-1;u<=27;u++)for(int v:new int[]{-1,21}) {
            BlockState roof=PosadBuildings.voxel(43+u,12,13+v);
            if(roof==null || !roof.is(Blocks.NETHER_BRICK_STAIRS))
                throw new IllegalStateException("Veche R1 eave missing at "+u+","+v);
            edit(u,11,v,null,Blocks.NETHER_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        }

        // Existing end gables are physically complete. Open real narrow
        // ventilation windows and frame them rather than filling fictive holes.
        for(int u:new int[]{0,26}) {
            for(int y:new int[]{14,15}) {
                edit(u,y,10,Blocks.NETHER_BRICKS,Blocks.IRON_BARS.defaultBlockState());
                for(int v:new int[]{9,11})
                    edit(u,y,v,Blocks.NETHER_BRICKS,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            }
            edit(u,13,10,Blocks.NETHER_BRICKS,Blocks.CRIMSON_PLANKS.defaultBlockState());
            edit(u,16,10,Blocks.NETHER_BRICKS,Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
        }
        if(PATCH.size()!=160)throw new IllegalStateException("Veche R2 edit count changed: "+PATCH.size());
    }

    public static BlockState voxel(int x,int y,int z) {
        if(!PosadGeometry.Slot.VECHE_HALL.local().contains(x,z) || y<0 || y>16)return null;
        return PATCH.get(key(x,y,z));
    }

    private PosadHallR2() {}
}
