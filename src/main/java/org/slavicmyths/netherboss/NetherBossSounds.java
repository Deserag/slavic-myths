package org.slavicmyths.netherboss;
import java.util.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
/** Semantic vanilla foley cues, normal pitch. Bespoke boss vocal recordings remain unavailable.
 * Events are independent so approved recordings can replace their resource definitions later. */
public final class NetherBossSounds {
 private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new LinkedHashMap<>();
 static {for(String boss:List.of("morova","trizna"))for(String cue:List.of("ambient","windup","impact","zone","wave","summon","phase","hurt","death","ritual","trophy")){
  String id=boss+"_"+cue;EVENTS.put(id,ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id))));
 }}
 public static SoundEvent cue(boolean morova,String cue){return EVENTS.get((morova?"morova":"trizna")+"_"+cue).get();}
 public static void init(){}
 private NetherBossSounds(){}
}
