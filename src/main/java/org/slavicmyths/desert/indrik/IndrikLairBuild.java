package org.slavicmyths.desert.indrik;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import static org.slavicmyths.desert.indrik.IndrikLairPlan.*;

public final class IndrikLairBuild {
    public static BoundingBox bounds(BlockPos o){
        return new BoundingBox(o.getX()+MIN_X,o.getY()+MIN_Y,o.getZ()+MIN_Z,
            o.getX()+MAX_X,o.getY()+MAX_Y,o.getZ()+MAX_Z);
    }
    public static boolean loaded(ServerLevel w,BoundingBox b){
        for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)
            if(!w.hasChunk(x,z))return false;
        return true;
    }
    private static boolean natural(BlockState s){
        return s.isAir()||s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(BlockTags.SAND)
            ||s.is(Blocks.SANDSTONE)||s.is(Blocks.RED_SANDSTONE)||s.is(Blocks.GRAVEL)
            ||s.is(Blocks.DIRT)||s.is(Blocks.COARSE_DIRT)||s.is(Blocks.CLAY)
            ||s.is(Blocks.DEAD_BUSH)||s.is(Blocks.TERRACOTTA);
    }
    private static BlockState desired(WorldGenLevel w,BlockPos p,Kind k){
        BlockState old=w.getBlockState(p);
        return switch(k){
            case FILL,SEAL -> old.isSolidRender(w,p)&&w.getFluidState(p).isEmpty()?old:Blocks.SANDSTONE.defaultBlockState();
            case SANDSTONE -> Blocks.SANDSTONE.defaultBlockState();
            case CUT -> Blocks.CUT_SANDSTONE.defaultBlockState();
            case CHISELED -> Blocks.CHISELED_SANDSTONE.defaultBlockState();
            case RED -> Blocks.RED_TERRACOTTA.defaultBlockState();
            case LOG -> Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState();
            case ROOT -> Blocks.JUNGLE_WOOD.defaultBlockState();
            case AIR -> Blocks.AIR.defaultBlockState();
            case STAIR -> Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH);
            case LIGHT -> Blocks.SHROOMLIGHT.defaultBlockState();
        };
    }
    /** Full preflight for operator placement, before any modification. */
    public static boolean canPlace(ServerLevel w,BlockPos o){
        for(Cell c:cells()){
            BlockPos p=o.offset(c.x(),c.y(),c.z());
            var old=w.getBlockState(p);var next=desired(w,p,c.kind());
            if(w.getBlockEntity(p)!=null||!w.getFluidState(p).isEmpty())return false;
            if(!old.equals(next)&&!natural(old))return false;
        }
        return true;
    }
    /** Only final changed states in the current chunk. No blanket air/foundation fill. */
    public static int render(WorldGenLevel w,BlockPos o,BoundingBox clip){
        int changed=0;
        for(Cell c:cells()){
            BlockPos p=o.offset(c.x(),c.y(),c.z());
            if(!clip.isInside(p)||w.getBlockEntity(p)!=null)continue;
            var old=w.getBlockState(p);var next=desired(w,p,c.kind());
            if(!old.equals(next)&&natural(old)&&w.setBlock(p,next,Block.UPDATE_CLIENTS))changed++;
        }
        return changed;
    }
    private IndrikLairBuild(){}
}
