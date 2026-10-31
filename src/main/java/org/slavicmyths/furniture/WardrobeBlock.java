package org.slavicmyths.furniture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.Containers;

public final class WardrobeBlock extends FurnitureBlock implements EntityBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public WardrobeBlock(){this(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
 private WardrobeBlock(Properties properties){super("wardrobe",properties);registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
 private static final com.mojang.serialization.MapCodec<WardrobeBlock> CODEC=simpleCodec(WardrobeBlock::new);
 @Override protected com.mojang.serialization.MapCodec<WardrobeBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(HALF);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return c.getClickedPos().getY()<c.getLevel().getMaxBuildHeight()-2&&c.getLevel().getBlockState(c.getClickedPos().above()).canBeReplaced(c)?super.getStateForPlacement(c):null;}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public BlockState updateShape(BlockState s,net.minecraft.core.Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){boolean lower=s.getValue(HALF)==DoubleBlockHalf.LOWER;if(d==(lower?net.minecraft.core.Direction.UP:net.minecraft.core.Direction.DOWN)&&(!other.is(this)||other.getValue(HALF)==s.getValue(HALF)))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER?new WardrobeTile(p,s):null;}
 @Override public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  BlockPos lower=s.getValue(HALF)==DoubleBlockHalf.UPPER?p.below():p;
  if(!level.isClientSide&&level.getBlockState(lower).is(this)&&level.getBlockEntity(lower) instanceof WardrobeTile tile)player.openMenu(tile);
  return ItemInteractionResult.sidedSuccess(level.isClientSide);
 }
 @Override public net.minecraft.world.InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){return useItemOn(ItemStack.EMPTY,s,level,p,player,InteractionHand.MAIN_HAND,hit).result();}
 @Override public void onRemove(BlockState s,Level level,BlockPos p,BlockState next,boolean moving){
  if(!s.is(next.getBlock())&&!level.isClientSide&&s.getValue(HALF)==DoubleBlockHalf.LOWER&&level.getBlockEntity(p) instanceof WardrobeTile tile){Containers.dropContents(level,p,tile);tile.clearContent();}
  super.onRemove(s,level,p,next,moving);
 }
}
