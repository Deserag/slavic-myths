package org.slavicmyths.furniture;
import net.minecraft.block.*;
import net.minecraft.state.*;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
public final class WardrobeBlock extends FurnitureBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public WardrobeBlock(){super("wardrobe");registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(HALF);}
 @Override public BlockState getStateForPlacement(BlockItemUseContext c){return c.getClickedPos().getY()<254&&c.getLevel().getBlockState(c.getClickedPos().above()).canBeReplaced(c)?super.getStateForPlacement(c):null;}
 @Override public void setPlacedBy(World w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){w.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
 @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,IWorld w,BlockPos p,BlockPos q){boolean lower=s.getValue(HALF)==DoubleBlockHalf.LOWER;if(d==(lower?Direction.UP:Direction.DOWN)&&(!other.is(this)||other.getValue(HALF)==s.getValue(HALF)))return Blocks.AIR.defaultBlockState();return super.updateShape(s,d,other,w,p,q);}
}
