package org.slavicmyths.morovoy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Saved single structure, but only the intersecting generation chunk is ever written. */
public final class MorovoyArenaPiece extends StructurePiece {
 private final BlockPos center;
 private final boolean zmey;
 private final int floorY,entrances;
 private final long seed;
 private final int[] entranceY;
 public MorovoyArenaPiece(MorovoyArenaTerrain.Plan plan){this(plan,false);}
 public MorovoyArenaPiece(MorovoyArenaTerrain.Plan plan,boolean zmey){
  super(MorovoyArenaWorldgen.PIECE.get(),0,bounds(plan.center()));
  center=plan.center().immutable();floorY=plan.floorY();seed=plan.seed();this.zmey=zmey;
  entrances=plan.entranceMask();entranceY=plan.entranceY().clone();
 }
 public MorovoyArenaPiece(CompoundTag tag){
  super(MorovoyArenaWorldgen.PIECE.get(),tag);
  zmey=tag.getBoolean("Zmey");center=BlockPos.of(tag.getLong("Center"));floorY=tag.getInt("FloorY");seed=tag.getLong("DesignSeed");
  entrances=tag.getInt("EntranceMask");entranceY=tag.getIntArray("EntranceHeights");
  if(entranceY.length!=4)throw new IllegalArgumentException("Invalid arena entrance heights");
 }
 private static BoundingBox bounds(BlockPos center){
  int r=MorovoyArenaGeometry.OUTER_RADIUS;
  return new BoundingBox(center.getX()-r,center.getY()-MorovoyArenaGeometry.FOUNDATION,center.getZ()-r,
   center.getX()+r,MorovoyArenaGeometry.CLEAR_TOP_Y,center.getZ()+r);
 }
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){
  tag.putBoolean("Zmey",zmey);tag.putLong("Center",center.asLong());tag.putInt("FloorY",floorY);tag.putLong("DesignSeed",seed);
  tag.putInt("EntranceMask",entrances);tag.putIntArray("EntranceHeights",entranceY);
 }
 /** Future boss may retrieve these through the persisted native structure start. */
 public BlockPos arenaCenter(){return center;}
 public int effectiveRadius(){return MorovoyArenaGeometry.OUTER_RADIUS;}
 public int combatRadius(){return MorovoyArenaGeometry.COMBAT_RADIUS;}
 public int flightRadius(){return MorovoyArenaGeometry.FLIGHT_RADIUS;}
 public int flightMinY(){return floorY+30;}
 public int flightMaxY(){return MorovoyArenaGeometry.FLIGHT_TOP_Y;}
 public BlockPos futureBossAnchor(){return center.above(12);}
 public List<BlockPos> accessPoints(){
  List<BlockPos> points=new ArrayList<>();
  for(int i=0;i<4;i++)if((entrances&(1<<i))!=0){
   int dx=i==0?114:i==2?-114:0,dz=i==1?114:i==3?-114:0;
   int y=MorovoyArenaGeometry.surfaceY(dx,dz,floorY,seed,entrances,entranceY);
   points.add(center.offset(dx,y-floorY+1,dz));
  }
  return List.copyOf(points);
 }
 private BlockState floorBlock(int dx,int dz){
  int pick=MorovoyArenaGeometry.choice(seed,dx,dz,1000);
  double dist=MorovoyArenaGeometry.distance(dx,dz,seed);
  if(dx==0&&dz==0)return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
  if(dist>82&&pick<180)return Blocks.BASALT.defaultBlockState();
  if(dist>90&&pick<350)return Blocks.BLACKSTONE.defaultBlockState();
  if(pick<11&&dist>22)return Blocks.CRYING_OBSIDIAN.defaultBlockState();
  if(pick>=11&&pick<19&&dist>30)return Blocks.WARPED_WART_BLOCK.defaultBlockState();
  if(pick>=19&&pick<95)return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
  if(pick>=95&&pick<195)return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
  return Blocks.BLACKSTONE.defaultBlockState();
 }
 @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,
   RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  int x0=Math.max(clip.minX(),boundingBox.minX()),x1=Math.min(clip.maxX(),boundingBox.maxX());
  int z0=Math.max(clip.minZ(),boundingBox.minZ()),z1=Math.min(clip.maxZ(),boundingBox.maxZ());
  int low=Math.max(clip.minY(),boundingBox.minY()),high=Math.min(clip.maxY(),boundingBox.maxY());
  BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int z=z0;z<=z1;z++)for(int x=x0;x<=x1;x++){
   int dx=x-center.getX(),dz=z-center.getZ();
   int grade=MorovoyArenaGeometry.surfaceY(dx,dz,floorY,seed,entrances,entranceY);
   if(grade==Integer.MIN_VALUE)continue;
   BlockState surface=floorBlock(dx,dz);
   for(int y=low;y<=high;y++){
    BlockState next=y<grade-MorovoyArenaGeometry.FOUNDATION?null:
     y<grade?(y<grade-3?Blocks.BLACKSTONE.defaultBlockState():Blocks.BASALT.defaultBlockState()):
     y==grade?surface:Blocks.CAVE_AIR.defaultBlockState();
    if(next==null)continue;
    pos.set(x,y,z);
    if(!world.ensureCanWrite(pos))continue;
    BlockState old=world.getBlockState(pos);
    if(old.is(Blocks.BEDROCK)||old.hasBlockEntity()||!MorovoyArenaTerrain.editable(old)||old.equals(next))continue;
    world.setBlock(pos,next,2);
   }
  }
  // The center belongs to exactly one generation chunk, and an arena chooses
  // one species via the shared structure set. Raven worlds are unaffected.
  BlockPos spawn=center.above(2);
  if(zmey&&clip.isInside(spawn)){
   MorovoyFireZmey boss=MorovoyFireZmeyContent.BOSS.get().create(world.getLevel());
   if(boss!=null){boss.moveTo(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,0,0);world.addFreshEntity(boss);}
  }
 }
}
