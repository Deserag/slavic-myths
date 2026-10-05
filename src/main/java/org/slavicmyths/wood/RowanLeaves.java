package org.slavicmyths.wood;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
public final class RowanLeaves extends LeavesBlock {
 public static final BooleanProperty BERRIES=BooleanProperty.create("berries");
 public RowanLeaves(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));registerDefaultState(defaultBlockState().setValue(BERRIES,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(BERRIES);}
 @Override public boolean isRandomlyTicking(BlockState s){return true;}
 @Override public void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){
  super.randomTick(s,w,p,r);
  if(w.getBlockState(p).is(this)&&!s.getValue(BERRIES)&&s.getValue(DISTANCE)<7&&w.getMaxLocalRawBrightness(p.above())>=9&&r.nextInt(12)==0)w.setBlock(p,s.setValue(BERRIES,true),2);
 }
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),s,w,p,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
  if(!s.getValue(BERRIES))return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  if(!w.isClientSide){w.setBlock(p,s.setValue(BERRIES,false),3);popResource(w,p,new ItemStack(Woodlands.BERRIES.get(),1+w.random.nextInt(2)));w.playSound(null,p,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundSource.BLOCKS,1,1);}
  return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);
 }
@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 30;}
}
