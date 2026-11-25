package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
public final class LamyaSounds {
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {for(String s:new String[]{"ambient","spawn","hurt","death","crawl","charge","fire","solar","wind","claw","tail_sweep","tail_sting","drought","combo_fire_wind","combo_sun_wind","combo_fire_sun","ultimate","phase","impact","stagger"}){String id="lamya_"+s;SOUNDS.put(s,ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id))));}}
 public static SoundEvent get(String key){return SOUNDS.get(key).get();}
 public static void init(){}
 private LamyaSounds(){}
}
