package org.slavicmyths.block;
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
import org.slavicmyths.kitchen.KitchenMenu;
public final class KitchenTableBlock extends Block {
 public KitchenTableBlock(){super(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion());}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return Shapes.or(box(0,11,0,16,14,16),box(1,0,1,4,11,4),box(12,0,1,15,11,4),box(1,0,12,4,11,15),box(12,0,12,15,11,15),box(2,3,2,14,5,14));}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){if(!w.isClientSide)((ServerPlayer)p).openMenu(new SimpleMenuProvider((id,inv,player)->new KitchenMenu(id,inv,pos),Component.translatable("block.slavicmyths.kitchen_table")),buffer -> buffer.writeBlockPos(pos));return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
}
