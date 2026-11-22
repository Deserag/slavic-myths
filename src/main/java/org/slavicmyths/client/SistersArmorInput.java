package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.sisters.SistersArmor;
import org.slavicmyths.sisters.SistersArmorNetwork;

/** One main-hand intent works on blocks and in air without activating twice. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SistersArmorInput {
    @SubscribeEvent public static void use(InputEvent.InteractionKeyMappingTriggered event){
        var mc=Minecraft.getInstance();
        if(!event.isUseItem()||event.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND
                ||mc.player==null||mc.screen!=null||!mc.player.isAlive()||mc.player.isSpectator()
                ||!mc.player.isShiftKeyDown()||!mc.player.getMainHandItem().isEmpty()||!SistersArmor.full(mc.player))return;
        event.setCanceled(true);event.setSwingHand(false);
        PacketDistributor.sendToServer(new SistersArmorNetwork.Aura());
    }
    private SistersArmorInput(){}
}
