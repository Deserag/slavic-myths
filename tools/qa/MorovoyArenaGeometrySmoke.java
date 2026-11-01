package org.slavicmyths.morovoy;

/** Stand-alone Java 21 geometry check, NOT a Minecraft world generation test. */
public final class MorovoyArenaGeometrySmoke {
 private static void check(boolean p,String why){if(!p)throw new AssertionError(why);}
 public static void main(String[] args){
  long[] seeds={0L,1L,12345L,-1742048L,0x5ab1234aL};
  int[] exits={95,94,93,92};
  for(long seed:seeds){
   int checked=0,combat=0;
   for(int z=-120;z<=120;z++)for(int x=-120;x<=120;x++){
    int y=MorovoyArenaGeometry.surfaceY(x,z,48,seed,15,exits);
    check(y==MorovoyArenaGeometry.surfaceY(x,z,48,seed,15,exits),"Not deterministic");
    if(y==Integer.MIN_VALUE)continue;
    checked++;check(y>=48&&y<=112,"Floor too high");
    if(MorovoyArenaGeometry.combatPoint(x,z)){combat++;check(y<=52,"Obstacle on combat floor");}
   }
   check(checked>30000&&combat>9000,"Footprint too small");
   check(MorovoyArenaGeometry.CLEAR_TOP_Y-MorovoyArenaGeometry.FLIGHT_TOP_Y>=12,"Headroom");
   check(MorovoyArenaGeometry.FLIGHT_TOP_Y-48-30>=70,"Flight volume too low");
   int center=8,r=MorovoyArenaGeometry.OUTER_RADIUS;
   check(Math.floorDiv(center-r,16)>=-8&&Math.floorDiv(center+r,16)<=8,"Structure reference limit");
   System.out.println("Seed "+seed+" geometry-only PASS: footprint="+checked+" combat="+combat);
  }
 }
}
