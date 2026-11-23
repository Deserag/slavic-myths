package org.slavicmyths.auka;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/** Summoned companions do not drop loot or experience. */
@EventBusSubscriber(modid = "slavicmyths")
public final class AukaEvents {
    @SubscribeEvent public static void drops(LivingDropsEvent event) {
        if (event.getEntity() instanceof AukaMinion) event.getDrops().clear();
    }
    @SubscribeEvent public static void experience(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof AukaMinion) event.setDroppedExperience(0);
    }
    private AukaEvents() {}
}
