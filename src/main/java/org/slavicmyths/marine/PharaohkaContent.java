package org.slavicmyths.marine;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Sea monsters: isolated registration. No loot or equipment is introduced here. */
public final class PharaohkaContent {
    public static final DeferredHolder<EntityType<?>, EntityType<PharaohkaEntity>> PHARAOHKA =
        ModEntities.ENTITIES.register("pharaohka", () ->
            EntityType.Builder.of(PharaohkaEntity::new, MobCategory.MONSTER)
                .sized(0.9F, 2.15F).clientTrackingRange(14).updateInterval(2).build("slavicmyths:pharaohka"));

    public static final DeferredHolder<EntityType<?>, EntityType<PharaohkaEntity>> LEADER =
        ModEntities.ENTITIES.register("pharaohka_leader", () ->
            EntityType.Builder.of(PharaohkaEntity::new, MobCategory.MONSTER)
                .sized(1.45F, 3.8F).clientTrackingRange(24).updateInterval(1).build("slavicmyths:pharaohka_leader"));

    public static final DeferredHolder<Item, Item> EGG =
        ModItems.ITEMS.register("pharaohka_spawn_egg", () ->
            new net.neoforged.neoforge.common.DeferredSpawnEggItem(PHARAOHKA, 0x15323E, 0x62A9A6, new Item.Properties()));

    public static final DeferredHolder<Item, Item> LEADER_EGG =
        ModItems.ITEMS.register("pharaohka_leader_spawn_egg", () ->
            new net.neoforged.neoforge.common.DeferredSpawnEggItem(LEADER, 0x2D4553, 0xD2B474, new Item.Properties()));

    public static void init(IEventBus bus) {
        bus.addListener(PharaohkaContent::attributes);
        bus.addListener(PharaohkaContent::creativeTab);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(PHARAOHKA.get(), PharaohkaEntity.attributes(false).build());
        event.put(LEADER.get(), PharaohkaEntity.attributes(true).build());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(EGG.get());
            event.accept(LEADER_EGG.get());
        }
    }
    private PharaohkaContent() { }
}