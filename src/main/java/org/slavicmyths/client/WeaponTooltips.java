package org.slavicmyths.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID, value = Dist.CLIENT)
public final class WeaponTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ResourceLocation id = event.getItemStack().getItem().getRegistryName();
        if (id == null || !SlavicMyths.MOD_ID.equals(id.getNamespace())) return;
        String key = "tooltip.slavicmyths.weapon." + id.getPath();
        if (I18n.exists(key)) event.getToolTip().add(new TranslationTextComponent(key).withStyle(TextFormatting.GRAY));
    }
    private WeaponTooltips() { }
}
