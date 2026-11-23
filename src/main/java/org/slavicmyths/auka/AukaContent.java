package org.slavicmyths.auka;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Winter boss encounter: test-spawn only until its forest hut is approved. */
public final class AukaContent {
    public static final DeferredHolder<EntityType<?>, EntityType<AukaBoss>> AUKA =
        ModEntities.ENTITIES.register("auka", () -> EntityType.Builder.of(AukaBoss::new, MobCategory.MONSTER)
            .sized(1.45F, 3.1F).clientTrackingRange(16).updateInterval(2).build("slavicmyths:auka"));

    public static final DeferredHolder<EntityType<?>, EntityType<AukaMinion>> AUKA_BEAR =
        ModEntities.ENTITIES.register("auka_bear", () -> EntityType.Builder.<AukaMinion>of(
            (type, world) -> new AukaMinion(type, world, AukaMinion.Kind.BEAR), MobCategory.MONSTER)
            .sized(1.45F, 1.75F).clientTrackingRange(12).build("slavicmyths:auka_bear"));

    public static final DeferredHolder<EntityType<?>, EntityType<AukaMinion>> AUKA_WOLF =
        ModEntities.ENTITIES.register("auka_white_wolf", () -> EntityType.Builder.<AukaMinion>of(
            (type, world) -> new AukaMinion(type, world, AukaMinion.Kind.WOLF), MobCategory.MONSTER)
            .sized(.92F, 1.15F).clientTrackingRange(12).build("slavicmyths:auka_white_wolf"));

    public static final DeferredHolder<Item, Item> AUKA_EGG =
        ModItems.ITEMS.register("auka_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(
            AUKA, 0x5B4037, 0xDBE4E8, new Item.Properties()));

    public static void init(IEventBus bus) {
        bus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(AUKA.get(), AukaBoss.attributes().build());
            event.put(AUKA_BEAR.get(), AukaMinion.attributes(AukaMinion.Kind.BEAR).build());
            event.put(AUKA_WOLF.get(), AukaMinion.attributes(AukaMinion.Kind.WOLF).build());
        });
    }
    private AukaContent() {}
}
