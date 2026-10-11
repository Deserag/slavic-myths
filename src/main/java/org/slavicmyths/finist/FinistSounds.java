package org.slavicmyths.finist;
import java.util.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class FinistSounds {
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> ALL=new LinkedHashMap<>();
 static{for(String name:List.of("dialog_open","quest_select","dialog_page","quest_accept","quest_progress","quest_complete","reward_claim","reject","ring_activate","ring_pack_arrive","ring_expire","finist_magic_shot","finist_target_lock","finist_magic_impact","finist_inverted_impact","quiver_equip")){var id=ResourceLocation.fromNamespaceAndPath("slavicmyths","wolf_story."+name);ALL.put(name,org.slavicmyths.registry.ModSounds.SOUNDS.register("wolf_story."+name,()->SoundEvent.createVariableRangeEvent(id)));}}
 public static SoundEvent get(String s){return ALL.get(s).get();}public static void init(){}
 private FinistSounds(){}
}
