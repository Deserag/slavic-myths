package org.slavicmyths.posad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
/** Issue416: new-city council roof. Reserved footprint and all functional floors remain intact. */
final class PosadHallDominant {
 static BlockState voxel(int x,int y,int z){
  if(x<42||x>70||z<12||z>34||y<12||y>17)return null;
  int distance=Math.abs(x-56),height=17-(distance+2)/3;
  boolean rib=z==12||z==17||z==23||z==29||z==34;
  if(y==height){
   if(distance==0)return (z==34?Blocks.CRYING_OBSIDIAN:Blocks.CHISELED_POLISHED_BLACKSTONE).defaultBlockState();
   if(distance%3!=2)return (rib?Blocks.POLISHED_BLACKSTONE_BRICKS:Blocks.NETHER_BRICKS).defaultBlockState();
   return (rib?Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS:Blocks.NETHER_BRICK_STAIRS).defaultBlockState().setValue(StairBlock.FACING,x<56?Direction.EAST:Direction.WEST);
  }
  if(y>height)return Blocks.AIR.defaultBlockState();
  // Masonry closes only the two end gables; the nave keeps its vaulted volume.
  if(z==12||z==34){
   if(y==14&&distance<=1)return Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState();
   return (distance==height-y?Blocks.POLISHED_BLACKSTONE_BRICKS:Blocks.NETHER_BRICKS).defaultBlockState();
  }
  return null;
 }
 private PosadHallDominant(){}
}
