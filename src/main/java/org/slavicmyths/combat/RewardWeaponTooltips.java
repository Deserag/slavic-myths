package org.slavicmyths.combat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class RewardWeaponTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
        var id=BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem());
        if(!id.getNamespace().equals("slavicmyths"))return;
        var raw=RewardWeaponBalance.RAW.get(id.getPath());
        if(raw!=null)e.getToolTip().add(Component.translatable("tooltip.slavicmyths.reward_raw",raw));
    }
    private RewardWeaponTooltips(){}
}
