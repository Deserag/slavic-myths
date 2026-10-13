package org.slavicmyths.wood;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
public final class TimberLog extends RotatedPillarBlock {
 private final Woodlands.Set set; private final boolean wood,stripped;
 public TimberLog(Woodlands.Set s,boolean w,boolean t){super(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG));set=s;wood=w;stripped=t;}
 @Override public BlockState getToolModifiedState(BlockState state,UseOnContext context,ItemAbility ability,boolean simulate){
  if(ability==ItemAbilities.AXE_STRIP&&!stripped&&context.getItemInHand().canPerformAction(ability))return set.get(wood?"stripped_wood":"stripped_log").defaultBlockState().setValue(AXIS,state.getValue(AXIS));
  return super.getToolModifiedState(state,context,ability,simulate);
 }
@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}
}
