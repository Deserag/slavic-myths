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
    @SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) {
        var mc=net.minecraft.client.Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null||mc.options.hideGui
                ||!(mc.player.getMainHandItem().getItem() instanceof MorovoyStaff.Staff))return;
        var tag=mc.player.getMainHandItem().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        boolean fire=tag.getInt("MorovoyStaffMode")==1;
        var g=e.getGuiGraphics();
        int x=Math.max(5,g.guiWidth()-210),y=g.guiHeight()-112;
        g.fill(x,y,x+205,y+43,0xB212121C);
        g.drawString(mc.font,fire?"ПОСОХ МОРА · ОГОНЬ":"ПОСОХ МОРА · МОР",x+6,y+4,fire?0xFFB85A:0xC58EED,false);
        g.drawString(mc.font,"Shift+ПКМ — стихия  •  ПКМ — выстрел",x+6,y+17,0xD0CED8,false);
        g.drawString(mc.font,fire?"Shift+ЛКМ — огненно-моровая буря":"Shift+ЛКМ — стая воронов",x+6,y+29,0xC8C8D0,false);
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent e) {
        if(!(e.getItemStack().getItem() instanceof MorovoyStaff.Staff))return;
        boolean fire=e.getItemStack().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getInt("MorovoyStaffMode")==1;
        e.getToolTip().add(Component.literal("ПКМ — магический заряд  •  Shift+ПКМ — смена стихии").withStyle(ChatFormatting.GRAY));
        e.getToolTip().add(Component.literal(fire?"Shift+ЛКМ — огненно-моровой удар":"Shift+ЛКМ — стая воронов").withStyle(ChatFormatting.AQUA));
    }

    private MorovoyStaffKeys(){}
}
