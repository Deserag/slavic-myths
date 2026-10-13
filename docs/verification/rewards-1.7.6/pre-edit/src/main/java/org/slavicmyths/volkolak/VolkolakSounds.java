package org.slavicmyths.volkolak;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Explicit animation cues; no vanilla growl substitution. */
public final class VolkolakSounds {
 public static final List<String> NAMES=List.of("ambient","howl_short","alert","snarl","claw_windup","claw_swing","bite","pounce_grunt","land","stance_shift","grab_prepare","grab_bite","grab_release","hurt","phase_roar","fake_fall","curse_revive","escape_howl","bad_blood_apply","grave_rustle","grave_dig","skull_unearth","curse_cleared");
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new LinkedHashMap<>();
 static {for(String name:NAMES){var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","volkolak."+name);EVENTS.put(name,org.slavicmyths.registry.ModSounds.SOUNDS.register("volkolak."+name,()->SoundEvent.createVariableRangeEvent(id)));}}
 public static SoundEvent get(String name){return EVENTS.get(name).get();}
 public static void init(){}
 private VolkolakSounds(){}
}
