package org.slavicmyths.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.slavicmyths.savanna.SteppeWeapons;

/** Warm tint over the exact vanilla mace/crossbow pixel silhouettes. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class SteppeVanillaColors {
    private static final int BRONZE = 0xFFFFCF80;

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? BRONZE : -1,
                SteppeWeapons.item(SteppeWeapons.Kind.STEPPE_MACE),
                SteppeWeapons.item(SteppeWeapons.Kind.STEPPE_CROSSBOW));
    }

    private SteppeVanillaColors() {}
}

