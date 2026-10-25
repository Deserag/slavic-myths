package org.slavicmyths.navya;

import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class NavyaSafety {
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void target(LivingChangeTargetEvent event) {
        if(event.getNewAboutToBeSetTarget() instanceof NavyaMob mob && mob.kind==NavyaContent.Kind.WISP)
            event.setNewAboutToBeSetTarget(null);
    }
    private NavyaSafety() { }
}
