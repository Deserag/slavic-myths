package org.slavicmyths.wood;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.state.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
public final class RowanLeaves extends LeavesBlock {
 public static final BooleanProperty BERRIES=BooleanProperty.create("berries");
 public RowanLeaves(){super(AbstractBlock.Properties.copy(Blocks.OAK_LEAVES));registerDefaultState(defaultBlockState().setValue(BERRIES,false));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(BERRIES);}
 @Override public boolean isRandomlyTicking(BlockState s){return true;}
 @Override public void randomTick(BlockState s,ServerWorld w,BlockPos p,Random r){
  super.randomTick(s,w,p,r);
  if(w.getBlockState(p).is(this)&&!s.getValue(BERRIES)&&s.getValue(DISTANCE)<7&&w.getMaxLocalRawBrightness(p.above())>=9&&r.nextInt(12)==0)w.setBlock(p,s.setValue(BERRIES,true),2);
 }
 @Override public ActionResultType use(BlockState s,World w,BlockPos p,PlayerEntity player,Hand hand,BlockRayTraceResult hit){
  if(!s.getValue(BERRIES))return ActionResultType.PASS;
  if(!w.isClientSide){w.setBlock(p,s.setValue(BERRIES,false),3);popResource(w,p,new ItemStack(Woodlands.BERRIES.get(),1+w.random.nextInt(2)));w.playSound(null,p,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundCategory.BLOCKS,1,1);}
  return ActionResultType.sidedSuccess(w.isClientSide);
 }
@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 30;}
}
