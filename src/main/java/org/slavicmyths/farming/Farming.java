package org.slavicmyths.farming;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModItems;

public final class Farming {
    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("slavicmyths", "sickle_harvestable"));
    public static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, "slavicmyths");
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CappedFortune>> FORTUNE = FUNCTIONS.register("capped_crop_fortune", () -> new LootItemFunctionType<>(CappedFortune.CODEC));
    public static List<Block> crops() { return List.of(ModBlocks.RYE_CROP.get(), ModBlocks.BARLEY_CROP.get(), ModBlocks.OAT_CROP.get(), ModBlocks.TURNIP_CROP.get(), ModBlocks.CABBAGE_CROP.get(), ModBlocks.PEA_CROP.get(), ModBlocks.FLAX_CROP.get(),ModBlocks.GARLIC_CROP.get(),ModBlocks.HORSERADISH_CROP.get(),ModBlocks.BLACK_RADISH_CROP.get(),ModBlocks.MUSTARD_CROP.get(),ModBlocks.ONION_CROP.get(),ModBlocks.CRIMSON_PEPPER_CROP.get(),ModBlocks.HEATROOT_CROP.get(),ModBlocks.ASH_MUSHROOM_CROP.get(),ModBlocks.ASH_HORSERADISH_CROP.get(),ModBlocks.COAL_GARLIC_CROP.get(), ModBlocks.SERNICA_CROP.get(), ModBlocks.HEAT_BERRY_BUSH.get()); }
    public static List<Item> seeds() { return List.of(ModItems.RYE_SEEDS.get(), ModItems.BARLEY_SEEDS.get(), ModItems.OAT_SEEDS.get(), ModItems.TURNIP_SEEDS.get(), ModItems.CABBAGE_SEEDS.get(), ModItems.PEA_SEEDS.get(), ModItems.FLAX_SEEDS.get(),ModItems.GARLIC_CLOVE.get(),ModItems.HORSERADISH_CUTTING.get(),ModItems.BLACK_RADISH_SEEDS.get(),ModItems.MUSTARD_SEEDS.get(),ModItems.ONION_SET.get(),ModItems.CRIMSON_PEPPER.get()); }
    public static List<Item> creativeItems() {
        var items = new java.util.ArrayList<Item>(seeds());
        items.addAll(List.of(ModItems.RYE_GRAIN.get(), ModItems.BARLEY_GRAIN.get(), ModItems.OAT_GRAIN.get(), ModItems.TURNIP.get(), ModItems.CABBAGE.get(), ModItems.PEA_POD.get(), ModItems.FLAX_STALK.get(), ModItems.SICKLE.get(), ModItems.FIELD_HOE.get(), ModItems.WATERING_CAN.get(), ModItems.ORGANIC_FERTILIZER.get()));
        return items;
    }
    private Farming() { }
}
