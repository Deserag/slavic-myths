package org.slavicmyths.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.SlavicMyths;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SlavicMyths.MOD_ID);

    // Wild plants: no growth state, random ticks, tile entities or scanning.
    public static final RegistryObject<Block> FLAX = BLOCKS.register("flax",
            () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> WORMWOOD = BLOCKS.register("wormwood",
            () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> PERUNITE_ORE = BLOCKS.register("perunite_ore",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(3.0F, 3.0F)
                    .sound(SoundType.STONE).harvestTool(ToolType.PICKAXE).harvestLevel(2)
                    .requiresCorrectToolForDrops()));

    private static AbstractBlock.Properties plantProperties() {
        return AbstractBlock.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS);
    }

    public static final RegistryObject<Block> HEARTH = BLOCKS.register("hearth", () -> new org.slavicmyths.block.HearthBlock(
            AbstractBlock.Properties.of(Material.STONE).strength(2.5F).sound(SoundType.STONE).lightLevel(s -> 7)));
    public static final RegistryObject<Block> ANCIENT_IDOL = BLOCKS.register("ancient_idol", () -> new org.slavicmyths.block.IdolBlock(
            AbstractBlock.Properties.of(Material.WOOD).strength(3.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> ALTAR = BLOCKS.register("altar", () -> new org.slavicmyths.block.AltarBlock(
            AbstractBlock.Properties.of(Material.STONE).strength(3.0F).sound(SoundType.STONE).harvestTool(ToolType.PICKAXE).noOcclusion()));

    private ModBlocks() { }
}
