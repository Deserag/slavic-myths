package org.slavicmyths.brewing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class BrewEvents {
 @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Post event){var s=event.getOriginalStack();if(s.getItem()instanceof BeverageItem)BeverageItem.obtained(event.getPlayer(),s);}
 private BrewEvents(){}
}
