package org.slavicmyths.navya;

import java.util.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;

/** Every custom cue is a documented reference to a valid vanilla event, no synthetic OGG. */
public final class NavyaSounds {
    public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> ALL=new LinkedHashMap<>();
    static {
        for(String name:List.of("navka","rootling","bone_crawler","navya_wisp","grove_guardian"))
            for(String cue:List.of("idle","hurt","death","step","warn","strike")) {
                if(name.equals("navya_wisp") && !List.of("idle","hurt","death").contains(cue))continue;
                if(name.equals("navka") && cue.equals("step"))continue;
                register(name+"."+cue);
            }
        for(String cue:List.of("roots_warn","roots_rise","roots_recede","shot_hit","trunk_lift","trunk_throw","trunk_hit","summon","phase"))register(cue);
    }
    private static void register(String cue) {
        ALL.put(cue,ModSounds.SOUNDS.register("navya."+cue,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths","navya."+cue))));
    }
    public static SoundEvent get(String cue) { return ALL.get(cue).get(); }
    public static void init() { }
    private NavyaSounds() { }
}
