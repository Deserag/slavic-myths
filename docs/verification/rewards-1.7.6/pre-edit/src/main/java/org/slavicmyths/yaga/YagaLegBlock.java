package org.slavicmyths.yaga;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.BlockGetter;
/** Native scaled skin surfaces and separate joints/toes; never recolored log columns. */
public final class YagaLegBlock extends Block {
 public static final IntegerProperty PART=IntegerProperty.create("part",0,6);public static final DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public YagaLegBlock(){super(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).strength(-1,3600000).noOcclusion().noLootTable());registerDefaultState(stateDefinition.any().setValue(PART,0).setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PART,FACING);}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){int part=s.getValue(PART);if(part==0)return box(3,0,3,13,16,13);if(part==2)return box(5,0,5,11,16,11);if(part==6)return box(3,0,0,13,16,16);if(part==5)return box(0,0,0,16,4,16);boolean east=s.getValue(FACING).getAxis()==Direction.Axis.X;int a=part==1?3:6,h=part==1?10:4;return east?box(0,0,a,16,h,16-a):box(a,0,0,16-a,h,16);}
}
