package org.slavicmyths.bayun;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slavicmyths.yaga.YagaPlacement;

/** Existing nook geometry, with a small supported platform instead of a cleared 13x13x28 prism. */
public final class BayunPlacement {
    private static YagaPlacement.Site fail(YagaPlacement.Site site,String reason,BlockPos pos){site.failure=YagaPlacement.Result.fail(reason,pos);return site;}
    public static YagaPlacement.Site prepare(LevelAccessor world,BlockPos candidate){
        var site=new YagaPlacement.Site();site.anchor=candidate;
        if(!world.hasChunksAt(candidate.offset(-6,0,-6),candidate.offset(6,0,6)))return fail(site,"chunks",candidate);
        int[][] ground=new int[13][13];int min=world.getMaxBuildHeight(),max=world.getMinBuildHeight()-1;
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){
            int y=YagaPlacement.ground(world,candidate.getX()+x,candidate.getZ()+z);
            if(y<world.getMinBuildHeight())return fail(site,"surface",candidate.offset(x,0,z));
            ground[x+6][z+6]=y;min=Math.min(min,y);max=Math.max(max,y);
        }
        // Retain the old natural slope limit, but support the platform rather than requiring air at a common plane.
        if(max-min>4)return fail(site,"slope",candidate);
        int y=max+1;if(y+3>=world.getMaxBuildHeight())return fail(site,"height",candidate);site.anchor=candidate.atY(y);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
            for(int h=ground[x+6][z+6]+1;h<y-1;h++)put(world,site,new BlockPos(candidate.getX()+x,h,candidate.getZ()+z),Blocks.COBBLESTONE.defaultBlockState());
            put(world,site,site.anchor.offset(x,-1,z),(Math.floorMod(x+z,3)==0?Blocks.MOSSY_COBBLESTONE:Blocks.STONE_BRICKS).defaultBlockState());
            for(int h=0;h<3;h++)put(world,site,site.anchor.offset(x,h,z),Blocks.AIR.defaultBlockState());
        }
        for(int x:new int[]{-4,4})for(int z:new int[]{-4,4}){
            for(int h=ground[x+6][z+6]+1;h<y;h++)put(world,site,new BlockPos(candidate.getX()+x,h,candidate.getZ()+z),Blocks.DARK_OAK_LOG.defaultBlockState());
            put(world,site,site.anchor.offset(x,-1,z),Blocks.DARK_OAK_LOG.defaultBlockState());
            put(world,site,site.anchor.offset(x,0,z),Blocks.MOSS_BLOCK.defaultBlockState());
        }
        for(int h=0;h<3;h++)put(world,site,site.anchor.offset(3,h,3),Blocks.IRON_BARS.defaultBlockState());
        return site;
    }
    private static void put(LevelAccessor world,YagaPlacement.Site site,BlockPos pos,BlockState next){
        if(site.failure!=null)return;
        var old=world.getBlockState(pos);
        boolean knownPlacedLog=world instanceof net.minecraft.server.level.ServerLevel level&&org.slavicmyths.rpg.tools.ToolLogOrigins.get(level).blocked(pos);
        boolean natural=org.slavicmyths.worldgen.ManualStructureJobs.natural(old)||old.is(Blocks.DIRT)||old.is(Blocks.COARSE_DIRT)||old.is(Blocks.PODZOL)||old.is(Blocks.STONE)||old.is(Blocks.DEEPSLATE);
        if(world.getBlockEntity(pos)!=null||knownPlacedLog||old.is(Blocks.BEDROCK)||old.is(Blocks.NETHER_PORTAL)||old.is(Blocks.END_PORTAL)||old.is(Blocks.END_GATEWAY)||old.is(Blocks.END_PORTAL_FRAME)||!natural){fail(site,"protected",pos);return;}
        site.old.putIfAbsent(pos.immutable(),old);site.desired.put(pos.immutable(),next);
    }
    private BayunPlacement(){}
}
