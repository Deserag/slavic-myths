package org.slavicmyths.ships.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.ships.RaftContent;

public final class RaftClient {
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RaftContent.RAFT.get(), RaftRenderer::new);
    }
    private RaftClient() {}
}
