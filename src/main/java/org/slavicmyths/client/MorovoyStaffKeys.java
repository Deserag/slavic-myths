package org.slavicmyths.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slavicmyths.morovoy.MorovoyStaff;

/** Controls are routed by the held item: Shift+RMB changes element, Shift+LMB casts its mode skill. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class MorovoyStaffKeys {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e) {
        if(!(e.getItemStack().getItem() instanceof MorovoyStaff.Staff))return;
        boolean fire=e.getItemStack().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getInt("MorovoyStaffMode")==1;
        e.getToolTip().add(Component.literal("ПКМ — магический заряд  •  Shift+ПКМ — смена стихии").withStyle(ChatFormatting.GRAY));
        e.getToolTip().add(Component.literal(fire?"Shift+ЛКМ — огненно-моровой удар":"Shift+ЛКМ — стая воронов").withStyle(ChatFormatting.AQUA));
    }

    private MorovoyStaffKeys(){}
}
