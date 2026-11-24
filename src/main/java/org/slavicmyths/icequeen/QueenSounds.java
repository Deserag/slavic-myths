package org.slavicmyths.icequeen;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModSounds;

/** Server-authoritative cues for the Queen's trial. */
public final class QueenSounds {
    public enum Cue {
        AMBIENT("ambient", 24, 0.32F, 0.9F),
        TRIAL_START("trial_start", 32, 1F, 0.75F),
        ATTACK_CHARGE("attack_charge", 32, 0.85F, 1.1F),
        ATTACK_HIT("attack_hit", 24, 0.95F, 0.8F),
        MAGIC_CAST("magic_cast", 32, 1F, 1.05F),
        SUMMON("summon", 32, 1F, 0.9F),
        MINION_ARRIVAL("minion_arrival", 24, 0.65F, 1.15F),
        PHASE_TRANSITION("phase_transition", 48, 1.2F, 0.8F),
        HURT("hurt", 24, 1F, 1F),
        DEATH("death", 32, 1F, 0.65F),
        STEP("step", 16, 0.42F, 1.1F),
        STORM_START("storm_start", 48, 1F, 0.85F),
        STORM_LOOP("storm_loop", 32, 0.45F, 1F),
        GUARD("guard", 24, 0.9F, 1.1F),
        GUARD_HIT("guard_hit", 24, 1F, 1.05F),
        YIELD("yield", 32, 1F, 0.85F),
        MINION_HIT("minion_hit", 20, 0.7F, 1.15F);
        public final String id;
        final int range;
        final float volume, pitch;
        Cue(String id, int range, float volume, float pitch) {
            this.id = "ice_queen." + id;
            this.range = range;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
    private static final Map<Cue, DeferredHolder<SoundEvent, SoundEvent>> EVENTS = new EnumMap<>(Cue.class);
    static {
        for (Cue cue : Cue.values()) {
            EVENTS.put(cue, ModSounds.SOUNDS.register(cue.id, () -> SoundEvent.createFixedRangeEvent(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", cue.id), cue.range)));
        }
    }
    static void init() {}
    public static SoundEvent event(Cue cue) { return EVENTS.get(cue).get(); }
    public static void play(Entity entity, Cue cue) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), event(cue),
            SoundSource.HOSTILE, cue.volume, cue.pitch);
    }
    private QueenSounds() {}
}
