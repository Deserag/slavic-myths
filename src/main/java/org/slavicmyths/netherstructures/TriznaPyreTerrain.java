package org.slavicmyths.netherstructures;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;
public final class TriznaPyreTerrain {
    public record Plan(BlockPos center,Rotation rotation,int variant,long seed) {
        public BlockPos at(int x,int y,int z){return center.offset(new BlockPos(x-7,y,z-7).rotate(rotation));}
        public BoundingBox bounds(){return new BoundingBox(center.getX()-7,center.getY(),center.getZ()-7,center.getX()+7,center.getY()+8,center.getZ()+7);}
    }
    public static boolean natural(BlockState s){return AncestorShrineTerrain.natural(s);}
    public static boolean stable(BlockState s){return natural(s)&&!s.is(Blocks.GRAVEL);}
    public static Plan find(Structure.GenerationContext c) {
        long h=c.seed()^c.chunkPos().toLong()^21016021L;
        h=(h^(h>>>30))*-4658895280553007687L;
        int roll=(int)Math.floorMod(h>>>8,100),variant=roll<30?0:roll<60?1:roll<85?2:3;
        var columns=new HashMap<Long,NoiseColumn>();
        for(int y=104;y>=24;y--)for(int k=0;k<16;k++) {
            var rot=Rotation.values()[(int)Math.floorMod(h+k,4)];
            var p=new Plan(new BlockPos(c.chunkPos().getMinBlockX()+7+(k/4&1),y,c.chunkPos().getMinBlockZ()+7+(k/8&1)),rot,variant,h);
            boolean good=true;
            for(int[] anchor:new int[][]{{7,7},{3,3},{11,11},{11,8}}) {
                var pos=p.at(anchor[0],0,anchor[1]);long key=((long)pos.getX()<<32)^(pos.getZ()&0xffffffffL);
                var col=columns.computeIfAbsent(key,unused->c.chunkGenerator().getBaseColumn(pos.getX(),pos.getZ(),c.heightAccessor(),c.randomState()));
                if(!stable(col.getBlock(y-1))){good=false;break;}
            }
            if(!good)continue;
            for(int x=0;x<15&&good;x++)for(int z=0;z<15&&good;z++) {
                var floor=TriznaPyrePiece.voxel(variant,x,0,z);if(floor==null)continue;
                var pos=p.at(x,0,z);long key=((long)pos.getX()<<32)^(pos.getZ()&0xffffffffL);
                var col=columns.computeIfAbsent(key,unused->c.chunkGenerator().getBaseColumn(pos.getX(),pos.getZ(),c.heightAccessor(),c.randomState()));
                if(!stable(col.getBlock(y-1))){good=false;break;}
                for(int dy=0;dy<=8;dy++)if(dy<=2?!(col.getBlock(y+dy).isAir()||natural(col.getBlock(y+dy))):!col.getBlock(y+dy).isAir()){good=false;break;}
            }
            if(good)return p;
        }
        return null;
    }
    private TriznaPyreTerrain(){}
}
