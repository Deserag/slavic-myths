package org.slavicmyths.worldgen;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** Plant-family replacement for the removed PLANT / REPLACEABLE_PLANT material checks. */
public final class Vegetation {
 public static boolean plant(BlockState state){Block block=state.getBlock();return block instanceof BushBlock||block instanceof GrowingPlantBlock||block instanceof VineBlock||block instanceof SugarCaneBlock;}
 private Vegetation(){}
}
