package org.slavicmyths.desert.winds;

import java.util.OptionalInt;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import static org.slavicmyths.desert.winds.FourWindsPlan.*;

public final class FourWindsBuild {
    private static final int FOUNDATION=6;
    private static BlockState[] palette(){
        return new BlockState[]{
            Blocks.AIR.defaultBlockState(),Blocks.CHAIN.defaultBlockState(),Blocks.CUT_SANDSTONE.defaultBlockState(),
            Blocks.CYAN_TERRACOTTA.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),
            Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(),
            Blocks.RAW_GOLD_BLOCK.defaultBlockState(),Blocks.RED_TERRACOTTA.defaultBlockState(),Blocks.SANDSTONE.defaultBlockState(),
            Blocks.SEA_LANTERN.defaultBlockState(),Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
            Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),
            Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
        };
    }
    public static BoundingBox bounds(BlockPos o){
        return new BoundingBox(o.getX()+MIN_X,o.getY()-FOUNDATION,o.getZ()+MIN_Z,
            o.getX()+MAX_X,o.getY()+H-1,o.getZ()+MAX_Z);
    }
    /** Surface is the first air block. Only the small entrance pad may adapt to terrain. */
    static OptionalInt anchor(IntBinaryOperator surface,int x,int z,int minY,int maxY){
        int low=Integer.MAX_VALUE,high=Integer.MIN_VALUE;
        for(int dx=-8;dx<=8;dx+=4)for(int dz=-8;dz<=8;dz+=4){
            if(dx*dx+dz*dz>64)continue;
            int h=surface.applyAsInt(x+dx,z+ENTRANCE_Z+dz);
            low=Math.min(low,h);high=Math.max(high,h);
        }
        int base=high-1;
        if(high-low>FOUNDATION||base-FOUNDATION<minY||base+H>maxY)return OptionalInt.empty();
        // Check the first section of stairs and leave a real gap under the floating deck.
        for(int i=0;i<34;i+=3)for(int dx=-2;dx<=2;dx+=2)
            if(surface.applyAsInt(x+dx,z+ENTRANCE_Z+i)>base+2+i)return OptionalInt.empty();
        for(int dx=-20;dx<=20;dx+=10)for(int dz=-20;dz<=20;dz+=10)
            if(surface.applyAsInt(x+dx,z+dz)>base+17)return OptionalInt.empty();
        return OptionalInt.of(base);
    }
    public static void render(WorldGenLevel w,BlockPos o,BoundingBox clip){
        var palette=palette();
        var p=new BlockPos.MutableBlockPos();
        int y0=Math.max(0,clip.minY()-o.getY()),y1=Math.min(H-1,clip.maxY()-o.getY());
        for(int x=Math.max(MIN_X,clip.minX()-o.getX());x<=Math.min(MAX_X,clip.maxX()-o.getX());x++){
            for(int z=Math.max(MIN_Z,clip.minZ()-o.getZ());z<=Math.min(MAX_Z,clip.maxZ()-o.getZ());z++){
                int wx=o.getX()+x,wz=o.getZ()+z;
                if(landing(x,z)){
                    for(int y=o.getY()-1;y>=o.getY()-FOUNDATION&&y>=w.getMinBuildHeight();y--){
                        if(y<clip.minY()||y>clip.maxY())continue;
                        p.set(wx,y,wz);var existing=w.getBlockState(p);
                        if(existing.isSolidRender(w,p)||w.getBlockEntity(p)!=null)break;
                        w.setBlock(p,palette[STONE],Block.UPDATE_CLIENTS);
                    }
                }
                for(int y=y0;y<=y1;y++){
                    int b=at(x,y,z);
                    if(b!=AIR)w.setBlock(p.set(wx,o.getY()+y,wz),palette[b],Block.UPDATE_CLIENTS);
                }
                int step=z-ENTRANCE_Z;
                if(Math.abs(x)<=2&&step>=0&&step<34){
                    for(int y=step+3;y<=step+4;y++){
                        if(y<y0||y>y1||at(x,y,z)!=AIR)continue;
                        p.set(wx,o.getY()+y,wz);
                        // Only terrain intruding into the approved two-block stair clearance.
                        if(w.getBlockEntity(p)==null&&!w.isEmptyBlock(p))w.setBlock(p,palette[AIR],Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }
    static boolean loaded(ServerLevel w,BoundingBox b){
        for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)
            if(!w.hasChunk(x,z))return false;
        return true;
    }
    /** Manual placement must not replace a building or another palace. */
    static boolean canPlace(ServerLevel w,BlockPos o){
        var p=new BlockPos.MutableBlockPos();
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=MIN_Z;z<=MAX_Z;z++){
            for(int y=0;y<H;y++){
                if(at(x,y,z)==AIR)continue;
                p.set(o.getX()+x,o.getY()+y,o.getZ()+z);
                if(w.getBlockEntity(p)!=null||!w.getFluidState(p).isEmpty())return false;
                if(y>0&&!w.isEmptyBlock(p))return false;
            }
            int step=z-ENTRANCE_Z;
            if(Math.abs(x)<=2&&step>=0&&step<34)
                for(int y=step+3;y<=step+4;y++)if(!w.isEmptyBlock(p.set(o.getX()+x,o.getY()+y,o.getZ()+z)))return false;
        }
        return true;
    }
    private FourWindsBuild(){}
}
