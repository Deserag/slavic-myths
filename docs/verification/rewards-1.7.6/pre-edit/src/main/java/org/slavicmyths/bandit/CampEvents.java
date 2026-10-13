package org.slavicmyths.bandit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class CampEvents {
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void open(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide||!(e.getEntity() instanceof ServerPlayer)||e.getEntity().isSpectator()||e.getEntity().isSecondaryUseActive())return;
        BlockEntity tile=e.getLevel().getBlockEntity(e.getPos());if(tile!=null&&tile.getPersistentData().getBoolean("BanditLoot"))org.slavicmyths.progression.Knowledge.award(e.getEntity(),"stolen_goods");
    }
}
