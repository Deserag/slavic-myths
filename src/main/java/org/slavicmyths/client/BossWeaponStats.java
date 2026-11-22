package org.slavicmyths.client;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class BossWeaponStats {
 @SubscribeEvent public static void tooltip(ItemTooltipEvent event){
  var key=BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());if(!key.getNamespace().equals("slavicmyths"))return;
  String suffix=switch(key.getPath()){
   case "morozko_staff"->"frost";case "morovoy_staff"->"mor";
   case "storm_anchor"->"anchor";case "abyss_staff"->"abyss";
   case "pool_spear"->"pool";case "pharaohka_trident"->"trident";default->null;};
  if(suffix!=null)event.getToolTip().add(Component.translatable("tooltip.slavicmyths.boss_stats."+suffix));
 }
 private BossWeaponStats(){}
}
