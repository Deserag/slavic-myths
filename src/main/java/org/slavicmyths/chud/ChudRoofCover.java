package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Natural overburden, separate from the approved architectural roof. Carvers and
 * lakes can lower the final ground below the seed-only height survey. Only a thin
 * column is repaired, only air/fluid is filled, and never above the surveyed ground.
 * New starts opt in; old starts do not acquire earthworks on load.
 */
public final class ChudRoofCover {
    public static final int DEPTH=20;
    private final Map<Long,Integer> roofs;
    public final int top;private final int bottom;
    private ChudRoofCover(Map<Long,Integer> roofs,int bottom){this.bottom=bottom;this.roofs=Map.copyOf(roofs);top=roofs.values().stream().mapToInt(Integer::intValue).max().orElseThrow()+DEPTH;}
    public static ChudRoofCover from(List<ChudGeometry.Fill> fills){
        var roofs=new HashMap<Long,Integer>();
        for(var f:fills){var b=f.box();for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++)roofs.merge(BlockPos.asLong(x,0,z),b.maxY(),Math::max);}
        return new ChudRoofCover(roofs,fills.stream().mapToInt(f->f.box().minY()).min().orElseThrow());
    }
    public BoundingBox include(BoundingBox b){return new BoundingBox(b.minX(),Math.max(b.minY(),bottom),b.minZ(),b.maxX(),Math.max(b.maxY(),top),b.maxZ());}
    public boolean intersects(BoundingBox b){
        for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++){var roof=roofs.get(BlockPos.asLong(x,0,z));if(roof!=null&&b.maxY()>=roof+1&&b.minY()<=roof+DEPTH)return true;}return false;
    }
    /** Include the required overburden in protected-structure preflight. */
    public void protect(Map<Long,BoundingBox> chunks){
        roofs.forEach((key,roof)->{var p=BlockPos.of(key);var b=new BoundingBox(p.getX(),roof+1,p.getZ(),p.getX(),roof+DEPTH,p.getZ());chunks.merge(ChunkPos.asLong(p.getX()>>4,p.getZ()>>4),b,(a,c)->BoundingBox.encapsulatingBoxes(List.of(a,c)).orElseThrow());});
    }
    /** Explicit story placements in existing chunks reject thin terrain before writes. */
    public boolean sufficient(WorldGenLevel world,ChunkPos chunk){
        for(int x=chunk.getMinBlockX();x<=chunk.getMaxBlockX();x++)for(int z=chunk.getMinBlockZ();z<=chunk.getMaxBlockZ();z++){
            var roof=roofs.get(BlockPos.asLong(x,0,z));if(roof!=null&&org.slavicmyths.worldgen.LandTerrain.ground(world,x,z)<roof+DEPTH)return false;
        }return true;
    }
    public void repair(WorldGenLevel world,BoundingBox clip,ChunkPos chunk){
        int repaired=0;var at=new BlockPos.MutableBlockPos();
        for(int x=chunk.getMinBlockX();x<=chunk.getMaxBlockX();x++)for(int z=chunk.getMinBlockZ();z<=chunk.getMaxBlockZ();z++){
            var roof=roofs.get(BlockPos.asLong(x,0,z));if(roof==null||org.slavicmyths.worldgen.LandTerrain.ground(world,x,z)>=roof+DEPTH)continue;
            for(int y=Math.max(roof+1,clip.minY());y<=Math.min(roof+DEPTH,clip.maxY());y++){
                at.set(x,y,z);var old=world.getBlockState(at);
                if((old.isAir()||old.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock||old.is(Blocks.BUBBLE_COLUMN))&&world.getBlockEntity(at)==null){world.setBlock(at,Blocks.STONE.defaultBlockState(),2);repaired++;}
            }
        }
        if(repaired>0&&Boolean.getBoolean("slavicmyths.chudPlacementDiagnostics"))com.mojang.logging.LogUtils.getLogger().info("Chud natural overburden: {} repaired air/fluid cells in {}",repaired,chunk);
    }
}
