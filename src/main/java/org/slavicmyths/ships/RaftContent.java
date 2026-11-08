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
    /** Independent first ship ID; no raft item or legacy packed NBT is repurposed. */
    public static final DeferredHolder<EntityType<?>, EntityType<FirstShipEntity>> FIRST_SHIP = ModEntities.ENTITIES.register(
            "ship_wooden_7x11_entity", () -> EntityType.Builder.<FirstShipEntity>of(FirstShipEntity::new, MobCategory.MISC)
                    .sized(2.95F, .65F).clientTrackingRange(16).updateInterval(1).build("slavicmyths:ship_wooden_7x11_entity"));
    public static final DeferredHolder<Item, Item> FIRST_SHIP_ITEM = ModItems.ITEMS.register(
            "ship_wooden_7x11", () -> new FirstShipItem(new Item.Properties().stacksTo(1)));
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
    public static final DeferredHolder<Item, Item> REINFORCED_SAILCLOTH = ModItems.ITEMS.register(
            "reinforced_sailcloth", () -> new NamedShipPart("Прочная парусная ткань", new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAIL_LINEN_MODULE = ModItems.ITEMS.register(
            "ship_sail_linen_module", () -> new NamedShipPart("Льняной комплект паруса", new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAIL_REINFORCED_MODULE = ModItems.ITEMS.register(
            "ship_sail_reinforced_module", () -> new NamedShipPart("Прочный комплект паруса", new Item.Properties()));
    public static final DeferredHolder<Item, Item> SAIL_RIGGING_KIT = ModItems.ITEMS.register(
            "ship_rigging_upgrade", () -> new NamedShipPart("Комплект усиления мачты и рей", new Item.Properties()));
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

    /** Ready-made Creative rafts store their upgrades in the same payload as a safely packed vessel. */
    public static net.minecraft.world.item.ItemStack readyRaft(RaftSize size, int sails) {
        if (sails < 0 || sails > size.maxSails()) throw new IllegalArgumentException("Invalid sails");
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(raftItem(size));
        net.minecraft.nbt.CompoundTag packed = new net.minecraft.nbt.CompoundTag();
        packed.putInt("Version", 2);
        packed.putInt("RaftWidth", size.width);
        packed.putInt("RaftLength", size.length);
        packed.putInt("RaftSails", sails);
        packed.putInt("RaftFlags", sails == 0 ? 0 : 2); // ready to sail: helm installed
        packed.put("Raft2511Modules", new net.minecraft.nbt.ListTag());
        net.minecraft.nbt.CompoundTag root = new net.minecraft.nbt.CompoundTag();
        root.put("Raft2511Pack", packed);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(root));
        return stack;
    }

    public static int sailPreset(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.world.item.component.CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        net.minecraft.nbt.CompoundTag root = data.copyTag();
        if (!root.contains("Raft2511Pack", net.minecraft.nbt.Tag.TAG_COMPOUND)) return 0;
        return net.minecraft.util.Mth.clamp(root.getCompound("Raft2511Pack").getInt("RaftSails"), 0, 2);
    }

    public static void init() { ShipyardMenu.register(); }
    private RaftContent() {}
}
