package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slavicmyths.sisters.SistersArmor;

/** The vanilla ten-icons armor bar caps visually at twenty; show actual attributes numerically. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class SistersArmorHud {
    @SubscribeEvent public static void draw(RenderGuiEvent.Post event) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui)return;
        boolean worn=false;
        for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})worn|=SistersArmor.wears(mc.player,slot);
        if(!worn)return;
        int armor=(int)Math.round(mc.player.getAttributeValue(Attributes.ARMOR));
        double toughness=mc.player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        String label="Защита: "+armor+"  Стойкость: "+String.format(java.util.Locale.ROOT,"%.1f",toughness);
        int x=Math.max(4,(event.getGuiGraphics().guiWidth()-mc.font.width(label))/2);
        event.getGuiGraphics().drawString(mc.font,label,x,event.getGuiGraphics().guiHeight()-64,0xFFE8D7FF,true);
    }
    private SistersArmorHud() {}
}
