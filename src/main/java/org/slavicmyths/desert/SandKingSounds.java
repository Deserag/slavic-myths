package org.slavicmyths.desert;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;
public final class SandKingSounds {
 private static DeferredHolder<SoundEvent,SoundEvent> sound(String name){String id="sand_king_"+name;return ModSounds.SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
 public static final DeferredHolder<SoundEvent,SoundEvent> AMBIENT=sound("ambient"), ALERT=sound("alert"), HURT=sound("hurt"), DEATH=sound("death"), STEP=sound("step"), STING=sound("sting"), POISON=sound("poison"), CLAW=sound("claw"), JUMP=sound("jump"), LAND=sound("land"), SLAM_CHARGE=sound("slam_charge"), SLAM=sound("slam"), RAGE=sound("rage");
 public static void init(){}
 private SandKingSounds(){}
}
