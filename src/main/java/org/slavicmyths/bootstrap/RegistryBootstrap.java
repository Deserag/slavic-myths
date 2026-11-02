package org.slavicmyths.bootstrap;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Sole authority for attaching deferred registries to the NeoForge mod event bus. */
public final class RegistryBootstrap {
    private static final Set<DeferredRegister<?>> REGISTERED = Collections.newSetFromMap(new IdentityHashMap<>());
    private RegistryBootstrap() { }
    public static synchronized void register(IEventBus modBus, DeferredRegister<?> registry) {
        if (!REGISTERED.add(registry)) throw new IllegalStateException("Duplicate registry bootstrap: " + registry);
        registry.register(modBus);
    }
}
