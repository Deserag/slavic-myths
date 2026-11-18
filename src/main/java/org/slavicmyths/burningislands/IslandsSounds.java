package org.slavicmyths.burningislands;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
/** Explicit legal vanilla aliases: audible action cues, not claimed original boss recordings. */
public final class IslandsSounds {
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new HashMap<>();
 static {for(String id:List.of("idle","hurt","death","wake","phase","claws_charge","claws_release","fracture_charge","fracture_release","pull_charge","pull_release","hands_charge","hands_release","eruption_charge","eruption_release","ring_charge","ring_release"))EVENTS.put(id,ModSounds.SOUNDS.register("islands.magma_lord."+id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths","islands.magma_lord."+id))));}
 public static SoundEvent get(String id){return EVENTS.get(id).get();}
 public static void init(){}
 private IslandsSounds(){}
}
