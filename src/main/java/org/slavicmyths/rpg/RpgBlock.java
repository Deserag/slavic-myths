package org.slavicmyths.rpg;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
public final class RpgBlock extends Block {
 private final boolean anvil;
 public RpgBlock(Properties p,boolean anvil){super(p);this.anvil=anvil;}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return anvil?VoxelShapes.or(box(1,0,2,15,3,14),box(5,3,5,11,10,11),box(0,10,2,16,15,14)):VoxelShapes.or(box(1,0,1,15,4,15),box(4,4,4,12,24,12));}
 @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity p,Hand h,BlockRayTraceResult hit){
  if(!w.isClientSide){ServerPlayerEntity sp=(ServerPlayerEntity)p;RpgNetwork.sync(sp);NetworkHooks.openGui(sp,new SimpleNamedContainerProvider((id,inv,player)->new RpgMenu(id,inv,pos,anvil),new TranslationTextComponent("block.slavicmyths."+(anvil?"runic_anvil":"path_stone"))),b->{b.writeBlockPos(pos);b.writeBoolean(anvil);});}
  return ActionResultType.sidedSuccess(w.isClientSide);
 }
}
