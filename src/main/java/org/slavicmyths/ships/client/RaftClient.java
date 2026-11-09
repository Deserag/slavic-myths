package org.slavicmyths.ships.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.ships.RaftContent;

public final class RaftClient {
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RaftContent.RAFT.get(), RaftRenderer::new);
        event.registerEntityRenderer(RaftContent.FIRST_SHIP.get(), FirstShipRenderer::new);
    }
    /** Sail level is a real item component; one base item can render several equipped variants. */
    public static void itemProperties() {
        net.minecraft.resources.ResourceLocation sails =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths", "sails");
        for (org.slavicmyths.ships.RaftSize size : org.slavicmyths.ships.RaftSize.values()) {
            net.minecraft.client.renderer.item.ItemProperties.register(
                    RaftContent.raftItem(size), sails,
                    (stack, level, living, seed) -> RaftContent.sailPreset(stack));
        }
    }
    private RaftClient() {}
}
