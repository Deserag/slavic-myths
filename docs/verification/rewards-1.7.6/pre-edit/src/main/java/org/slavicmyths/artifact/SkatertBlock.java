package org.slavicmyths.artifact;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public final class SkatertBlock extends Block implements EntityBlock {
 public static final IntegerProperty PORTIONS=IntegerProperty.create("portions",0,6);
 public SkatertBlock(){super(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOL).strength(.5F).sound(SoundType.WOOL).noOcclusion());registerDefaultState(stateDefinition.any().setValue(PORTIONS,6));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PORTIONS);}
 public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return box(0,0,0,16,2,16);}
 public boolean hasTileEntity(BlockState s){return true;}public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState s){return new SkatertTile(pos,s);}
 public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP);}
 public BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor w,BlockPos p,BlockPos q){return d==Direction.DOWN&&!canSurvive(s,w,p)?Blocks.AIR.defaultBlockState():s;}
 public void setPlacedBy(Level w,BlockPos pos,BlockState s,LivingEntity who,ItemStack stack){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof SkatertTile){SkatertTile t=(SkatertTile)w.getBlockEntity(pos);t.cloth=stack.copy();t.cloth.setCount(1);t.expires=ArtifactEvents.now(w)+2400;t.setChanged();w.scheduleTick(pos,this,2400);if(who instanceof Player)org.slavicmyths.progression.Knowledge.award((Player)who,"table_is_set");}}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){if(w.isClientSide)return net.minecraft.world.ItemInteractionResult.SUCCESS;if(p.isSecondaryUseActive()){w.removeBlock(pos,false);return net.minecraft.world.ItemInteractionResult.CONSUME;}int portions=s.getValue(PORTIONS);if(portions>0&&p.canEat(false)&&!p.getCooldowns().isOnCooldown(org.slavicmyths.registry.ModItems.SKATERT.get())){p.getFoodData().eat(6,.6F);p.getCooldowns().addCooldown(org.slavicmyths.registry.ModItems.SKATERT.get(),24);w.setBlock(pos,s.setValue(PORTIONS,portions-1),3);w.playSound(null,pos,SoundEvents.GENERIC_EAT,SoundSource.PLAYERS,.65F,1);((ServerLevel)w).sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,new ItemStack(org.slavicmyths.registry.ModItems.PANCAKES.get())),p.getX(),p.getEyeY()-.2,p.getZ(),4,.1,.1,.1,.015);}return net.minecraft.world.ItemInteractionResult.CONSUME;}
 public void tick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){BlockEntity t=w.getBlockEntity(p);if(t instanceof SkatertTile){long remaining=((SkatertTile)t).expires-ArtifactEvents.now(w);if(remaining<=0)w.removeBlock(p,false);else w.scheduleTick(p,this,(int)Math.min(2400,remaining));}}
 public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&!w.isClientSide){BlockEntity te=w.getBlockEntity(p);if(te instanceof SkatertTile){SkatertTile t=(SkatertTile)te;ItemStack result=t.cloth.copy();t.cloth=ItemStack.EMPTY;if(!result.isEmpty()){org.slavicmyths.item.ItemState.clothReady(result,ArtifactEvents.now(w)+6000);popResource(w,p,result);}}}super.onRemove(s,w,p,next,moving);}
}
