package org.slavicmyths.kurgan;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.util.RandomSource;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.wood.Woodlands;
/** Exterior shell plus a short entrance recess, deliberately not a dungeon. */
public final class KurganPiece extends StructurePiece {
 private int kind,x,y,z;private UUID id;private boolean burialPlaced;
 public KurganPiece(int k,int x,int y,int z,UUID id){super(KurganStructures.PIECE.get(),0,box(k,x,y,z));this.kind=k;this.x=x;this.y=y;this.z=z;this.id=id;bounds();}
 public KurganPiece(StructureTemplateManager tm,CompoundTag n){super(KurganStructures.PIECE.get(),n);kind=n.getInt("Kind");x=n.getInt("X");y=n.getInt("Y");z=n.getInt("Z");id=n.getUUID("Barrow");burialPlaced=n.getBoolean("BurialPlaced");bounds();}
 private static BoundingBox box(int kind,int x,int y,int z){int r=KurganShape.RADIUS[kind]+8;return new BoundingBox(x-r,y-8,z-r,x+r,y+KurganShape.HEIGHT[kind]+22,z+r);}
 private void bounds(){int r=KurganShape.RADIUS[kind]+8;boundingBox=new BoundingBox(x-r,y-8,z-r,x+r,y+KurganShape.HEIGHT[kind]+22,z+r);}
 public BlockPos arrival(){return new BlockPos(x,y+1,z+KurganShape.RADIUS[kind]+2);}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag n){n.putInt("Kind",kind);n.putInt("X",x);n.putInt("Y",y);n.putInt("Z",z);n.putUUID("Barrow",id);n.putBoolean("BurialPlaced",burialPlaced);}
 private int surface(WorldGenLevel w,ChunkGenerator g,int dx,int dz){double q=Math.min(1,(dx*dx+dz*dz)/(double)(KurganShape.RADIUS[kind]*KurganShape.RADIUS[kind]));int ground=g.getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,w,w.getLevel().getChunkSource().randomState())-1;return y+KurganShape.height(kind,dx,dz)+(int)Math.round((ground-y)*q*q);}
 private void put(WorldGenLevel w,BoundingBox clip,int dx,int yy,int dz,BlockState state){BlockPos p=new BlockPos(x+dx,yy,z+dz);if(clip.isInside(p))w.setBlock(p,state,2);}
 @Override public void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource rand,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  int r=KurganShape.RADIUS[kind];
  for(int dx=Math.max(-r,clip.minX()-x);dx<=Math.min(r,clip.maxX()-x);dx++)for(int dz=Math.max(-r,clip.minZ()-z);dz<=Math.min(r,clip.maxZ()-z);dz++){
   double q=(dx*dx+dz*dz)/(double)(r*r);if(q>1)continue;int ground=g.getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,w,w.getLevel().getChunkSource().randomState())-1;
   int top=y+KurganShape.height(kind,dx,dz)+(int)Math.round((ground-y)*q*q);int noise=Math.floorMod(dx*73+dz*131+(int)id.getLeastSignificantBits(),37);
   for(int yy=ground;yy<=top;yy++)put(w,clip,dx,yy,dz,(yy==top?Blocks.GRASS_BLOCK:yy<top-3?Blocks.DIRT:Blocks.DIRT).defaultBlockState());
   // Broken winding approach and an incomplete contour path on the great mound.
   boolean path=dz>0&&Math.abs(dx-Math.round(1.8*Math.sin(dz*.3)))<1+(noise%2);
   if(kind==2&&q>.22&&q<.27&&dx> -r/2)path=true;
   if(path&&noise%5!=0)put(w,clip,dx,top,dz,new Block[]{Blocks.COBBLESTONE,Blocks.MOSSY_COBBLESTONE,Blocks.GRAVEL,Blocks.STONE,Blocks.COARSE_DIRT}[noise%5].defaultBlockState());
   else if(noise==0)put(w,clip,dx,top,dz,Blocks.COARSE_DIRT.defaultBlockState());
   if(!path&&noise==2)put(w,clip,dx,top+1,dz,Blocks.SHORT_GRASS.defaultBlockState());
   if(!path&&noise==7&&q>.1)put(w,clip,dx,top+1,dz,Blocks.MOSSY_COBBLESTONE.defaultBlockState());
  }
  // Entrance is a small, accessible recess; future rooms remain solid earth.
  int front=r-3,back=r-8,width=kind==0?0:1;
  for(int dx=-width;dx<=width;dx++)for(int dz=back;dz<=r+1;dz++){
   int ground=g.getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,w,w.getLevel().getChunkSource().randomState())-1;
   for(int yy=ground;yy<=y;yy++)put(w,clip,dx,yy,dz,Blocks.COBBLESTONE.defaultBlockState());
   for(int yy=y+1;yy<=y+3;yy++)put(w,clip,dx,yy,dz,Blocks.AIR.defaultBlockState());
  }
  for(int side:new int[]{-width-1,width+1})for(int yy=y;yy<=y+3;yy++)put(w,clip,side,yy,front,DarkenedWood.get("log").defaultBlockState());
  for(int dx=-width-1;dx<=width+1;dx++)put(w,clip,dx,y+4,front,DarkenedWood.get("log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
  for(int dz=back;dz<front;dz++)for(int dx=-width-1;dx<=width+1;dx++)put(w,clip,dx,y+4,dz,DarkenedWood.get("planks").defaultBlockState());
  for(int dx=-width;dx<=width;dx++)for(int yy=y+1;yy<y+4;yy++)put(w,clip,dx,yy,back-1,Blocks.COBBLESTONE.defaultBlockState());
  // Reuse the established tree geometry; deterministic seed keeps chunk order irrelevant.
  int trees=kind==0?2:kind==1?3:5;
  for(int i=0;i<trees;i++){
   double angle=i*Math.PI*2/trees+.8;int tx=(int)(Math.cos(angle)*r*.52),tz=(int)(Math.sin(angle)*r*.52),base=surface(w,g,tx,tz);String wood=i%2==0?"pine":"linden";
   org.slavicmyths.wood.TreeShape tree=new org.slavicmyths.wood.TreeShape(wood,new Random(id.getLeastSignificantBits()+i));
   for(Map.Entry<org.slavicmyths.wood.TreeShape.Cell,Integer> e:tree.logs.entrySet()){org.slavicmyths.wood.TreeShape.Cell c=e.getKey();put(w,clip,tx+c.x,base+1+c.y,tz+c.z,Woodlands.SETS.get(wood).get("log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[e.getValue()]));}
   for(Map.Entry<org.slavicmyths.wood.TreeShape.Cell,Integer> e:tree.leaves.entrySet()){org.slavicmyths.wood.TreeShape.Cell c=e.getKey();if(base+1+c.y>surface(w,g,tx+c.x,tz+c.z))put(w,clip,tx+c.x,base+1+c.y,tz+c.z,Woodlands.SETS.get(wood).get("leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,e.getValue()).setValue(LeavesBlock.PERSISTENT,true));}
   int mx=tx+3,mz=tz,top=surface(w,g,mx,mz);for(int yy=1;yy<=2;yy++)put(w,clip,mx,top+yy,mz,(i%2==0?DarkenedWood.get("stripped_log"):Blocks.MOSSY_COBBLESTONE).defaultBlockState());
   if(i%2==0)for(int dx=1;dx<=3;dx++)put(w,clip,mx+dx,surface(w,g,mx+dx,mz)+1,mz,DarkenedWood.get("fence").defaultBlockState());
  }
  for(int step=1;step<=6;step++){int dz=r+1+step,ground=g.getBaseHeight(x,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,w,w.getLevel().getChunkSource().randomState())-1;int stairY=Math.max(ground,y-step);for(int yy=ground;yy<stairY;yy++)put(w,clip,0,yy,dz,Blocks.COBBLESTONE.defaultBlockState());put(w,clip,0,stairY,dz,Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));}
  BlockPos foot=new BlockPos(x,y+1,z+back+3),head=foot.north();
  if(clip.isInside(foot)&&!burialPlaced){BlockState coffin=ModBlocks.BURIAL_COFFIN.get().defaultBlockState().setValue(BurialCoffinBlock.FACING,Direction.NORTH);w.setBlock(foot,coffin,2);w.setBlock(head,coffin.setValue(BurialCoffinBlock.PART,BedPart.HEAD),2);if(w.getBlockEntity(foot)instanceof BurialCoffinTile){BurialCoffinTile tile=(BurialCoffinTile)w.getBlockEntity(foot);tile.natural(id,Math.floorMod(id.hashCode(),4));tile.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/burial_"+(kind==0?"small":kind==1?"warrior":"great"))),id.getLeastSignificantBits());burialPlaced=true;}}
  return;
 }
}
