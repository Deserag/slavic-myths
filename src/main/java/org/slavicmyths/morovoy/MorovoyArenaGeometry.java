package org.slavicmyths.morovoy;

/** Seed-only, chunk-order-independent terrain geometry. No Minecraft dependency. */
public final class MorovoyArenaGeometry {
 public static final int OUTER_RADIUS=120, COMBAT_RADIUS=55, FLIGHT_RADIUS=70, CLEAR_TOP_Y=186, FLIGHT_TOP_Y=169, FOUNDATION=8;
 private MorovoyArenaGeometry(){}
 private static long mix(long v){v=(v^(v>>>30))*0xbf58476d1ce4e5b9L;v=(v^(v>>>27))*0x94d049bb133111ebL;return v^(v>>>31);}
 private static double at(long seed,long x,long z){long h=mix(seed^mix(x*341873128712L)^mix(z*132897987541L));return ((h>>>11)*0x1.0p-53)*2.0-1.0;}
 private static double noise(long seed,int x,int z,int scale){
  int ix=Math.floorDiv(x,scale),iz=Math.floorDiv(z,scale);
  double tx=(double)Math.floorMod(x,scale)/scale,tz=(double)Math.floorMod(z,scale)/scale;
  tx=tx*tx*(3-2*tx);tz=tz*tz*(3-2*tz);
  double a=at(seed,ix,iz)*(1-tx)+at(seed,ix+1,iz)*tx,b=at(seed,ix,iz+1)*(1-tx)+at(seed,ix+1,iz+1)*tx;
  return a*(1-tz)+b*tz;
 }
 public static double distance(int dx,int dz,long seed){
  return Math.hypot(dx*((seed&1L)==0?1.035:1),dz*((seed&1L)==0?1:1.035))+2.2*noise(seed^0x71ab23L,dx,dz,24);
 }
 public static boolean inside(int dx,int dz,long seed){return Math.hypot(dx,dz)<=119&&distance(dx,dz,seed)<=118;}
 public static int surfaceY(int dx,int dz,int floor,long seed,int mask,int[] exits){
  // Approved entrances extend to the exact piece edge, adjacent to the preflighted exterior column.
  int pathSide=-1,pathDistance=0;
  if(exits!=null&&exits.length==4)for(int side=0;side<4;side++){
   if((mask&(1<<side))==0||exits[side]<0)continue;
   int radial=switch(side){case 0->dx;case 1->dz;case 2->-dx;default->-dz;};
   int cross=(side%2==0)?dz:dx;
   if(radial>=56&&radial<=120&&Math.abs(cross)<=5){pathSide=side;pathDistance=radial;break;}
  }
  if(pathSide>=0){
   if(pathDistance<=64)return floor+2+(int)Math.floor((pathDistance-56)*3.0/8.0);
   return floor+5+(int)Math.floor((pathDistance-64)*(exits[pathSide]-floor-5)/56.0);
  }
  if(!inside(dx,dz,seed))return Integer.MIN_VALUE;
  double d=Math.max(0,distance(dx,dz,seed)),n=noise(seed^0x35b018L,dx,dz,12);
  double rise=d<=56?Math.max(0,1.5+n):d<=80?2+(d-56)*.42+n*1.6:d<=108?12.08+(d-80)*1.25+n*2.3:47.08+(d-108)*.68+n*2.3;
  return floor+(int)Math.round(rise);
 }
 public static boolean combatPoint(int x,int z){return x*x+z*z<=COMBAT_RADIUS*COMBAT_RADIUS;}
 public static boolean flightPoint(int x,int z,int y,int floor){return Math.hypot(x,z)<=FLIGHT_RADIUS&&y>=floor+30&&y<=FLIGHT_TOP_Y;}
 public static int choice(long seed,int x,int z,int count){return (int)Math.floorMod(mix(seed^mix(x*0x9e3779b9L)^mix(z*0x7f4a7c15L)),count);}
}
