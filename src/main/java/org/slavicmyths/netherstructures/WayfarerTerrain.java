package org.slavicmyths.netherstructures;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;

/**
 * Finite Nether cave/ledge preflight with real 3D noise columns. Never requests full
 * chunks, chunk tickets, or a heightmap of the bedrock roof. The small structure is
 * deliberately contained in its origin chunk so placement is all-or-nothing.
 */
public final class WayfarerTerrain {
    public record Plan(BlockPos center, Rotation rotation, int variant, boolean lit, long seed) {
        public BlockPos at(int x, int y, int z) {
            return center.offset(new BlockPos(x - 4, y, z - 3).rotate(rotation));
        }
        public BoundingBox bounds() {
            BlockPos a=at(0,0,0),b=at(8,5,6);
            return new BoundingBox(Math.min(a.getX(),b.getX()),center.getY(),Math.min(a.getZ(),b.getZ()),
                    Math.max(a.getX(),b.getX()),center.getY()+5,Math.max(a.getZ(),b.getZ()));
        }
    }
    public static boolean natural(BlockState s) {
        return s.is(Blocks.NETHERRACK) || s.is(Blocks.CRIMSON_NYLIUM) ||
            s.is(Blocks.WARPED_NYLIUM) || s.is(Blocks.BLACKSTONE) ||
            s.is(Blocks.BASALT) || s.is(Blocks.SOUL_SAND) || s.is(Blocks.SOUL_SOIL) ||
            s.is(Blocks.GRAVEL) || s.is(Blocks.MAGMA_BLOCK) ||
            s.is(Blocks.NETHER_QUARTZ_ORE) || s.is(Blocks.NETHER_GOLD_ORE);
    }
    private static long hash(long seed, int x, int z) {
        long a=seed ^ (x * 341873128712L) ^ (z * 132897987541L) ^ 20213013L;
        a=(a^(a>>>30))*-4658895280553007687L;
        a=(a^(a>>>27))*-7723592293110705685L;
        return a^(a>>>31);
    }
    public static Plan find(Structure.GenerationContext c) {
        int cx=c.chunkPos().getMiddleBlockX(), cz=c.chunkPos().getMiddleBlockZ();
        int[][] offsets={{0,0},{-2,0},{2,0},{0,-2},{0,2},{-3,-2},{3,2},{-2,3},{2,-3}};
        int minY=Math.max(24,c.heightAccessor().getMinBuildHeight()+4);
        int maxY=Math.min(106,c.heightAccessor().getMaxBuildHeight()-9);
        // Columns are immutable terrain samples; cache within this one candidate only.
        Map<Long,NoiseColumn> columns=new HashMap<>();
        for(int[] off:offsets) {
            int ax=cx+off[0],az=cz+off[1];
            long h=hash(c.seed(),ax,az);
            Rotation rotation=Rotation.values()[(int)Math.floorMod(h,4)];
            int chance=(int)Math.floorMod(h>>>8,100);
            int variant=chance<55?0:chance<80?1:2; // provisional SPEC weights
            boolean lit=Math.floorMod(h>>>20,100)<80;
            for(int y=maxY;y>=minY;y--) {
                var center=new BlockPos(ax,y,az);
                var plan=new Plan(center,rotation,variant,lit,h);
                boolean ok=true;
                for(int x=0;x<9&&ok;x++)for(int z=0;z<7;z++) {
                    BlockPos at=plan.at(x,0,z);
                    long k=(((long)at.getX())<<32)^(at.getZ()&0xffffffffL);
                    NoiseColumn col=columns.computeIfAbsent(k,ignored->
                        c.chunkGenerator().getBaseColumn(at.getX(),at.getZ(),c.heightAccessor(),c.randomState()));
                    // Full footprint and six-block high open volume, even where no block is placed.
                    for(int dy=0;dy<=5;dy++) {
                        if(!col.getBlock(y+dy).isAir()) {ok=false;break;}
                    }
                    if(!ok)break;
                    // Every actual authored paving/foundation block must rest on genuine terrain.
                    if((WayfarerPiece.foundation(x,z) || WayfarerPiece.walkingGround(x,z)) && !natural(col.getBlock(y-1))) {
                        ok=false;break;
                    }
                    if(col.getBlock(y-1).is(Blocks.BEDROCK) ||
                        !col.getBlock(y-1).getFluidState().isEmpty()) {
                        ok=false;break;
                    }
                }
                if(ok)return plan;
            }
        }
        return null;
    }
    private WayfarerTerrain() {}
}
