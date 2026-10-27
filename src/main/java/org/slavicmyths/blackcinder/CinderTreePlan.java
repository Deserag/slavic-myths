package org.slavicmyths.blackcinder;
import java.util.*;
/** Three connected dead-tree silhouettes, kept inside a single chunk's center. */
public final class CinderTreePlan {
 public record Cell(int x,int y,int z){}
 public final Map<Cell,Integer> logs=new LinkedHashMap<>();
 public final Set<Cell> leaves=new LinkedHashSet<>();
 public CinderTreePlan(long seed){var r=new Random(seed);int shape=r.nextInt(3),height=shape==0?4:shape==1?6:7;int lean=shape==2?(r.nextBoolean()?1:-1):0;int lastX=0;
  for(int y=0;y<height;y++){int x=lean*(y/3);if(x!=lastX)logs.put(new Cell(x,y-1,0),0);logs.put(new Cell(x,y,0),1);lastX=x;}
  for(int side=-1;side<=1;side+=2)for(boolean alongX:new boolean[]{false,true}){
   int len=shape==0?2:3,by=height-2;int bx=lean*(by/3);
   for(int i=1;i<=len;i++){int x=bx+(alongX?side*i:0),z=alongX?0:side*i;logs.put(new Cell(x,by,z),alongX?0:2);if(i==len){logs.put(new Cell(x,by+1,z),1);leaves.add(new Cell(x,by+2,z));}}
  }
 }
}
