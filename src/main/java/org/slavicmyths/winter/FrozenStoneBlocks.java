package org.slavicmyths.winter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModItems;

/** Stable, non-melting building blocks based on vanilla stone. */
public final class FrozenStoneBlocks {
    public static final Map<String, DeferredHolder<Block, Block>> ALL = new LinkedHashMap<>();
    static {
        family("frozen_cobblestone", Blocks.COBBLESTONE);
        family("frozen_stone", Blocks.STONE);
        family("smooth_frozen_stone", Blocks.SMOOTH_STONE);
        family("frozen_stone_bricks", Blocks.STONE_BRICKS);
        family("frozen_granite", Blocks.GRANITE);
        family("frozen_diorite", Blocks.DIORITE);
        family("frozen_andesite", Blocks.ANDESITE);
        family("frozen_deepslate", Blocks.DEEPSLATE);
        family("frozen_cobbled_deepslate", Blocks.COBBLED_DEEPSLATE);
        family("frozen_deepslate_bricks", Blocks.DEEPSLATE_BRICKS);
        solid("chiseled_frozen_stone", Blocks.CHISELED_STONE_BRICKS);
        solid("cracked_frozen_stone_bricks", Blocks.CRACKED_STONE_BRICKS);
        solid("frozen_stone_pillar", Blocks.QUARTZ_PILLAR);
        solid("frozen_paving", Blocks.POLISHED_ANDESITE);
    }
    private static BlockBehaviour.Properties properties(Block base) {
        return BlockBehaviour.Properties.ofFullCopy(base).requiresCorrectToolForDrops();
    }
    private static void add(String id, Supplier<Block> factory) {
        var block = ModBlocks.BLOCKS.register(id, factory);
        ALL.put(id, block);
        ModItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    private static void solid(String id, Block base) { add(id, () -> new Block(properties(base))); }
    private static void family(String id, Block base) {
        solid(id, base);
        add(id + "_stairs", () -> new StairBlock(ALL.get(id).get().defaultBlockState(), properties(base)));
        add(id + "_slab", () -> new SlabBlock(properties(base)));
        add(id + "_wall", () -> new WallBlock(properties(base)));
    }
    public static void init() { }
    private FrozenStoneBlocks() { }
}
