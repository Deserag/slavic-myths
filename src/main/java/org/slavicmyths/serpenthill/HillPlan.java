package org.slavicmyths.serpenthill;
import java.util.*;
/** One deterministic architecture shared by native worldgen, command and offline QA. */
public final class HillPlan {
 public record Patch(int x0,int y0,int z0,int x1,int y1,int z1,String state){}
 public record Room(String name,int x0,int floor,int z0,int x1,int ceiling,int z1){}
 public record Chest(int x,int y,int z){}
 public final List<Patch> patches=new ArrayList<>();public final List<Room> rooms=new ArrayList<>();public final List<Chest> chests=new ArrayList<>();
 private final List<Patch>[] columns;private final long seed;private final int[] tops=new int[128*128];private final String[] skins=new String[128*128];
 private static final String AIR="minecraft:air",STONE="slavicmyths:serpent_masonry",CRACK="slavicmyths:cracked_serpent_masonry";
 @SuppressWarnings("unchecked") public HillPlan(long seed){this.seed=seed;columns=new List[128*128];build();for(int x=0;x<128;x++)for(int z=0;z<128;z++){columns[x*128+z]=new ArrayList<>();tops[x*128+z]=HillRules.hillTop(x,z,seed);boolean vein=Math.abs(x-64-22*Math.sin(z*.075+seed%31))<1.2||Math.abs(z-64-24*Math.sin(x*.085+seed%17))<1.1;String skin=vein?"slavicmyths:mor_stone_bricks":Math.floorMod(x*13+z*19,11)<8?"minecraft:cobbled_deepslate":"minecraft:coarse_dirt";skins[x*128+z]=skin;}for(var p:patches)for(int x=Math.max(0,p.x0);x<=Math.min(127,p.x1);x++)for(int z=Math.max(0,p.z0);z<=Math.min(127,p.z1);z++)columns[x*128+z].add(p);}
 private void box(int x0,int y0,int z0,int x1,int y1,int z1,String state){patches.add(new Patch(x0,y0,z0,x1,y1,z1,state));}
 private void room(String name,int x0,int y,int z0,int x1,int h,int z1,boolean roof){rooms.add(new Room(name,x0,y,z0,x1,y+h,z1));box(x0,y,z0,x1,y+h,z1,STONE);box(x0+1,y+1,z0+1,x1-1,y+h-1,z1-1,AIR);if(!roof)box(x0,y+h,z0,x1,y+h+15,z1,AIR);}
 private void door(int x0,int y,int z0,int x1,int z1,int height){box(x0,y,z0,x1,y+height-1,z1,AIR);}
 private void stairX(int x0,int x1,int z,int y,int sign){int n=0;for(int x=x0;sign>0?x<=x1:x>=x1;x+=sign,n++){if(x>=80||z>=100)box(x,0,z-2,x,y+n,z+2,STONE);box(x,y+n,z-2,x,y+n+6,z+2,AIR);box(x,y+n,z-2,x,y+n,z+2,"minecraft:polished_blackstone_brick_stairs@"+(sign>0?"EAST":"WEST"));}}
 private void stairZ(int z0,int z1,int x,int y,int sign){int n=0;for(int z=z0;sign>0?z<=z1:z>=z1;z+=sign,n++){if(x>=100)box(x-2,0,z,x+2,y+n,z,STONE);box(x-2,y+n,z,x+2,y+n+6,z,AIR);box(x-2,y+n,z,x+2,y+n,z,"minecraft:polished_blackstone_brick_stairs@"+(sign>0?"SOUTH":"NORTH"));}}
 private void lamp(int x,int y,int z,boolean mor){box(x,y,z,x,y,z,mor?"slavicmyths:defiled_ritual_brazier":"slavicmyths:ritual_brazier");}
 private void chest(int x,int y,int z){box(x,y,z,x,y,z,"minecraft:chest@SOUTH");chests.add(new Chest(x,y,z));}
 /** All authored reward containers, including the five barrels beside the upper chests. */
 public List<Chest> rewardContainers(){var out=new ArrayList<>(chests);for(var c:chests)if(c.y()==81)out.add(new Chest(c.x()+1,c.y(),c.z()));return List.copyOf(out);}
 private void build(){
  // The plateau belongs to the same solid hill, with four different fortress levels.
  box(28,72,28,99,80,99,STONE);box(28,81,28,99,143,99,AIR);
  room("entrance_hall",78,80,74,97,12,91,true);room("main_corridor",43,80,78,81,9,87,true);
  room("inner_hall",38,80,32,61,15,51,true);room("ritual_chamber",63,80,32,84,12,51,true);
  room("armory",80,80,52,96,11,70,true);room("ruined_side_room",30,80,52,50,10,73,false);
  room("ritual_courtyard",51,80,52,79,10,77,false);room("internal_ascent_landing",32,80,78,52,10,95,true);
  // Perimeter walls and gallery, visible crenellations, tower stairs and overlook.
  for(int x=28;x<=99;x++){box(x,80,28,x,94,31,STONE);box(x,80,96,x,94,99,STONE);if(x%3!=0){box(x,95,28,x,97,31,CRACK);box(x,95,96,x,97,99,CRACK);}}
  for(int z=28;z<=99;z++){box(28,80,z,31,94,z,STONE);box(96,80,z,99,94,z,STONE);if(z%3!=0){box(28,95,z,31,97,z,CRACK);box(96,95,z,99,97,z,CRACK);}}
  for(int[] corner:new int[][]{{28,28},{87,28},{28,87},{87,87}}){int x=corner[0],z=corner[1];room("tower_"+x+"_"+z,x,80,z,x+12,48,z+12,true);for(int y=81;y<=128;y++){int t=(y-81)%24,lap=(y-81)/24,xx=t<6?x+3+t:t<12?x+9:t<18?x+9-(t-12):x+3,zz=t<6?z+3:t<12?z+3+t-6:t<18?z+9:z+9-(t-18);box(xx,y,zz,xx+1,y,zz+1,"minecraft:stone_brick_stairs@"+(t<6?"EAST":t<12?"SOUTH":t<18?"WEST":"NORTH"));box(xx,y+1,zz,xx+1,y+3,zz+1,AIR);}for(int xx=x;xx<=x+12;xx+=3)for(int zz=z;zz<=z+12;zz+=3)if(xx==x||xx==x+12||zz==z||zz==z+12)box(xx,129,zz,xx,133,zz,STONE);box(x+4,129,z+4,x+8,129,z+8,"slavicmyths:serpent_bas_relief");}
  // Gallery connections are at the same elevation as the tower's first lap.
  box(31,92,31,95,92,33,STONE);box(31,93,31,95,96,33,AIR);box(31,92,94,95,92,96,STONE);box(31,93,94,95,96,96,AIR);
  box(31,92,31,33,92,96,STONE);box(31,93,31,33,96,96,AIR);box(94,92,31,96,92,96,STONE);box(94,93,31,96,96,96,AIR);
  // Upper ceremonial gate on the eastern facade; the lower entrance is a cave.
  door(95,81,78,100,86,10);for(int z:new int[]{77,87})for(int y=81;y<=93;y++)box(100,y,z,100,y,z,"slavicmyths:serpent_bas_relief@EAST");
  box(100,94,78,100,96,86,"slavicmyths:serpent_bas_relief@EAST");
  stairX(82,112,116,12,1);stairZ(116,82,112,42,-1);stairX(112,108,82,76,-1);box(99,80,80,108,80,84,STONE);door(99,81,80,108,84,7);
  // Connect every interior, then furnish distinct rooms.
  door(77,81,80,83,85,6);door(49,81,79,53,86,6);door(61,81,40,64,45,6);door(52,81,49,57,54,7);door(65,81,49,72,54,7);door(77,81,60,82,65,6);door(48,81,61,53,66,6);door(59,81,75,66,80,6);door(88,81,69,92,75,6);door(87,81,50,91,54,6);
  door(36,81,38,40,42,6);door(89,81,38,94,42,6);door(38,81,89,44,94,6);door(87,81,88,92,93,6);
  box(58,80,60,72,80,72,"slavicmyths:defiled_ritual_slab");lamp(65,81,66,true);box(66,82,65,66,84,65,"slavicmyths:mor_crystal");
  for(int x:new int[]{42,57,67,80}){box(x,81,35,x,91,35,STONE);lamp(x,92,35,false);}
  for(int[] p:new int[][]{{83,55},{90,56},{84,65},{45,44},{36,89}}){chest(p[0],81,p[1]);box(p[0]+1,81,p[1],p[0]+1,81,p[1],"minecraft:barrel");}
  for(int z=56;z<=66;z+=2)box(94,82,z,94,84,z,"minecraft:iron_bars");
  box(30,84,59,33,93,69,AIR);box(35,81,66,39,83,70,"slavicmyths:ruined_serpent_masonry");box(67,81,36,80,81,42,"slavicmyths:ancient_ritual_slab");lamp(73,82,39,true);
  // Genuine underground network, not an inaccessible solid shell.
  room("ritual_cave",52,4,82,74,11,99,true);room("tainted_side_cave",82,2,76,106,14,98,true);
  door(57,5,98,63,119,6);for(int z=106;z<=119;z++){int y=4+Math.min(8,z-106);box(56,y,z,64,y,z,"minecraft:polished_blackstone_brick_stairs@SOUTH");door(57,y+1,z,63,z,6);}
  box(56,12,114,64,12,127,STONE);door(57,13,114,63,127,8);box(60,12,114,82,12,118,STONE);door(60,13,114,82,118,7);door(57,13,113,63,117,8);box(55,12,113,56,22,113,CRACK);box(64,12,113,65,22,113,CRACK);box(55,21,113,65,23,113,"slavicmyths:serpent_bas_relief@SOUTH");
  door(73,5,88,84,93,6);box(74,4,88,83,4,93,STONE);box(83,3,88,85,3,93,STONE);
  door(48,5,82,53,91,6);box(48,4,82,53,4,91,STONE);
  stairZ(82,65,48,4,-1);stairX(48,67,65,21,1);stairZ(65,84,65,40,1);stairX(65,44,84,59,-1);door(43,81,81,48,86,6);
  box(57,4,87,70,4,95,"slavicmyths:defiled_ritual_slab");lamp(63,5,91,true);chest(54,5,86);
  for(int x=84;x<105;x+=5){box(x,3,78,x,6,78,"slavicmyths:mor_crystal");lamp(x,3,96,true);}
  room("blocked_lair_approach",106,2,83,116,14,94,true);door(104,3,87,109,91,7);box(110,3,84,112,13,93,"slavicmyths:serpent_bas_relief");lamp(108,3,85,true);
  // Ruined terraces, dead trees and restrained blue lights away from the warm main route.
  for(int i=0;i<22;i++){int x=6+Math.floorMod(i*31,116),z=7+Math.floorMod(i*47,114),y=HillRules.hillTop(x,z,seed)+1;if(y<1||x>25&&x<102&&z>25&&z<102||z>110&&x>52&&x<116)continue;box(x,y,z,x,y+5,z,"minecraft:dark_oak_log");box(x-2,y+4,z,x+2,y+4,z,"minecraft:dark_oak_log");if(i%3==0)box(x,y+1,z+1,x,y+1,z+1,"slavicmyths:mor_crystal");}
  for(int[] p:new int[][]{{55,14,114},{65,14,114},{102,81,78},{102,81,87},{56,81,56},{76,81,74},{49,7,86},{63,7,98}})lamp(p[0],p[1],p[2],false);
  for(int[] p:new int[][]{{44,88,41,95},{73,88,38,92},{85,88,60,91}}){box(p[0],p[1],p[2],p[0],p[1],p[2],"slavicmyths:ancient_hanging_lamp@NORTH@true");box(p[0],p[1]+1,p[2],p[0],p[3]-1,p[2],"minecraft:chain");}
  box(80,85,75,80,85,75,"slavicmyths:serpent_torch@SOUTH@true");box(94,84,53,94,84,53,"slavicmyths:serpent_torch@SOUTH@true");
  box(56,0,120,64,11,127,STONE);stairX(44,56,126,0,1);
  for(int[] p:new int[][]{{34,41},{93,41},{34,100},{93,100}})for(int y:new int[]{104,110,116,122})box(p[0],y,p[1],p[0],y,p[1],"minecraft:red_wall_banner@SOUTH");
  for(int y:new int[]{105,112,119})box(100,y,93,100,y,93,"minecraft:red_wall_banner@EAST");
  for(int i=0;i<26;i++){int x=8+Math.floorMod(i*37,110),z=6+Math.floorMod(i*53,110),y=HillRules.hillTop(x,z,seed)+1;if(y<5||x>=26&&x<=103&&z>=26&&z<=103||z>100&&x>42&&x<118)continue;box(x-1,y,z-1,x+2,y+1+i%3,z+1,"slavicmyths:ruined_serpent_masonry");if(i%4==0)box(x,y+3,z,x,y+3,z,"slavicmyths:mor_crystal");}
  for(int z=82;z<103;z+=6)box(55,7,z,55,7,z,"minecraft:soul_torch");
 }
 public boolean authored(int x,int z){return tops[x*128+z]>=0||!columns[x*128+z].isEmpty();}
 public String state(int x,int y,int z){if(x<0||x>=128||z<0||z>=128||y<0||y>=144)return AIR;int top=tops[x*128+z];String skin=skins[x*128+z];String state=y<=top?y>=top-2?skin:"minecraft:deepslate":AIR;for(var p:columns[x*128+z])if(y>=p.y0&&y<=p.y1)state=p.state;return state;}
}
