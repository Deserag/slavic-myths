package org.slavicmyths.furniture;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
public final class WardrobeBlock extends FurnitureBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public WardrobeBlock(){this(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}
 private WardrobeBlock(Properties properties){super("wardrobe",properties);registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
 private static final com.mojang.serialization.MapCodec<WardrobeBlock> CODEC=simpleCodec(WardrobeBlock::new);
 @Override protected com.mojang.serialization.MapCodec<WardrobeBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(HALF);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return c.getClickedPos().getY()<c.getLevel().getMaxBuildHeight()-2&&c.getLevel().getBlockState(c.getClickedPos().above()).canBeReplaced(c)?super.getStateForPlacement(c):null;}
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos q){boolean lower=s.getValue(HALF)==DoubleBlockHalf.LOWER;if(d==(lower?Direction.UP:Direction.DOWN)&&(!other.is(this)||other.getValue(HALF)==s.getValue(HALF)))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
}
