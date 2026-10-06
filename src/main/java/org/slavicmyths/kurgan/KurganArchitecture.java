package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.registry.ModBlocks;

/** Version-two architecture; each operation is clipped and containers are never replaced on a retry. */
final class KurganArchitecture {
 private final KurganDungeonPiece piece;private final KurganPlan plan;private final WorldGenLevel world;private final BoundingBox clip;private final Set<Integer> filled;
 private final Set<BlockPos> containerSpaces=new HashSet<>();
 KurganArchitecture(KurganDungeonPiece piece,WorldGenLevel world,BoundingBox clip,Set<Integer> filled){this.piece=piece;plan=piece.plan;this.world=world;this.clip=clip;this.filled=filled;
  for(var r:plan.rooms)for(int i=0;i<KurganLayout.rewards(r);i++){BlockPos p=cache(r,i);containerSpaces.add(p);containerSpaces.add(p.above());if(coffin(r,i)){containerSpaces.add(p.relative(coffinDirection(p)));containerSpaces.add(p.relative(coffinDirection(p)).above());}}
  for(var r:plan.rooms)if(r.archetype==KurganPlan.Archetype.TRAP){containerSpaces.add(pos(r.x,r.y,r.z));containerSpaces.add(pos(r.x,r.y+1,r.z));}
 }
 private BlockPos pos(int x,int y,int z){return piece.origin.offset(x,y,z);}
 private void put(int x,int y,int z,BlockState state){BlockPos p=pos(x,y,z);if(clip.isInside(p)&&!containerSpaces.contains(p))world.setBlock(p,state,2);}
 private void raw(BlockPos p,BlockState state){if(clip.isInside(p))world.setBlock(p,state,2);}
 private BlockState stone(int palette,int x,int y,int z){
  // Contiguous 4x3x4 masonry patches, with the same material family throughout a room.
  int patch=Math.floorMod(Math.floorDiv(x,4)*19+Math.floorDiv(z,4)*31+Math.floorDiv(y,3)*7+(int)plan.seed,100);
  return patch<12?Blocks.STONE_BRICKS.defaultBlockState():patch<18?Blocks.STONE.defaultBlockState():KurganBlocks.stone(palette,patch<68?0:patch<88?1:patch<95?2:3);
 }
 private static final int[] JUNCTION_MATERIALS=paletteWeights(null,true);
 private static final java.util.EnumMap<KurganPlan.Archetype,int[][]> MATERIALS=materialTables();
 private static java.util.EnumMap<KurganPlan.Archetype,int[][]> materialTables(){var map=new java.util.EnumMap<KurganPlan.Archetype,int[][]>(KurganPlan.Archetype.class);for(var type:KurganPlan.Archetype.values())map.put(type,new int[][]{paletteWeights(type,true),paletteWeights(type,false)});return map;}
 private static int[] paletteWeights(KurganPlan.Archetype type,boolean floor){return type==null?new int[]{0,45,1,35,2,15,6,5}:switch(type){
   case VESTIBULE->floor?new int[]{0,25,1,25,2,20,3,15,4,10,5,5}:new int[]{1,35,0,25,2,20,6,10,8,5,7,5};
   case CROSSROADS->floor?new int[]{1,40,0,25,2,20,7,15}:new int[]{1,40,0,25,2,25,6,10};
   case BURIAL,DEEP->floor?new int[]{0,35,1,30,7,20,2,15}:new int[]{1,45,0,25,2,15,7,10,6,5};
   case WARRIOR->floor?new int[]{1,45,0,25,2,20,6,10}:new int[]{1,50,0,20,2,20,6,5,8,5};
   case TREASURY->floor?new int[]{1,40,0,30,2,20,7,10}:new int[]{1,45,0,20,2,20,6,10,7,5};
   case RITUAL->floor?new int[]{0,35,7,30,1,20,2,15}:new int[]{1,35,0,25,7,15,2,15,6,5,9,5};
   case TRAP->floor?new int[]{1,45,0,20,2,20,6,10,3,5}:new int[]{1,35,0,25,4,15,8,10,6,15};
   case FLOODED->floor?new int[]{0,25,9,20,1,15,10,20,4,10,5,10}:new int[]{9,30,1,20,0,20,10,20,6,10};
   case OSSUARY->floor?new int[]{0,40,1,25,4,20,6,15}:new int[]{1,35,0,30,2,20,6,10,8,5};
   case RELIQUARY->floor?new int[]{0,35,1,30,7,20,2,15}:new int[]{1,40,0,25,7,20,2,15};
   case COLLAPSED->floor?new int[]{0,20,1,20,6,20,8,15,4,15,5,10}:new int[]{1,20,0,20,6,20,8,10,4,10,3,20};
   default->floor?new int[]{0,40,1,30,2,20,6,10}:new int[]{1,40,0,30,2,20,6,10};
  };
 }
 private BlockState roomStone(KurganPlan.Room r,int x,int y,int z){
  // Material/weight pairs: cold, brick, vanilla brick, cracked vanilla, stone, gravel,
  // cracked custom, carved, cobble, damp custom, mossy vanilla.
  int[] weights=r.archetype==null?JUNCTION_MATERIALS:MATERIALS.get(r.archetype)[y<=r.y?0:1];
  int patch=Math.floorMod(Math.floorDiv(x-r.x,4)*19+Math.floorDiv(z-r.z,4)*31+Math.floorDiv(y-r.y,3)*7+(int)plan.seed+r.id*17,100),kind=0;
  for(int i=0;i<weights.length;i+=2){patch-=weights[i+1];if(patch<0){kind=weights[i];break;}}
  return switch(kind){case 1->KurganBlocks.stone(0,1);case 2->Blocks.STONE_BRICKS.defaultBlockState();case 3->Blocks.CRACKED_STONE_BRICKS.defaultBlockState();case 4->Blocks.STONE.defaultBlockState();case 5->Blocks.GRAVEL.defaultBlockState();case 6->KurganBlocks.stone(r.palette,2);case 7->KurganBlocks.get("carved_burial_stone").defaultBlockState();case 8->Blocks.COBBLESTONE.defaultBlockState();case 9->KurganBlocks.stone(r.palette,3);case 10->Blocks.MOSSY_STONE_BRICKS.defaultBlockState();default->KurganBlocks.stone(0,0);};
 }
 private BlockState slab(int palette){return KurganBlocks.get((palette==0?"pale_blue":"bog_green")+"_kurgan_cobblestone_slab").defaultBlockState();}
 private void prop(KurganPlan.Room r,int dx,int dy,int dz,String id){put(r.x+dx,r.y+dy,r.z+dz,KurganBlocks.get(id).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));}
 void place(){
  buryVestibule();
  for(var r:plan.rooms)shell(r);
  // All walls precede all carving; a bend or a rising step cannot reseal an earlier slice.
  for(var l:plan.links)corridor(l,0);
  for(var l:plan.links)corridor(l,1);
  for(var l:plan.links)corridor(l,2);
  restoreProtectedTomb();
  for(var r:plan.rooms)decorate(r);
  entrance();
  for(var r:plan.rooms)for(int i=0;i<KurganLayout.rewards(r);i++)container(r,i);
  if(plan.seal!=null)for(int x=plan.seal.x0;x<=plan.seal.x1;x++)for(int z=plan.seal.z0;z<=plan.seal.z1;z++)for(int y=plan.seal.y0;y<=plan.seal.y1;y++)put(x,y,z,KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState());
 }
 private void buryVestibule(){
  var r=plan.rooms.getFirst();int front=plan.radius;
  // Local earthen shoulders cover the entrance vault; the mound remains earth, not a stone building.
  for(int x=-r.rx-3;x<=r.rx+3;x++)for(int z=r.z-r.rz-3;z<=front;z++){
   int outside=Math.max(Math.max(0,Math.abs(x)-r.rx),Math.max(0,r.z-r.rz-z));int top=Math.max(0,r.height+2-outside*2-Math.max(0,z-r.z-r.rz));
   for(int y=0;y<=top;y++){BlockPos p=pos(x,y,z);if(clip.isInside(p)&&world.getBlockState(p).isAir())put(x,y,z,(y==top?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState());}
  }
 }
 private void shell(KurganPlan.Room r){var b=r.box();
  for(int x=Math.max(b.x0,clip.minX()-piece.origin.getX());x<=Math.min(b.x1,clip.maxX()-piece.origin.getX());x++)for(int z=Math.max(b.z0,clip.minZ()-piece.origin.getZ());z<=Math.min(b.z1,clip.maxZ()-piece.origin.getZ());z++)for(int y=r.y-1;y<=b.y1;y++){
   boolean air=plan.roomAir(r,x,y,z),outer=plan.tier==2&&r.archetype==KurganPlan.Archetype.DEEP&&(x==b.x0||x==b.x1||z==b.z0||z==b.z1||y==b.y0||y==b.y1);
   put(x,y,z,outer?KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState():air?Blocks.CAVE_AIR.defaultBlockState():y<r.y?KurganBlocks.stone(r.palette,0):roomStone(r,x,y,z));
  }
 }
 private void restoreProtectedTomb(){if(plan.tier!=2)return;var r=plan.rooms.get(plan.finalRoom);var b=r.box();
  // Corridor shells must never turn the roof above the sealed doorway into mineable masonry.
  for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++)for(int y=b.y0;y<=b.y1;y++)if(x==b.x0||x==b.x1||z==b.z0||z==b.z1||y==b.y0||y==b.y1)put(x,y,z,KurganBlocks.get("sealed_kurgan_masonry").defaultBlockState());
 }
 private void corridor(KurganPlan.Link l,int pass){
  for(int i=0;i<l.steps.size();i++){var s=l.steps.get(i);var b=l.slice(i);int h=l.secret?3:l.height;
   // Skip slices outside this placement section before any world reads.
   if(b.x1+piece.origin.getX()<clip.minX()||b.x0+piece.origin.getX()>clip.maxX()||b.z1+piece.origin.getZ()<clip.minZ()||b.z0+piece.origin.getZ()>clip.maxZ()||s.y+h+1+piece.origin.getY()<clip.minY()||s.y+piece.origin.getY()>clip.maxY())continue;
   for(int x=b.x0;x<=b.x1;x++)for(int z=b.z0;z<=b.z1;z++){
    boolean side=x==b.x0&&b.x0!=b.x1||x==b.x1&&b.x0!=b.x1||z==b.z0&&b.z0!=b.z1||z==b.z1&&b.z0!=b.z1;
    if(pass==0){for(int y=s.y;y<=s.y+h+1;y++)put(x,y,z,l.style==2&&y<=s.y+2?KurganBlocks.stone(1,3):l.style==1&&side?KurganBlocks.stone(l.palette,2):stone(l.palette,x,y,z));}
    else if(pass==1&&!side){for(int y=s.y+1;y<=s.y+h;y++)put(x,y,z,Blocks.CAVE_AIR.defaultBlockState());}
    else if(pass==2&&!side){BlockState floor=stone(l.palette,x,s.y,z);
     if(l.stair&&i>0){var prev=l.steps.get(i-1);if(prev.y>s.y){Direction up=prev.x>s.x?Direction.EAST:prev.x<s.x?Direction.WEST:prev.z>s.z?Direction.SOUTH:Direction.NORTH;floor=KurganBlocks.get("pale_blue_kurgan_stone_stairs").defaultBlockState().setValue(StairBlock.FACING,up);}}
     if(l.stair&&l.transitionStyle==2&&i%11==7)floor=KurganBlocks.stone(l.palette,2);
     if(l.stair&&l.transitionStyle==3&&i%6==0)floor=KurganBlocks.get("carved_burial_stone").defaultBlockState();
     put(x,s.y,z,floor);
    }
    if(pass==2&&(i==0||i==l.steps.size()-1||i%6==0))put(x,s.y+h+1,z,KurganBlocks.get("carved_burial_stone").defaultBlockState());
    if(pass==2&&side&&i%6==0)for(int y=s.y+1;y<=s.y+h;y++)if(clip.isInside(pos(x,y,z))&&!world.getBlockState(pos(x,y,z)).isAir())put(x,y,z,KurganBlocks.get("carved_burial_stone").defaultBlockState());
   }
   if(pass==2&&l.secret&&i==Math.min(3,l.steps.size()-1))for(int x=b.x0+ (b.x0==b.x1?0:1);x<=b.x1-(b.x0==b.x1?0:1);x++)for(int z=b.z0+(b.z0==b.z1?0:1);z<=b.z1-(b.z0==b.z1?0:1);z++)for(int y=s.y+1;y<=s.y+3;y++)put(x,y,z,KurganBlocks.stone(l.palette,2));
  }
 }
 private void decorate(KurganPlan.Room r){
  int side=r.rx-2,back=r.rz-2;
  // Structural piers sit in corners, never in the three-block connector axes.
  if(r.roomNode)for(int dx:new int[]{1-r.rx,r.rx-1})for(int dz:new int[]{1-r.rz,r.rz-1})for(int y=1;y<=r.height;y++)put(r.x+dx,r.y+y,r.z+dz,KurganBlocks.stone(r.palette,y%4==0?1:0));
  if(r.archetype!=null)switch(r.archetype){
   case VESTIBULE->{for(int dx:new int[]{1-r.rx,r.rx-1})prop(r,dx,1,back,"kurgan_column_fragment");}
   case CROSSROADS->{for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)put(r.x+dx,r.y,r.z+dz,KurganBlocks.get("carved_burial_stone").defaultBlockState());prop(r,side,1,back,"burial_stone_slab");}
   case BURIAL,DEEP->{
    if(r.archetype==KurganPlan.Archetype.DEEP){int a=r.rx-4,b=r.rz-5;for(int[] pier:new int[][]{{-a,-b},{a,-b},{-a,b},{a,b},{-b,-a},{b,-a},{-b,a},{b,a}})for(int y=1;y<=r.height;y++)put(r.x+pier[0],r.y+y,r.z+pier[1],KurganBlocks.stone(0,y%4==0?1:0));}
    // Dais and monumental cover are behind the clear boss spawning centre.
    int start=r.archetype==KurganPlan.Archetype.DEEP?5:Math.max(2,back-3);
    for(int dx=-2;dx<=2;dx++)for(int dz=start;dz<=Math.min(back,start+3);dz++){put(r.x+dx,r.y+1,r.z+dz,stone(r.palette,dx,r.y,dz));put(r.x+dx,r.y+2,r.z+dz,slab(r.palette));}
    for(int dx:new int[]{-side,side}){prop(r,dx,1,-back,"kurgan_pottery");prop(r,dx,1,Math.max(2,back-2),"kurgan_ritual_bowl");}
    for(int dz=-back;dz<=back;dz+=4)for(int dx:new int[]{-side,side})prop(r,dx,r.height,dz,"burial_stone_slab");
    if(r.archetype==KurganPlan.Archetype.DEEP)for(var n:plan.niches){int x=(n.x0+n.x1)/2,z=(n.z0+n.z1)/2;put(x,r.y+1,z,KurganBlocks.get("kurgan_stone_altar").defaultBlockState());}
   }
   case WARRIOR->{for(int dz=-back;dz<=back;dz+=4)if(Math.abs(dz)>1)for(int dx:new int[]{-side,side}){put(r.x+dx,r.y+1,r.z+dz,slab(r.palette));prop(r,dx,2,dz,"burial_stone_slab");}}
   case TREASURY->{for(int dz=-back+1;dz<back;dz+=3)for(int dx:new int[]{-side,side}){put(r.x+dx,r.y+1,r.z+dz,slab(r.palette));prop(r,dx,2,dz,"kurgan_pottery");}}
   case RITUAL->{for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)if(Math.abs(dx)==Math.abs(dz)||dx==0||dz==0)put(r.x+dx,r.y,r.z+dz,KurganBlocks.get("carved_burial_stone").defaultBlockState());for(int dx:new int[]{-side,side}){prop(r,dx,1,2,"kurgan_stone_altar");prop(r,dx,2,2,"kurgan_ritual_bowl");}}
   case FLOODED->{for(int dx=-r.rx+1;dx<r.rx;dx++)for(int dz=-r.rz+1;dz<r.rz;dz++)if(Math.abs(dx)>1&&Math.abs(dz)>1){put(r.x+dx,r.y-1,r.z+dz,KurganBlocks.stone(1,0));put(r.x+dx,r.y,r.z+dz,Blocks.WATER.defaultBlockState());}prop(r,side,1,-back,"kurgan_rubble");}
   case OSSUARY->{for(int dz=-back+1;dz<back;dz+=3)for(int dx:new int[]{-side,side}){put(r.x+dx,r.y+1,r.z+dz,slab(r.palette));put(r.x+dx,r.y+2,r.z+dz,Blocks.BONE_BLOCK.defaultBlockState());}}
   case OFFERING->{for(int dx:new int[]{-side,side}){prop(r,dx,1,2,"kurgan_stone_altar");prop(r,dx,2,2,"kurgan_pottery");prop(r,dx,1,-back,"kurgan_ritual_bowl");}}
   case RELIQUARY->{prop(r,side,1,back,"kurgan_column_fragment");prop(r,side,1,-back,"kurgan_ritual_bowl");put(r.x,r.y,r.z,KurganBlocks.get("carved_burial_stone").defaultBlockState());}
   case COLLAPSED->{for(int dx=-side;dx<=side;dx++)for(int dz=-back;dz<=back;dz++)if(Math.abs(dx)>1&&Math.abs(dz)>1&&Math.floorMod(dx*7+dz*11+(int)plan.seed,5)<2){put(r.x+dx,r.y+1,r.z+dz,slab(r.palette));if(Math.floorMod(dx+dz,3)==0)prop(r,dx,2,dz,"kurgan_rubble");}}
   case DESCENT->{prop(r,side,1,back,"kurgan_column_fragment");}
   case TRAP->{for(int dz=-back;dz<=back;dz+=3)put(r.x,r.y,r.z+dz,Blocks.CRACKED_STONE_BRICKS.defaultBlockState());trap(r);}
  }
  if(r.archetype==KurganPlan.Archetype.VESTIBULE||r.roomNode){
   put(r.x+side,r.y+3,r.z-r.rz,KurganBlocks.get("kurgan_wall_torch_holder").defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH));
   put(r.x-r.rx+2,r.y+r.height,r.z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
   if(r.height>=7){boolean round=r.archetype==KurganPlan.Archetype.DEEP||r.archetype==KurganPlan.Archetype.RITUAL;prop(r,round?4-r.rx:-side,r.height,round?4-r.rz:-back,"kurgan_hanging_brazier");}
  }
 }
 private void trap(KurganPlan.Room r){
  // Visible upward dispenser and a contrasting pressure plate; finite ammunition, vanilla damage.
  BlockPos p=pos(r.x,r.y,r.z);int key=r.id*16+15;
  if(!clip.isInside(p)||filled.contains(key))return;
  if(!(world.getBlockEntity(p) instanceof DispenserBlockEntity))raw(p,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.UP));
  if(world.getBlockEntity(p) instanceof DispenserBlockEntity dispenser){
   if(!dispenser.getPersistentData().getBoolean("SlavicKurganTrap")){raw(p.above(),Blocks.STONE_PRESSURE_PLATE.defaultBlockState());dispenser.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW,8));dispenser.getPersistentData().putBoolean("SlavicKurganTrap",true);dispenser.setChanged();}filled.add(key);
  }
 }
 private void entrance(){int front=plan.radius+1;int h=plan.tier+4,half=plan.tier+3,variant=Math.floorMod((int)plan.seed,3);
  for(int x:new int[]{-half,half})for(int y=0;y<=h;y++)put(x,y,front,KurganBlocks.stone(0,y==h?1:0));
  for(int x=-half;x<=half;x++)if(variant!=1||x<half-1)put(x,h+1,front,KurganBlocks.get("carved_burial_stone").defaultBlockState());
  if(half>3)for(int x:new int[]{-half+1,half-1})for(int y=1;y<=h;y++)put(x,y,front,KurganBlocks.stone(0,1));
  for(int z=front;z<=front+3;z++)for(int x=-1;x<=1;x++)for(int y=1;y<=h;y++)put(x,y,z,Blocks.CAVE_AIR.defaultBlockState());
  // Three authored conditions: intact, broken lintel, earth partly covering the outer pier.
  if(variant==1){put(half-1,1,front+1,slab(0));put(half,1,front+2,KurganBlocks.get("kurgan_rubble").defaultBlockState());}
  if(variant==2)for(int x=half;x<=half+1;x++)for(int y=0;y<=2;y++)put(x,y,front+1,(y==2?Blocks.GRASS_BLOCK:Blocks.DIRT).defaultBlockState());
  // Worn approach and two to five scattered markers. No cleared plaza or new trees.
  for(int z=front;z<=front+12;z++)for(int x=-2;x<=2;x++)if(Math.floorMod(x*17+z*13+(int)plan.seed,5)<2)put(x,piece.entranceSurfaceY(x,z),z,Blocks.COBBLESTONE.defaultBlockState());
  int marks=2+Math.floorMod((int)(plan.seed>>>3),4);for(int i=0;i<marks;i++){int dx=(i%2==0?-1:1)*(3+i%3),dz=front+4+i*2;int surface=piece.entranceSurfaceY(dx,dz);put(dx,surface,dz,KurganBlocks.stone(0,2));put(dx,surface+1,dz,i%2==0?slab(0):KurganBlocks.get("kurgan_rubble").defaultBlockState());}
 }
 private boolean coffin(KurganPlan.Room r,int i){return i==0&&(r.archetype==KurganPlan.Archetype.BURIAL||r.archetype==KurganPlan.Archetype.WARRIOR||r.archetype==KurganPlan.Archetype.DEEP);}
 private BlockPos cache(KurganPlan.Room r,int i){var s=KurganLayout.rewardPoint(r,i);return pos(s.x,s.y,s.z);}
 private Direction coffinDirection(BlockPos foot){return (foot.getX()>>4)==(foot.west().getX()>>4)?Direction.WEST:Direction.EAST;}
 private void container(KurganPlan.Room r,int index){BlockPos foot=cache(r,index);int key=r.id*16+index;if(!clip.isInside(foot))return;
  Direction direction=coffinDirection(foot);boolean burial=coffin(r,index);
  if(filled.contains(key))return;
  // Preserve existing inventory even if Filled was absent in an interrupted save.
  if(world.getBlockEntity(foot) instanceof RandomizableContainerBlockEntity existing&&existing.getPersistentData().hasUUID("SlavicKurganContainer")){filled.add(key);return;}
  raw(foot.below(),stone(r.palette,foot.getX(),foot.getY(),foot.getZ()));raw(foot.above(),Blocks.CAVE_AIR.defaultBlockState());
  if(burial){raw(foot.relative(direction).below(),stone(r.palette,foot.getX(),foot.getY(),foot.getZ()));raw(foot.relative(direction).above(),Blocks.CAVE_AIR.defaultBlockState());raw(foot,ModBlocks.BURIAL_COFFIN.get().defaultBlockState().setValue(BurialCoffinBlock.FACING,direction));raw(foot.relative(direction),ModBlocks.BURIAL_COFFIN.get().defaultBlockState().setValue(BurialCoffinBlock.FACING,direction).setValue(BurialCoffinBlock.PART,BedPart.HEAD));}
  else raw(foot,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.UP));
  if(world.getBlockEntity(foot) instanceof RandomizableContainerBlockEntity tile){
   if(tile instanceof BurialCoffinTile coffin)coffin.natural(piece.id,Math.floorMod(r.id,4));
   tile.getPersistentData().putUUID("SlavicKurganContainer",piece.id);
   tile.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","kurgan/"+r.loot)),plan.seed+r.id*31L+index);tile.setChanged();filled.add(key);
  }
 }
}
