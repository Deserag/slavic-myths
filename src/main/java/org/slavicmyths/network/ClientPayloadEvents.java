package org.slavicmyths.network;

import net.neoforged.bus.api.Event;

/** Notifications from main-thread client-bound payload handlers. */
public final class ClientPayloadEvents {
    public static final class LoreOpened extends Event {
        public final MythPayloads.LoreOpen payload;
        public LoreOpened(MythPayloads.LoreOpen payload) { this.payload = payload; }
    }
    public static final class RpgSynced extends Event {
        public final MythPayloads.RpgSync payload;
        public RpgSynced(MythPayloads.RpgSync payload) { this.payload = payload; }
    }
    private ClientPayloadEvents() { }
}
