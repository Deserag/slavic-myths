package org.slavicmyths.desert.temple;

/** Compact V2. Integer block coordinates; entrance faces north, stairs ascend south.
 * All structural and decorative geometry is shared by manual and natural generation. */
public final class SunTemplePlan {
    public static final int VERSION=2,W=81,D=91,H=53,CX=40,CZ=58,FLOOR=9,RADIUS=20;
    public static final int AIR=0,STONE=1,BRICK=2,CRACK=3,DARK=4,SMOOTH=5,CARVED=6,BLACK=7,RED=8,GOLD=9,SOLAR=10,WOOL=11,YELLOW=12,LIGHT=13,SEAL=14,WATER=15,FIRE=16,STAIR=17,LADDER=18,CARPET=19;
    public static final SunTemplePlan INSTANCE=new SunTemplePlan();
    private final byte[] voxels=new byte[W*D*H];
    public int at(int x,int y,int z){return x<0||x>=W||y<0||y>=H||z<0||z>=D?AIR:voxels[(x*D+z)*H+y];}
    static int width(int v){return v==1?LegacySunTemplePlan.W:W;}
    static int depth(int v){return v==1?LegacySunTemplePlan.D:D;}
    static int height(int v){return v==1?LegacySunTemplePlan.H:H;}
    static int cx(int v){return v==1?LegacySunTemplePlan.CX:CX;}
    static int cz(int v){return v==1?LegacySunTemplePlan.CZ:CZ;}
    static int floor(int v){return v==1?LegacySunTemplePlan.FLOOR:FLOOR;}
    static int at(int v,int x,int y,int z){return v==1?LegacySunTemplePlan.INSTANCE.at(x,y,z):INSTANCE.at(x,y,z);}
    static boolean arena(int v,int x,int y,int z){return v==1?LegacySunTemplePlan.arena(x,y,z):arena(x,y,z);}
    private void b(int x,int X,int z,int Z,int y,int Y,int m){
        int lo=Math.max(0,y),hi=Math.min(H,Y+1);if(lo>=hi)return;
        for(int i=Math.max(0,x);i<=Math.min(W-1,X);i++)for(int j=Math.max(0,z);j<=Math.min(D-1,Z);j++)
            java.util.Arrays.fill(voxels,(i*D+j)*H+lo,(i*D+j)*H+hi,(byte)m);
    }
    private void wall(int x,int X,int z,int Z,int y,int Y,int t,int m){
        b(x,X,z,z+t-1,y,Y,m);b(x,X,Z-t+1,Z,y,Y,m);b(x,x+t-1,z,Z,y,Y,m);b(X-t+1,X,z,Z,y,Y,m);
    }
    public static boolean octa(int x,int z,int r){return Math.max(Math.abs(x),Math.abs(z))<=r&&Math.abs(x)+Math.abs(z)<=r*3/2;}
    public static boolean arena(int x,int y,int z){return y>=FLOOR&&y<=FLOOR+25&&octa(x-CX,z-CZ,RADIUS);}
    private void pier(int x,int z,int y,int h){
        b(x-1,x+1,z-1,z+1,y,y+h-1,BRICK);
        b(x-2,x+2,z-2,z+2,y,y,SMOOTH);
        b(x,x,z-1,z-1,y+2,y+h-3,DARK);
        b(x-2,x+2,z-2,z+2,y+h-2,y+h-2,CARVED);
        b(x-2,x+2,z-2,z+2,y+h-1,y+h-1,GOLD);
    }
    private void brazier(int x,int z,int y){
        b(x-1,x+1,z-1,z+1,y,y,BLACK);b(x,x,z,z,y+1,y+1,SOLAR);
        b(x,x,z,z,y+2,y+2,FIRE);
    }
    private void sun(int x,int z,int y,int r,int ray){
        for(int i=-r-ray;i<=r+ray;i++)for(int j=-r-ray;j<=r+ray;j++){
            double d=Math.hypot(i,j),a=Math.atan2(j,i);
            if(d<=r||d<=r+ray&&Math.abs(Math.sin(a*4))*d<.9)b(x+i,x+i,z,z,y+j,y+j,GOLD);
        }
    }
    private void banner(int x,int z,int top){
        b(x-2,x+2,z,z,top-7,top,GOLD);b(x-1,x+1,z,z,top-6,top-1,WOOL);b(x,x,z,z,top-4,top-3,YELLOW);
        b(x-1,x+1,z,z,top-4,top-4,YELLOW);b(x,x,z,z,top-8,top-8,WOOL);
    }
    private void stair(int x,int X,int start,int end,int firstY){
        for(int z=start;z<=end;z++){
            int y=firstY+z-start;
            b(x,X,z,z,0,y-1,STONE);b(x,X,z,z,y,y,STAIR);
            b(x,X,z,z,y+1,y+5,AIR);
        }
    }
    private void batt(int x,int X,int z,int Z,int y){
        wall(x,X,z,Z,y,y,1,CARVED);
        for(int i=x;i<=X;i+=4){b(i,Math.min(i+1,X),z,z,y+1,y+1,SMOOTH);b(i,Math.min(i+1,X),Z,Z,y+1,y+1,SMOOTH);}
        for(int i=z;i<=Z;i+=4){b(x,x,i,Math.min(i+1,Z),y+1,y+1,SMOOTH);b(X,X,i,Math.min(i+1,Z),y+1,y+1,SMOOTH);}
    }
    private SunTemplePlan(){
        // Ground platform: three low courses, with the entrance steps starting at terrain level.
        b(5,75,4,86,0,2,STONE);b(5,75,4,86,3,3,SMOOTH);
        wall(4,76,5,87,0,5,2,BRICK);
        stair(31,49,0,3,0);
        b(29,30,0,4,0,3,BRICK);b(50,51,0,4,0,3,BRICK);
        // Front gate, with a single contiguous opening rather than a blind internal wall.
        for(int x:new int[]{21,59}){
            b(x-7,x+7,5,12,0,17,BRICK);b(x-5,x+5,4,4,4,15,BLACK);
            b(x-7,x+7,4,12,16,17,CARVED);b(x-7,x+7,4,12,18,18,GOLD);
            for(int y=0;y<=14;y++){int t=Math.max(0,4-y/4);b(x-7,x-6,5-t,6,y,y,SMOOTH);b(x+6,x+7,5-t,6,y,y,SMOOTH);}
            banner(x,3,14);
        }
        b(29,51,5,8,4,17,BLACK);
        for(int x=29;x<=51;x++){int dx=Math.abs(x-CX);int top=9+Math.max(0,5-dx/2);
            if(dx<=7)b(x,x,4,12,4,top,AIR);
            if(dx>=8&&dx<=10)b(x,x,4,4,4,top+1,SMOOTH);
            if(dx<=7)b(x,x,4,4,top+1,top+2,SMOOTH);
        }
        b(28,52,4,9,18,18,CARVED);b(28,52,4,9,19,19,GOLD);sun(CX,3,17,2,3);
        for(int x:new int[]{28,52})brazier(x,2,3);
        // Open courtyard at Y=3. Columns only on the sides; no roof spans the approach.
        for(int x=13;x<=67;x++)for(int z=13;z<=32;z++){
            if(x==13||x==67||(z==13||z==32)&&Math.abs(x-CX)>10)b(x,x,z,z,3,3,CARVED);
            if(Math.abs(x-CX)<=4)b(x,x,z,z,3,3,WOOL);
            if(Math.abs(x-CX)==5)b(x,x,z,z,3,3,YELLOW);
        }
        // Side galleries and sloping buttresses, connected to the courtyard at ground level.
        for(int s:new int[]{-1,1}){
            int e=CX+s*34,inner=CX+s*26,a=Math.min(e,inner),c=Math.max(e,inner);
            for(int z=15;z<=72;z+=9){
                for(int y=0;y<=10;y++){int t=Math.max(0,4-y/3),outer=e+s*t;b(Math.min(e,outer),Math.max(e,outer),z-1,z+1,y,y,SMOOTH);}
                b(e,e,z+3,z+5,5,9,BLACK);
                pier(e,z,4,9);pier(inner,z,4,9);
                if(z%2==1)brazier(inner-s*3,z,4);
            }
            b(a-1,c+1,14,72,13,13,SMOOTH);b(a-1,c+1,14,72,14,14,CARVED);
            batt(a-1,c+1,14,72,15);
        }
        // Solid stepped arena plinth, never a hollow unsupported deck.
        for(int x=17;x<=63;x++)for(int z=35;z<=81;z++){
            int dx=x-CX,dz=z-CZ;if(!octa(dx,dz,22))continue;
            b(x,x,z,z,0,8,STONE);
            if(!octa(dx,dz,21))b(x,x,z,z,4,8,BRICK);
            double r=Math.hypot(dx,dz),a=Math.atan2(dz,dx);int m=SMOOTH;
            if(r>=18&&r<19||r>=11&&r<12)m=BLACK;
            if(r>=16&&r<17||r>=7&&r<8)m=GOLD;
            if(r<=3||r>3&&r<15&&Math.abs(Math.sin(a*4))*r<.85)m=SOLAR;
            if(r>3&&r<=5)m=WOOL;
            b(x,x,z,z,FLOOR,FLOOR,m);
            if(!octa(dx,dz,20)&&!(z<=40&&Math.abs(dx)<=8))b(x,x,z,z,10,11,CARVED);
        }
        stair(33,47,30,35,4);
        b(33,47,36,40,0,9,SMOOTH);
        // Four flush basins: 3 x 3 water, one block deep, with a continuous stone liner.
        for(int dx:new int[]{-12,12})for(int dz:new int[]{-12,12}){
            int x=CX+dx,z=CZ+dz;b(x-2,x+2,z-2,z+2,9,9,CARVED);
            b(x-1,x+1,z-1,z+1,8,8,SMOOTH);b(x-1,x+1,z-1,z+1,9,9,WATER);
        }
        // Rear and side colonnade is broken at the entrance and leaves the center sky open.
        for(int[] p:new int[][]{{19,55},{19,64},{24,73},{32,78},{40,78},{48,78},{56,73},{61,64},{61,55}}){
            pier(p[0],p[1],10,10);
            if(p[1]>=73)banner(p[0],p[1]-3,18);
        }
        for(int x=24;x<=56;x++){
            int z=78-Math.max(0,Math.abs(x-CX)-8);
            b(x,x,z-1,z+1,20,20,CARVED);b(x,x,z-1,z+1,21,21,GOLD);
        }
        for(int x:new int[]{22,58})for(int z:new int[]{48,67})brazier(x,z,10);
        // Twin compact towers. Usable vertical ladder and bridge landing.
        for(int x:new int[]{10,70}){
            int z=80;
            b(x-7,x+7,z-7,z+7,0,9,STONE);
            wall(x-6,x+6,z-6,z+6,10,38,2,BRICK);
            for(int y=10;y<36;y++){
                int t=Math.max(0,3-(y-10)/7);
                for(int sg:new int[]{-1,1}){
                    int a=x+sg*6,c=x+sg*(6+t);b(Math.min(a,c),Math.max(a,c),z-1,z+1,y,y,SMOOTH);
                }
            }
            b(x-3,x+3,z-7,z-7,12,35,BLACK);b(x-4,x-4,z-7,z-7,12,35,SMOOTH);b(x+4,x+4,z-7,z-7,12,35,SMOOTH);
            banner(x,z-8,31);
            for(int y:new int[]{22,38}){b(x-6,x+6,z-6,z+6,y,y,CARVED);wall(x-7,x+7,z-7,z+7,y+1,y+1,1,GOLD);}
            for(int dx:new int[]{-5,5})for(int dz:new int[]{-5,5})pier(x+dx,z+dz,39,7);
            b(x-6,x+6,z-6,z+6,46,46,CARVED);
            for(int i=0;i<4;i++)b(x-5+i,x+5-i,z-5+i,z+5-i,47+i,47+i,BLACK);
            b(x,x,z,z,51,52,GOLD);b(x-2,x+2,z,z,50,50,GOLD);
            brazier(x-2,z-2,39);
            // Tower access from the arena-side platform and an uninterrupted shaft.
            int sg=x<CX?1:-1,edge=x+sg*6;
            // Connect the tower door to the rear arena ring on a real supported landing.
            b(Math.min(edge,x+sg*20),Math.max(edge,x+sg*20),74,78,0,9,SMOOTH);
            b(Math.min(edge,x+sg*20),Math.max(edge,x+sg*20),75,77,10,13,AIR);
            b(Math.min(edge,x+sg*4),Math.max(edge,x+sg*4),75,77,10,14,AIR);
            b(x-1,x+3,z-1,z+1,10,43,AIR);b(x+3,x+3,z-1,z+1,10,43,BRICK);b(x+2,x+2,z,z,10,42,LADDER);
            b(x-1,x+1,z-4,z,10,13,AIR);
        }
        // High rear bridge outside the battle footprint. Walkable along the central lane.
        b(16,64,78,82,38,38,BRICK);b(16,64,78,82,39,39,SMOOTH);
        b(16,64,78,78,40,40,CARVED);b(16,64,82,82,40,40,CARVED);
        for(int x=16;x<=64;x+=6){b(x,x+1,78,78,40,44,SMOOTH);b(x,x+1,82,82,40,44,SMOOTH);}
        b(16,64,78,82,45,45,CARVED);b(16,64,78,78,46,46,GOLD);
        b(16,64,82,82,46,46,GOLD);sun(CX,77,46,2,4);
        for(int x:new int[]{10,70}){
            int a=Math.min(x,x==10?16:64),c=Math.max(x,x==10?16:64);
            b(a,c,79,81,39,39,SMOOTH);b(a,c,79,81,40,44,AIR);
            b(x+2,x+2,80,80,39,42,LADDER);
        }
        // Final approach contract: a continuous surface and full three-block headroom.
        // At z=0..3 the staircase is ascending; z=4..29 is the open courtyard.
        for(int z=0;z<=40;z++){
            int y=z<=3?z:z<30?3:z<=35?4+z-30:9;
            int radius=z<=3?8:z<=12?6:z<30?8:6;
            b(CX-radius,CX+radius,z,z,y+1,y+3,AIR);
        }
        // No decorative blocks inside the central 21 x 21 combat clearance.
        // Open the tower connector at the arena ring only, outside the central combat zone.
        for(int x:new int[]{30,50})b(x-1,x+1,75,77,10,13,AIR);
    }
}
