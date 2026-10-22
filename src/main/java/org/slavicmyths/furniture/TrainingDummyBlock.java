package org.slavicmyths.furniture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Two occupied cells, one item, matching half checks and vanilla creative removal. */
public final class TrainingDummyBlock extends FurnitureBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public TrainingDummyBlock(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
 private TrainingDummyBlock(Properties p){super("training_dummy",p);registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
 private static final MapCodec<TrainingDummyBlock> CODEC=simpleCodec(TrainingDummyBlock::new);
 @Override protected MapCodec<TrainingDummyBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(HALF);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){BlockPos p=c.getClickedPos();return p.getY()<c.getLevel().getMaxBuildHeight()-1&&c.getLevel().getBlockState(p.above()).canBeReplaced(c)?super.getStateForPlacement(c):null;}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public void onPlace(BlockState s,Level w,BlockPos p,BlockState old,boolean moving){super.onPlace(s,w,p,old,moving);if(!w.isClientSide&&!old.is(this)&&s.getValue(HALF)==DoubleBlockHalf.LOWER&&p.getY()<w.getMaxBuildHeight()-1&&w.getBlockState(p.above()).canBeReplaced())w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){boolean lower=s.getValue(HALF)==DoubleBlockHalf.LOWER;if(d==(lower?Direction.UP:Direction.DOWN)&&(!other.is(this)||other.getValue(HALF)==s.getValue(HALF)||other.getValue(FACING)!=s.getValue(FACING)))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){if(!w.isClientSide&&player.isCreative()&&s.getValue(HALF)==DoubleBlockHalf.UPPER){BlockPos below=p.below();BlockState lower=w.getBlockState(below);if(lower.is(this)&&lower.getValue(HALF)==DoubleBlockHalf.LOWER)w.setBlock(below,Blocks.AIR.defaultBlockState(),35);}return super.playerWillDestroy(w,p,s,player);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){VoxelShape shape=s.getValue(HALF)==DoubleBlockHalf.LOWER?Shapes.or(box(2,0,2,14,2,14),box(6,2,6,10,16,10)):Shapes.or(box(3,0,4,13,10,12),box(0,5,6,16,7,10),box(5,10,5,11,16,11));if(s.getValue(FACING).getAxis()==Direction.Axis.X){final VoxelShape[]rotated={Shapes.empty()};shape.forAllBoxes((a,b,d,e,f,g)->rotated[0]=Shapes.or(rotated[0],Shapes.box(1-g,b,a,1-d,f,e)));return rotated[0];}return shape;}
}
