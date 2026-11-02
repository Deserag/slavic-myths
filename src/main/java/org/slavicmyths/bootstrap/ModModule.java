package org.slavicmyths.bootstrap;

import net.neoforged.bus.api.IEventBus;

/** A feature module owns lifecycle listeners, never bus registration of DeferredRegisters. */
@FunctionalInterface
public interface ModModule {
    void register(IEventBus modBus);
}
