package org.slavicmyths.furniture;
import net.minecraft.block.*;
import net.minecraft.state.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
public class FurnitureBlock extends HorizontalBlock {
 public final String kind;
 public FurnitureBlock(String kind){super(AbstractBlock.Properties.copy(Blocks.OAK_PLANKS).strength(2).noOcclusion());this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState>b){b.add(FACING);}
 @Override public BlockState getStateForPlacement(BlockItemUseContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 public boolean seat(){return kind.equals("chair")||kind.equals("stool")||kind.equals("bench");}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){
  VoxelShape shape;
  if(kind.equals("table"))shape=VoxelShapes.or(box(0,12,0,16,16,16),box(1,0,1,4,12,4),box(12,0,1,15,12,4),box(1,0,12,4,12,15),box(12,0,12,15,12,15));
  else if(kind.equals("chair"))shape=VoxelShapes.or(box(2,0,2,14,9,14),box(2,9,12,14,16,15));
  else if(seat())shape=box(kind.equals("bench")?0:2,0,2,kind.equals("bench")?16:14,9,14);
  else if(kind.equals("shelf"))shape=VoxelShapes.or(box(0,6,10,16,9,16),box(2,2,13,4,6,16),box(12,2,13,14,6,16));
  else if(kind.equals("firewood_bundle"))shape=box(1,0,2,15,7,14);
  else if(kind.equals("cloth_bag"))shape=VoxelShapes.or(box(2,0,2,14,8,14),box(4,8,4,12,12,12),box(6,12,6,10,15,10));
  else if(kind.equals("weapon_rack"))shape=VoxelShapes.or(box(1,0,4,4,16,8),box(12,0,4,15,16,8),box(0,9,3,16,12,9));
  else if(kind.equals("signal_bell"))shape=VoxelShapes.or(box(1,0,6,3,16,10),box(13,0,6,15,16,10),box(1,14,6,15,16,10),box(5,5,7,11,13,9));
  else if(kind.equals("training_dummy"))shape=VoxelShapes.or(box(6,0,6,10,16,10),box(3,5,4,13,13,12),box(0,9,6,16,11,10));
  else shape=box(1,0,1,15,16,15);
  int turns=s.getValue(FACING)==Direction.NORTH?0:s.getValue(FACING)==Direction.EAST?1:s.getValue(FACING)==Direction.SOUTH?2:3;
  for(int i=0;i<turns;i++){final VoxelShape[]next={VoxelShapes.empty()};shape.forAllBoxes((a,b,d,e,f,g)->next[0]=VoxelShapes.or(next[0],VoxelShapes.box(1-g,b,a,1-d,f,e)));shape=next[0];}return shape;
 }
 @Override public boolean canSurvive(BlockState s,IWorldReader w,BlockPos p){if(!kind.equals("shelf"))return true;Direction facing=s.getValue(FACING);BlockPos back=p.relative(facing.getOpposite());return w.getBlockState(back).isFaceSturdy(w,back,facing);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,IWorld w,BlockPos p,BlockPos q){if(kind.equals("shelf")&&!canSurvive(s,w,p))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
 @Override public boolean hasTileEntity(BlockState s){return kind.equals("weapon_rack");}
 @Override public TileEntity createTileEntity(BlockState s,IBlockReader w){return hasTileEntity(s)?new RackTile():null;}
 @Override public ActionResultType use(BlockState s,World w,BlockPos p,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
  if(seat()){if(!w.isClientSide)SeatEntity.sit(w,p,player);return ActionResultType.sidedSuccess(w.isClientSide);}
  if(kind.equals("signal_bell")){if(!w.isClientSide){net.minecraft.world.server.ServerWorld server=(net.minecraft.world.server.ServerWorld)w;org.slavicmyths.bandit.StrongholdRecords records=org.slavicmyths.bandit.StrongholdRecords.get(server);for(org.slavicmyths.bandit.BanditEntity b:w.getEntitiesOfClass(org.slavicmyths.bandit.BanditEntity.class,new AxisAlignedBB(p).inflate(24),e->e.campTotal==26&&e.camp!=null)){if(p.equals(records.record(b.camp).bells[b.zone])){records.ring(server,b.camp,b.zone);break;}}}return ActionResultType.sidedSuccess(w.isClientSide);}
  if(kind.equals("weapon_rack")){if(!w.isClientSide&&w.getBlockEntity(p)instanceof RackTile)((RackTile)w.getBlockEntity(p)).interact(player,hand);return ActionResultType.sidedSuccess(w.isClientSide);}
  return super.use(s,w,p,player,hand,hit);
 }
 @Override public void onRemove(BlockState s,World w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&w.getBlockEntity(p)instanceof RackTile){RackTile rack=(RackTile)w.getBlockEntity(p);if(!w.isClientSide&&!rack.weapon.isEmpty()){popResource(w,p,rack.weapon);rack.weapon=ItemStack.EMPTY;}}super.onRemove(s,w,p,next,moving);}
 @Override public int getFlammability(BlockState s,IBlockReader w,BlockPos p,Direction d){return kind.equals("signal_bell")?0:20;}
 @Override public int getFireSpreadSpeed(BlockState s,IBlockReader w,BlockPos p,Direction d){return kind.equals("signal_bell")?0:5;}
}
