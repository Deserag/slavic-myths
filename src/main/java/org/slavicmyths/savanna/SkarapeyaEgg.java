package org.slavicmyths.savanna;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.slavicmyths.registry.ModEntities;
public final class SkarapeyaEgg extends DeferredSpawnEggItem {
 public SkarapeyaEgg(Properties p){super(ModEntities.SKARAPEYA,0x342721,0xB69B31,p);}
 @Override public Component getName(ItemStack stack){return Component.translatableWithFallback(getDescriptionId(),"Яйцо призыва Скарапеи");}
}
