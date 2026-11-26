package org.slavicmyths.desert.temple;

/** Integer voxel plan, derived from the approved 111x137x83 Python V1.
 * Coordinates: x east, z south (entrance at z=0), y up. No game dependencies. */
final class LegacySunTemplePlan {
    public static final int W=111,D=137,H=83,CX=55,CZ=96,FLOOR=29;
    public static final int AIR=0,STONE=1,BRICK=2,CRACK=3,DARK=4,SMOOTH=5,CARVED=6,BLACK=7,RED=8,GOLD=9,SOLAR=10,WOOL=11,YELLOW=12,LIGHT=13,SEAL=14,WATER=15,FIRE=16,STAIR=17,LADDER=18,CARPET=19;
    public static final LegacySunTemplePlan INSTANCE=new LegacySunTemplePlan();
    private final byte[] voxels=new byte[W*D*H];
    public int at(int x,int y,int z){return x<0||x>=W||y<0||y>=H||z<0||z>=D?AIR:voxels[(x*D+z)*H+y];}
    private void b(int x,int X,int z,int Z,int y,int Y,int m){
        for(int i=Math.max(0,x);i<=Math.min(W-1,X);i++)
            for(int j=Math.max(0,z);j<=Math.min(D-1,Z);j++)
                java.util.Arrays.fill(voxels,(i*D+j)*H+Math.max(0,y),(i*D+j)*H+Math.min(H,Y+1),(byte)m);
    }
    private void wall(int x,int X,int z,int Z,int y,int Y,int t,int m){
        b(x,X,z,z+t-1,y,Y,m);b(x,X,Z-t+1,Z,y,Y,m);b(x,x+t-1,z,Z,y,Y,m);b(X-t+1,X,z,Z,y,Y,m);
    }
    private void pier(int x,int z,int y,int h){
        b(x-1,x+1,z-1,z+1,y,y+h,BRICK);b(x-2,x+2,z-2,z+2,y,y+1,SMOOTH);
        b(x-2,x+2,z-2,z+2,y+h-2,y+h-1,CARVED);b(x-2,x+2,z-2,z+2,y+h,y+h,GOLD);
    }
    private void fire(int x,int z,int y){
        b(x-2,x+2,z-2,z+2,y,y,SMOOTH);b(x-1,x+1,z-1,z+1,y+1,y+3,BLACK);
        b(x-2,x+2,z-2,z+2,y+4,y+4,SOLAR);b(x,x,z,z,y+5,y+5,FIRE);
    }
    private void banner(int x,int z,int y,int w,int h){
        for(int i=-w/2;i<=w/2;i++)for(int j=0;j<h-Math.max(0,Math.abs(i)-1);j++){
            double r=Math.hypot(i,j-h/2);int m=Math.abs(i)==w/2||j==0?GOLD:WOOL;
            if(r>1.1&&r<2.3||((i==0||j==h/2||Math.abs(i)==Math.abs(j-h/2))&&r<3.3))m=YELLOW;
            b(x+i,x+i,z,z,y-j,y-j,m);
        }
    }
    private void sun(int x,int z,int y,int r){
        for(int i=-r-7;i<=r+7;i++)for(int j=-r-7;j<=r+7;j++){
            double d=Math.hypot(i,j),a=Math.atan2(j,i);
            if(d<=r||d<=r+6&&Math.abs(Math.sin(a*4))*d<1.25)b(x+i,x+i,z,z,y+j,y+j,GOLD);
        }
    }
    private void arc(int x,int z,int y,int r,int m,boolean rays){
        for(int i=-r-8;i<=r+8;i++)for(int j=0;j<=r+7;j++){
            double d=Math.hypot(i,j),a=Math.atan2(j,i);
            if(Math.abs(d-r)<=1.4||rays&&d>=r&&d<=r+6&&Math.abs(Math.sin(a*6))*d<1.1)b(x+i,x+i,z,z+1,y+j,y+j,m);
        }
    }
    private static boolean ellipse(int i,int j,int r,int straight,int rise){return Math.abs(i)<=r&&(j<=straight||Math.pow((double)i/r,2)+Math.pow((double)(j-straight)/rise,2)<=1);}
    private void arch(int x,int z,int y,int r,int straight,int rise){
        for(int i=-r-4;i<=r+4;i++)for(int j=0;j<straight+rise+5;j++)
            if(ellipse(i,j,r+3,straight,rise+3)&&!ellipse(i,j,r,straight,rise))b(x+i,x+i,z,z+2,y+j,y+j,SMOOTH);
    }
    private void batt(int x,int X,int z,int Z,int y){
        wall(x,X,z,Z,y,y+1,1,SMOOTH);
        for(int i=x;i<=X;i+=5){b(i,Math.min(i+2,X),z,z,y+2,y+3,CARVED);b(i,Math.min(i+2,X),Z,Z,y+2,y+3,CARVED);}
        for(int i=z;i<=Z;i+=5){b(x,x,i,Math.min(i+2,Z),y+2,y+3,CARVED);b(X,X,i,Math.min(i+2,Z),y+2,y+3,CARVED);}
    }
    public static boolean octa(int x,int z,int r){return Math.max(Math.abs(x),Math.abs(z))<=r&&Math.abs(x)+Math.abs(z)<=(int)(r*1.45);}
    public static boolean arena(int x,int y,int z){return y>=FLOOR&&y<=FLOOR+25&&octa(x-CX,z-CZ,30);}
    private void stair(int x,int X,int start,int end,int firstY){
        for(int z=start;z<=end;z++){
            int y=firstY+z-start;
            b(x,X,z,z,0,y-1,STONE);b(x,X,z,z,y,y,STAIR);b(x,X,z,z,y+1,y+4,AIR);
        }
    }
    private LegacySunTemplePlan(){
        b(8,102,16,132,0,2,STONE);wall(5,105,16,133,3,20,3,BRICK);wall(3,107,18,131,3,5,2,SMOOTH);wall(4,106,16,133,19,21,2,CARVED);
        b(12,98,18,28,3,24,STONE);b(37,73,16,20,8,35,BLACK);
        for(int i=-12;i<=12;i++){int top=9+(int)(7*Math.sqrt(Math.max(0,1-Math.pow(i/13.0,2))));b(55+i,55+i,16,28,8,8+top,AIR);}
        arch(55,15,8,12,9,7);
        for(int z=0;z<16;z++){int y=z/2;b(39,71,z,z,0,y,SMOOTH);b(37,38,z,z,0,y+1,BRICK);b(72,73,z,z,0,y+1,BRICK);if(z%2==0)b(39,71,z,z,y,y,STAIR);}
        b(42,68,16,28,7,7,SMOOTH);b(51,59,16,28,8,8,CARPET);
        for(int x:new int[]{29,81}){
            b(x-5,x+5,16,19,5,25,BLACK);b(x-6,x-5,15,20,5,25,SMOOTH);b(x+5,x+6,15,20,5,25,SMOOTH);b(x-6,x+6,15,20,25,26,CARVED);banner(x,14,22,7,15);
        }
        for(int x=35;x<=75;x++){int cap=36-Math.abs(x-55)/5;b(x,x,15,20,cap,cap+1,SMOOTH);}
        sun(55,15,29,4);
        for(int x:new int[]{16,95}){
            b(x-4,x+4,14,28,3,24,BRICK);b(x-5,x+5,14,28,24,25,SMOOTH);b(x-4,x+4,13,13,18,18,GOLD);
            for(int y=3;y<23;y++)b(x-3,x+3,14-Math.max(1,6-y/4),15,y,y,SMOOTH);
        }
        for(int s=0;s<2;s++){
            int e=s==0?6:104,d=s==0?1:-1;
            for(int z=27;z<126;z+=15){
                for(int y=3;y<25;y++){int t=Math.max(2,7-(y-3)/4),a=e-d*t,c=e+d*2;b(Math.min(a,c),Math.max(a,c),z-3,z+3,y,y,BRICK);b(a,a,z-2,z+2,y,y,SMOOTH);if(y>7&&y<17)b(a,a,z,z,y,y,RED);}
                b(e,e,z+5,z+7,5,22,BLACK);b(e-d,e-d,z+5,z+7,19,20,GOLD);
            }
            int a=Math.min(e,e+d*16),c=Math.max(e,e+d*16);
            b(a,c,27,125,21,23,SMOOTH);
            for(int z=32;z<124;z+=10){pier(e+d*4,z,24,11);pier(e+d*14,z,24,11);}
            b(a,c,28,124,36,37,SMOOTH);b(a,c,28,124,38,38,DARK);batt(a,c,28,124,39);
            int inner=e+d*14;b(inner,inner,30,125,3,20,BRICK);
            for(int z=34;z<122;z+=14){b(Math.min(e+d*3,inner),Math.max(e+d*3,inner),z,z,3,20,BRICK);b(inner,inner,z+4,z+7,3,8,AIR);}
        }
        wall(23,87,29,62,3,20,2,BRICK);b(23,87,29,62,21,23,SMOOTH);
        // Continuous dry stair route from gate to terrace, then arena.
        stair(45,65,29,44,8);
        b(45,65,45,60,23,23,SMOOTH);b(51,59,45,60,24,24,CARPET);
        for(int x:new int[]{29,81})for(int z:new int[]{45,58})fire(x,z,24);
        for(int x=22;x<89;x++)for(int z=63;z<130;z++){
            int dx=x-CX,dz=z-CZ;if(!octa(dx,dz,32))continue;
            if(!octa(dx,dz,29)){b(x,x,z,z,3,25,BRICK);if((x+z)%11<2)b(x,x,z,z,4,25,DARK);}
            b(x,x,z,z,26,28,BRICK);double r=Math.hypot(dx,dz),a=Math.atan2(dz,dx);int m=SMOOTH;
            if(r>27&&r<29||r>18&&r<20)m=BLACK;if(r>24&&r<25||r>12&&r<13)m=GOLD;
            if(r<5||r>=6&&r<=18&&Math.abs(Math.sin(a*4))*r<1.5)m=SOLAR;if(r>=5&&r<7)m=WOOL;
            b(x,x,z,z,FLOOR,FLOOR,m);
            if(!octa(dx,dz,30)&&!(z<72&&Math.abs(dx)<13)){b(x,x,z,z,30,33,CARVED);if(z>=CZ||Math.abs(dx)>=26){b(x,x,z,z,45,46,SMOOTH);b(x,x,z,z,47,47,GOLD);}}
        }
        stair(44,66,61,66,24);b(44,66,67,72,29,29,SMOOTH);
        for(int deg=0;deg<360;deg+=30){
            if(deg>=240&&deg<=300)continue;double a=Math.toRadians(deg),c=Math.cos(a),s=Math.sin(a),r=Math.min(30/Math.max(Math.abs(c),Math.abs(s)),43/(Math.abs(c)+Math.abs(s)));
            int x=CX+(int)Math.round(r*c),z=CZ+(int)Math.round(r*s);pier(x,z,30,16);if(z>CZ)banner(x,z-3,44,3,9);
        }
        for(int deg:new int[]{45,135,225,315}){
            double a=Math.toRadians(deg);int x=CX+(int)Math.round(23*Math.cos(a)),z=CZ+(int)Math.round(23*Math.sin(a));
            b(x-3,x+3,z-3,z+3,29,29,CARVED);b(x-2,x+2,z-2,z+2,29,29,WATER);b(x-2,x+2,z-2,z+2,28,28,SMOOTH);
        }
        for(int deg:new int[]{0,60,120,180,210,330}){double a=Math.toRadians(deg);fire(CX+(int)Math.round(27*Math.cos(a)),CZ+(int)Math.round(27*Math.sin(a)),30);}
        arc(55,124,47,22,GOLD,true);arc(55,127,47,26,SMOOTH,false);
        for(int x:new int[]{14,96}){
            int z=107;b(x-11,x+11,z-12,z+12,21,24,SMOOTH);wall(x-9,x+9,z-10,z+10,25,57,3,BRICK);
            for(int y=26;y<57;y++)for(int sg:new int[]{-1,1}){
                int t=Math.max(0,7-(y-26)/5),a=x+sg*8,c=x+sg*(9+t);b(Math.min(a,c),Math.max(a,c),z-2,z+2,y,y,SMOOTH);
                a=z+sg*9;c=z+sg*(10+t);b(x-2,x+2,Math.min(a,c),Math.max(a,c),y,y,SMOOTH);
            }
            for(int sg:new int[]{-1,1}){b(x-5,x+5,z+sg*10,z+sg*10,27,55,BLACK);b(x+sg*9,x+sg*9,z-5,z+5,28,55,BLACK);}
            b(x-5,x+5,92,96,40,55,BLACK);b(x-6,x-5,91,96,39,55,SMOOTH);b(x+5,x+6,91,96,39,55,SMOOTH);banner(x,91,53,7,14);
            for(int y:new int[]{36,47,57}){b(x-10,x+10,z-11,z+11,y,y,CARVED);wall(x-11,x+11,z-12,z+12,y+1,y+1,1,GOLD);}
            for(int dx:new int[]{-7,0,7})for(int dz:new int[]{-8,8})pier(x+dx,z+dz,59,10);
            for(int dx:new int[]{-7,7})pier(x+dx,z,59,10);
            fire(x,z,59);b(x-10,x+10,z-11,z+11,70,70,SMOOTH);
            for(int j=0;j<5;j++)b(x-8+j,x+8-j,z-9+j,z+9-j,71+j,71+j,BLACK);
            b(x,x,z,z,76,82,GOLD);
            for(int sg:new int[]{-1,1})for(int j=1;j<=5;j++){b(x+sg*j,x+sg*j,z,z,76+j/2,76+j/2,GOLD);b(x,x,z+sg*j,z+sg*j,76+j/2,76+j/2,GOLD);}
            // Interior ladder shaft with continuous wall backing and access at each level.
            b(x+2,x+5,105,109,25,66,AIR);b(x+5,x+5,105,109,25,66,BRICK);b(x+4,x+4,107,107,25,66,LADDER);
            b(x-2,x+4,96,106,25,29,AIR);b(x-2,x+4,96,106,24,24,SMOOTH);
        }
        int z=113;b(23,87,z-3,z+3,60,62,BRICK);b(23,87,z-4,z+4,63,63,SMOOTH);
        b(23,87,z-4,z-4,64,64,GOLD);b(23,87,z+4,z+4,64,64,GOLD);
        for(int x=26;x<88;x+=8){b(x-1,x+1,z-3,z+3,64,68,SMOOTH);b(x-2,x+2,z-4,z+4,68,68,CARVED);if(x%16<8)banner(x,z-5,63,3,7);}
        b(23,87,z-4,z+4,69,69,CARVED);b(23,87,z-4,z+4,70,70,GOLD);
        for(int x=24;x<88;x++){int y=40+(int)(20*Math.sqrt(Math.max(0,1-Math.pow((x-55)/32.0,2))));b(x,x,z-2,z+2,y-2,y,SMOOTH);b(x,x,z-3,z-3,y-2,y,DARK);}
        sun(55,z-5,71,5);
        for(int x:new int[]{34,76}){fire(x,9,4);fire(x,25,25);}
        for(int x:new int[]{13,97})for(int q:new int[]{24,64})fire(x,q,42);
        for(int x=26;x<87;x+=9){b(x-1,x+1,131,133,5,24,BLACK);b(x-2,x+2,130,134,24,25,CARVED);}
        for(int x:new int[]{29,81}){b(x-2,x+2,124,129,30,46,BRICK);b(x-3,x+3,123,129,45,47,CARVED);}
        // Carve the primary path last, after supports and facades.
        b(43,67,16,28,8,17,AIR);b(51,59,16,28,8,8,CARPET);
        for(int q=29;q<=44;q++){int y=8+q-29;b(45,65,q,q,y+1,y+4,AIR);}
        b(45,65,45,60,25,28,AIR);
        for(int q=61;q<=66;q++){int y=24+q-61;b(44,66,q,q,y+1,y+4,AIR);}
        b(44,66,67,72,30,35,AIR);
        // Bridge is traversable, columns only on edges.
        b(23,87,112,114,64,68,AIR);
        for(int x:new int[]{14,96}){int a=Math.min(x, x==14?23:87),c=Math.max(x,x==14?23:87);b(a,c,112,114,63,63,SMOOTH);b(a,c,112,114,64,68,AIR);}
        java.util.Random rng=new java.util.Random(26);
        for(int x:new int[]{5,105})for(int i=0;i<140;i++){int q=24+rng.nextInt(106),y=4+rng.nextInt(16);if(at(x,y,q)==STONE||at(x,y,q)==BRICK)b(x,x,q,q,y,y,CRACK);}
    }
}
