package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
public final class HalaSounds {
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new HashMap<>();
 static {for(String s:new String[]{"ambient","step","hurt","death","charge","beam","volley","orb","pillars","flash","dive","claw","ultimate"}){String id="hala_"+s;SOUNDS.put(s,ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id))));}}
 public static SoundEvent get(String key){return SOUNDS.get(key).get();}
 public static void init(){}
 private HalaSounds(){}
}
