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
    public static final java.util.Set<String> REACH_ITEMS=java.util.Set.of("ancient_spear","restored_spear","thunder_spear");
    /** Explicit owner approval 22.10.2026: ordinary player reach 3 + 1.25. */
    public static net.minecraft.world.item.component.ItemAttributeModifiers spearReach(net.minecraft.world.item.component.ItemAttributeModifiers original){
        return original.withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE,
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","spear_reach"),1.25,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
    }
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
        REACH_ITEMS.forEach(id->{
            var item=BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).orElseThrow();
            event.modify(item,patch->patch.set(DataComponents.ATTRIBUTE_MODIFIERS,spearReach(item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY))));
        });
    }

    private WeaponDefaults() {}
}
