package org.slavicmyths.client.runeglow;

import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.rpg.Runes;

/** Called only for an entity whose equipment is actually being rendered.
 * No world/player tick scan, packets, NBT, retained entities or server timer.
 */
public final class ArmorRuneWave {
    private static final Map<LivingEntity, RuneWaveState> STATES = new WeakHashMap<>();
    private static WeakReference<Object> world = new WeakReference<>(null);
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private ArmorRuneWave() {}

    public static float phase(LivingEntity entity, float partial) {
        if (world.get() != entity.level()) {
            STATES.clear(); world = new WeakReference<>(entity.level());
        }
        boolean full = true;
        for (EquipmentSlot slot : SLOTS) {
            var stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || Runes.list(stack).isEmpty()) { full = false; break; }
        }
        return STATES.computeIfAbsent(entity, ignored -> new RuneWaveState())
                .observe(full, entity.tickCount + Math.clamp(partial, 0, 1), entity.isAlive());
    }
}
