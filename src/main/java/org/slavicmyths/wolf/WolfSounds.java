package org.slavicmyths.wolf;
import java.util.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class WolfSounds {
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new LinkedHashMap<>();
 static {for(String n:List.of("ambient","growl","bite","claw","pounce","dash","summon_howl","pack_answer","step","hurt","trial_end","sit"))EVENTS.put(n,org.slavicmyths.registry.ModSounds.SOUNDS.register("grey_wolf."+n,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths","grey_wolf."+n))));}
 public static SoundEvent get(String name){return EVENTS.get(name).get();}public static void init(){}
 private WolfSounds(){}
}
