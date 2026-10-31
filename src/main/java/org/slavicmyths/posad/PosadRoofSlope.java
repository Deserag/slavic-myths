package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** New schema roof slope. Convert authored 6,6,7,7... stair rows to real 6,7,8,9...
 * ascending geometry. No rotated top-half stairs, no decorative tooth rows.
 * Original R1 and saved schema<=23 cities are unchanged.
 */
public final class PosadRoofSlope {
    public static final int MAX_Y=25;
    @FunctionalInterface public interface Source { BlockState at(int x,int y,int z); }
    private record Step(int x,int y,int z,BlockState state) {}
    private record End(Step last,int newY,int dx,int dz,BlockState solid) {}
    public static long key(int x,int y,int z){return ((long)(x & 0xffff)<<40)|((long)(y & 255)<<32)|((long)(z & 0xffff)<<16);}
    private static boolean in(int x,int z){return x>=0&&x<=112&&z>=0&&z<=112;}
    private static boolean empty(BlockState b){return b==null||b.isAir();}
    private static boolean same(BlockState a,BlockState b){return a!=null&&b!=null&&a.getBlock() instanceof StairBlock
        &&a.getBlock()==b.getBlock()&&a.getValue(StairBlock.FACING)==b.getValue(StairBlock.FACING)
        &&a.getValue(StairBlock.HALF)==Half.BOTTOM&&b.getValue(StairBlock.HALF)==Half.BOTTOM;}
    private static BlockState solid(BlockState s){
        if(s.is(Blocks.NETHER_BRICK_STAIRS))return Blocks.NETHER_BRICKS.defaultBlockState();
        if(s.is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS))return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if(s.is(Blocks.CRIMSON_STAIRS))return Blocks.CRIMSON_PLANKS.defaultBlockState();
        if(s.is(Blocks.BLACKSTONE_STAIRS))return Blocks.BLACKSTONE.defaultBlockState();
        if(s.is(Blocks.POLISHED_BLACKSTONE_STAIRS))return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        return null;
    }
    private static void place(Map<Long,BlockState> m,int x,int y,int z,BlockState s){
        if(!in(x,z)||y<5||y>MAX_Y)throw new IllegalStateException("Roof out of bounds "+x+","+y+","+z);
        BlockState prior=m.putIfAbsent(key(x,y,z),s);
        if(prior!=null&&!prior.equals(s))throw new IllegalStateException("Conflicting roof cells "+x+","+y+","+z);
    }
    public static Map<Long,BlockState> build(Source source){
        Map<Long,BlockState> changes=new HashMap<>();
        List<End> ends=new ArrayList<>();
        for(int z=0;z<=112;z++)for(int x=0;x<=112;x++)for(int y=5;y<=17;y++){
            BlockState first=source.at(x,y,z);
            if(first==null||!(first.getBlock() instanceof StairBlock)||first.getValue(StairBlock.HALF)!=Half.BOTTOM)continue;
            BlockState full=solid(first);if(full==null)continue;
            Direction facing=first.getValue(StairBlock.FACING);
            int dx=-facing.getStepX(),dz=-facing.getStepZ();
            if(in(x-dx,z-dz)&&(same(first,source.at(x-dx,y,z-dz))||same(first,source.at(x-dx,y-1,z-dz))))continue;
            List<Step> run=new ArrayList<>();int xx=x,zz=z,yy=y,flat=0;
            while(in(xx,zz)&&yy<=17&&run.size()<24){
                BlockState b=source.at(xx,yy,zz);if(!same(first,b))break;
                run.add(new Step(xx,yy,zz,b));
                int nx=xx+dx,nz=zz+dz;
                if(!in(nx,nz))break;
                if(same(first,source.at(nx,yy,nz))){flat++;xx=nx;zz=nz;}
                else if(same(first,source.at(nx,yy+1,nz))){xx=nx;zz=nz;yy++;}
                else break;
            }
            if(run.size()<4||flat==0)continue;
            boolean safe=true;
            for(int i=0;i<run.size()&&safe;i++){
                Step step=run.get(i);int target=y+i;
                if(target>MAX_Y){safe=false;break;}
                for(int h=step.y+1;h<=target;h++){
                    BlockState existing=source.at(step.x,h,step.z);
                    boolean ridgeCap=i==run.size()-1&&h==step.y+1&&existing!=null&&existing.getBlock() instanceof SlabBlock;
                    if(!empty(existing)&&!ridgeCap){safe=false;break;}
                }
            }
            if(!safe)continue;
            for(int i=0;i<run.size();i++){
                Step step=run.get(i);int target=y+i;
                if(target==step.y)continue;
                for(int h=step.y;h<target;h++)place(changes,step.x,h,step.z,full);
                place(changes,step.x,target,step.z,step.state);
                if(i==run.size()-1){
                    BlockState top=source.at(step.x,step.y+1,step.z);
                    if(top!=null&&top.getBlock() instanceof SlabBlock){
                        place(changes,step.x,step.y+1,step.z,full);
                        place(changes,step.x,target+1,step.z,top);
                    }
                }
            }
            ends.add(new End(run.get(run.size()-1),y+run.size()-1,dx,dz,full));
        }
        // Restore central masonry ridge above both slopes (e.g. Zeleynaya).
        for(End a:ends)for(End b:ends){
            if(a==b||a.dx!=-b.dx||a.dz!=-b.dz||a.last.y!=b.last.y)continue;
            for(int gap=1;gap<=2;gap++){
                if(a.last.x+a.dx*(gap+1)!=b.last.x||a.last.z+a.dz*(gap+1)!=b.last.z)continue;
                int peak=Math.max(a.newY,b.newY)+1;if(peak>MAX_Y)continue;
                for(int i=1;i<=gap;i++){
                    int cx=a.last.x+a.dx*i,cz=a.last.z+a.dz*i,sourceY=-1;
                    BlockState ridge=null;
                    for(int yy=a.last.y;yy<=Math.min(a.last.y+3,17);yy++){
                        BlockState s=source.at(cx,yy,cz);
                        if(s!=null&&!s.isAir()&&!(s.getBlock() instanceof StairBlock)&&!(s.getBlock() instanceof SlabBlock)){ridge=s;sourceY=yy;break;}
                    }
                    if(ridge==null)continue;
                    for(int yy=sourceY+1;yy<=peak;yy++)if(empty(source.at(cx,yy,cz)))place(changes,cx,yy,cz,ridge);
                }
            }
        }
        return Map.copyOf(changes);
    }
    private PosadRoofSlope(){}
}
