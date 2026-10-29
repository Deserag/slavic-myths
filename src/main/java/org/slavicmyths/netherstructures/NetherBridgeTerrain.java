package org.slavicmyths.netherstructures;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;

/** Bounded noise-column-only Nether preflight. NEVER loads chunks, tickets or mutates worlds. */
public final class NetherBridgeTerrain {
 public static final int MAX_DEPTH=24,MIN_LENGTH=8,MAX_LENGTH=40;
 private static final int[] LENGTHS={38,30,23,17,12,9};
 public record Plan(BlockPos origin,Rotation rotation,int length,int variant,int gapStart,int gapEnd,int[] supports,long seed,int lavaSamples,int totalSamples){
  public BlockPos at(int lane,int dy,int t){return origin.offset(new BlockPos(lane,dy,t).rotate(rotation));}
  public BoundingBox bounds(){
   BlockPos a=at(-3,-MAX_DEPTH,0),b=at(3,4,length-1);
   return new BoundingBox(Math.min(a.getX(),b.getX()),origin.getY()-MAX_DEPTH,Math.min(a.getZ(),b.getZ()),
          Math.max(a.getX(),b.getX()),origin.getY()+4,Math.max(a.getZ(),b.getZ()));
  }
 }
 public static boolean natural(BlockState s){
  return s.is(Blocks.NETHERRACK)||s.is(Blocks.BLACKSTONE)||s.is(Blocks.BASALT)||
   s.is(Blocks.SOUL_SOIL)||s.is(Blocks.SOUL_SAND)||s.is(Blocks.GRAVEL)||
   s.is(Blocks.MAGMA_BLOCK)||s.is(Blocks.NETHER_QUARTZ_ORE)||s.is(Blocks.NETHER_GOLD_ORE);
 }
 private static boolean open(NoiseColumn col,int y){
  for(int dy=0;dy<=3;dy++)if(!col.getBlock(y+dy).isAir())return false;return true;
 }
 private static boolean stone(NoiseColumn col,int y){
  var s=col.getBlock(y);return natural(s)&&s.getFluidState().isEmpty();
 }
 private static int footing(NoiseColumn col,int deck){
  for(int d=1;d<=MAX_DEPTH;d++){
   var state=col.getBlock(deck-d);
   if(stone(col,deck-d))return d;
   if(state.is(Blocks.BEDROCK)||(!state.isAir()&&state.getFluidState().isEmpty()))return -1;
  }return -1;
 }
 public static boolean pier(int t,int length){return t==0||t==length-1||(t%9==0&&t>0);}
 private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 private static final class Columns {
  final Structure.GenerationContext c;
  final Map<Long,NoiseColumn> cache=new HashMap<>();
  Columns(Structure.GenerationContext c){this.c=c;}
  NoiseColumn get(int x,int z){return cache.computeIfAbsent(key(x,z),k->
   c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState()));}
 }
 public static Plan find(Structure.GenerationContext c){
  var cols=new Columns(c);int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();
  long salt=c.seed()^(x*341873128712L)^(z*132897987541L);
  int r0=(int)Math.floorMod(salt,4L),l0=(int)Math.floorMod(salt>>>8,LENGTHS.length);
  int minY=Math.max(c.heightAccessor().getMinBuildHeight()+MAX_DEPTH+2,35);
  int maxY=Math.min(c.heightAccessor().getMaxBuildHeight()-6,79);
  for(int r=0;r<4;r++){
   Rotation rotation=Rotation.values()[(r0+r)%4];
   for(int n=0;n<LENGTHS.length;n++){
    int len=LENGTHS[(l0+n)%LENGTHS.length];
    for(int deck=maxY;deck>=minY;deck--){
     BlockPos origin=new BlockPos(x,deck,z);int[] depths=new int[len*2];
     boolean valid=true;int lava=0,total=0,wetStreak=0,maxWet=0;
     for(int t=0;t<len;t++){
      BlockPos cp=origin.offset(new BlockPos(0,0,t).rotate(rotation));
      NoiseColumn mid=cols.get(cp.getX(),cp.getZ());
      if(!open(mid,deck)){valid=false;break;}
      if(t==0||t==len-1){
       for(int lane:new int[]{-2,0,2}){
        BlockPos bp=origin.offset(new BlockPos(lane,0,t).rotate(rotation));
        NoiseColumn bank=cols.get(bp.getX(),bp.getZ());
        if(!open(bank,deck)||!stone(bank,deck-1)){valid=false;break;}
       }if(!valid)break;
      }else{
       boolean wet=false;
       for(int dy=1;dy<=Math.min(18,deck-c.heightAccessor().getMinBuildHeight());dy++){
        if(mid.getBlock(deck-dy).is(Blocks.LAVA)){wet=true;break;}
        if(stone(mid,deck-dy))break;
       }
       if(wet){lava++;wetStreak++;maxWet=Math.max(maxWet,wetStreak);}else wetStreak=0;total++;
      }
      if(pier(t,len)){
       for(int side=0;side<2;side++){
        BlockPos pp=origin.offset(new BlockPos(side==0?-3:3,0,t).rotate(rotation));
        NoiseColumn col=cols.get(pp.getX(),pp.getZ());
        int depth=open(col,deck)?footing(col,deck):-1;
        if(depth<1){valid=false;break;}
        depths[t*2+side]=depth;
       }if(!valid)break;
      }
     }
     if(!valid||total<6||lava<Math.max(3,total/3)||maxWet<3)continue;
     int variant=(int)Math.floorMod(salt^(len*37L)^(deck*103L),4);
     int gap=variant==2?Math.min(5,Math.max(2,len/6)):variant==3?Math.min(5,Math.max(3,len/7)):0;
     int from=(len-gap)/2;
     return new Plan(origin,rotation,len,variant,from,from+gap,depths,
            salt^((long)deck<<32)^len,lava,total);
    }
   }
  }return null;
 }
 private NetherBridgeTerrain(){}
}
