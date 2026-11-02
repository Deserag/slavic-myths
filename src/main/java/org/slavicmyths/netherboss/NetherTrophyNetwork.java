
package org.slavicmyths.netherboss;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slavicmyths.network.MythPayloads;

public final class NetherTrophyNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2.3").playToServer(
            MythPayloads.NetherTrophy.TYPE,
            MythPayloads.NetherTrophy.CODEC,
            (payload, context) -> {
                if (context.player() instanceof ServerPlayer player) {
                    NetherTrophies.activateEquipped(player, payload.pendant());
                }
            }
        );
    }

    private NetherTrophyNetwork() {
    }
}
