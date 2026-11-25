package org.slavicmyths.desert;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
public final class AntSounds {
 private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){String id="ant_"+name;return ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
 public static final DeferredHolder<SoundEvent,SoundEvent> AMBIENT=sound("ambient"),ALERT=sound("alert"),HURT=sound("hurt"),DEATH=sound("death"),STEP=sound("step"),BITE=sound("bite"),CALL=sound("call");
 public static void init(){}
 private AntSounds(){}
}
