package org.slavicmyths.farming;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import org.slavicmyths.registry.*;

/** Repeatable Nether cultivation on its native substrate, without water or daylight. */
public class NetherPlant extends CropBlock {
 public final int kind; // 0: berries, 1: sernica, 2: plum sapling, 3: fruiting foliage
 public NetherPlant(int kind,Properties properties){super(properties);this.kind=kind;}
 @Override public MapCodec<? extends CropBlock> codec(){return simpleCodec(p->new NetherPlant(kind,p));}
 @Override public int getMaxAge(){return 3;}
 @Override protected net.minecraft.world.level.block.state.properties.IntegerProperty getAgeProperty(){return net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_3;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_3);}
 @Override protected ItemLike getBaseSeedId(){return kind==0?ModItems.HEAT_BERRY_CUTTING.get():kind==1?ModItems.SERNICA_SEEDS.get():ModItems.COAL_PLUM_SAPLING.get();}
 @Override protected boolean mayPlaceOn(BlockState s,BlockGetter w,BlockPos p){return kind==3||s.is(kind==1?Blocks.SOUL_SOIL:Blocks.CRIMSON_NYLIUM);}
 @Override public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){return kind==3||w.getBlockState(p.below()).is(kind==1?Blocks.SOUL_SOIL:Blocks.CRIMSON_NYLIUM);}
 @Override protected int getBonemealAgeIncrease(Level w){return 1;}
 @Override public boolean isValidBonemealTarget(LevelReader w,BlockPos p,BlockState s){return canSurvive(s,w,p)&&(kind==2?treeSpace(w,p):!isMaxAge(s));}
 @Override public void performBonemeal(ServerLevel w,RandomSource r,BlockPos p,BlockState s){if(!isValidBonemealTarget(w,p,s))return;if(kind==2)growTree(w,p);else w.setBlock(p,getStateForAge(Math.min(3,getAge(s)+1)),3);}
 @Override public void randomTick(BlockState s,ServerLevel w,BlockPos p,RandomSource r){if(r.nextInt(12)==0){if(kind==2){if(treeSpace(w,p))growTree(w,p);}else if(!isMaxAge(s)&&canSurvive(s,w,p))w.setBlock(p,getStateForAge(getAge(s)+1),2);}}
 public int harvestedAge(){return kind==0?2:1;}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
  if(kind!=0&&kind!=3||!isMaxAge(s)||!w.mayInteract(player,p)||!player.mayUseItemAt(p,hit.getDirection(),player.getMainHandItem()))return InteractionResult.PASS;
  if(w instanceof ServerLevel server){var drops=Block.getDrops(s,server,p,null,player,ItemStack.EMPTY);if(server.setBlock(p,getStateForAge(harvestedAge()),3))for(var stack:drops)Block.popResource(server,p,stack);}
  return InteractionResult.sidedSuccess(w.isClientSide);
 }
 private boolean treeSpace(LevelReader w,BlockPos p){for(int y=1;y<=7;y++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){var q=p.offset(x,y,z);if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q)||!w.getBlockState(q).isAir())return false;}return true;}
 private void growTree(ServerLevel w,BlockPos p){if(!treeSpace(w,p))return;int height=5+w.random.nextInt(3);for(int y=0;y<height;y++)w.setBlock(p.above(y),Blocks.CRIMSON_STEM.defaultBlockState(),3);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)w.setBlock(p.offset(x,height-1,z),ModBlocks.COAL_PLUM_LEAVES.get().defaultBlockState(),3);w.setBlock(p.above(height),ModBlocks.COAL_PLUM_LEAVES.get().defaultBlockState(),3);}
}