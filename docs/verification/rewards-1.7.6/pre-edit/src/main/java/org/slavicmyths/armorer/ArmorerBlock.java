package org.slavicmyths.armorer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
public final class ArmorerBlock extends Block {
    public ArmorerBlock(){super(Properties.of().mapColor(MapColor.WOOD).strength(3).sound(SoundType.WOOD).noOcclusion());}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer serverPlayer)serverPlayer.openMenu(new SimpleMenuProvider((id,inventory,owner)->new ArmorerMenu(id,inventory,pos),Component.translatable("block.slavicmyths.armorer_table")),buffer->buffer.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
