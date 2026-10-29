package org.slavicmyths.netherstructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
/** One multi-chunk clipped piece; strictly five unobstructed central lanes, outside rails only. */
public final class NetherBridgePiece extends StructurePiece {
 private final BlockPos origin;
 private final Rotation rotation;
 private final int length,variant,gapStart,gapEnd;
 private final int[] supports;
 private final long seed;
 public NetherBridgePiece(NetherBridgeTerrain.Plan p){
  super(NetherBridgeWorldgen.PIECE.get(),0,p.bounds());
  origin=p.origin();rotation=p.rotation();length=p.length();variant=p.variant();
  gapStart=p.gapStart();gapEnd=p.gapEnd();supports=p.supports().clone();seed=p.seed();
 }
 public NetherBridgePiece(CompoundTag n){
  super(NetherBridgeWorldgen.PIECE.get(),n);
  origin=BlockPos.of(n.getLong("Origin"));rotation=Rotation.valueOf(n.getString("Rotation"));
  length=n.getInt("Length");variant=n.getInt("Variant");gapStart=n.getInt("GapStart");
  gapEnd=n.getInt("GapEnd");supports=n.getIntArray("Supports");seed=n.getLong("DesignSeed");
 }
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){
  n.putLong("Origin",origin.asLong());n.putString("Rotation",rotation.name());
  n.putInt("Length",length);n.putInt("Variant",variant);
  n.putInt("GapStart",gapStart);n.putInt("GapEnd",gapEnd);
  n.putIntArray("Supports",supports);n.putLong("DesignSeed",seed);
 }
 public int length(){return length;}
 public int variant(){return variant;}
 public BlockPos at(int lane,int dy,int t){return origin.offset(new BlockPos(lane,dy,t).rotate(rotation));}
 private boolean absent(int t){
  if(variant==2)return t>=gapStart&&t<gapEnd;
  if(variant==3)return(t>=gapStart&&t<gapEnd)||(t>2&&t<length-3&&t%8==4);
  return false;
 }
 public boolean span(int t){return t>=0&&t<length&&!absent(t);}
 private static boolean harmlessFlora(BlockState s){
  return s.is(Blocks.CRIMSON_FUNGUS)||s.is(Blocks.CRIMSON_ROOTS)||
    s.is(Blocks.WARPED_FUNGUS)||s.is(Blocks.WARPED_ROOTS)||
    s.is(Blocks.NETHER_SPROUTS)||s.is(Blocks.WEEPING_VINES)||
    s.is(Blocks.WEEPING_VINES_PLANT)||s.is(Blocks.TWISTING_VINES)||
    s.is(Blocks.TWISTING_VINES_PLANT)||s.is(Blocks.BROWN_MUSHROOM)||
    s.is(Blocks.RED_MUSHROOM);
 }
 private boolean battered(int lane,int t){
  long h=seed+lane*73856093L+t*19349663L;
  h=(h^(h>>>30))*-4658895280553007687L;
  return Math.floorMod(h^(h>>>27),9)<(variant==0?1:variant==1?3:variant==2?2:5);
 }
 /** null means untouched world, not an AIR carve or extra loot. */
 public BlockState voxel(int lane,int dy,int t){
  if(lane< -3||lane>3||t<0||t>=length||!span(t))return null;
  int a=Math.abs(lane);
  if(dy>=1&&dy<=2&&a<=2)return Blocks.AIR.defaultBlockState(); // clear harmless foliage from five usable lanes
  if(dy==0){
   if(a<=2)return battered(lane,t)?Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState():
     (t%7==0?Blocks.POLISHED_BLACKSTONE.defaultBlockState():Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
   return Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState();
  }
  if(dy==-1&&(a==3||t==0||t==length-1))return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
  if(dy==-2&&a==3&&t%3==0)return Blocks.POLISHED_BASALT.defaultBlockState();
  if(dy==1&&a==3){
   if(variant>0&&battered(lane,t))return null;
   if(t%9==1&&variant<=1)return Blocks.SOUL_LANTERN.defaultBlockState();
   return Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
  }
  if(a==3&&dy<0&&supports.length>=length*2&&NetherBridgeTerrain.pier(t,length)){
   int depth=supports[t*2+(lane==-3?0:1)];
   if(depth>0&&-dy<depth)return(dy%4==0?Blocks.POLISHED_BASALT:Blocks.BASALT).defaultBlockState();
  }return null;
 }
 @Override public void postProcess(WorldGenLevel w,StructureManager m,ChunkGenerator g,
       RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  if(supports.length!=length*2||length<NetherBridgeTerrain.MIN_LENGTH||length>NetherBridgeTerrain.MAX_LENGTH)return;
  for(int t=0;t<length;t++)for(int lane=-3;lane<=3;lane++)
   for(int dy=-NetherBridgeTerrain.MAX_DEPTH;dy<=2;dy++){
    BlockState next=voxel(lane,dy,t);
    if(next==null)continue;
    BlockPos at=at(lane,dy,t);
    if(!clip.isInside(at)||!w.ensureCanWrite(at))continue;
    BlockState old=w.getBlockState(at);
    if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||
      !(old.isAir()||old.is(Blocks.LAVA)||NetherBridgeTerrain.natural(old)||
        harmlessFlora(old)||old.canBeReplaced()))continue;
    // Air clears ONLY ambient vegetation; never cut another solid structure to create headroom.
    if(next.isAir() && !(old.isAir()||harmlessFlora(old)||(old.canBeReplaced()&&old.getFluidState().isEmpty())))continue;
    if(!old.equals(next))w.setBlock(at,next,2);
   }
 }
}
