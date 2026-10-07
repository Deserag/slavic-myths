package org.slavicmyths.brewing;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
public final class HopsCrop extends CropBlock {
 public static final IntegerProperty AGE=IntegerProperty.create("age",0,5);public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public static final MapCodec<HopsCrop> CODEC=simpleCodec(HopsCrop::new);
 public HopsCrop(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(AGE,0).setValue(HALF,DoubleBlockHalf.LOWER));}
 public MapCodec<HopsCrop> codec(){return CODEC;}protected IntegerProperty getAgeProperty(){return AGE;}public int getMaxAge(){return 5;}protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AGE,HALF);}protected ItemLike getBaseSeedId(){return Brewing.item("hops_cutting");}
 public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){if(s.getValue(HALF)==DoubleBlockHalf.UPPER){var below=w.getBlockState(p.below());return below.is(this)&&below.getValue(HALF)==DoubleBlockHalf.LOWER&&below.getValue(AGE)>=3;}return super.canSurvive(s,w,p);}
 public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,net.minecraft.world.entity.player.Player player){if(!w.isClientSide&&s.getValue(HALF)==DoubleBlockHalf.UPPER){BlockPos root=p.below();BlockState lower=w.getBlockState(root);if(lower.is(this)){if(!player.getAbilities().instabuild)Block.dropResources(lower,w,root,null,player,player.getMainHandItem());w.setBlock(root,Blocks.AIR.defaultBlockState(),2);}}return super.playerWillDestroy(w,p,s,player);}
 public boolean growTo(LevelAccessor w,BlockPos p,int age){BlockState old=w.getBlockState(p);if(!old.is(this))return false;if(old.getValue(HALF)==DoubleBlockHalf.UPPER)p=p.below();var above=w.getBlockState(p.above());if(age>=3&&!above.isAir()&&!above.is(this))return false;w.setBlock(p,getStateForAge(age),2);if(age>=3)w.setBlock(p.above(),getStateForAge(age).setValue(HALF,DoubleBlockHalf.UPPER),2);else if(above.is(this))w.setBlock(p.above(),Blocks.AIR.defaultBlockState(),2);w.setBlock(p,getStateForAge(age),3);return true;}
 public BlockState updateShape(BlockState s,Direction d,BlockState neighbor,LevelAccessor w,BlockPos p,BlockPos q){if(s.getValue(HALF)==DoubleBlockHalf.UPPER)return canSurvive(s,w,p)?s.setValue(AGE,w.getBlockState(p.below()).getValue(AGE)):Blocks.AIR.defaultBlockState();if(d==Direction.UP&&s.getValue(AGE)>=3&&!neighbor.is(this))return s.setValue(AGE,2);return super.updateShape(s,d,neighbor,w,p,q);}
 protected void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(s.getValue(HALF)!=DoubleBlockHalf.LOWER||!w.isAreaLoaded(p,1)||w.getRawBrightness(p,0)<9||s.getValue(AGE)>=5)return;float speed=getGrowthSpeed(s,w,p);if(r.nextFloat()<.85F&&r.nextInt((int)(25/speed)+1)==0)growTo(w,p,s.getValue(AGE)+1);}
 public boolean isValidBonemealTarget(LevelReader w,BlockPos p,BlockState s){if(s.getValue(HALF)==DoubleBlockHalf.UPPER){p=p.below();s=w.getBlockState(p);}return s.is(this)&&s.getValue(AGE)<5&&(s.getValue(AGE)<2||w.getBlockState(p.above()).isAir()||w.getBlockState(p.above()).is(this));}
 public void performBonemeal(ServerLevel w,RandomSource r,BlockPos p,BlockState s){if(s.getValue(HALF)==DoubleBlockHalf.UPPER){p=p.below();s=w.getBlockState(p);}growTo(w,p,Math.min(5,s.getValue(AGE)+1));}
}
