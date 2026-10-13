package org.slavicmyths.chud;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Surface-only palette: every replacement has the same full-cube collision as its input. */
final class ChudDecor {
    private static final Block[][] DRESSED={
        {Blocks.TUFF_BRICKS,Blocks.POLISHED_TUFF,Blocks.CRACKED_STONE_BRICKS,Blocks.CHISELED_TUFF_BRICKS},
        {Blocks.STONE_BRICKS,Blocks.COBBLESTONE,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS},
        {Blocks.DEEPSLATE_BRICKS,Blocks.POLISHED_BASALT,Blocks.CRACKED_DEEPSLATE_BRICKS,Blocks.COBBLED_DEEPSLATE},
        {Blocks.COBBLESTONE,Blocks.TUFF_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.MOSSY_COBBLESTONE},
        {Blocks.STONE_BRICKS,Blocks.POLISHED_ANDESITE,Blocks.CRACKED_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS},
        {Blocks.DEEPSLATE_TILES,Blocks.DEEPSLATE_BRICKS,Blocks.CRACKED_DEEPSLATE_TILES,Blocks.CHISELED_DEEPSLATE}
    };
    private static final Block[][] ROUGH={
        {Blocks.TUFF,Blocks.ANDESITE,Blocks.COBBLESTONE,Blocks.TUFF_BRICKS},
        {Blocks.STONE,Blocks.COBBLESTONE,Blocks.ANDESITE,Blocks.MOSSY_COBBLESTONE},
        {Blocks.DEEPSLATE,Blocks.COBBLED_DEEPSLATE,Blocks.BASALT,Blocks.ANDESITE},
        {Blocks.TUFF,Blocks.COBBLESTONE,Blocks.STONE,Blocks.MOSSY_COBBLESTONE},
        {Blocks.STONE,Blocks.ANDESITE,Blocks.TUFF,Blocks.COBBLESTONE},
        {Blocks.DEEPSLATE,Blocks.COBBLED_DEEPSLATE,Blocks.TUFF,Blocks.CRACKED_DEEPSLATE_BRICKS}
    };
    private final ChudPlan city;
    private final AbandonedPlan abandoned;
    private final BlockPos anchor;
    private final int rotation;
    private final long seed;
    ChudDecor(ChudPlan city,AbandonedPlan abandoned){
        this.city=city;this.abandoned=abandoned;anchor=city!=null?city.anchor:abandoned.anchor;
        rotation=city!=null?city.rotation:abandoned.rotation;seed=city!=null?city.seed:abandoned.seed;
    }
    private int localX(int x,int z){int dx=x-anchor.getX(),dz=z-anchor.getZ();return switch(rotation){case 0->dx;case 1->dz;case 2->-dx;default->-dz;};}
    private int localZ(int x,int z){int dx=x-anchor.getX(),dz=z-anchor.getZ();return switch(rotation){case 0->dz;case 1->-dx;case 2->-dz;default->dx;};}
    /** Zone lookup once per horizontal column, including long distinct corridor segments. */
    int zone(int x,int z){
        int lx=localX(x,z),lz=localZ(x,z);
        if(city!=null)for(var h:city.halls)if(Math.abs(lx-h.x())<=h.width()/2&&Math.abs(lz-h.z())<=h.depth()/2)return h.role().ordinal();
        if(abandoned!=null)for(var h:abandoned.chambers)if(Math.abs(lx-h.x())<=h.width()/2&&Math.abs(lz-h.z())<=h.depth()/2)return 8+switch(h.role()){case QUARTER->1;case GALLERY->3;case SHAFT->2;case LABYRINTH->5;case SEALED_GATE->5;case SECONDARY->Math.floorMod(h.module().ordinal(),6);};
        return (city==null?8:0)+Math.floorMod(Math.floorDiv(lx,24)+Math.floorDiv(lz,24),6);
    }
    BlockState surface(BlockState original,int x,int y,int z,int zone,boolean openAbove){
        boolean dressed=original.is(Blocks.STONE_BRICKS)||original.is(Blocks.CRACKED_STONE_BRICKS);
        boolean rough=original.is(Blocks.STONE)||original.is(Blocks.ANDESITE)||original.is(Blocks.TUFF);
        if(!dressed&&!rough)return original;
        int lx=localX(x,z),lz=localZ(x,z);
        // Only an explicit carved air space qualifies; outside roofs and furniture stay untouched.
        if(openAbove&&ChudDecorPolicy.floorLight(lx,lz))return Blocks.GLOWSTONE.defaultBlockState();
        int family=zone%8,patch=ChudDecorPolicy.patch(seed,lx,y-anchor.getY(),lz,family);
        return (dressed?DRESSED:ROUGH)[family][ChudDecorPolicy.material(patch,zone>=8)].defaultBlockState();
    }
}
