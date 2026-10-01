package org.slavicmyths.wood;
import net.minecraft.block.*;
import net.minecraft.item.ItemUseContext;
import net.minecraftforge.common.ToolType;
public final class TimberLog extends RotatedPillarBlock {
 private final Woodlands.Set set; private final boolean wood,stripped;
 public TimberLog(Woodlands.Set s,boolean w,boolean t){super(AbstractBlock.Properties.copy(Blocks.OAK_LOG));set=s;wood=w;stripped=t;}
 @Override public BlockState getToolModifiedState(BlockState state,net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.entity.player.PlayerEntity player,net.minecraft.item.ItemStack stack,ToolType tool){
  if(tool==ToolType.AXE&&!stripped)return set.get(wood?"stripped_wood":"stripped_log").defaultBlockState().setValue(AXIS,state.getValue(AXIS));
  return super.getToolModifiedState(state,world,pos,player,stack,tool);
 }
@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}
}
