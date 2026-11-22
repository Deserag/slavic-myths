package org.slavicmyths.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slavicmyths.morovoy.MorovoyStaff;

/** Staff labels only. All four actions are routed through WeaponInputRouter. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT)
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
        g.drawString(mc.font,"ЛКМ — выстрел  •  ПКМ — стихия",x+6,y+17,0xD0CED8,false);
        g.drawString(mc.font,"Shift+ЛКМ — вороны  •  Shift+ПКМ — буря",x+6,y+29,0xC8C8D0,false);
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent e) {
        if(!(e.getItemStack().getItem() instanceof MorovoyStaff.Staff))return;
        var text=e.getToolTip();
        text.add(Component.literal("ЛКМ — магический заряд  •  ПКМ — смена стихии").withStyle(ChatFormatting.GRAY));
        text.add(Component.literal("Shift+ЛКМ — стая воронов  •  Shift+ПКМ — огненно-моровой удар").withStyle(ChatFormatting.AQUA));
    }

    private MorovoyStaffKeys(){}
}
