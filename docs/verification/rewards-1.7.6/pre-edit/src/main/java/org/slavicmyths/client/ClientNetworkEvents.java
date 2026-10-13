package org.slavicmyths.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.network.ClientPayloadEvents;

@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class ClientNetworkEvents {
    @SubscribeEvent public static void lore(ClientPayloadEvents.LoreOpened event) {
        LoreScreen.open(event.payload.mask());
    }
    @SubscribeEvent public static void rpg(ClientPayloadEvents.RpgSynced event) {
        RpgClient.sync(event.payload.data(), event.payload.xp());
    }
    private ClientNetworkEvents() { }
}
