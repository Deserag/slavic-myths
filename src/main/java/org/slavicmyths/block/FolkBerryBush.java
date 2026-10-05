package org.slavicmyths.block;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.ModItems;
public final class FolkBerryBush extends BushBlock implements BonemealableBlock {
 public static final IntegerProperty AGE=IntegerProperty.create("age",0,3);
 public static final EnumProperty<DoubleBlockHalf> HALF=net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;
 private final boolean raspberry;
 public FolkBerryBush(boolean raspberry){this(raspberry,BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.SWEET_BERRY_BUSH));}
 private FolkBerryBush(boolean raspberry,Properties properties){super(properties);this.raspberry=raspberry;registerDefaultState(stateDefinition.any().setValue(AGE,0).setValue(HALF,DoubleBlockHalf.LOWER));}
 private static final com.mojang.serialization.MapCodec<FolkBerryBush> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance->instance.group(
  com.mojang.serialization.Codec.BOOL.fieldOf("raspberry").forGetter(block->block.raspberry),propertiesCodec()).apply(instance,FolkBerryBush::new));
 @Override protected com.mojang.serialization.MapCodec<FolkBerryBush> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AGE,HALF);}
 @Override public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return s.getValue(HALF)==DoubleBlockHalf.UPPER?w.getBlockState(p.below()).getBlock()==this&&w.getBlockState(p.below()).getValue(HALF)==DoubleBlockHalf.LOWER&&w.getBlockState(p.below()).getValue(AGE)>=2:super.canSurvive(s,w,p);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos next){
  if(s.getValue(HALF)==DoubleBlockHalf.UPPER){if(!canSurvive(s,w,p))return Blocks.AIR.defaultBlockState();return s.setValue(AGE,w.getBlockState(p.below()).getValue(AGE));}
  if(raspberry&&s.getValue(AGE)>=2&&d==Direction.UP&&other.getBlock()!=this)return s.setValue(AGE,1);
  return super.updateShape(s,d,other,w,p,next);
 }
 public void growTo(LevelAccessor w,BlockPos p,int age){
  if(raspberry&&age>=2){if(!w.getBlockState(p.above()).canBeReplaced()&&w.getBlockState(p.above()).getBlock()!=this)return;
   w.setBlock(p,defaultBlockState().setValue(AGE,age),2);w.setBlock(p.above(),defaultBlockState().setValue(AGE,age).setValue(HALF,DoubleBlockHalf.UPPER),3);w.setBlock(p,defaultBlockState().setValue(AGE,age),3);
  }else w.setBlock(p,defaultBlockState().setValue(AGE,age),3);
 }
 @Override public void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(s.getValue(HALF)==DoubleBlockHalf.LOWER&&s.getValue(AGE)<3&&w.getRawBrightness(p.above(),0)>=(raspberry?8:5)&&r.nextInt(5)==0)growTo(w,p,s.getValue(AGE)+1);}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){
  BlockPos base=s.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;
  if(s.getValue(AGE)!=3)return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  if(!w.isClientSide){popResource(w,base,new ItemStack(raspberry?ModItems.RASPBERRY.get():ModItems.BLUEBERRY.get(),2+w.random.nextInt(3)));growTo(w,base,2);w.playSound(null,base,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundSource.BLOCKS,.7F,1);}
  return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);
 }
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){int age=s.getValue(AGE);return s.getValue(HALF)==DoubleBlockHalf.UPPER?box(1,0,1,15,13,15):box(age==0?5:1,0,age==0?5:1,age==0?11:15,age==0?5:raspberry?(age==1?12:16):(age==1?8:12),age==0?11:15);}
 @Override public boolean isValidBonemealTarget(LevelReader w,BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER&&s.getValue(AGE)<3&&(!raspberry||s.getValue(AGE)==0||w.getBlockState(p.above()).canBeReplaced()||w.getBlockState(p.above()).getBlock()==this);}
 @Override public boolean isBonemealSuccess(Level w,RandomSource r,BlockPos p,BlockState s){return true;}
 @Override public void performBonemeal(ServerLevel w,RandomSource r,BlockPos p,BlockState s){growTo(w,p,Math.min(3,s.getValue(AGE)+1));}
}
