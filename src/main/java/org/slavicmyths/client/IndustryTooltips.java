package org.slavicmyths.client;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class IndustryTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e){if(e.getItemStack().is(org.slavicmyths.item.SilverCombat.SILVER))e.getToolTip().add(Component.translatable("industry.slavicmyths.silver_existing"));}
    private IndustryTooltips(){}
}
