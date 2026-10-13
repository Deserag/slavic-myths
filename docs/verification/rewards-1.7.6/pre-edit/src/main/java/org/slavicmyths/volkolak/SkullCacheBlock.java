package org.slavicmyths.volkolak;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
/** Persistent interaction cache; never drops a freely transferable quest proof. */
public final class SkullCacheBlock extends Block {
 public SkullCacheBlock(){super(Properties.ofFullCopy(Blocks.COARSE_DIRT).strength(-1,3600000).noOcclusion().noLootTable());}
 @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){return Block.box(.8,0,.8,15.2,12,15.2);}
 @Override public InteractionResult useWithoutItem(BlockState state,Level world,BlockPos pos,Player player,BlockHitResult hit){if(player instanceof ServerPlayer p)return VolkolakStory.cache(p,pos)?InteractionResult.CONSUME:InteractionResult.FAIL;return InteractionResult.SUCCESS;}
}
