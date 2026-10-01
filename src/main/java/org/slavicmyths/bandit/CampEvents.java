package org.slavicmyths.bandit;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class CampEvents {
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void open(PlayerInteractEvent.RightClickBlock e){
        if(e.getWorld().isClientSide||!(e.getPlayer() instanceof ServerPlayerEntity)||e.getPlayer().isSpectator()||e.getPlayer().isSecondaryUseActive())return;
        TileEntity tile=e.getWorld().getBlockEntity(e.getPos());if(tile!=null&&tile.getTileData().getBoolean("BanditLoot"))org.slavicmyths.progression.Knowledge.award(e.getPlayer(),"stolen_goods");
    }
}
