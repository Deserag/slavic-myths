package org.slavicmyths.ships;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Isolated first playable sailing prototype; independent of Create and Sable. */
public final class RaftContent {
    public static final DeferredHolder<EntityType<?>, EntityType<RaftBoatEntity>> RAFT = ModEntities.ENTITIES.register(
            "raft_2511_test", () -> EntityType.Builder.<RaftBoatEntity>of(RaftBoatEntity::new, MobCategory.MISC)
                    .sized(2.95F, 0.65F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:raft_2511_test"));
    public static final DeferredHolder<Item, Item> RAFT_ITEM = ModItems.ITEMS.register(
            "raft_2511_test", () -> new RaftItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SHIPYARD_KEY = ModItems.ITEMS.register(
            "shipyard_key", () -> new ShipyardKeyItem(new Item.Properties().stacksTo(1)));
    public static void init() {}
    private RaftContent() {}
}
