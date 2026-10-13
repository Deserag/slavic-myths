package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.minecraft.network.chat.Component;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GreyWolfEffectTooltip {
 @SubscribeEvent public static void tooltip(GatherEffectScreenTooltipsEvent e){if(e.getEffectInstance().is(org.slavicmyths.registry.ModEffects.WOLF_CUT))e.getTooltip().add(Component.translatable("wolf.slavicmyths.cut_hint"));}
 private GreyWolfEffectTooltip(){}
}
