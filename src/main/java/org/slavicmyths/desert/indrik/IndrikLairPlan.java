package org.slavicmyths.desert.indrik;

import java.util.*;
import java.util.function.IntBinaryOperator;

/** Pure, versioned geometry. KEEP is implicit; every cell has exactly one final operation. */
public final class IndrikLairPlan {
    public static final int MIN_X=-16, MAX_X=16, MIN_Z=-17, MAX_Z=18;
    public static final int MIN_Y=-12, MAX_Y=29, ENTRANCE_Z=-17, FLOOR=-12, ARENA_Z=4, RADIUS=13;
    public static final int LIMIT=15000, VERSION=1;
    public enum Kind { FILL, SANDSTONE, CUT, CHISELED, RED, LOG, ROOT, AIR, SEAL, STAIR, LIGHT }
    public record Cell(int x,int y,int z,Kind kind){}
    private record Pos(int x,int y,int z){}
    private static final List<Cell> CELLS=build();
    public static List<Cell> cells(){return CELLS;}
    public static long count(Kind k){return CELLS.stream().filter(c->c.kind()==k).count();}
    private static void put(Map<Pos,Kind> m,int x,int y,int z,Kind k){
        if(x<MIN_X||x>MAX_X||z<MIN_Z||z>MAX_Z||y<MIN_Y||y>MAX_Y)
            throw new IllegalStateException("Indrik lair cell outside bounds");
        m.put(new Pos(x,y,z),k);
    }
    public static int mound(int x,int z){
        double r=x*x/256.0+z*z/256.0;
        return r>=1?0:Math.max(1,(int)Math.round(9*Math.pow(1-r,1.35)));
    }
    public static boolean arena(int x,int z){return x*x+(z-ARENA_Z)*(z-ARENA_Z)<=RADIUS*RADIUS;}
    private static List<Cell> build(){
        Map<Pos,Kind> m=new LinkedHashMap<>();
        // Only the mound footprint, not the surrounding desert. Short fill follows local depressions.
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=-16;z<=16;z++){
            int h=mound(x,z);if(h==0)continue;
            for(int y=-3;y<h;y++)put(m,x,y,z,y<0?Kind.FILL:Kind.SANDSTONE);
        }
        // Closed, one-block shell; retain natural solid rock wherever possible.
        for(int x=-14;x<=14;x++)for(int z=-10;z<=18;z++){
            int d=x*x+(z-ARENA_Z)*(z-ARENA_Z);
            if(d>196)continue;
            boolean inner=d<=169;
            // The rear boundary at z=18 is sealed, rather than open to outside terrain.
            if(z==18)inner=false;
            for(int y=FLOOR;y<=-3;y++){
                Kind k=(!inner||y==FLOOR||y==-3)?Kind.SEAL:Kind.AIR;
                if(inner&&y==FLOOR&&(d<=9||Math.abs(d-100)<=8))k=Kind.RED;
                put(m,x,y,z,k);
            }
        }
        // Five-wide descending stairs. North is uphill; four blocks of clear headroom.
        for(int z=ENTRANCE_Z;z<=-6;z++){
            int y=-1-(z-ENTRANCE_Z);
            for(int x=-3;x<=3;x++){
                put(m,x,y,z,Math.abs(x)<=2?Kind.STAIR:Kind.CUT);
                for(int h=1;h<=5;h++)
                    put(m,x,y+h,z,Math.abs(x)<=2?Kind.AIR:Kind.SEAL);
                if(z>=-12)put(m,x,y+6,z,Kind.SEAL);
            }
        }
        // Framed entrance and restrained Slavic diamond ornament.
        for(int x=-4;x<=4;x++)for(int y=-1;y<=5;y++)
            if(Math.abs(x)>=3||y==5){
                Kind k=(y==5&&Math.abs(x)%2==0||Math.abs(x)==3&&y==2)?Kind.CHISELED:Kind.CUT;
                put(m,x,y,-16,k);
            }
        put(m,-4,3,-17,Kind.LIGHT);put(m,4,3,-17,Kind.LIGHT);
        // Arena lights sit outside the central fighting area.
        for(int[] p:new int[][]{{-11,5},{11,5},{-8,12},{8,12},{0,16}}){
            put(m,p[0],FLOOR+1,p[1],Kind.CUT);
            put(m,p[0],FLOOR+2,p[1],Kind.LIGHT);
        }
        // Single leafless palm: tapering, slightly leaning trunk, closed top.
        for(int y=8;y<=28;y++){
            int cx=(y-8)/8,cz=-(y-8)/11,r=y<13?2:1;
            for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)
                if(Math.abs(x)+Math.abs(z)<=r+1&&(y==28||y<13||Math.abs(x)==r||Math.abs(z)==r))
                    put(m,cx+x,y,cz+z,Kind.LOG);
        }
        // Stepped roots with narrowing cross sections, eight irregular directions.
        for(int a=0;a<8;a++){
            double angle=a*Math.PI/4+.13;int len=9+(a%3)*2;
            for(int i=0;i<=len;i++){
                int x=(int)Math.round(i*Math.cos(angle)),z=(int)Math.round(i*Math.sin(angle));
                int y=Math.max(0,mound(x,z)-1),r=i<4?2:i<8?1:0;
                for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                    if(Math.abs(dx)+Math.abs(dz)>r+1||Math.abs(x+dx)>15||Math.abs(z+dz)>15)continue;
                    // Don't obstruct the open entrance.
                    if(z+dz<=-6&&Math.abs(x+dx)<=4)continue;
                    put(m,x+dx,y,z+dz,Kind.ROOT);
                    if(i<5)put(m,x+dx,y+1,z+dz,Kind.ROOT);
                }
            }
        }
        if(m.size()>LIMIT)throw new IllegalStateException("Indrik lair exceeds block budget: "+m.size());
        return m.entrySet().stream().map(e->new Cell(e.getKey().x,e.getKey().y,e.getKey().z,e.getValue())).toList();
    }
    /** First air at entrance fixes staircase alignment. Reject cliffs, shallow cover and water. */
    public static OptionalInt anchor(IntBinaryOperator surface,IntBinaryOperator ground,int x,int z,int minY,int maxY){
        int base=surface.applyAsInt(x,z+ENTRANCE_Z);
        if(base+MIN_Y<minY||base+MAX_Y>=maxY)return OptionalInt.empty();
        for(int dx=-16;dx<=16;dx+=4)for(int dz=-16;dz<=16;dz+=4){
            if(mound(dx,dz)==0&&!arena(dx,dz))continue;
            int s=surface.applyAsInt(x+dx,z+dz),g=ground.applyAsInt(x+dx,z+dz);
            if(s!=g||s<base-2||s>base+5)return OptionalInt.empty();
        }
        for(int dx=-3;dx<=3;dx++){
            int s=surface.applyAsInt(x+dx,z+ENTRANCE_Z);
            if(s!=ground.applyAsInt(x+dx,z+ENTRANCE_Z)||Math.abs(s-base)>1)return OptionalInt.empty();
        }
        return OptionalInt.of(base);
    }
    private IndrikLairPlan(){}
}
