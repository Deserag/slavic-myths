package org.slavicmyths.blackcinder;
import java.util.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
/** Scoped vanilla aliases, with explicit source events in sounds.json. */
public final class CinderSounds {
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new LinkedHashMap<>();
 static {for(String name:List.of("crawler_warn","crawler_hit","spirit_warn","spirit_pulse","pekelnik_warn","pekelnik_throw","spark_idle","ring","spikes","shift","vortex","phase","death","hurt","mantle")){var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","cinder."+name);EVENTS.put(name,ModSounds.SOUNDS.register("cinder."+name,()->SoundEvent.createVariableRangeEvent(id)));}}
 public static SoundEvent get(String name){return EVENTS.get(name).get();}
 public static void init(){}
 private CinderSounds(){}
}
