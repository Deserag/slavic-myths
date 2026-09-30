package org.slavicmyths.water;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.*;
public final class FishingNetBlock extends Block {
 public static final BooleanProperty FILLED=BooleanProperty.create("filled");
 public FishingNetBlock(){super(Properties.of(Material.WOOD).strength(.6F).noOcclusion().noCollission().sound(SoundType.WOOL));registerDefaultState(stateDefinition.any().setValue(FILLED,false));}
 protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> b){b.add(FILLED);}
 public BlockState getStateForPlacement(BlockItemUseContext c){return c.getLevel().getFluidState(c.getClickedPos()).is(FluidTags.WATER)?defaultBlockState():null;}
 public FluidState getFluidState(BlockState s){return Fluids.WATER.getSource(false);}
 public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return box(0,0,6,16,15,10);}
 public boolean hasTileEntity(BlockState s){return true;}public TileEntity createTileEntity(BlockState s,IBlockReader w){return new FishingNetTile();}
 public void setPlacedBy(World w,BlockPos pos,BlockState s,LivingEntity e,ItemStack stack){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof FishingNetTile){FishingNetTile t=(FishingNetTile)w.getBlockEntity(pos);t.wear=stack.getDamageValue();t.owner=e.getUUID();t.setChanged();w.getBlockTicks().scheduleTick(pos,this,6000);}}
 public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand h,BlockRayTraceResult hit){if(w.isClientSide)return ActionResultType.SUCCESS;FishingNetTile t=(FishingNetTile)w.getBlockEntity(pos);if(t==null)return ActionResultType.PASS;if(p.isSecondaryUseActive()){w.setBlock(pos,Blocks.WATER.defaultBlockState(),3);return ActionResultType.CONSUME;}boolean caught=false;for(int i=0;i<4;i++){ItemStack loot=t.catchItems.get(i);if(!loot.isEmpty()){t.catchItems.set(i,ItemStack.EMPTY);if(!p.inventory.add(loot))p.drop(loot,false);caught=true;}}if(caught){org.slavicmyths.progression.Knowledge.award(p,"net_catch");w.playSound(null,pos,SoundEvents.FISHING_BOBBER_RETRIEVE,SoundCategory.BLOCKS,.5F,.8F);}w.setBlock(pos,s.setValue(FILLED,false),3);t.setChanged();return ActionResultType.CONSUME;}
 public void tick(BlockState s,ServerWorld w,BlockPos p,Random r){FishingNetTile t=(FishingNetTile)w.getBlockEntity(p);if(t==null)return;w.getBlockTicks().scheduleTick(p,this,6000+r.nextInt(2400));int empty=-1;for(int i=0;i<4;i++)if(t.catchItems.get(i).isEmpty()){empty=i;break;}if(empty<0)return;
  int water=0,near=0;for(BlockPos q:BlockPos.betweenClosed(p.offset(-3,-1,-3),p.offset(3,1,3))){if(!w.hasChunkAt(q))continue;if(w.getFluidState(q).is(FluidTags.WATER))water++;if(!q.equals(p)&&w.getBlockState(q).is(this))near++;}if(water<20||r.nextInt(2+near*4)!=0)return;
  if(near>=3&&t.owner!=null&&WaterFishing.pressure(w,p,w.getPlayerByUUID(t.owner),8)){t.catchItems.clear();t.wear+=4;w.setBlock(p,s.setValue(FILLED,false),3);}else{t.catchItems.set(empty,new ItemStack(WaterFishing.fish(w,p,r)));t.wear++;w.setBlock(p,s.setValue(FILLED,true),3);}t.setChanged();if(t.wear>=32){w.playSound(null,p,SoundEvents.ITEM_BREAK,SoundCategory.BLOCKS,.5F,.8F);w.setBlock(p,Blocks.WATER.defaultBlockState(),3);}
 }
 public void onRemove(BlockState s,World w,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&!w.isClientSide&&w.getBlockEntity(p) instanceof FishingNetTile){FishingNetTile t=(FishingNetTile)w.getBlockEntity(p);for(int i=0;i<4;i++){popResource(w,p,t.catchItems.get(i));t.catchItems.set(i,ItemStack.EMPTY);}if(t.wear<32){ItemStack net=new ItemStack(ModItems.FISHING_NET.get());net.setDamageValue(t.wear);popResource(w,p,net);}}super.onRemove(s,w,p,next,moving);}
}
