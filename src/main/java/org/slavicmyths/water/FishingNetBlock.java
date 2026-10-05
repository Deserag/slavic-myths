package org.slavicmyths.water;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.tags.FluidTags;
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
import org.slavicmyths.registry.*;
public final class FishingNetBlock extends Block implements EntityBlock {
 public static final BooleanProperty FILLED=BooleanProperty.create("filled");
 public FishingNetBlock(){super(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD).strength(.6F).noOcclusion().noCollission().sound(SoundType.WOOL));registerDefaultState(stateDefinition.any().setValue(FILLED,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FILLED);}
 public BlockState getStateForPlacement(BlockPlaceContext c){return c.getLevel().getFluidState(c.getClickedPos()).is(FluidTags.WATER)?defaultBlockState():null;}
 public FluidState getFluidState(BlockState s){return Fluids.WATER.getSource(false);}
 public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return box(0,0,6,16,15,10);}
 public boolean hasTileEntity(BlockState s){return true;}public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState s){return new FishingNetTile(pos,s);}
 public void setPlacedBy(Level w,BlockPos pos,BlockState s,LivingEntity e,ItemStack stack){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof FishingNetTile){FishingNetTile t=(FishingNetTile)w.getBlockEntity(pos);t.wear=stack.getDamageValue();t.owner=e.getUUID();t.setChanged();w.scheduleTick(pos,this,6000);}}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){if(w.isClientSide)return net.minecraft.world.ItemInteractionResult.SUCCESS;FishingNetTile t=(FishingNetTile)w.getBlockEntity(pos);if(t==null)return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(p.isSecondaryUseActive()){w.setBlock(pos,Blocks.WATER.defaultBlockState(),3);return net.minecraft.world.ItemInteractionResult.CONSUME;}boolean caught=false;for(int i=0;i<4;i++){ItemStack loot=t.catchItems.get(i);if(!loot.isEmpty()){t.catchItems.set(i,ItemStack.EMPTY);if(!p.getInventory().add(loot))p.drop(loot,false);caught=true;}}if(caught){org.slavicmyths.progression.Knowledge.award(p,"net_catch");w.playSound(null,pos,SoundEvents.FISHING_BOBBER_RETRIEVE,SoundSource.BLOCKS,.5F,.8F);}w.setBlock(pos,s.setValue(FILLED,false),3);t.setChanged();return net.minecraft.world.ItemInteractionResult.CONSUME;}
 public void tick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){FishingNetTile t=(FishingNetTile)w.getBlockEntity(p);if(t==null)return;w.scheduleTick(p,this,6000+r.nextInt(2400));int empty=-1;for(int i=0;i<4;i++)if(t.catchItems.get(i).isEmpty()){empty=i;break;}if(empty<0)return;
  int water=0,near=0;for(BlockPos q:BlockPos.betweenClosed(p.offset(-3,-1,-3),p.offset(3,1,3))){if(!w.hasChunkAt(q))continue;if(w.getFluidState(q).is(FluidTags.WATER))water++;if(!q.equals(p)&&w.getBlockState(q).is(this))near++;}if(water<20||r.nextInt(2+near*4)!=0)return;
  if(near>=3&&t.owner!=null&&WaterFishing.pressure(w,p,w.getPlayerByUUID(t.owner),8)){t.catchItems.clear();t.wear+=4;w.setBlock(p,s.setValue(FILLED,false),3);}else{t.catchItems.set(empty,new ItemStack(WaterFishing.fish(w,p,r)));t.wear++;w.setBlock(p,s.setValue(FILLED,true),3);}t.setChanged();if(t.wear>=32){w.playSound(null,p,SoundEvents.ITEM_BREAK,SoundSource.BLOCKS,.5F,.8F);w.setBlock(p,Blocks.WATER.defaultBlockState(),3);}
 }
 public void onRemove(BlockState s,Level w,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&!w.isClientSide&&w.getBlockEntity(p) instanceof FishingNetTile){FishingNetTile t=(FishingNetTile)w.getBlockEntity(p);for(int i=0;i<4;i++){popResource(w,p,t.catchItems.get(i));t.catchItems.set(i,ItemStack.EMPTY);}if(t.wear<32){ItemStack net=new ItemStack(ModItems.FISHING_NET.get());net.setDamageValue(t.wear);popResource(w,p,net);}}super.onRemove(s,w,p,next,moving);}
}
