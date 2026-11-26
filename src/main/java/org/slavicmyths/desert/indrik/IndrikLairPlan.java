package org.slavicmyths.desert.indrik;

import java.util.*;
import java.util.function.IntBinaryOperator;

/** Pure, versioned geometry. KEEP is implicit; every cell has exactly one final operation. */
public final class IndrikLairPlan {
    public static final int MIN_X=-16, MAX_X=16, MIN_Z=-17, MAX_Z=18;
    public static final int MIN_Y=-12, MAX_Y=29, ENTRANCE_Z=-17, FLOOR=-12, ARENA_Z=4, RADIUS=13;
    public static final int LIMIT=15000, VERSION=2;
    public enum Kind { FILL, SANDSTONE, CUT, CHISELED, RED, LOG, ROOT, AIR, SEAL, STAIR, LIGHT }
    public record Cell(int x,int y,int z,Kind kind){}
    private record Pos(int x,int y,int z){}
    private static final List<Cell> LEGACY_CELLS=buildLegacy();
    private static final List<Cell> CELLS=build();
    public static List<Cell> cells(){return CELLS;}
    public static List<Cell> cells(int version){
        return switch(version){case 1 -> LEGACY_CELLS;case VERSION -> CELLS;default -> throw new IllegalArgumentException("Unknown Indrik lair plan: "+version);};
    }
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
    private static List<Cell> buildLegacy(){
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
    // Asymmetrical, compact mound: no edits outside the original footprint.
    public static int moundV2(int x,int z){
        int h=mound(x,z);
        if(h==0)return 0;
        double r=Math.hypot(x,z)/16.0, a=Math.atan2(z,x);
        int variation=(int)Math.round((Math.sin(a*3+.7)+.55*Math.cos(a*5-1))*Math.min(1,r*2)*1.2);
        return Math.max(1,Math.min(9,h+variation));
    }
    public static int tunnelFloor(int z){return -1-(z-ENTRANCE_Z);}
    public static int tunnelRoof(int z){return 6+((z>=-12&&z<=-8)?2:(z>=-15&&z<=-7)?1:0);}
    public static int tunnelOffset(int z){return z>=-13&&z<=-9?1:0;}
    public static int tunnelHalf(int z){return z>=-14&&z<=-8?4:3;}
    public static boolean trigger(int x,int y,int z){
        // The doorway and the descending burrow activate the boss, not the surface above it.
        boolean tunnel=z>=ENTRANCE_Z+1&&z<=-6&&Math.abs(x-tunnelOffset(z))<=3
            &&y>tunnelFloor(z)&&y<=tunnelFloor(z)+Math.min(4,tunnelRoof(z))&&y<=0;
        return tunnel||(arena(x,z)&&y>FLOOR&&y<-3);
    }
    private static List<Cell> build(){
        Map<Pos,Kind> m=new LinkedHashMap<>();
        for(int x=MIN_X;x<=MAX_X;x++)for(int z=-16;z<=16;z++){
            int h=moundV2(x,z);if(h==0)continue;
            for(int y=-3;y<h;y++)put(m,x,y,z,y<0?Kind.FILL:Kind.SANDSTONE);
        }
        // The original arena footprint and floor are unchanged. Existing rock is retained.
        for(int x=-14;x<=14;x++)for(int z=-10;z<=18;z++){
            int d=x*x+(z-ARENA_Z)*(z-ARENA_Z);if(d>196)continue;
            boolean inner=d<=169&&z!=18;
            for(int y=FLOOR;y<=-3;y++){
                Kind k=(!inner||y==FLOOR||y==-3)?Kind.SEAL:Kind.AIR;
                if(inner&&y==FLOOR&&(d<=9||Math.abs(d-100)<=8))k=Kind.RED;
                put(m,x,y,z,k);
            }
        }
        // A sloping, irregular burrow. The central three stair lanes are uninterrupted.
        // The margins are rough sandstone ledges, not a paved road or straight masonry walls.
        for(int z=ENTRANCE_Z;z<=-6;z++){
            int floor=tunnelFloor(z),roof=tunnelRoof(z),center=tunnelOffset(z),half=tunnelHalf(z);
            for(int dx=-half-1;dx<=half+1;dx++){
                int x=center+dx,dist=Math.abs(dx);
                int clearance=dist<=2?roof:dist==3?roof-2:dist==4&&half==4?roof-4:0;
                put(m,x,floor,z,Math.abs(x)<=2?Kind.STAIR:Kind.SEAL);
                // Stop natural sand falling into the full opening; preserve rock if already sound.
                for(int h=1;h<=roof+1;h++){
                    if(h<=clearance)put(m,x,floor+h,z,Kind.AIR);
                    else if(z>ENTRANCE_Z&&(h<=clearance+1||dist>=3&&h<=roof))
                        put(m,x,floor+h,z,Kind.SEAL);
                }
                // Rough entrance lip merges into the mound rather than a freestanding portal.
                if(z>=-16&&z<=-13&&clearance>0){
                    int cap=floor+clearance+1;
                    put(m,x,cap,z,dist>=3?Kind.SANDSTONE:Kind.SEAL);
                    if(z>=-15&&cap+1<=8&&dist<=3)put(m,x,cap+1,z,Kind.SANDSTONE);
                }
            }
        }
        // Clear, full-height connection from the final stair to the main arena.
        for(int z=-5;z<=-3;z++)for(int x=-3;x<=3;x++){
            put(m,x,FLOOR,z,Kind.SEAL);
            for(int y=FLOOR+1;y<=-4;y++)put(m,x,y,z,Kind.AIR);
            put(m,x,-3,z,Kind.SEAL);
        }
        // Small traces of a former shrine, integrated in the rock, no separate gate or statues.
        for(int z=-16;z<=-14;z++)for(int side:new int[]{-1,1}){
            int x=tunnelOffset(z)+side*(tunnelHalf(z)+1);
            put(m,x,tunnelFloor(z)+2,z,z==-15?Kind.CHISELED:Kind.CUT);
            if(z==-15)put(m,x,tunnelFloor(z)+3,z,Kind.LIGHT);
        }
        // Embedded lighting: a ring at floor level and a second ring in the ceiling,
        // plus the central ceiling. No protruding pedestals in the fighting area.
        for(int a=0;a<12;a++){
            double angle=2*Math.PI*a/12;
            int x=(int)Math.round(11*Math.cos(angle)),z=ARENA_Z+(int)Math.round(11*Math.sin(angle));
            if(z<=-5&&Math.abs(x)<=4)continue;
            put(m,x,FLOOR,z,Kind.LIGHT);
        }
        for(int a=0;a<8;a++){
            double angle=2*Math.PI*a/8+.3;
            int x=(int)Math.round(7*Math.cos(angle)),z=ARENA_Z+(int)Math.round(7*Math.sin(angle));
            put(m,x,-3,z,Kind.LIGHT);
        }
        put(m,0,-3,ARENA_Z,Kind.LIGHT);
        // Two recessed lamps in the descending burrow, never inside its central lanes.
        for(int z:new int[]{-14,-10}){
            int x=tunnelOffset(z)+(tunnelHalf(z)+1)*(z==-14?-1:1);
            put(m,x,tunnelFloor(z)+3,z,Kind.LIGHT);
        }
        // Massive flared base, smoothly curving taper and irregular broken crown.
        // Every layer is face-connected to the next; the main shaft is hollow where thick.
        for(int y=7;y<=29;y++){
            double t=(y-7)/22.0;
            double cx=2.8*t*t-.8*Math.sin(t*Math.PI),cz=-2.2*t+.65*Math.sin(t*2*Math.PI);
            double radius=y<=10?3.45-(y-7)*.25:y<=22?2.65-(y-10)*.075:1.75-(y-22)*.08;
            double rz=radius*(.92+.07*Math.sin(y*.65));
            for(int x=-5;x<=6;x++)for(int z=-6;z<=5;z++){
                double d=Math.hypot((x-cx)/radius,(z-cz)/rz);
                double a=Math.atan2(z-cz,x-cx);
                double contour=1+.11*Math.sin(3*a+y*.22)+.06*Math.cos(5*a-y*.4);
                if(d>contour)continue;
                if(y>=29&&((x+2*z)&3)==0)continue;
                if(y>10&&y<28&&d<.40)continue;
                Kind k=(y%3==0&&d>.63)||(d>.80&&Math.floorMod(x*13+z*7+y,9)==0)?Kind.ROOT:Kind.LOG;
                put(m,x,y,z,k);
            }
        }
        // Short broken leaf bases on the crown only: no leaves, fronds or long branches.
        for(int[] stub:new int[][]{{1,27,-2,-1,0},{3,27,-2,1,0},{3,28,-3,0,-1},{2,26,-1,0,1}}){
            put(m,stub[0],stub[1],stub[2],Kind.ROOT);
            put(m,stub[0]+stub[3],stub[1]+1,stub[2]+stub[4],Kind.ROOT);
        }
        // Seven independent, tapering buttress roots with continuous exposed ridges.
        for(int a=0;a<7;a++){
            double angle=a*2*Math.PI/7+.34;int len=10+(a%3)*2;
            for(int i=0;i<=len;i++){
                double bend=.8*Math.sin(i*.32+a)*i/len;
                int x=(int)Math.round(i*Math.cos(angle)-bend*Math.sin(angle)),z=(int)Math.round(i*Math.sin(angle)+bend*Math.cos(angle));
                int y=Math.max(0,moundV2(x,z)-1),r=i<3?2:i<9?1:0;
                int rise=i<4?3:i<8?2:1;
                for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                    int wx=x+dx,wz=z+dz;
                    if(Math.abs(dx)+Math.abs(dz)>r+1||Math.abs(wx)>15||Math.abs(wz)>15)continue;
                    int ground=Math.max(0,moundV2(wx,wz)-1);
                    for(int yy=Math.min(y,ground);yy<=Math.max(y,ground)+rise-1;yy++){
                        // Preserve burrow air and its shell, including the tall entrance.
                        if(wz<=-6&&Math.abs(wx-tunnelOffset(wz))<=tunnelHalf(wz)+1)continue;
                        Kind old=m.get(new Pos(wx,yy,wz));
                        if(old==Kind.AIR||old==Kind.LIGHT||old==Kind.STAIR)continue;
                        put(m,wx,yy,wz,Kind.ROOT);
                    }
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
