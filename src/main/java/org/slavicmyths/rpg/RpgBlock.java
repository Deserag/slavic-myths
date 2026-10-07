package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
public final class RpgBlock extends Block {
 private final boolean anvil;
 public RpgBlock(Properties p,boolean anvil){super(p);this.anvil=anvil;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return anvil?Shapes.or(box(1,0,2,15,3,14),box(5,3,5,11,10,11),box(0,10,2,16,15,14)):Shapes.or(box(1,0,1,15,4,15),box(4,4,4,12,24,12));}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){
  if(!w.isClientSide){ServerPlayer sp=(ServerPlayer)p;if(!anvil){org.slavicmyths.navigation.NavigationManager.waystone(sp,pos);PathData.message(sp,"marker_set");}RpgNetwork.sync(sp);var opened=sp.openMenu(new SimpleMenuProvider((id,inv,player)->new RpgMenu(id,inv,pos,anvil),Component.translatable("block.slavicmyths."+(anvil?"runic_anvil":"path_stone"))),b->{b.writeBlockPos(pos);b.writeBoolean(anvil);});if(opened.isEmpty())sp.displayClientMessage(Component.translatable("rpg.slavicmyths.menu_unavailable"),false);}
  return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);
 }
}
