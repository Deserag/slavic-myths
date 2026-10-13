package org.slavicmyths.furniture;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
public class FurnitureBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public final String kind;
 public FurnitureBlock(String kind){this(kind,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
 protected FurnitureBlock(String kind,Properties properties){super(properties);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 private static final com.mojang.serialization.MapCodec<FurnitureBlock> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance->instance.group(
  com.mojang.serialization.Codec.STRING.fieldOf("kind").forGetter(block->block.kind),propertiesCodec()).apply(instance,FurnitureBlock::new));
 @Override protected com.mojang.serialization.MapCodec<? extends FurnitureBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 public boolean seat(){return kind.equals("chair")||kind.equals("stool")||kind.equals("bench");}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
  VoxelShape shape;
  if(kind.equals("table"))shape=Shapes.or(box(0,12,0,16,16,16),box(1,0,1,4,12,4),box(12,0,1,15,12,4),box(1,0,12,4,12,15),box(12,0,12,15,12,15));
  else if(kind.equals("chair"))shape=Shapes.or(box(2,0,2,14,9,14),box(2,9,12,14,16,15));
  else if(seat())shape=box(kind.equals("bench")?0:2,0,2,kind.equals("bench")?16:14,9,14);
  else if(kind.equals("shelf"))shape=Shapes.or(box(0,6,10,16,9,16),box(2,2,13,4,6,16),box(12,2,13,14,6,16));
  else if(kind.equals("firewood_bundle"))shape=box(1,0,2,15,7,14);
  else if(kind.equals("cloth_bag"))shape=Shapes.or(box(2,0,2,14,8,14),box(4,8,4,12,12,12),box(6,12,6,10,15,10));
  else if(kind.equals("weapon_rack"))shape=Shapes.or(box(1,0,4,4,16,8),box(12,0,4,15,16,8),box(0,9,3,16,12,9));
  else if(kind.equals("signal_bell"))shape=Shapes.or(box(1,0,6,3,16,10),box(13,0,6,15,16,10),box(1,14,6,15,16,10),box(5,5,7,11,13,9));
  else if(kind.equals("training_dummy"))shape=Shapes.or(box(6,0,6,10,16,10),box(3,5,4,13,13,12),box(0,9,6,16,11,10));
  else shape=box(1,0,1,15,16,15);
  int turns=s.getValue(FACING)==Direction.NORTH?0:s.getValue(FACING)==Direction.EAST?1:s.getValue(FACING)==Direction.SOUTH?2:3;
  for(int i=0;i<turns;i++){final VoxelShape[]next={Shapes.empty()};shape.forAllBoxes((a,b,d,e,f,g)->next[0]=Shapes.or(next[0],Shapes.box(1-g,b,a,1-d,f,e)));shape=next[0];}return shape;
 }
 @Override public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){if(!kind.equals("shelf"))return true;Direction facing=s.getValue(FACING);BlockPos back=p.relative(facing.getOpposite());return w.getBlockState(back).isFaceSturdy(w,back,facing);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){if(kind.equals("shelf")&&!canSurvive(s,w,p))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
 public boolean hasTileEntity(BlockState s){return kind.equals("weapon_rack");}
 @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState s){return hasTileEntity(s)?new RackTile(pos,s):null;}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),s,w,p,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(seat()){if(!w.isClientSide)SeatEntity.sit(w,p,player);return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
  if(kind.equals("signal_bell")){if(!w.isClientSide){net.minecraft.server.level.ServerLevel server=(net.minecraft.server.level.ServerLevel)w;org.slavicmyths.bandit.StrongholdRecords records=org.slavicmyths.bandit.StrongholdRecords.get(server);for(org.slavicmyths.bandit.BanditEntity b:w.getEntitiesOfClass(org.slavicmyths.bandit.BanditEntity.class,new AABB(p).inflate(24),e->e.campTotal==26&&e.camp!=null)){if(p.equals(records.record(b.camp).bells[b.zone])){records.ring(server,b.camp,b.zone);break;}}}return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
  if(kind.equals("weapon_rack")){if(!w.isClientSide&&w.getBlockEntity(p)instanceof RackTile)((RackTile)w.getBlockEntity(p)).interact(player,hand);return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
  return super.useItemOn(interactionStack,s,w,p,player,hand,hit);
 }
 @Override public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&w.getBlockEntity(p)instanceof RackTile){RackTile rack=(RackTile)w.getBlockEntity(p);if(!w.isClientSide&&!rack.weapon.isEmpty()){popResource(w,p,rack.weapon);rack.weapon=ItemStack.EMPTY;}}super.onRemove(s,w,p,next,moving);}
 @Override public int getFlammability(BlockState s,BlockGetter w,BlockPos p,Direction d){return kind.equals("signal_bell")?0:20;}
 @Override public int getFireSpreadSpeed(BlockState s,BlockGetter w,BlockPos p,Direction d){return kind.equals("signal_bell")?0:5;}
}
