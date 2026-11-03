package org.slavicmyths.sisters;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original Tryaseya ghost effects; safe distinct registry namespace. */
public final class TryaseyaSounds {
    private static final DeferredRegister<SoundEvent> EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT,"slavicmyths");
    public static final DeferredHolder<SoundEvent,SoundEvent> AMBIENT = register("tryaseya_ambient");
    public static final DeferredHolder<SoundEvent,SoundEvent> CAST = register("tryaseya_cast");
    public static final DeferredHolder<SoundEvent,SoundEvent> IMPACT = register("tryaseya_impact");
    public static final DeferredHolder<SoundEvent,SoundEvent> HURT = register("tryaseya_hurt");
    public static final DeferredHolder<SoundEvent,SoundEvent> DEATH = register("tryaseya_death");
    public static final DeferredHolder<SoundEvent,SoundEvent> ENRAGE = register("tryaseya_enrage");
    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) { return EVENTS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",name))); }
    public static void init(IEventBus bus) { EVENTS.register(bus); }
    private TryaseyaSounds() {}
}
