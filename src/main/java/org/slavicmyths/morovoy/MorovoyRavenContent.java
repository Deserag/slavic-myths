package org.slavicmyths.morovoy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Runtime content only. Arena ownership/worldgen belongs to its separate implementation. */
public final class MorovoyRavenContent {
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("slavicmyths", path); }
    public static final DeferredHolder<EntityType<?>, EntityType<MorovoyRaven>> BOSS = ModEntities.ENTITIES.register("morovoy_raven", () -> EntityType.Builder.of(MorovoyRaven::new, MobCategory.MONSTER).sized(2.7F, 3.5F).clientTrackingRange(32).updateInterval(2).build("slavicmyths:morovoy_raven"));
    public static final DeferredHolder<EntityType<?>, EntityType<MorovoyRaven>> MINION = ModEntities.ENTITIES.register("morovoy_raven_minion", () -> EntityType.Builder.of(MorovoyRaven::new, MobCategory.MONSTER).sized(.8F, .9F).clientTrackingRange(16).updateInterval(2).build("slavicmyths:morovoy_raven_minion"));
    public static final DeferredHolder<EntityType<?>, EntityType<RavenBlightBolt>> BOLT = ModEntities.ENTITIES.register("morovoy_raven_blight", () -> EntityType.Builder.<RavenBlightBolt>of(RavenBlightBolt::new, MobCategory.MISC).sized(.42F, .42F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:morovoy_raven_blight"));
    public static final DeferredHolder<EntityType<?>, EntityType<RavenMiasma>> MIASMA = ModEntities.ENTITIES.register("morovoy_raven_miasma", () -> EntityType.Builder.<RavenMiasma>of(RavenMiasma::new, MobCategory.MISC).sized(.2F, .2F).clientTrackingRange(12).updateInterval(10).build("slavicmyths:morovoy_raven_miasma"));
    public static final DeferredHolder<Item, Item> FEATHER = ModItems.ITEMS.register("morovoy_raven_feather", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> CORE = ModItems.ITEMS.register("mor_core", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SPAWN_EGG = ModItems.ITEMS.register("morovoy_raven_spawn_egg", () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(BOSS, 0x24272A, 0x62B538, new Item.Properties()));
    public static void init(IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) -> {
            event.put(BOSS.get(), MorovoyRaven.attributes(false).build());
            event.put(MINION.get(), MorovoyRaven.attributes(true).build());
        });
    }
    private MorovoyRavenContent() {}
}
