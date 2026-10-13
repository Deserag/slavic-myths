package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.winters.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WintersClient {
 @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){
  if(e.getEntity()!=null&&e.getItemStack().is(WintersContent.AMULET.get()))e.getToolTip().add(Component.translatable("winters.slavicmyths.mode",Component.translatable("winters.slavicmyths."+ (WintersEvents.dark(e.getEntity())?"karachun":"morozko"))));
 }
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Setup {
  @SubscribeEvent public static void ready(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.renderer.item.ItemProperties.register(WintersContent.AMULET.get(),ResourceLocation.fromNamespaceAndPath("slavicmyths","winter_mode"),(s,w,entity,seed)->entity instanceof net.minecraft.world.entity.player.Player p?(WintersEvents.dark(p)?2:1):0));}
 }
 private WintersClient(){}
}
