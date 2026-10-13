package org.slavicmyths.yaga;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.*;
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
import net.minecraft.server.level.ServerLevel;
/** World-only cauldron service, no portable drop or block entity inventory. */
public final class YagaCauldronBlock extends Block{
 private static final VoxelShape SHAPE=Block.box(1,0,1,15,15,15);
 public YagaCauldronBlock(){super(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(-1,3600000).sound(SoundType.METAL).noOcclusion().noLootTable());}
 @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return SHAPE;}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(w.isClientSide)return net.minecraft.world.ItemInteractionResult.SUCCESS;if(!(player instanceof ServerPlayer))return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;YagaData d=YagaData.get((ServerLevel)w);if(d.npc==null)return net.minecraft.world.ItemInteractionResult.FAIL;net.minecraft.world.entity.Entity e=((ServerLevel)w).getEntity(d.npc);if(!(e instanceof BabaYaga))return net.minecraft.world.ItemInteractionResult.FAIL;BabaYaga y=(BabaYaga)e;if(y.home==null||!pos.equals(y.home.offset(-2,0,1))||player.distanceToSqr(y)>64)return net.minecraft.world.ItemInteractionResult.FAIL;YagaData.Progress p=d.progress(player.getUUID());if(p.intro<2||p.blocked(w.getGameTime()))return net.minecraft.world.ItemInteractionResult.FAIL;YagaMenu.open((ServerPlayer)player,y,3);y.gesture(4);return net.minecraft.world.ItemInteractionResult.CONSUME;}
}
