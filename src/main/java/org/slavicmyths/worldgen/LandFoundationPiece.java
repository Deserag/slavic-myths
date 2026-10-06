package org.slavicmyths.worldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
/** Saved native earth terrace; placed before authored buildings, clipped to the generated chunk. */
public final class LandFoundationPiece extends StructurePiece {
 private final int x0,z0,x1,z1,target,padding;
 public LandFoundationPiece(BoundingBox core,int target,int padding,int minY,int maxY){super(org.slavicmyths.swamp.SwampStructures.LAND_FOUNDATION.get(),0,new BoundingBox(core.minX()-padding,minY,core.minZ()-padding,core.maxX()+padding,maxY,core.maxZ()+padding));x0=core.minX();z0=core.minZ();x1=core.maxX();z1=core.maxZ();this.target=target;this.padding=padding;}
 public LandFoundationPiece(CompoundTag n){super(org.slavicmyths.swamp.SwampStructures.LAND_FOUNDATION.get(),n);x0=n.getInt("X0");z0=n.getInt("Z0");x1=n.getInt("X1");z1=n.getInt("Z1");target=n.getInt("Target");padding=n.getInt("Padding");}
 public int constructionY(){return target;}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag n){n.putInt("X0",x0);n.putInt("Z0",z0);n.putInt("X1",x1);n.putInt("Z1",z1);n.putInt("Target",target);n.putInt("Padding",padding);}
 @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  for(int x=Math.max(clip.minX(),x0-padding);x<=Math.min(clip.maxX(),x1+padding);x++)for(int z=Math.max(clip.minZ(),z0-padding);z<=Math.min(clip.maxZ(),z1+padding);z++){
   int g=LandTerrain.ground(world,x,z),surface=world.getHeight(world instanceof net.minecraft.server.level.ServerLevel?Heightmap.Types.WORLD_SURFACE:Heightmap.Types.WORLD_SURFACE_WG,x,z)-1;
   int distance=Math.max(Math.max(x0-x,x-x1),Math.max(z0-z,z-z1));int top=distance<=0?target:g+Integer.signum(target-g)*Math.max(0,Math.abs(target-g)-distance);
   for(int y=Math.max(clip.minY(),Math.min(g,top));y<=Math.min(clip.maxY(),Math.max(surface,top+4));y++){var p=new BlockPos(x,y,z);var old=world.getBlockState(p);if(old.is(Blocks.BEDROCK)||old.hasBlockEntity())continue;world.setBlock(p,(y>top?Blocks.AIR:y==top?Blocks.GRASS_BLOCK:y<top-4?Blocks.STONE:Blocks.DIRT).defaultBlockState(),2);}
  }
 }
 public static void prepend(java.util.List<StructurePiece> pieces,int target,int minY,int maxY){if(pieces.isEmpty())return;var b=pieces.get(0).getBoundingBox();var box=new BoundingBox(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());for(var p:pieces)box.encapsulate(p.getBoundingBox());pieces.add(0,new LandFoundationPiece(box,target,8,minY,maxY));}
}
