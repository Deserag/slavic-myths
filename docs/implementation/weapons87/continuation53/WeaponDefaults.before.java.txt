package org.slavicmyths.combat;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/** Owner-approved #158/#159 package A. TieredItem otherwise overwrites Properties.durability. */
@EventBusSubscriber(modid="slavicmyths", bus=EventBusSubscriber.Bus.MOD)
public final class WeaponDefaults {
    public static final Map<String,Integer> DURABILITY = Map.of(
        "nightingale_dagger",700,
        "ancient_carolingian_sword",90,
        "restored_carolingian_sword",450,
        "ancient_spear",90,
        "restored_spear",380,
        "ancient_chekan",90,
        "chekan",400,
        "pool_spear",420);

    @SubscribeEvent public static void modify(ModifyDefaultComponentsEvent event) {
        DURABILITY.forEach((id,uses)->event.modify(
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).orElseThrow(),
            patch->patch.set(DataComponents.MAX_DAMAGE,uses)));
    }

    private WeaponDefaults() {}
}
