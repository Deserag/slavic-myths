package org.slavicmyths.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SlavicMyths.MOD_ID);
    public static final RegistryObject<SoundEvent> BANDIT_IDLE=sound("bandit_idle");
    public static final RegistryObject<SoundEvent> BANDIT_ALERT=sound("bandit_alert");
    public static final RegistryObject<SoundEvent> BANDIT_ATTACK=sound("bandit_attack");
    public static final RegistryObject<SoundEvent> BANDIT_HURT=sound("bandit_hurt");
    public static final RegistryObject<SoundEvent> BANDIT_DEATH=sound("bandit_death");
    public static final RegistryObject<SoundEvent> BANDIT_COMMAND=sound("bandit_command");
    public static final RegistryObject<SoundEvent> BANDIT_HEAVY=sound("bandit_heavy");
    public static final RegistryObject<SoundEvent> DOMOVOY_AMBIENT = sound("domovoy_ambient");
    public static final RegistryObject<SoundEvent> DOMOVOY_HURT = sound("domovoy_hurt");
    public static final RegistryObject<SoundEvent> DOMOVOY_DEATH = sound("domovoy_death");
    public static final RegistryObject<SoundEvent> LESHY_AMBIENT = sound("leshy_ambient");
    public static final RegistryObject<SoundEvent> LESHY_HURT = sound("leshy_hurt");
    public static final RegistryObject<SoundEvent> LESHY_DEATH = sound("leshy_death");
    public static final RegistryObject<SoundEvent> LESHY_ANGRY = sound("leshy_angry");
    public static final RegistryObject<SoundEvent> KIKIMORA_AMBIENT = sound("kikimora_ambient");
    public static final RegistryObject<SoundEvent> KIKIMORA_HURT = sound("kikimora_hurt");
    public static final RegistryObject<SoundEvent> KIKIMORA_DEATH = sound("kikimora_death");
    public static final RegistryObject<SoundEvent> KIKIMORA_ANGRY = sound("kikimora_angry");
    public static final RegistryObject<SoundEvent> KIKIMORA_POWER = sound("kikimora_power");
    public static final RegistryObject<SoundEvent> POLUDNITSA_AMBIENT = sound("poludnitsa_ambient");
    public static final RegistryObject<SoundEvent> POLUDNITSA_HURT = sound("poludnitsa_hurt");
    public static final RegistryObject<SoundEvent> POLUDNITSA_DEATH = sound("poludnitsa_death");
    public static final RegistryObject<SoundEvent> POLUDNITSA_ANGRY = sound("poludnitsa_angry");
    public static final RegistryObject<SoundEvent> POLUDNITSA_POWER = sound("poludnitsa_power");
    public static final RegistryObject<SoundEvent> POLEVIK_AMBIENT = sound("polevik_ambient");
    public static final RegistryObject<SoundEvent> POLEVIK_HURT = sound("polevik_hurt");
    public static final RegistryObject<SoundEvent> POLEVIK_DEATH = sound("polevik_death");
    public static final RegistryObject<SoundEvent> POLEVIK_ANGRY = sound("polevik_angry");
    public static final RegistryObject<SoundEvent> POLEVIK_POWER = sound("polevik_power");
    public static final RegistryObject<SoundEvent> BANNIK_AMBIENT = sound("bannik_ambient");
    public static final RegistryObject<SoundEvent> BANNIK_HURT = sound("bannik_hurt");
    public static final RegistryObject<SoundEvent> BANNIK_DEATH = sound("bannik_death");
    public static final RegistryObject<SoundEvent> BANNIK_ANGRY = sound("bannik_angry");
    public static final RegistryObject<SoundEvent> BANNIK_POWER = sound("bannik_power");
    public static final RegistryObject<SoundEvent> IGOSHA_AMBIENT = sound("igosha_ambient");
    public static final RegistryObject<SoundEvent> IGOSHA_HURT = sound("igosha_hurt");
    public static final RegistryObject<SoundEvent> IGOSHA_DEATH = sound("igosha_death");
    public static final RegistryObject<SoundEvent> IGOSHA_ANGRY = sound("igosha_angry");
    public static final RegistryObject<SoundEvent> IGOSHA_POWER = sound("igosha_power");
    public static final RegistryObject<SoundEvent> OVINNIK_AMBIENT = sound("ovinnik_ambient");
    public static final RegistryObject<SoundEvent> OVINNIK_HURT = sound("ovinnik_hurt");
    public static final RegistryObject<SoundEvent> OVINNIK_DEATH = sound("ovinnik_death");
    public static final RegistryObject<SoundEvent> OVINNIK_ANGRY = sound("ovinnik_angry");
    public static final RegistryObject<SoundEvent> OVINNIK_POWER = sound("ovinnik_power");
    public static final RegistryObject<SoundEvent> POLUDNITSA_NOTICE = sound("poludnitsa_notice");
    public static final RegistryObject<SoundEvent> POLUDNITSA_TRANSFORM = sound("poludnitsa_transform");
    public static final RegistryObject<SoundEvent> POLUDNITSA_ATTACK = sound("poludnitsa_attack");
    public static final RegistryObject<SoundEvent> POLEVIK_STEP = sound("polevik_step");
    public static final RegistryObject<SoundEvent> BANNIK_STEP = sound("bannik_step");
    public static final RegistryObject<SoundEvent> BANNIK_ATTACK = sound("bannik_attack");
    public static final RegistryObject<SoundEvent> BANNIK_STEAM = sound("bannik_steam");
    public static final RegistryObject<SoundEvent> KIKIMORA_NOTICE = sound("kikimora_notice");
    public static final RegistryObject<SoundEvent> KIKIMORA_LAUGH = sound("kikimora_laugh");
    public static final RegistryObject<SoundEvent> KIKIMORA_ATTACK = sound("kikimora_attack");
    public static final RegistryObject<SoundEvent> KIKIMORA_STEP = sound("kikimora_step");
    static {
        for(String animal:new String[]{"bear","cub","wolf","boar","stag","doe"})
            for(String event:new String[]{"ambient","hurt","death","alert","roar","attack","impact"}) sound(animal+"_"+event);
    }
    private static RegistryObject<SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(SlavicMyths.MOD_ID, id)));
    }
    public static final RegistryObject<SoundEvent> GUSLI_LOOP = sound("gusli_loop");
    public static final RegistryObject<SoundEvent> VODYANOY_AMBIENT=sound("vodyanoy_ambient"),VODYANOY_ANGRY=sound("vodyanoy_angry"),VODYANOY_HURT=sound("vodyanoy_hurt"),VODYANOY_DEATH=sound("vodyanoy_death"),RUSALKA_AMBIENT=sound("rusalka_ambient"),RUSALKA_ANGRY=sound("rusalka_angry"),RUSALKA_HURT=sound("rusalka_hurt"),RUSALKA_DEATH=sound("rusalka_death"),RUSALKA_SONG=sound("rusalka_song");
    public static final RegistryObject<SoundEvent> ELDER_IDLE=sound("elder_idle"),ELDER_HURT=sound("elder_hurt"),ELDER_ATTACK=sound("elder_attack"),ELDER_HEAVY=sound("elder_heavy"),ELDER_DASH=sound("elder_dash"),ELDER_WAVE=sound("elder_wave"),ELDER_PULL=sound("elder_pull"),ELDER_PHASE=sound("elder_phase"),ELDER_DEATH=sound("elder_death");
    private ModSounds() { }
}
