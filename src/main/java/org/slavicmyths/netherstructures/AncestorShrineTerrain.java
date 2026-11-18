package org.slavicmyths.netherstructures;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;

/**
 * Bounded Nether 3D noise-column preflight. No heightmap of the bedrock ceiling,
 * chunk loads, block writes, delayed tasks or claim-unaware terrain excavation.
 */
public final class AncestorShrineTerrain {
    public record Plan(BlockPos center,Rotation rotation,int variant,long seed) {
        public BlockPos at(int x,int y,int z) {
            return center.offset(new BlockPos(x-6,y,z-7).rotate(rotation));
        }
        public BoundingBox bounds() {
            BlockPos a=at(0,0,0),b=at(12,10,14);
            return new BoundingBox(Math.min(a.getX(),b.getX()),center.getY(),Math.min(a.getZ(),b.getZ()),
                    Math.max(a.getX(),b.getX()),center.getY()+10,Math.max(a.getZ(),b.getZ()));
        }
    }
    private static long mix(long x) {
        x=(x^(x>>>30))*-4658895280553007687L;
        x=(x^(x>>>27))*-7723592293110705685L;
        return x^(x>>>31);
    }
    static boolean natural(BlockState s) {
        return s.is(Blocks.NETHERRACK)||s.is(Blocks.BLACKSTONE)||s.is(Blocks.BASALT)||
                s.is(Blocks.SOUL_SAND)||s.is(Blocks.SOUL_SOIL)||s.is(Blocks.GRAVEL)||
                s.is(Blocks.NETHER_QUARTZ_ORE)||s.is(Blocks.NETHER_GOLD_ORE)||
                s.is(Blocks.MAGMA_BLOCK)||
                s.is(org.slavicmyths.blackcinder.CinderBlocks.get("black_cinder_soil"))||
                s.is(org.slavicmyths.blackcinder.CinderBlocks.get("ash_crust"))||
                s.is(org.slavicmyths.blackcinder.CinderBlocks.get("smoking_cinder"))||
                s.is(org.slavicmyths.blackcinder.CinderBlocks.get("charred_stone"));
    }
    private static long packed(int x,int z) {return ((long)x<<32)^(z&0xffffffffL);}
    public static Plan find(Structure.GenerationContext c) {
        int bx=c.chunkPos().getMiddleBlockX(),bz=c.chunkPos().getMiddleBlockZ();
        long hash=mix(c.seed()^packed(bx,bz)^21015021L);
        int chance=(int)Math.floorMod(hash>>>9,100);
        int variant=chance<25?0:chance<70?1:2; // 25 / 45 / 30 provisional source-art weights
        var cache=new HashMap<Long,NoiseColumn>();
        int minY=Math.max(35,c.heightAccessor().getMinBuildHeight()+5);
        int maxY=Math.min(104,c.heightAccessor().getMaxBuildHeight()-15);
        // Try every legal 13x15/15x13 footprint, highest shelf first. The old
        // four samples often chose one low lava ledge before inspecting a
        // neighbouring dry shelf higher in the SAME chunk.
        for(int y=maxY;y>=minY;y--)for(int k=0;k<64;k++) {
            BlockPos at=new BlockPos(bx+(k/4)%4-2,0,bz+k/16-2);
            Rotation rotation=Rotation.values()[(int)Math.floorMod((hash>>>20)+k,4)];
                var center=new BlockPos(at.getX(),y,at.getZ());
                var plan=new Plan(center,rotation,variant,hash^((long)y<<32));
                var bounds=plan.bounds();
                // Reject any footprint reaching a different chunk: placement is atomic.
                if((bounds.minX()>>4)!=c.chunkPos().x||(bounds.maxX()>>4)!=c.chunkPos().x||
                   (bounds.minZ()>>4)!=c.chunkPos().z||(bounds.maxZ()>>4)!=c.chunkPos().z)continue;
                boolean good=true;
                // Check the five load-bearing substrate points BEFORE scanning the entire volume.
                for(int[] footing:new int[][]{{2,2},{10,2},{2,12},{10,12},{6,13}}) {
                    BlockPos pos=plan.at(footing[0],0,footing[1]);
                    NoiseColumn column=cache.computeIfAbsent(packed(pos.getX(),pos.getZ()),ignored->
                        c.chunkGenerator().getBaseColumn(pos.getX(),pos.getZ(),c.heightAccessor(),c.randomState()));
                    BlockState below=column.getBlock(y-1);
                    if(!natural(below)||!below.getFluidState().isEmpty()){good=false;break;}
                }
                if(!good)continue;
                int supported=0;
                for(int x=0;x<13&&good;x++)for(int z=0;z<15;z++) {
                    BlockPos pos=plan.at(x,0,z);
                    NoiseColumn column=cache.computeIfAbsent(packed(pos.getX(),pos.getZ()),ignored->
                        c.chunkGenerator().getBaseColumn(pos.getX(),pos.getZ(),c.heightAccessor(),c.randomState()));
                    if(natural(column.getBlock(y-1)))supported++;
                    // No lava at/below platform, no solid geometry hidden in a roof or room.
                    if(!column.getBlock(y-1).getFluidState().isEmpty()) {good=false;break;}
                    // The authored raised floor occupies Y=0/1. Grade at most
                    // two additional terrain layers Y=2/3 above that floor.
                    for(int dy=0;dy<11;dy++)if(dy<=3?
                            !(column.getBlock(y+dy).isAir()||natural(column.getBlock(y+dy))):
                            !column.getBlock(y+dy).isAir()){
                        good=false;break;
                    }
                    if(!good)break;
                }
                // Cantilever is supported by five real anchors and a substantial stone shelf.
                if(good&&supported>=40)return plan;
        }
        return null;
    }
    private AncestorShrineTerrain(){}
}
