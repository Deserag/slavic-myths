package org.slavicmyths.storage;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
public final class SheafBlock extends HorizontalDirectionalBlock{
 private static final MapCodec<SheafBlock>CODEC=simpleCodec(SheafBlock::new);
 public SheafBlock(){this(Properties.ofFullCopy(Blocks.HAY_BLOCK).strength(.5F).noOcclusion());}private SheafBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 protected MapCodec<? extends SheafBlock>codec(){return CODEC;}protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
 public BlockState getStateForPlacement(BlockPlaceContext c){BlockState s=defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());return canSurvive(s,c.getLevel(),c.getClickedPos())?s:null;}
 public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP);}
 public BlockState updateShape(BlockState s,Direction d,BlockState o,LevelAccessor w,BlockPos p,BlockPos q){return d==Direction.DOWN&&!canSurvive(s,w,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,d,o,w,p,q);}
 public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return box(3,0,3,13,14,13);}public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 public int getFlammability(BlockState s,BlockGetter w,BlockPos p,Direction d){return 60;}public int getFireSpreadSpeed(BlockState s,BlockGetter w,BlockPos p,Direction d){return 60;}
}
