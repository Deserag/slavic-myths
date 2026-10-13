package org.slavicmyths.skim;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** One authored rotatable valley and compact northward den; shared immutable cells. */
public final class ValleyBlueprint {
 public record Cell(BlockPos at,BlockState state){}
 private static List<Cell> cached;
 public static synchronized List<Cell> plan(){if(cached!=null)return cached;var p=new LinkedHashMap<BlockPos,BlockState>();
  for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++){int h=Math.floorMod(x*13+z*7,100);put(p,x,0,z,h<40?Blocks.GRASS_BLOCK:h<62?Blocks.COARSE_DIRT:h<86?(h%2==0?Blocks.ANDESITE:Blocks.STONE):Blocks.PODZOL);put(p,x,-1,z,Blocks.DIRT);put(p,x,-2,z,Blocks.STONE);
   double ellipse=x*x/(23D*23)+(z-2)*(z-2)/(21D*21);if(ellipse>1.4&&(Math.abs(x)>28||z<-26||z>29)&&!(z>26&&Math.abs(x)<=4)&&!(z<-26&&Math.abs(x)<=4)){int ridge=7+Math.floorMod(x*19+z*31,9);for(int y=1;y<=ridge;y++)put(p,x,y,z,(x+y+z)%4==0?Blocks.GRANITE:Blocks.STONE);}
  }
  boulder(p,18,5,2,2,4);boulder(p,-19,-10,2,2,4);
  for(int side:new int[]{-1,1})for(int z:new int[]{-21,-5,12,24})tree(p,side*27,z,8+Math.floorMod(z,4));tree(p,-20,-20,9);tree(p,24,22,9);
  for(int x=-28;x<=-20;x++)for(int z=15;z<=21;z++)put(p,x,0,z,Blocks.PODZOL);for(int x=-28;x<=-21;x+=2)put(p,x,1,21,Blocks.SPRUCE_FENCE);put(p,-24,1,18,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));for(int i=0;i<5;i++)put(p,-26+i,1,15,Blocks.SPRUCE_SLAB);put(p,-27,1,16,Blocks.SPRUCE_LOG);
  for(int x=18;x<=23;x++)for(int z=-19;z<=-15;z++)if((x+z)%3==0)put(p,x,1,z,Blocks.BONE_BLOCK);put(p,20,2,-17,Blocks.SKELETON_SKULL);
  for(int i=0;i<=10;i++){int x=22+i*8/10,z=16+i;put(p,x,0,z,Blocks.WATER);put(p,x,-1,z,Blocks.STONE);if(i%3==0)put(p,x,0,z,Blocks.COBBLESTONE_SLAB);}
  for(int x=-1;x<=1;x++)for(int z=26;z<=28;z++)put(p,x,0,z,Blocks.CHISELED_STONE_BRICKS);
  // Tunnel and chamber extend outside the 65x65 meadow, north to -50. Roof and walls protect the hollow.
  for(int x=-7;x<=7;x++)for(int z=-50;z<=-28;z++){boolean chamber=z<=-35;int width=chamber?6:4;for(int y=0;y<=8;y++){if(Math.abs(x)>width)continue;boolean shell=Math.abs(x)==width||z==-50||y==0||y==(chamber?7:6);put(p,x,y,z,shell?((x+y+z)%5==0?Blocks.MOSSY_COBBLESTONE:Blocks.STONE):Blocks.AIR);}}
  for(int x=-3;x<=3;x++)for(int y=1;y<=5;y++)put(p,x,y,-30,Blocks.CRACKED_STONE_BRICKS);
  for(int x=-2;x<=1;x++)for(int z=-46;z<=-43;z++)put(p,x,1,z,Blocks.PODZOL);for(int y=1;y<=4;y++)put(p,3,y,-40,Blocks.POINTED_DRIPSTONE);
  for(int z=-48;z<=-39;z+=3){put(p,-5,1,z,Blocks.BONE_BLOCK);put(p,-5,2,z,Blocks.SKELETON_SKULL);put(p,5,2,z,Blocks.TORCH);}put(p,4,1,-43,Blocks.CHEST);put(p,-4,1,-46,Blocks.CHEST);
  cached=p.entrySet().stream().map(e->new Cell(e.getKey(),e.getValue())).toList();return cached;
 }
 private static void put(Map<BlockPos,BlockState> p,int x,int y,int z,Block b){put(p,x,y,z,b.defaultBlockState());}private static void put(Map<BlockPos,BlockState> p,int x,int y,int z,BlockState s){p.put(new BlockPos(x,y,z),s);}
 private static void boulder(Map<BlockPos,BlockState> p,int x,int z,int rx,int rz,int h){for(int dx=-rx;dx<=rx;dx++)for(int dz=-rz;dz<=rz;dz++)for(int y=1;y<=h-(Math.abs(dx)+Math.abs(dz))/2;y++)put(p,x+dx,y,z+dz,Blocks.ANDESITE);}
 private static void tree(Map<BlockPos,BlockState> p,int x,int z,int h){for(int y=1;y<=h;y++)put(p,x,y,z,Blocks.SPRUCE_LOG);for(int y=4;y<=h+1;y++){int r=Math.max(1,(h+2-y)/3);for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(dx!=0||dz!=0)put(p,x+dx,y,z+dz,Blocks.SPRUCE_LEAVES);}}
 private ValleyBlueprint(){}
}
