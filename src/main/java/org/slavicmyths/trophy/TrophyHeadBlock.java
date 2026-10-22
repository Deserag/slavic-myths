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
 public final TrophyHeads.Kind kind;
 public TrophyHeadBlock(TrophyHeads.Kind k){this(k,Properties.ofFullCopy(Blocks.DRAGON_HEAD).strength(3).noOcclusion().pushReaction(PushReaction.BLOCK));}
 private TrophyHeadBlock(TrophyHeads.Kind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER));}
 private static final MapCodec<TrophyHeadBlock> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.xmap(TrophyHeads.Kind::valueOf,Enum::name).fieldOf("kind").forGetter(b->b.kind),propertiesCodec()).apply(i,TrophyHeadBlock::new));
 @Override protected MapCodec<TrophyHeadBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,HALF);}
 @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){var w=c.getLevel();var p=c.getClickedPos();for(int i=0;i<kind.height;i++){var at=p.above(i);if(!w.hasChunkAt(at)||at.getY()>=w.getMaxBuildHeight()||!w.getWorldBorder().isWithinBounds(at)||!w.getBlockState(at).canBeReplaced(c)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return null;}return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){if(!w.isClientSide&&kind.height==2)w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){var b=kind.bounds();int offset=s.getValue(HALF)==DoubleBlockHalf.UPPER?1:0;double bottom=Math.max(0,b[1]-offset),top=Math.min(1,b[4]-offset);if(top<=bottom)return Shapes.empty();if(s.getValue(FACING).getAxis()==Direction.Axis.X)return Shapes.box(b[2],bottom,b[0],b[5],top,b[3]);return Shapes.box(b[0],bottom,b[2],b[3],top,b[5]);}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER?new Tile(p,s):null;}
 private BlockPos anchor(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.UPPER?p.below():p;}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){if(!w.isClientSide&&player.isCreative()&&s.getValue(HALF)==DoubleBlockHalf.UPPER){var at=p.below();var lower=w.getBlockState(at);if(lower.is(this)&&lower.getValue(HALF)==DoubleBlockHalf.LOWER)w.destroyBlock(at,false);}return super.playerWillDestroy(w,p,s,player);}
 @Override protected void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){super.onRemove(s,w,p,next,moving);if(w.isClientSide||s.is(next.getBlock())||kind.height==1)return;var at=s.getValue(HALF)==DoubleBlockHalf.LOWER?p.above():p.below();var other=w.getBlockState(at);if(other.is(this)&&other.getValue(HALF)!=s.getValue(HALF)&&other.getValue(FACING)==s.getValue(FACING)){if(s.getValue(HALF)==DoubleBlockHalf.UPPER)w.destroyBlock(at,true);else w.removeBlock(at,false);}}
 public static final class Tile extends BlockEntity {public Tile(BlockPos p,BlockState s){super(TrophyHeads.TILE.get(),p,s);}public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).expandTowards(0,((TrophyHeadBlock)getBlockState().getBlock()).kind.height-1,0);}}
}
