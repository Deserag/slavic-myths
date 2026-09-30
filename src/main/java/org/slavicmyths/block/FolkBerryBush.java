package org.slavicmyths.block;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.ModItems;
public final class FolkBerryBush extends BushBlock implements IGrowable {
 public static final IntegerProperty AGE=IntegerProperty.create("age",0,3);
 public static final EnumProperty<DoubleBlockHalf> HALF=net.minecraft.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
 private final boolean raspberry;
 public FolkBerryBush(boolean raspberry){super(AbstractBlock.Properties.of(net.minecraft.block.material.Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.SWEET_BERRY_BUSH));this.raspberry=raspberry;registerDefaultState(stateDefinition.any().setValue(AGE,0).setValue(HALF,DoubleBlockHalf.LOWER));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> b){b.add(AGE,HALF);}
 @Override public boolean canSurvive(BlockState s,IWorldReader w,BlockPos p){return s.getValue(HALF)==DoubleBlockHalf.UPPER?w.getBlockState(p.below()).getBlock()==this&&w.getBlockState(p.below()).getValue(HALF)==DoubleBlockHalf.LOWER&&w.getBlockState(p.below()).getValue(AGE)>=2:super.canSurvive(s,w,p);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,IWorld w,BlockPos p,BlockPos next){
  if(s.getValue(HALF)==DoubleBlockHalf.UPPER){if(!canSurvive(s,w,p))return Blocks.AIR.defaultBlockState();return s.setValue(AGE,w.getBlockState(p.below()).getValue(AGE));}
  if(raspberry&&s.getValue(AGE)>=2&&d==Direction.UP&&other.getBlock()!=this)return s.setValue(AGE,1);
  return super.updateShape(s,d,other,w,p,next);
 }
 public void growTo(IWorld w,BlockPos p,int age){
  if(raspberry&&age>=2){if(!w.getBlockState(p.above()).getMaterial().isReplaceable()&&w.getBlockState(p.above()).getBlock()!=this)return;
   w.setBlock(p,defaultBlockState().setValue(AGE,age),2);w.setBlock(p.above(),defaultBlockState().setValue(AGE,age).setValue(HALF,DoubleBlockHalf.UPPER),3);w.setBlock(p,defaultBlockState().setValue(AGE,age),3);
  }else w.setBlock(p,defaultBlockState().setValue(AGE,age),3);
 }
 @Override public void randomTick(BlockState s,ServerWorld w,BlockPos p,Random r){if(s.getValue(HALF)==DoubleBlockHalf.LOWER&&s.getValue(AGE)<3&&w.getRawBrightness(p.above(),0)>=(raspberry?8:5)&&r.nextInt(5)==0)growTo(w,p,s.getValue(AGE)+1);}
 @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand h,BlockRayTraceResult hit){
  BlockPos base=s.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;
  if(s.getValue(AGE)!=3)return ActionResultType.PASS;
  if(!w.isClientSide){popResource(w,base,new ItemStack(raspberry?ModItems.RASPBERRY.get():ModItems.BLUEBERRY.get(),2+w.random.nextInt(3)));growTo(w,base,2);w.playSound(null,base,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundCategory.BLOCKS,.7F,1);}
  return ActionResultType.sidedSuccess(w.isClientSide);
 }
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){int age=s.getValue(AGE);return s.getValue(HALF)==DoubleBlockHalf.UPPER?box(1,0,1,15,13,15):box(age==0?5:1,0,age==0?5:1,age==0?11:15,age==0?5:raspberry?(age==1?12:16):(age==1?8:12),age==0?11:15);}
 @Override public boolean isValidBonemealTarget(IBlockReader w,BlockPos p,BlockState s,boolean client){return s.getValue(HALF)==DoubleBlockHalf.LOWER&&s.getValue(AGE)<3&&(!raspberry||s.getValue(AGE)==0||w.getBlockState(p.above()).getMaterial().isReplaceable()||w.getBlockState(p.above()).getBlock()==this);}
 @Override public boolean isBonemealSuccess(World w,Random r,BlockPos p,BlockState s){return true;}
 @Override public void performBonemeal(ServerWorld w,Random r,BlockPos p,BlockState s){growTo(w,p,Math.min(3,s.getValue(AGE)+1));}
}
