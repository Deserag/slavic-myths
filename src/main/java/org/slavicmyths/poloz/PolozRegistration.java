package org.slavicmyths.poloz;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class PolozRegistration {
 @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(PolozContent.BOSS.get(),GreatPoloz.attributes().build());}
}
