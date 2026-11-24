package org.slavicmyths.wolverine;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

public final class WolverineContent {
    public static final DeferredHolder<EntityType<?>, EntityType<NorthernWolverine>> BOSS =
        ModEntities.ENTITIES.register("northern_wolverine",
            () -> EntityType.Builder.of(NorthernWolverine::new, MobCategory.MONSTER)
                .sized(2.35F, 2.8F).clientTrackingRange(20).updateInterval(1).build("slavicmyths:northern_wolverine"));
    public static final DeferredHolder<Item, Item> EGG =
        ModItems.ITEMS.register("northern_wolverine_spawn_egg",
            () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS, 0xF2F4F7, 0x414C62, new Item.Properties()));

    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(BOSS.get(), NorthernWolverine.attributes().build());
    }
    public static void init() {}
    private WolverineContent() {}
}
