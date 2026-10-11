package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.military.GreyWolfStory;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class VolkolakTooltip {
 @SubscribeEvent public static void skull(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){if(!e.getItemStack().is(ModItems.CURSED_VOLKOLAK_SKULL.get()))return;var tag=e.getItemStack().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();boolean own=e.getEntity()!=null&&tag.hasUUID("Owner")&&tag.getUUID("Owner").equals(e.getEntity().getUUID());e.getToolTip().add(GreyWolfStory.text(own?"skull_bound":"skull_foreign").withStyle(own?net.minecraft.ChatFormatting.GRAY:net.minecraft.ChatFormatting.RED));}
 @SubscribeEvent public static void blood(net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent e){if(e.getEffectInstance().is(org.slavicmyths.registry.ModEffects.BAD_BLOOD))e.getTooltip().add(GreyWolfStory.text("bad_blood_help"));}
 private VolkolakTooltip(){}
}
