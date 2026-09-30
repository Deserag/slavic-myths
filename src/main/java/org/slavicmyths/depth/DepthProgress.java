package org.slavicmyths.depth;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class DepthProgress {
 @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent e){net.minecraft.item.Item i=e.getCrafting().getItem();if(i==ModItems.POOL_SPEAR.get()||i==ModItems.DEPTH_AMULET.get()||i==ModItems.VODYANOY_NET.get())org.slavicmyths.progression.Knowledge.award(e.getPlayer(),"depth_gift");}
}
