package org.slavicmyths.wood;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
public final class HangingLeaves extends Block {
 public HangingLeaves(){super(AbstractBlock.Properties.copy(Blocks.VINE).randomTicks().noCollission());}
 @Override public boolean canSurvive(BlockState s,IWorldReader w,BlockPos p){BlockState above=w.getBlockState(p.above());return above.is(this)||above.is(Woodlands.SETS.get("willow").get("leaves"));}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,IWorld w,BlockPos p,BlockPos q){if(d==Direction.UP&&!canSurvive(s,w,p))w.getBlockTicks().scheduleTick(p,this,1);return super.updateShape(s,d,other,w,p,q);}
 @Override public void tick(BlockState s,ServerWorld w,BlockPos p,Random r){if(!canSurvive(s,w,p))w.destroyBlock(p,true);}
 @Override public void randomTick(BlockState s,ServerWorld w,BlockPos p,Random r){if(!canSurvive(s,w,p))w.destroyBlock(p,true);}
@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 30;}
}
