package org.slavicmyths.trophy;

import com.mojang.serialization.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.material.PushReaction;

/** One or two occupied cells, one rendering/loot anchor, following Poloz's removal model. */
public final class TrophyHeadBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public enum Quarter implements net.minecraft.util.StringRepresentable {
  NW("nw",0,0), NE("ne",1,0), SW("sw",0,1), SE("se",1,1);
  public final String name;public final int dx,dz;
  Quarter(String name,int dx,int dz){this.name=name;this.dx=dx;this.dz=dz;}
  @Override public String getSerializedName(){return name;}
 }
 public static final EnumProperty<Quarter> QUARTER=EnumProperty.create("quarter",Quarter.class);
 public enum Cell implements net.minecraft.util.StringRepresentable {
  A("a",0,0),B("b",1,0),C("c",2,0),D("d",3,0),E("e",4,0),F("f",0,1),G("g",1,1),H("h",2,1),I("i",3,1),J("j",4,1),K("k",0,2),L("l",1,2),M("m",2,2),N("n",3,2),O("o",4,2),P("p",0,3),Q("q",1,3),R("r",2,3),S("s",3,3),T("t",4,3),U("u",0,4),V("v",1,4),W("w",2,4),X("x",3,4),Y("y",4,4);
  public final String name;public final int dx,dz;Cell(String n,int x,int z){name=n;dx=x;dz=z;}
  @Override public String getSerializedName(){return name;}
 }
 public static final EnumProperty<Cell> CELL=EnumProperty.create("cell",Cell.class);
 private boolean nineMor(){return kind==TrophyHeads.Kind.NINE_MOR_SERPENT;}
 private static BlockPos nineAnchor(BlockPos pos,BlockState state){Cell cell=state.getValue(CELL);return pos.offset(-cell.dx,0,-cell.dz);}

 private static BlockPos widePos(BlockPos anchor,Direction face,Quarter quarter){
  return anchor.relative(face.getClockWise(),quarter.dx).relative(face.getOpposite(),quarter.dz);
 }
 private static BlockPos wideAnchor(BlockPos pos,BlockState state){
  Quarter q=state.getValue(QUARTER);Direction f=state.getValue(FACING);
  return pos.relative(f.getCounterClockWise(),q.dx).relative(f,q.dz);
 }
 public final TrophyHeads.Kind kind;
 private boolean ravenLong(){return kind==TrophyHeads.Kind.MOROVOY_RAVEN;}
 public TrophyHeadBlock(TrophyHeads.Kind k){this(k,Properties.ofFullCopy(Blocks.DRAGON_HEAD).strength(3).noOcclusion().pushReaction(PushReaction.BLOCK));}
 private TrophyHeadBlock(TrophyHeads.Kind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER).setValue(QUARTER,Quarter.NW).setValue(CELL,Cell.A));}
 private static final MapCodec<TrophyHeadBlock> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.xmap(TrophyHeads.Kind::valueOf,Enum::name).fieldOf("kind").forGetter(b->b.kind),propertiesCodec()).apply(i,TrophyHeadBlock::new));
 @Override protected MapCodec<TrophyHeadBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,HALF,QUARTER,CELL);}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){
  if(nineMor()){
   var world=c.getLevel();BlockPos anchor=c.getClickedPos();
   for(Cell cell:Cell.values()){BlockPos at=anchor.offset(cell.dx,0,cell.dz);
    if(!world.hasChunkAt(at)||at.getY()+5>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(at)||!world.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;
   }
   return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());
  }
  if(ravenLong()){
   var world=c.getLevel();BlockPos anchor=c.getClickedPos();Direction face=c.getHorizontalDirection().getOpposite();
   for(BlockPos at:new BlockPos[]{anchor,anchor.relative(face)}){
    if(!world.hasChunkAt(at)||at.getY()>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(at)||!world.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;
   }
   return defaultBlockState().setValue(FACING,face).setValue(QUARTER,Quarter.NW);
  }
  if(kind.wide()){
   var w=c.getLevel();BlockPos p=c.getClickedPos();
   Direction face=c.getHorizontalDirection().getOpposite();
   for(Quarter q:Quarter.values()){
    BlockPos at=widePos(p,face,q);
    if(!w.hasChunkAt(at)||at.getY()>=w.getMaxBuildHeight()||!w.getWorldBorder().isWithinBounds(at)||!w.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;
   }
   return defaultBlockState().setValue(FACING,face).setValue(QUARTER,Quarter.NW);
  }
  var w=c.getLevel();var p=c.getClickedPos();
  for(int k=0;k<kind.height;k++){
   var at=p.above(k);
   if(!w.hasChunkAt(at)||at.getY()>=w.getMaxBuildHeight()||!w.getWorldBorder().isWithinBounds(at)||!w.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;
  }
  return defaultBlockState().setValue(FACING,(kind==TrophyHeads.Kind.MAGMA_LORD||kind.ordinal()>=TrophyHeads.Kind.GROVE_GUARDIAN.ordinal())&&c.getClickedFace().getAxis().isHorizontal()?c.getClickedFace():c.getHorizontalDirection().getOpposite());
 }
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){
  if(w.isClientSide)return;
  if(nineMor()){for(Cell cell:Cell.values())if(cell!=Cell.A)w.setBlock(p.offset(cell.dx,0,cell.dz),s.setValue(CELL,cell),3);return;}
  if(ravenLong()){w.setBlock(p.relative(s.getValue(FACING)),s.setValue(QUARTER,Quarter.NE),3);return;}
  if(kind.wide()){
   for(Quarter q:Quarter.values())if(q!=Quarter.NW)w.setBlock(widePos(p,s.getValue(FACING),q),s.setValue(QUARTER,q),3);
   return;
  }
  if(kind.height==2)w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);
 }
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 private boolean singleZmeyHead(){return kind==TrophyHeads.Kind.FIRE_SERPENT||kind==TrophyHeads.Kind.MOR_SERPENT||kind==TrophyHeads.Kind.MOROVOY_FIRE_ZMEY;}
 private static final VoxelShape ZMEY_SHAPE=Block.box(2.0D,0.0D,2.0D,14.0D,12.0D,14.0D);
 /** The raven occupies its anchor block; the narrow beak projects into one front cell. */
 private static VoxelShape ravenShape(BlockState state){
  if(state.getValue(QUARTER)==Quarter.NW)return Block.box(2,0,2,14,15,14);
  if(state.getValue(QUARTER)!=Quarter.NE)return Shapes.empty();
  return switch(state.getValue(FACING)){
   case NORTH->Block.box(5,2,3,11,10,16);
   case SOUTH->Block.box(5,2,0,11,10,13);
   case EAST->Block.box(0,2,5,13,10,11);
   case WEST->Block.box(3,2,5,16,10,11);
   default->Shapes.empty();
  };
 }
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){if(nineMor())return Block.box(0,0,0,16,16,16);if(ravenLong())return ravenShape(s);if(kind.wide())return Block.box(0,0,0,16,12,16);if(singleZmeyHead())return ZMEY_SHAPE;var b=kind.bounds();int offset=s.getValue(HALF)==DoubleBlockHalf.UPPER?1:0;double bottom=Math.max(0,b[1]-offset),top=Math.min(1,b[4]-offset);if(top<=bottom)return Shapes.empty();if(s.getValue(FACING).getAxis()==Direction.Axis.X)return Shapes.box(b[2],bottom,b[0],b[5],top,b[3]);return Shapes.box(b[0],bottom,b[2],b[3],top,b[5]);}
 @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){if(ravenLong())return ravenShape(s);if(kind.wide())return Block.box(0,0,0,16,12,16);return singleZmeyHead()?ZMEY_SHAPE:super.getCollisionShape(s,w,p,c);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER&&(!kind.wide()||s.getValue(QUARTER)==Quarter.NW)&&(!nineMor()||s.getValue(CELL)==Cell.A)?new Tile(p,s):null;}
 private BlockPos anchor(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.UPPER?p.below():p;}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){
  if(nineMor()&&!w.isClientSide&&player.isCreative()&&s.getValue(CELL)!=Cell.A){BlockPos at=nineAnchor(p,s);if(w.getBlockState(at).is(this))w.destroyBlock(at,false);}
  if(ravenLong()&&!w.isClientSide&&player.isCreative()&&s.getValue(QUARTER)==Quarter.NE){
   BlockPos anchor=p.relative(s.getValue(FACING).getOpposite());
   if(w.getBlockState(anchor).is(this))w.destroyBlock(anchor,false);
  }
  if(kind.wide()&&!ravenLong()&&!w.isClientSide&&player.isCreative()&&s.getValue(QUARTER)!=Quarter.NW){BlockPos anchor=wideAnchor(p,s);if(w.getBlockState(anchor).is(this))w.destroyBlock(anchor,false);}if(!w.isClientSide&&player.isCreative()&&s.getValue(HALF)==DoubleBlockHalf.UPPER){var at=p.below();var lower=w.getBlockState(at);if(lower.is(this)&&lower.getValue(HALF)==DoubleBlockHalf.LOWER)w.destroyBlock(at,false);}return super.playerWillDestroy(w,p,s,player);}
 @Override protected void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){
  super.onRemove(s,w,p,next,moving);
  if(w.isClientSide||s.is(next.getBlock()))return;
  if(nineMor()){
   if(s.getValue(CELL)==Cell.A){breakBurst(w,p);for(Cell cell:Cell.values())if(cell!=Cell.A){BlockPos at=p.offset(cell.dx,0,cell.dz);if(w.getBlockState(at).is(this))w.removeBlock(at,false);}}
   else{BlockPos at=nineAnchor(p,s);if(w.getBlockState(at).is(this))w.destroyBlock(at,true);}
   return;
  }
  if(ravenLong()){
   Direction face=s.getValue(FACING);
   if(s.getValue(QUARTER)==Quarter.NW){
    breakBurst(w,p);
    BlockPos front=p.relative(face);BlockState other=w.getBlockState(front);
    if(other.is(this)&&other.getValue(QUARTER)==Quarter.NE&&other.getValue(FACING)==face)w.removeBlock(front,false);
   }else if(s.getValue(QUARTER)==Quarter.NE){
    BlockPos anchor=p.relative(face.getOpposite());BlockState other=w.getBlockState(anchor);
    if(other.is(this)&&other.getValue(QUARTER)==Quarter.NW&&other.getValue(FACING)==face)w.destroyBlock(anchor,false);
   }
   return;
  }
  if(kind.wide()){
   if(s.getValue(QUARTER)==Quarter.NW){
    breakBurst(w,p);
    for(Quarter q:Quarter.values())if(q!=Quarter.NW){
     BlockPos at=widePos(p,s.getValue(FACING),q);
     if(w.getBlockState(at).is(this))w.removeBlock(at,false);
    }
   }else{
    BlockPos anchor=wideAnchor(p,s);
    if(w.getBlockState(anchor).is(this))w.destroyBlock(anchor,false);
   }
   return;
  }
  if(s.getValue(HALF)==DoubleBlockHalf.LOWER)breakBurst(w,p);
  if(kind.height==1)return;
  var at=s.getValue(HALF)==DoubleBlockHalf.LOWER?p.above():p.below();
  var other=w.getBlockState(at);
  if(other.is(this)&&other.getValue(HALF)!=s.getValue(HALF)&&other.getValue(FACING)==s.getValue(FACING)){
   if(s.getValue(HALF)==DoubleBlockHalf.UPPER)w.destroyBlock(at,true);else w.removeBlock(at,false);
  }
 }
 private void breakBurst(Level world,BlockPos anchor){
  if(!(world instanceof net.minecraft.server.level.ServerLevel server))return;
  var particle=switch(kind){case GROVE_GUARDIAN->net.minecraft.core.particles.ParticleTypes.ASH;case ZHARENITSA->net.minecraft.core.particles.ParticleTypes.SMALL_FLAME;case POSTEN->net.minecraft.core.particles.ParticleTypes.SOUL;case MAGMA_LORD->net.minecraft.core.particles.ParticleTypes.SMALL_FLAME;case TUGARIN->net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH;case SKIM->net.minecraft.core.particles.ParticleTypes.CLOUD;case VOLKOLAK->net.minecraft.core.particles.ParticleTypes.SMOKE;case PODZEMNIK->net.minecraft.core.particles.ParticleTypes.ASH;case LIKHO->net.minecraft.core.particles.ParticleTypes.SOUL;case MOR_LIKHO->net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE;case MOROVOY_RAVEN->net.minecraft.core.particles.ParticleTypes.ASH;case MOR_SERPENT->net.minecraft.core.particles.ParticleTypes.SOUL;case MOROVOY_FIRE_ZMEY->net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH;case FIRE_SERPENT->net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH;case NINE_MOR_SERPENT->net.minecraft.core.particles.ParticleTypes.SOUL;};
  double height=Math.min(kind.height,1.8);
  double cx=anchor.getX()+(ravenLong()?.5:nineMor()?2.5:kind.wide()?1:.5),cz=anchor.getZ()+(ravenLong()?.5:nineMor()?1.5:kind.wide()?1:.5);
  server.sendParticles(particle,cx,anchor.getY()+height*.5,cz,kind.wide()?45:24,kind.wide()?.62:.325,height*.35,kind.wide()?.62:.325,.06);
  if(kind==TrophyHeads.Kind.MOROVOY_FIRE_ZMEY)server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,cx+.40,anchor.getY()+.45,cz-.20,18,.24,.17,.3,.035);
 }
 public static final class Tile extends BlockEntity {public long clientLastFxTick=Long.MIN_VALUE;public float maskAngle,maskPrevious;public int maskCycle,guardianBurst;public boolean magmaAwake;public float magmaOpenness;public long magmaPoseTick=Long.MIN_VALUE;public Tile(BlockPos p,BlockState s){super(TrophyHeads.TILE.get(),p,s);}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 @Override public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
public net.minecraft.world.phys.AABB getRenderBoundingBox(){
 TrophyHeadBlock block=(TrophyHeadBlock)getBlockState().getBlock();
 if(block.kind==TrophyHeads.Kind.NINE_MOR_SERPENT)return new net.minecraft.world.phys.AABB(worldPosition).expandTowards(5,6,5);
 if(block.kind==TrophyHeads.Kind.MOROVOY_RAVEN){Direction face=getBlockState().getValue(FACING);return new net.minecraft.world.phys.AABB(worldPosition).expandTowards(face.getStepX(),0,face.getStepZ()).inflate(.125);}
 return new net.minecraft.world.phys.AABB(worldPosition).expandTowards(block.kind.wide()?1:0,block.kind.height-1,block.kind.wide()?1:0);
}}
}
