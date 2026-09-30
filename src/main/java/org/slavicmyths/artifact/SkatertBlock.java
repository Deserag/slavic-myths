package org.slavicmyths.artifact;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
public final class SkatertBlock extends Block {
 public static final IntegerProperty PORTIONS=IntegerProperty.create("portions",0,6);
 public SkatertBlock(){super(Properties.of(Material.WOOL).strength(.5F).sound(SoundType.WOOL).noOcclusion());registerDefaultState(stateDefinition.any().setValue(PORTIONS,6));}
 protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> b){b.add(PORTIONS);}
 public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return box(0,0,0,16,2,16);}
 public boolean hasTileEntity(BlockState s){return true;}public TileEntity createTileEntity(BlockState s,IBlockReader w){return new SkatertTile();}
 public boolean canSurvive(BlockState s,IWorldReader w,BlockPos p){return w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP);}
 public BlockState updateShape(BlockState s,Direction d,BlockState n,IWorld w,BlockPos p,BlockPos q){return d==Direction.DOWN&&!canSurvive(s,w,p)?Blocks.AIR.defaultBlockState():s;}
 public void setPlacedBy(World w,BlockPos pos,BlockState s,LivingEntity who,ItemStack stack){if(!w.isClientSide&&w.getBlockEntity(pos) instanceof SkatertTile){SkatertTile t=(SkatertTile)w.getBlockEntity(pos);t.cloth=stack.copy();t.cloth.setCount(1);t.expires=ArtifactEvents.now(w)+2400;t.setChanged();w.getBlockTicks().scheduleTick(pos,this,2400);if(who instanceof PlayerEntity)org.slavicmyths.progression.Knowledge.award((PlayerEntity)who,"table_is_set");}}
 public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand hand,BlockRayTraceResult hit){if(w.isClientSide)return ActionResultType.SUCCESS;if(p.isSecondaryUseActive()){w.removeBlock(pos,false);return ActionResultType.CONSUME;}int portions=s.getValue(PORTIONS);if(portions>0&&p.canEat(false)&&!p.getCooldowns().isOnCooldown(org.slavicmyths.registry.ModItems.SKATERT.get())){p.getFoodData().eat(6,.6F);p.getCooldowns().addCooldown(org.slavicmyths.registry.ModItems.SKATERT.get(),24);w.setBlock(pos,s.setValue(PORTIONS,portions-1),3);w.playSound(null,pos,SoundEvents.GENERIC_EAT,SoundCategory.PLAYERS,.65F,1);((ServerWorld)w).sendParticles(new net.minecraft.particles.ItemParticleData(net.minecraft.particles.ParticleTypes.ITEM,new ItemStack(org.slavicmyths.registry.ModItems.PANCAKES.get())),p.getX(),p.getEyeY()-.2,p.getZ(),4,.1,.1,.1,.015);}return ActionResultType.CONSUME;}
 public void tick(BlockState s,ServerWorld w,BlockPos p,Random r){TileEntity t=w.getBlockEntity(p);if(t instanceof SkatertTile){long remaining=((SkatertTile)t).expires-ArtifactEvents.now(w);if(remaining<=0)w.removeBlock(p,false);else w.getBlockTicks().scheduleTick(p,this,(int)Math.min(2400,remaining));}}
 public void onRemove(BlockState s,World w,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&!w.isClientSide){TileEntity te=w.getBlockEntity(p);if(te instanceof SkatertTile){SkatertTile t=(SkatertTile)te;ItemStack result=t.cloth.copy();t.cloth=ItemStack.EMPTY;if(!result.isEmpty()){result.getOrCreateTag().putLong("SlavicClothReady",ArtifactEvents.now(w)+6000);popResource(w,p,result);}}}super.onRemove(s,w,p,next,moving);}
}
