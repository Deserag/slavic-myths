package org.slavicmyths.depth;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
public final class PoolStoneBlock extends Block implements EntityBlock {
 public PoolStoneBlock(){super(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).strength(-1,3600000).noOcclusion());}
 public boolean hasTileEntity(BlockState s){return true;}public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState s){return new PoolStoneTile(pos,s);}
 @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){return useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND),s,w,p,player,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){if(!w.isClientSide&&w.getBlockEntity(p) instanceof PoolStoneTile){org.slavicmyths.progression.Knowledge.award(player,"find_deep_pool");((PoolStoneTile)w.getBlockEntity(p)).activate(player,h);}return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);}
 public void tick(BlockState s,ServerLevel w,BlockPos p,net.minecraft.util.RandomSource r){if(w.getBlockEntity(p) instanceof PoolStoneTile)((PoolStoneTile)w.getBlockEntity(p)).step();}
}
