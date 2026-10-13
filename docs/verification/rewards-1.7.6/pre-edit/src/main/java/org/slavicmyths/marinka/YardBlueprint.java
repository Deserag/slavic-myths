package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
/** Authored local grid. Stable order makes interrupted construction resumable without new loot. */
public final class YardBlueprint {
 public record Cell(BlockPos local,BlockState state){}
 private static List<Cell> cached;
 private final LinkedHashMap<BlockPos,BlockState> cells=new LinkedHashMap<>();
 private void put(int x,int y,int z,Block b){put(x,y,z,b.defaultBlockState());}
 private void put(int x,int y,int z,BlockState b){cells.put(new BlockPos(x,y,z),b);}
 private void box(int x1,int y1,int z1,int x2,int y2,int z2,Block b){for(int x=x1;x<=x2;x++)for(int y=y1;y<=y2;y++)for(int z=z1;z<=z2;z++)put(x,y,z,b);}
 public static synchronized List<Cell> plan(){if(cached!=null)return cached;var a=new YardBlueprint();a.ground();a.house();a.barn();a.props();a.cellar();a.put(0,1,0,MarinkaContent.SEAL.get());a.put(1,1,0,Blocks.DEEPSLATE_BRICK_SLAB);var out=new ArrayList<Cell>();a.cells.forEach((p,s)->out.add(new Cell(p,s)));cached=List.copyOf(out);return cached;}
 private void ground(){for(int x=-30;x<30;x++)for(int z=-30;z<30;z++){put(x,-1,z,Blocks.STONE);put(x,-2,z,Blocks.STONE);int n=Math.floorMod(x*37+z*19+x*z,100);put(x,0,z,n<42?Blocks.COARSE_DIRT:n<66?Blocks.ROOTED_DIRT:n<76?Blocks.GRAVEL:n<86?Blocks.COBBLESTONE:n<96?Blocks.CRACKED_DEEPSLATE_BRICKS:Blocks.RED_TERRACOTTA);if((x==-30||x==29||z==-30||z==29)&&Math.floorMod(x+z,9)<6&&!(z==29&&Math.abs(x)<=3)){int h=1+Math.floorMod(x*3+z,3);for(int y=1;y<=h;y++)put(x,y,z,y==1?Blocks.MOSSY_COBBLESTONE:Blocks.DARK_OAK_FENCE);}}
 for(int i=0;i<24;i++){double t=i*Math.PI/12;put((int)Math.round(6*Math.cos(t)),0,(int)Math.round(6*Math.sin(t)),i%3==0?Blocks.CHISELED_DEEPSLATE:Blocks.RED_TERRACOTTA);}
 for(int[] q:new int[][]{{-10,10},{11,9},{-5,-12}}){for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)put(q[0]+x,0,q[1]+z,Math.abs(x)+Math.abs(z)==1?Blocks.RED_TERRACOTTA:Blocks.CHISELED_STONE_BRICKS);put(q[0],1,q[1],Blocks.STONE_BRICK_SLAB);}
 for(int x:new int[]{-3,3}){for(int y=1;y<=5;y++)put(x,y,29,Blocks.DARK_OAK_LOG);put(x,5,28,Blocks.LANTERN);} }
 private void house(){box(-29,1,-27,-12,1,-9,Blocks.DARK_OAK_PLANKS);for(int x=-29;x<=-12;x++)for(int z=-27;z<=-9;z++)if(x==-29||x==-12||z==-27||z==-9){for(int y=2;y<=7;y++){if(y>=6&&Math.floorMod(x*7+z,13)<3)continue;put(x,y,z,(x%5==0||z%5==0||y==5)?Blocks.DARK_OAK_LOG:Blocks.SPRUCE_PLANKS);}}
 box(-28,5,-26,-13,5,-10,Blocks.SPRUCE_PLANKS);box(-25,2,-9,-21,4,-9,Blocks.AIR);box(-25,1,-8,-21,1,-6,Blocks.SPRUCE_SLAB);
 for(int x=-29;x<=-12;x++){int roof=8+Math.min(x+29,-12-x)/2;for(int z=-27;z<=-9;z++)if(Math.floorMod(x*13+z*7,19)>3||x==-29||x==-12)put(x,roof,z,Blocks.DARK_OAK_PLANKS);}
 for(int[] q:new int[][]{{-26,-27},{-17,-27},{-12,-23},{-12,-13}}){box(q[0],3,q[1],q[0],4,q[1],Blocks.AIR);put(q[0],3,q[1],Blocks.IRON_BARS);}box(-28,2,-23,-28,11,-22,Blocks.BRICKS);box(-14,2,-25,-14,9,-25,Blocks.BRICKS);
 for(int i=0;i<4;i++)put(-27,2+i,-12-i,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));
 table(-23,2,-18,3);put(-26,2,-19,Blocks.CAULDRON);put(-17,2,-22,Blocks.BOOKSHELF);put(-24,6,-14,Blocks.BARREL);put(-26,4,-18,Blocks.LANTERN);put(-19,4,-26,Blocks.LANTERN);}
 private void barn(){box(12,1,-29,27,1,-14,Blocks.COARSE_DIRT);for(int x:new int[]{12,27})for(int z:new int[]{-29,-14})for(int y=2;y<=6;y++)put(x,y,z,Blocks.SPRUCE_LOG);for(int z=-29;z<=-14;z++){put(12,2,z,Blocks.DARK_OAK_FENCE);put(27,2,z,Blocks.DARK_OAK_FENCE);}for(int x=12;x<=27;x++)for(int z=-29;z<=-14;z++)if(Math.floorMod(x+z*3,11)>3)put(x,6+(x>16&&x<23?1:0),z,Blocks.SPRUCE_SLAB);
 box(13,2,-28,16,3,-26,Blocks.HAY_BLOCK);put(25,2,-18,Blocks.BARREL);put(24,2,-19,Blocks.COMPOSTER);put(13,4,-17,Blocks.LANTERN);}
 private void table(int x,int y,int z,int width){for(int i=0;i<width;i++){put(x+i,y,z,Blocks.DARK_OAK_FENCE);put(x+i,y+1,z,Blocks.SPRUCE_SLAB);}}
 private void props(){for(int[] p:new int[][]{{-24,14},{22,17},{-21,-2}}){for(int y=1;y<8;y++)put(p[0]+(y>4?1:0),y,p[1],Blocks.DARK_OAK_LOG);for(int i=0;i<4;i++){put(p[0]-1-i,5+i/2,p[1],Blocks.DARK_OAK_LOG);put(p[0]+2+i,6+i/2,p[1]+1,Blocks.DARK_OAK_LOG);}put(p[0]-3,4,p[1],Blocks.LANTERN);put(p[0]+3,4,p[1]+1,Blocks.CHAIN);}
 for(int x=20;x<=26;x++)for(int z=-10;z<=-4;z++)if(x==20||x==26||z==-10||z==-4)put(x,1,z,Blocks.MOSSY_COBBLESTONE);box(21,-4,-9,25,-4,-5,Blocks.COBBLESTONE);box(21,-3,-9,25,0,-5,Blocks.AIR);for(int x:new int[]{20,26})for(int y=2;y<=4;y++)put(x,y,-7,Blocks.OAK_LOG);box(20,5,-8,26,5,-6,Blocks.SPRUCE_SLAB);put(23,4,-7,Blocks.CHAIN);put(23,3,-7,Blocks.CAULDRON);
 box(-21,2,20,-17,2,22,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-21,-17})for(int z:new int[]{19,23})put(x,1,z,Blocks.DARK_OAK_LOG);for(int x=-20;x<=-18;x++)put(x,3,21,Blocks.BARREL);
 for(int x:new int[]{16,23})for(int z:new int[]{20,24})for(int y=1;y<=4;y++)put(x,y,z,Blocks.OAK_LOG);box(16,5,20,23,5,24,Blocks.SPRUCE_SLAB);put(18,1,22,Blocks.BARREL);put(21,1,22,Blocks.FLOWER_POT);
 for(int i=0;i<12;i++){double t=i*Math.PI/6;int x=(int)Math.round(24*Math.cos(t)),z=(int)Math.round(24*Math.sin(t));if(x< -12&&z< -9||x>11&&z< -13)continue;put(x,1,z,Blocks.MOSSY_STONE_BRICK_WALL);put(x,2,z,i%2==0?Blocks.LANTERN:Blocks.CANDLE);put(x+1,1,z,Blocks.FLOWER_POT);}
 for(int i=0;i<4;i++)put(i%2==0?-3:3,1,i<2?-3:3,Blocks.RED_CANDLE);}
 private void cellar(){box(-8,-10,-11,8,-4,11,Blocks.STONE_BRICKS);box(-7,-9,-10,7,-5,10,Blocks.AIR);
 // West workshop, east transformation hall, northern store, two-wide access.
 box(-1,-9,-5,-1,-5,3,Blocks.MOSSY_STONE_BRICKS);box(-1,-9,-1,-1,-7,0,Blocks.AIR);box(-7,-9,-6,7,-5,-6,Blocks.STONE_BRICKS);box(0,-9,-6,1,-7,-6,Blocks.AIR);
 table(-6,-9,-3,3);put(-6,-9,1,Blocks.CAULDRON);for(int z:new int[]{-5,-3,1})put(-7,-9,z,Blocks.BOOKSHELF);put(-4,-9,2,Blocks.FLOWER_POT);put(-3,-9,-4,Blocks.FLOWER_POT);
 box(2,-9,-3,4,-9,-1,Blocks.RED_TERRACOTTA);put(3,-8,-2,Blocks.GOLD_BLOCK);for(int x:new int[]{2,6}){box(x,-9,2,x,-6,2,Blocks.CHISELED_STONE_BRICKS);put(x,-5,2,Blocks.CHAIN);}put(5,-9,1,Blocks.BROWN_CARPET);
 put(-3,-9,-9,Blocks.CHEST);put(3,-9,-9,Blocks.CHEST);box(-6,-9,-10,-5,-8,-10,Blocks.BOOKSHELF);put(6,-9,-8,Blocks.BREWING_STAND);
 for(int[] q:new int[][]{{-6,-4},{-3,2},{2,-4},{6,2},{0,-9}}){put(q[0],-6,q[1],Blocks.LANTERN);put(q[0],-5,q[1],Blocks.CHAIN);} 
 // Two-block stair descending south; clear continuous headroom, sealed at surface until won.
 for(int z=0;z<=10;z++)for(int x=0;x<=1;x++){int y=-z;for(int h=1;h<=3;h++)put(x,y+h,z,Blocks.AIR);put(x,y,z,Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
 for(int x=0;x<=1;x++)for(int z=0;z<=2;z++)put(x,0,z,Blocks.CHISELED_DEEPSLATE);
 }
 private YardBlueprint(){}
}
