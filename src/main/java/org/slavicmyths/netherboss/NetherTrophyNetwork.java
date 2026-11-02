
package org.slavicmyths.netherboss;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slavicmyths.network.MythPayloads;

public final class NetherTrophyNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2.3");
        registrar.playToServer(
            MythPayloads.NetherTrophy.TYPE,
            MythPayloads.NetherTrophy.CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        NetherTrophies.activateEquipped(player, payload.pendant());
                    }
                });
            }
        );
    }

    private NetherTrophyNetwork() {
    }
}
