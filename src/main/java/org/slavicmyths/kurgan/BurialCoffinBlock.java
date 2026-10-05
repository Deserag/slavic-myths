package org.slavicmyths.kurgan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Containers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.block.state.properties.BedPart;
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
public final class BurialCoffinBlock extends HorizontalDirectionalBlock implements EntityBlock {
 public static final EnumProperty<BedPart> PART=net.minecraft.world.level.block.state.properties.BlockStateProperties.BED_PART;
 public BurialCoffinBlock(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion().pushReaction(PushReaction.BLOCK));}
 private BurialCoffinBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,BedPart.FOOT));}
 private static final com.mojang.serialization.MapCodec<BurialCoffinBlock> CODEC=simpleCodec(BurialCoffinBlock::new);
 @Override protected com.mojang.serialization.MapCodec<BurialCoffinBlock> codec(){return CODEC;}
 @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){
  return type==org.slavicmyths.registry.ModTiles.BURIAL_COFFIN.get()&&state.getValue(PART)==BedPart.FOOT?(world,pos,blockState,tile)->{if(tile instanceof BurialCoffinTile coffin)coffin.tick();}:null;
 }
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,PART);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){Direction d=c.getHorizontalDirection();BlockPos other=c.getClickedPos().relative(d);return c.getLevel().getBlockState(other).canBeReplaced(c)&&c.getLevel().getWorldBorder().isWithinBounds(other)?defaultBlockState().setValue(FACING,d):null;}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){if(!w.isClientSide){w.setBlock(p.relative(s.getValue(FACING)),s.setValue(PART,BedPart.HEAD),3);if(w.getBlockEntity(p)instanceof BurialCoffinTile)((BurialCoffinTile)w.getBlockEntity(p)).makeDomestic();}}
 public static BlockPos foot(BlockState s,BlockPos p){return s.getValue(PART)==BedPart.FOOT?p:p.relative(s.getValue(FACING).getOpposite());}
 public boolean hasTileEntity(BlockState s){return s.getValue(PART)==BedPart.FOOT;}
 @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState s){return s.getValue(PART)==BedPart.FOOT?new BurialCoffinTile(pos,s):null;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return box(1,0,1,15,13,15);}
 @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter w,BlockPos p){return Shapes.empty();}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}

 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),s,w,p,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){BlockPos base=foot(s,p);if(!w.isClientSide&&w.getBlockEntity(base)instanceof BurialCoffinTile)((ServerPlayer)player).openMenu((BurialCoffinTile)w.getBlockEntity(base),buffer -> buffer.writeBlockPos(base));return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){if(!w.isClientSide&&player.isCreative()&&s.getValue(PART)==BedPart.HEAD){BlockPos base=foot(s,p);if(w.getBlockState(base).is(this))w.removeBlock(base,false);}return super.playerWillDestroy(w,p,s,player);}
 @Override public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())){
  if(s.getValue(PART)==BedPart.FOOT){BlockEntity t=w.getBlockEntity(p);if(t instanceof BurialCoffinTile)Containers.dropContents(w,p,(BurialCoffinTile)t);BlockPos other=p.relative(s.getValue(FACING));if(w.getBlockState(other).is(this)&&w.getBlockState(other).getValue(PART)==BedPart.HEAD&&w.getBlockState(other).getValue(FACING)==s.getValue(FACING))w.removeBlock(other,false);}
  else {BlockPos base=foot(s,p);if(w.getBlockState(base).is(this)&&w.getBlockState(base).getValue(PART)==BedPart.FOOT&&w.getBlockState(base).getValue(FACING)==s.getValue(FACING))w.destroyBlock(base,true);}
 }super.onRemove(s,w,p,next,moving);}
}
