package org.slavicmyths.ships;

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

/** Medieval shipwright workbench. No Create technology or external dependency. */
public final class RaftShipyardBlock extends Block {
    public RaftShipyardBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).strength(3.5F).sound(SoundType.WOOD).noOcclusion());
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) server.openMenu(
                new SimpleMenuProvider((id, inventory, owner) -> new ShipyardMenu(id, inventory, pos), Component.literal("Корабельный верстак")),
                buf -> buf.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
