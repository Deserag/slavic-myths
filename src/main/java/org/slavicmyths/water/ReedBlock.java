package org.slavicmyths.water;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;
import net.minecraft.tags.FluidTags;
public final class ReedBlock extends BushBlock {
 public ReedBlock(){super(Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS));}
 public boolean canSurvive(BlockState s,IWorldReader w,BlockPos p){if(!super.canSurvive(s,w,p))return false;for(Direction d:Direction.Plane.HORIZONTAL)if(w.getFluidState(p.below().relative(d)).is(FluidTags.WATER))return true;return false;}
}
