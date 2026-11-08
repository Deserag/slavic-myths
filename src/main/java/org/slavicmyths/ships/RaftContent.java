package org.slavicmyths.ships;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModBlocks;

/** Non-industrial, self-contained Slavic Myths shipbuilding. No Create dependency. */
public final class RaftContent {
    public static final DeferredHolder<EntityType<?>, EntityType<RaftBoatEntity>> RAFT = ModEntities.ENTITIES.register(
            "raft_2511_test", () -> EntityType.Builder.<RaftBoatEntity>of(RaftBoatEntity::new, MobCategory.MISC)
                    .sized(2.95F, 0.65F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:raft_2511_test"));
    public static final DeferredHolder<Item, Item> RAFT_ITEM = ModItems.ITEMS.register(
            "raft_2511_test", () -> new RaftItem(RaftSize.SMALL, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> RAFT_LONG = ModItems.ITEMS.register(
            "raft_3x5", () -> new RaftItem(RaftSize.LONG, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> RAFT_WIDE = ModItems.ITEMS.register(
            "raft_5x5", () -> new RaftItem(RaftSize.WIDE, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> RAFT_CARGO = ModItems.ITEMS.register(
            "raft_5x7", () -> new RaftItem(RaftSize.CARGO, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SHIPYARD_KEY = ModItems.ITEMS.register(
            "shipyard_key", () -> new ShipyardKeyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> DECK_SECTION = ModItems.ITEMS.register(
            "ship_deck_section", () -> new NamedShipPart("Палубная секция", new Item.Properties()));
    public static final DeferredHolder<Item, Item> RAIL_MODULE = ModItems.ITEMS.register(
            "ship_rail_module", () -> new NamedShipPart("Бортовое ограждение", new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAIL_MODULE = ModItems.ITEMS.register(
            "ship_sail_module", () -> new NamedShipPart("Малый парус", new Item.Properties()));
    public static final DeferredHolder<Item, Item> HELM_MODULE = ModItems.ITEMS.register(
            "ship_helm_module", () -> new NamedShipPart("Судовой руль", new Item.Properties()));
    public static final DeferredHolder<Item, Item> OAR_MODULE = ModItems.ITEMS.register(
            "ship_oar_module", () -> new NamedShipPart("Дополнительные вёсла", new Item.Properties()));

    public static final DeferredHolder<Block, ShipyardWorkbenchBlock> SHIPYARD_BENCH = ModBlocks.BLOCKS.register(
            "shipyard_workbench", ShipyardWorkbenchBlock::new);
    public static final DeferredHolder<Item, Item> SHIPYARD_BENCH_ITEM = ModItems.ITEMS.register(
            "shipyard_workbench", () -> new ShipyardBenchItem(SHIPYARD_BENCH.get(), new Item.Properties()));

    public static Item raftItem(RaftSize size) {
        return switch (size) {
            case SMALL -> RAFT_ITEM.get();
            case LONG -> RAFT_LONG.get();
            case WIDE -> RAFT_WIDE.get();
            case CARGO -> RAFT_CARGO.get();
        };
    }

    public static void init() {}
    private RaftContent() {}
}
