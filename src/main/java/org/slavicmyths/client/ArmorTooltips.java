package org.slavicmyths.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Shared equipment palette; native attribute/enchantment rows retain their semantic formatting. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class ArmorTooltips {
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void tooltip(ItemTooltipEvent event){
        var stack=event.getItemStack();var lines=event.getToolTip();
        if(!(stack.getItem() instanceof ArmorItem)||!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("slavicmyths")||lines.isEmpty())return;
        if(!stack.has(DataComponents.CUSTOM_NAME)){
            int color=stack.getRarity()==Rarity.EPIC?0xAA55FF:stack.getRarity()==Rarity.RARE?0x55FFFF:0xE4E4E7;
            lines.set(0,lines.get(0).copy().withStyle(style->style.withColor(color)));
        }
        for(int i=1;i<lines.size();i++){
            var line=lines.get(i);
            if(!(line.getContents() instanceof TranslatableContents text))continue;
            String key=text.getKey();
            if(!(key.startsWith("item.slavicmyths.")||key.startsWith("armor.slavicmyths.")
                    ||key.startsWith("netherarmor.")||key.startsWith("unique.slavicmyths.")
                    ||key.startsWith("armor_set_health.")||key.startsWith("poloz.")))continue;
            String clean=line.getString().replace("✦", "").replace("✧", "").strip();
            lines.set(i,WeaponTooltips.styledDescription(Component.literal(clean)));
        }
    }
    private ArmorTooltips(){}
}
