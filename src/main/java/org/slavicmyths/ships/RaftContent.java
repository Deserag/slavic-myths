package org.slavicmyths.ships;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModItems;

/** Slavic shipbuilding: independent of Create, Aeronautics and Sable. */
public final class RaftContent {
    // Existing IDs stay unchanged to protect test-world saves and previously packed vessels.
    public static final DeferredHolder<EntityType<?>, EntityType<RaftBoatEntity>> RAFT = ModEntities.ENTITIES.register(
            "raft_2511_test", () -> EntityType.Builder.<RaftBoatEntity>of(RaftBoatEntity::new, MobCategory.MISC)
                    .sized(2.95F, 0.75F).clientTrackingRange(12).updateInterval(1).build("slavicmyths:raft_2511_test"));
    public static final DeferredHolder<Item, Item> RAFT_ITEM = ModItems.ITEMS.register(
            "raft_2511_test", () -> new RaftItem(new Item.Properties().stacksTo(1), RaftSize.SMALL));
    public static final DeferredHolder<Item, Item> LONG_RAFT = ModItems.ITEMS.register(
            "raft_long", () -> new RaftItem(new Item.Properties().stacksTo(1), RaftSize.LONG));
    public static final DeferredHolder<Item, Item> WIDE_RAFT = ModItems.ITEMS.register(
            "raft_wide", () -> new RaftItem(new Item.Properties().stacksTo(1), RaftSize.WIDE));
    public static final DeferredHolder<Item, Item> LARGE_RAFT = ModItems.ITEMS.register(
            "raft_large", () -> new RaftItem(new Item.Properties().stacksTo(1), RaftSize.LARGE));

    public static final DeferredHolder<Item, Item> SHIPYARD_KEY = ModItems.ITEMS.register(
            "shipyard_key", () -> new ShipyardKeyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Block, Block> SHIPYARD = ModBlocks.BLOCKS.register(
            "shipyard_workbench", RaftShipyardBlock::new);
    public static final DeferredHolder<Item, Item> SHIPYARD_ITEM = ModItems.ITEMS.register(
            "shipyard_workbench", () -> new BlockItem(SHIPYARD.get(), new Item.Properties()) {
                @Override public net.minecraft.network.chat.Component getName(net.minecraft.world.item.ItemStack stack) {
                    return net.minecraft.network.chat.Component.literal("Корабельный верстак");
                }
            });
    public static final DeferredHolder<Item, Item> DECK_SECTION = ModItems.ITEMS.register(
            "raft_deck_section", () -> new ShipPartItem(new Item.Properties(), "Палубная секция"));
    public static final DeferredHolder<Item, Item> OARS = ModItems.ITEMS.register(
            "raft_oars", () -> new ShipPartItem(new Item.Properties(), "Пара вёсел"));
    public static final DeferredHolder<Item, Item> ROPE = ModItems.ITEMS.register(
            "ship_rope", () -> new ShipPartItem(new Item.Properties(), "Корабельный канат"));
    public static final DeferredHolder<Item, Item> RAILING = ModItems.ITEMS.register(
            "raft_railing", () -> new ShipPartItem(new Item.Properties(), "Бортовое ограждение"));
    public static final DeferredHolder<Item, Item> SMALL_SAIL = ModItems.ITEMS.register(
            "raft_small_sail", () -> new ShipPartItem(new Item.Properties(), "Малый парус"));
    public static final DeferredHolder<Item, Item> HELM = ModItems.ITEMS.register(
            "raft_helm", () -> new ShipPartItem(new Item.Properties(), "Рулевой пост"));
    public static final DeferredHolder<Item, Item> BALLAST = ModItems.ITEMS.register(
            "raft_ballast", () -> new ShipPartItem(new Item.Properties(), "Балласт"));
    public static final DeferredHolder<Item, Item> LANTERN = ModItems.ITEMS.register(
            "raft_lantern", () -> new ShipPartItem(new Item.Properties(), "Судовой фонарь"));
    public static final DeferredHolder<Item, Item> REINFORCEMENT = ModItems.ITEMS.register(
            "raft_reinforcement", () -> new ShipPartItem(new Item.Properties(), "Усиление плота"));

    public static Item sizeItem(RaftSize size) {
        return switch (size) {
            case SMALL -> RAFT_ITEM.get();
            case LONG -> LONG_RAFT.get();
            case WIDE -> WIDE_RAFT.get();
            case LARGE -> LARGE_RAFT.get();
        };
    }

    public static void init() {}
    private RaftContent() {}
}
