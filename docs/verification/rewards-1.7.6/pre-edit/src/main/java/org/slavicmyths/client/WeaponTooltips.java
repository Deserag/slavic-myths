package org.slavicmyths.client;
import net.minecraft.network.chat.Component;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.SlavicMyths;

@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID, value = Dist.CLIENT)
public final class WeaponTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id == null || !SlavicMyths.MOD_ID.equals(id.getNamespace())) return;
        String key = "tooltip.slavicmyths.weapon." + id.getPath();
        if (I18n.exists(key)) event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
    private WeaponTooltips() { }
}
