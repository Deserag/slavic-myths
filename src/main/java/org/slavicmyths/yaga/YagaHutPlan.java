package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.core.BlockPos;
/** Pure, bounded hut blueprint. Positions are relative to ground level. */
public final class YagaHutPlan {
 public final LinkedHashMap<BlockPos,String> blocks=new LinkedHashMap<>();
 public static final BlockPos HOME=new BlockPos(0,4,2);
 private void b(int x,int y,int z,String id){blocks.put(new BlockPos(x,y,z),id.contains(":")?id:"minecraft:"+id);}
 public YagaHutPlan(){
  // Clear only the reserved cabin volume; terrain validation happens before placement.
  for(int x=-6;x<=6;x++)for(int z=-7;z<=7;z++)for(int y=1;y<=11;y++)b(x,y,z,"air");
  for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++)b(x,3,z,"spruce_planks");
  for(int y=4;y<=7;y++)for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++)if(Math.abs(x)==5||Math.abs(z)==6)b(x,y,z,(y%2==0?"dark_oak_log[axis=x]":"spruce_log[axis=z]"));
  for(int x:new int[]{-5,5})for(int z:new int[]{-6,6})for(int y=4;y<=8;y++)b(x,y,z,"stripped_dark_oak_log[axis=y]");
  for(int x:new int[]{-5,5})for(int z:new int[]{-3,3}){b(x,5,z,"glass_pane");b(x,6,z,"glass_pane");b(x,5,z-1,"spruce_trapdoor[facing=north,open=true]");b(x,5,z+1,"spruce_trapdoor[facing=south,open=true]");}
  for(int x=-6;x<=6;x++){int y=8+(6-Math.abs(x))/2;for(int z=-7;z<=7;z++){b(x,y,z,x<0?"dark_oak_stairs[facing=east]":"dark_oak_stairs[facing=west]");if(x==0)b(x,y+1,z,"dark_oak_slab");if(x==5&&z>0)b(x,y,z,"mossy_cobblestone_slab");}}
  // Close triangular gables below the roof, without lowering interior headroom.
  for(int x=-5;x<=5;x++)for(int y=8;y<8+(6-Math.abs(x))/2;y++)for(int z:new int[]{-6,6})b(x,y,z,"dark_oak_planks");
  // Irregular exterior growth and patched eaves keep the cabin visibly weathered.
  for(int y=5;y<=6;y++){b(-6,y,-4,"vine[east=true]");b(-6,y,4,"vine[east=true]");b(6,y,4,"vine[west=true]");b(-2,y,7,"vine[north=true]");}
  b(-6,8,-5,"spruce_stairs[facing=east]");b(-6,8,4,"spruce_stairs[facing=east]");b(0,11,-4,"spruce_slab");
  for(int y=8;y<=12;y++)b(3,y,3,"mossy_cobblestone_wall");b(3,13,3,"campfire[lit=true]");
  // Two bent legs with three front toes, one rear toe, and distinct dark claws.
  for(int x:new int[]{-3,3}){b(x,2,1,"slavicmyths:yaga_chicken_leg[part=0]");b(x,2,0,"slavicmyths:yaga_chicken_leg[part=1]");b(x,1,0,"slavicmyths:yaga_chicken_leg[part=2]");b(x,0,0,"slavicmyths:yaga_chicken_leg[part=6]");for(int dx=-1;dx<=1;dx++){b(x+dx,0,-1,"slavicmyths:yaga_chicken_leg[part=5]");b(x+dx,0,-2,"slavicmyths:yaga_chicken_leg[part=3]");b(x+dx,0,-3,"slavicmyths:yaga_chicken_leg[part=4]");}b(x,0,1,"slavicmyths:yaga_chicken_leg[part=3,facing=south]");b(x,0,2,"slavicmyths:yaga_chicken_leg[part=4,facing=south]");}
  for(int x=-2;x<=2;x++)for(int z=-9;z<=-7;z++)b(x,3,z,"spruce_planks");
  for(int z=-7;z>=-9;z--)for(int x:new int[]{-2,2})b(x,4,z,"spruce_fence");
  for(int y=0;y<=2;y++)for(int x=-1;x<=1;x++)b(x,y,-12+y,"spruce_stairs[facing=south]");
  b(0,4,-6,"spruce_door[facing=north,half=lower]");b(0,5,-6,"spruce_door[facing=north,half=upper]");
  b(-2,4,3,"slavicmyths:yaga_cauldron");
  b(4,4,4,"campfire");b(4,4,5,"mossy_cobblestone");b(4,5,5,"mossy_cobblestone_wall");b(4,6,5,"mossy_cobblestone_wall");
  for(int x=2;x<=3;x++){b(x,4,-2,"spruce_fence");b(x,5,-2,"spruce_slab[type=top]");}
  b(-4,4,4,"chest[facing=east]");b(-4,4,5,"barrel[facing=up]");
  for(int z=-3;z<=1;z++){b(-4,6,z,"spruce_slab[type=top]");b(-4,7,z,z%2==0?"slavicmyths:yaga_dried_herbs":"slavicmyths:yaga_bone_charm");}
  for(int x:new int[]{-3,3})b(x,7,-5,"slavicmyths:yaga_dried_herbs");b(0,6,-7,"slavicmyths:yaga_bone_charm");
  for(int x=-2;x<=2;x++)for(int z=-8;z<=-7;z++)b(x,7,z,"spruce_slab[type=top]");
  b(-4,7,-2,"flower_pot");b(-4,7,0,"flower_pot");b(3,6,-2,"lantern");b(2,6,-2,"slavicmyths:ritual_candle");b(4,5,-4,"flower_pot");
  for(int z=-11;z<=9;z++)for(int x:new int[]{-9,9}){if(z%5!=0)b(x,0,z,"spruce_fence");}
  for(int x=-9;x<=9;x++)if(Math.abs(x)>2){b(x,0,-11,"spruce_fence");b(x,0,9,"spruce_fence");}
  for(int z=-16;z<=-13;z++)for(int x=-1;x<=1;x++)b(x,-1,z,"coarse_dirt");
  for(int x=-8;x<=-6;x++)for(int z=4;z<=6;z++)b(x,0,z,"spruce_log[axis=z]");
  b(7,0,5,"stripped_spruce_log[axis=y]");b(6,0,-4,"campfire[lit=false]");
  for(int x:new int[]{-7,7})for(int z:new int[]{-7,-3,1,7})b(x,0,z,z<0?"slavicmyths:wormwood":"brown_mushroom");
 }
 /** Candidate positions never load chunks. The seeded order is stable across restarts. */
 public static List<BlockPos> candidates(long seed,BlockPos spawn){Random random=new Random(seed^0x596167614875744cL);List<BlockPos> out=new ArrayList<>();for(int i=0;i<64;i++){double a=random.nextDouble()*Math.PI*2,r=820+random.nextDouble()*1650;out.add(new BlockPos(spawn.getX()+(int)Math.round(Math.cos(a)*r),0,spawn.getZ()+(int)Math.round(Math.sin(a)*r)));}return out;}
}
