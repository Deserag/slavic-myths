package org.slavicmyths.water;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.tags.FluidTags;
public final class ReedBlock extends BushBlock {
 public ReedBlock(){this(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS));}
 private ReedBlock(Properties properties){super(properties);}
 private static final com.mojang.serialization.MapCodec<ReedBlock> CODEC=simpleCodec(ReedBlock::new);
 @Override protected com.mojang.serialization.MapCodec<ReedBlock> codec(){return CODEC;}
 public boolean canSurvive(BlockState s,LevelReader w,BlockPos p){if(!super.canSurvive(s,w,p))return false;for(Direction d:Direction.Plane.HORIZONTAL)if(w.getFluidState(p.below().relative(d)).is(FluidTags.WATER))return true;return false;}
}
