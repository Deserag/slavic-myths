package org.slavicmyths.block;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.slavicmyths.kitchen.*;
public final class KitchenTableBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public enum Part implements StringRepresentable{LEFT,RIGHT;public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
 public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);private static final MapCodec<KitchenTableBlock> CODEC=simpleCodec(p->new KitchenTableBlock(p));
 public KitchenTableBlock(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion());}
 private KitchenTableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,Part.LEFT));}
 protected MapCodec<? extends KitchenTableBlock> codec(){return CODEC;}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,PART);}
 public static BlockPos master(BlockState s,BlockPos p){return s.getValue(PART)==Part.LEFT?p:p.relative(s.getValue(FACING).getCounterClockWise());}
 private static BlockPos partner(BlockState s,BlockPos p){return p.relative(s.getValue(PART)==Part.LEFT?s.getValue(FACING).getClockWise():s.getValue(FACING).getCounterClockWise());}
 public BlockState getStateForPlacement(BlockPlaceContext c){Direction facing=c.getHorizontalDirection().getOpposite();BlockPos q=c.getClickedPos().relative(facing.getClockWise());return c.getLevel().getWorldBorder().isWithinBounds(q)&&c.getLevel().getBlockState(q).canBeReplaced(c)?defaultBlockState().setValue(FACING,facing):null;}
 public void setPlacedBy(Level w,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity e,ItemStack stack){w.setBlock(partner(s,p),s.setValue(PART,Part.RIGHT),3);}
 public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){if(q.equals(partner(s,p))&&(!other.is(this)||other.getValue(PART)==s.getValue(PART)||other.getValue(FACING)!=s.getValue(FACING)))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(PART)==Part.LEFT?new KitchenTile(p,s):null;}
 @SuppressWarnings("unchecked") public <T extends BlockEntity>BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T>type){return !w.isClientSide&&s.getValue(PART)==Part.LEFT&&type==KitchenII.TILE.get()?(l,p,st,t)->KitchenTile.tick(l,p,st,(KitchenTile)t):null;}
 public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){int x=s.getValue(PART)==Part.LEFT?1:12;VoxelShape shape=Shapes.or(box(0,12,0,16,16,16),box(x,0,1,x+3,12,4),box(x,0,12,x+3,12,15),box(1,3,2,15,5,14));int turns=s.getValue(FACING)==Direction.NORTH?0:s.getValue(FACING)==Direction.EAST?1:s.getValue(FACING)==Direction.SOUTH?2:3;for(int i=0;i<turns;i++){final VoxelShape[]next={Shapes.empty()};shape.forAllBoxes((a,b,z,d,e,f)->next[0]=Shapes.or(next[0],Shapes.box(1-f,b,a,1-z,e,d)));shape=next[0];}return shape;}

 public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 private KitchenTile tile(Level w,BlockPos p,BlockState s){return w.getBlockEntity(master(s,p))instanceof KitchenTile t?t:null;}
 public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){return useItemOn(player.getMainHandItem(),s,w,p,player,InteractionHand.MAIN_HAND,hit).result();}
 public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level w,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(player.isSpectator())return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;KitchenTile t=tile(w,p,s);if(t==null)return ItemInteractionResult.sidedSuccess(w.isClientSide);int slot=s.getValue(PART)==Part.LEFT?0:1;if(t.serve(player,stack)||t.install(player,hand,slot))return ItemInteractionResult.sidedSuccess(w.isClientSide);if(stack.isEmpty()&&player.isShiftKeyDown()){t.removeTool(player,slot);return ItemInteractionResult.sidedSuccess(w.isClientSide);}if(!w.isClientSide)((net.minecraft.server.level.ServerPlayer)player).openMenu(t,b->b.writeBlockPos(t.getBlockPos()));return ItemInteractionResult.sidedSuccess(w.isClientSide);}
 public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!w.isClientSide){if(s.getValue(PART)==Part.LEFT&&w.getBlockEntity(p)instanceof KitchenTile t){t.dropInventory();popResource(w,p,new ItemStack(this));}BlockPos q=partner(s,p);BlockState other=w.getBlockState(q);if(other.is(this)&&other.getValue(PART)!=s.getValue(PART)&&other.getValue(FACING)==s.getValue(FACING))w.removeBlock(q,false);}super.onRemove(s,w,p,next,moving);}
}
