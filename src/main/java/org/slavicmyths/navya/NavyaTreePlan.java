package org.slavicmyths.navya;

import java.util.*;

/** Pure bounded geometry; every wooden cell is face-connected to the trunk. */
public final class NavyaTreePlan {
    public record Cell(int x,int y,int z) { }
    public record Timber(int axis,boolean weathered) { }
    public final Map<Cell,Timber> logs=new LinkedHashMap<>();
    public final Set<Cell> foliage=new LinkedHashSet<>();
    public NavyaTreePlan(int size,long seed) {
        Random random=new Random(seed);
        int height=(size==0?6:size==1?12:20)+random.nextInt(3);
        int width=size==2?2:1;
        for(int y=0;y<height;y++) for(int x=0;x<(y<6?width:1);x++) for(int z=0;z<(y<6?width:1);z++)
            logs.put(new Cell(x,y,z),new Timber(1,size==2 && y<height*2/3));
        // Bends are additive stair steps: no diagonal-only trunk breaks.
        int bend=random.nextBoolean()?1:-1;
        for(int y=height-3;y<=height;y++) logs.put(new Cell(bend,y,0),new Timber(1,false));
        int branches=size==0?2:size==1?4:6;
        for(int i=0;i<branches;i++) {
            int dx=i%4==0?1:i%4==1?-1:0,dz=i%4==2?1:i%4==3?-1:0;
            int y=height-3-i*2/3, length=(size==0?2:size==1?3:4)+random.nextInt(2);
            int x=0,z=0;
            for(int step=1;step<=length;step++) {
                x+=dx;z+=dz;logs.put(new Cell(x,y,z),new Timber(dx!=0?0:2,size==2 && i%2==0));
                if(step%2==0) { y++; logs.put(new Cell(x,y,z),new Timber(1,size==2 && i%2==0)); }
            }
            // Sparse dry tufts, actual LeavesBlock semantics for external tree recognition.
            foliage.add(new Cell(x,y+1,z));foliage.add(new Cell(x+dx,y,z+dz));
        }
        if(size==2) for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int x=d[0]>0?width-1:0,z=d[1]>0?width-1:0;
            for(int step=1;step<=2;step++) {x+=d[0];z+=d[1];logs.put(new Cell(x,0,z),new Timber(d[0]!=0?0:2,true));}
        }
        foliage.removeAll(logs.keySet());
    }
}
