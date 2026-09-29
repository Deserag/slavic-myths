package org.slavicmyths.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SlavicMyths.MOD_ID);
    public static final RegistryObject<SoundEvent> DOMOVOY_AMBIENT = sound("domovoy_ambient");
    public static final RegistryObject<SoundEvent> DOMOVOY_HURT = sound("domovoy_hurt");
    public static final RegistryObject<SoundEvent> DOMOVOY_DEATH = sound("domovoy_death");
    public static final RegistryObject<SoundEvent> LESHY_AMBIENT = sound("leshy_ambient");
    public static final RegistryObject<SoundEvent> LESHY_HURT = sound("leshy_hurt");
    public static final RegistryObject<SoundEvent> LESHY_DEATH = sound("leshy_death");
    public static final RegistryObject<SoundEvent> LESHY_ANGRY = sound("leshy_angry");
    private static RegistryObject<SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(SlavicMyths.MOD_ID, id)));
    }
    private ModSounds() { }
}
