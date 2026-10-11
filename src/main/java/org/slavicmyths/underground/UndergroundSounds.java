package org.slavicmyths.underground;
import java.util.*;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
public final class UndergroundSounds {
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {var actions=Map.of("kladovik",List.of("idle","hurt","death","impact","rage","burrow","stone","coins"),"large_podzemnik",List.of("idle","hurt","death","warning","impact","rage","charge"),"podzemnik",List.of("idle","hurt","death"),"chud",List.of("work","panic"));actions.forEach((mob,list)->{for(String action:list){String key="underground."+mob+"."+action;SOUNDS.put(mob+"."+action,ModSounds.SOUNDS.register(key,()->SoundEvent.createVariableRangeEvent(UndergroundMobs.id(key))));}});}
 public static void init(){}public static SoundEvent sound(String mob,String action){return SOUNDS.get(mob+"."+action).get();}
 private UndergroundSounds(){}
}
