package org.slavicmyths.netherstructures;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;

public final class ForgottenShrineTerrain {
    public record Plan(BlockPos center,Rotation rotation,int variant,long seed) {
        public BlockPos at(int x,int y,int z){return center.offset(new BlockPos(x-7,y,z-7).rotate(rotation));}
        public BoundingBox bounds(){return new BoundingBox(center.getX()-7,center.getY()-((variant&1)==0?5:10),center.getZ()-7,center.getX()+7,center.getY()+8,center.getZ()+7);}
    }
    public static boolean natural(BlockState s){return WayfarerTerrain.natural(s);}
    public static boolean stable(BlockState s){return natural(s)&&!s.is(Blocks.GRAVEL);}
    public static Plan find(Structure.GenerationContext c) {
        long h=c.seed()^c.chunkPos().toLong()^21014021L;
        h=(h^(h>>>30))*-4658895280553007687L;
        int variant=(int)Math.floorMod(h>>>16,4),bottom=(variant&1)==0?-5:-10;
        var columns=new HashMap<Long,NoiseColumn>();
        for(int k=0;k<4;k++) {
            var rotation=Rotation.values()[(int)Math.floorMod(h+k,4)];
            int cx=c.chunkPos().getMinBlockX()+7+(k&1),cz=c.chunkPos().getMinBlockZ()+7+((k>>1)&1);
            for(int y=104;y>=24;y--) {
                var p=new Plan(new BlockPos(cx,y,cz),rotation,variant,h);
                boolean ok=true;
                // Cheap deep anchor and ground entrance check before the full envelope.
                for(int[] q:new int[][]{{7,bottom-1,7},{3,bottom-1,3},{11,bottom-1,11},{7,-1,14}}) {
                    var at=p.at(q[0],0,q[2]);long key=((long)at.getX()<<32)^(at.getZ()&0xffffffffL);
                    var col=columns.computeIfAbsent(key,ignored->c.chunkGenerator().getBaseColumn(at.getX(),at.getZ(),c.heightAccessor(),c.randomState()));
                    if(!stable(col.getBlock(y+q[1]))){ok=false;break;}
                }
                if(!ok)continue;
                for(int x=0;x<15&&ok;x++)for(int z=0;z<15&&ok;z++) {
                    var at=p.at(x,0,z);long key=((long)at.getX()<<32)^(at.getZ()&0xffffffffL);
                    var col=columns.computeIfAbsent(key,ignored->c.chunkGenerator().getBaseColumn(at.getX(),at.getZ(),c.heightAccessor(),c.randomState()));
                    for(int dy=bottom;dy<=8;dy++) {
                        var intended=ForgottenShrinePiece.voxel(variant,x,dy,z);if(intended==null)continue;
                        var old=col.getBlock(y+dy);
                        // Grade at most two surface rocks, never an underground cavity
                        // or a ceiling. The whole room envelope still must be solid.
                        if(dy<0?!natural(old):dy<=2?!(old.isAir()||natural(old)):!old.isAir()){ok=false;break;}
                    }
                    if(!ok)break;
                    var foundation=ForgottenShrinePiece.voxel(variant,x,bottom,z);
                    if(foundation!=null&&!foundation.isAir()&&!stable(col.getBlock(y+bottom-1)))ok=false;
                    var floor=ForgottenShrinePiece.voxel(variant,x,0,z);
                    if((x<2||x>12||z<2||z>12)&&floor!=null&&!floor.isAir()&&!stable(col.getBlock(y-1)))ok=false;
                }
                if(ok)return p;
            }
        }
        return null;
    }
    private ForgottenShrineTerrain(){}
}
