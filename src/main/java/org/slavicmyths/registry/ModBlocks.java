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
    public static final RegistryObject<Block> ST_JOHNS_WORT = BLOCKS.register("st_johns_wort", () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> NETTLE = BLOCKS.register("nettle", () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> FIREWEED = BLOCKS.register("fireweed", () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> JUNIPER_BERRIES = BLOCKS.register("juniper_berries", () -> new BushBlock(plantProperties()));
    public static final RegistryObject<Block> PERUNITE_ORE = BLOCKS.register("perunite_ore",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(3.0F, 3.0F)
                    .sound(SoundType.STONE).harvestTool(ToolType.PICKAXE).harvestLevel(2)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SILVER_ORE = BLOCKS.register("silver_ore",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(3.0F).sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE).harvestLevel(2).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CARVED_OAK_PILLAR = BLOCKS.register("carved_oak_pillar",
            () -> new Block(AbstractBlock.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> CARVED_BIRCH_PLANKS = BLOCKS.register("carved_birch_planks",
            () -> new Block(AbstractBlock.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> CARVED_OAK_BLOCK = BLOCKS.register("carved_oak_block",
            () -> new Block(AbstractBlock.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));
    public static final RegistryObject<Block> STRAW_BLOCK = BLOCKS.register("straw_block",
            () -> new Block(AbstractBlock.Properties.of(Material.GRASS).strength(0.5F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> AMBER_BLOCK = BLOCKS.register("amber_block",
            () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(1.5F).sound(SoundType.GLASS)));
    public static final RegistryObject<Block> RITUAL_CANDLE = BLOCKS.register("ritual_candle",
            () -> new Block(AbstractBlock.Properties.of(Material.DECORATION).noOcclusion().noCollission().strength(0.2F)
                    .sound(SoundType.WOOL).lightLevel(s -> 10)));
    public static final RegistryObject<Block> WALL_CARVING = BLOCKS.register("wall_carving",
            () -> new Block(AbstractBlock.Properties.of(Material.WOOD).strength(1.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE)));

    private static AbstractBlock.Properties plantProperties() {
        return AbstractBlock.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS);
    }

    public static final RegistryObject<Block> HEARTH = BLOCKS.register("hearth", () -> new org.slavicmyths.block.HearthBlock(
            AbstractBlock.Properties.of(Material.STONE).strength(2.5F).sound(SoundType.STONE).lightLevel(s -> 7).noOcclusion()));
    public static final RegistryObject<Block> ANCIENT_IDOL = BLOCKS.register("ancient_idol", () -> new org.slavicmyths.block.IdolBlock(
            AbstractBlock.Properties.of(Material.WOOD).strength(3.0F).sound(SoundType.WOOD).harvestTool(ToolType.AXE).noOcclusion()));
    public static final RegistryObject<Block> ALTAR = BLOCKS.register("altar", () -> new org.slavicmyths.block.AltarBlock(
            AbstractBlock.Properties.of(Material.STONE).strength(3.0F).sound(SoundType.STONE).harvestTool(ToolType.PICKAXE).noOcclusion()));

    public static final RegistryObject<Block> BATH_STOVE = BLOCKS.register("bath_stove", () -> new org.slavicmyths.block.BathStoveBlock(AbstractBlock.Properties.of(Material.STONE).strength(3).sound(SoundType.STONE).noOcclusion().lightLevel(s -> 4)));
    public static final RegistryObject<Block> WOODEN_TUB = BLOCKS.register("wooden_tub", () -> new Block(AbstractBlock.Properties.of(Material.WOOD).strength(2).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> PATH_STONE = BLOCKS.register("path_stone", () -> new org.slavicmyths.rpg.RpgBlock(AbstractBlock.Properties.of(Material.STONE).strength(3.5F).sound(SoundType.STONE).noOcclusion(),false));
    public static final RegistryObject<Block> RUNIC_ANVIL = BLOCKS.register("runic_anvil", () -> new org.slavicmyths.rpg.RpgBlock(AbstractBlock.Properties.of(Material.METAL).strength(4F).sound(SoundType.ANVIL).noOcclusion(),true));
    public static final RegistryObject<Block> RASPBERRY_BUSH=BLOCKS.register("raspberry_bush",()->new org.slavicmyths.block.FolkBerryBush(true));
    public static final RegistryObject<Block> BLUEBERRY_BUSH=BLOCKS.register("blueberry_bush",()->new org.slavicmyths.block.FolkBerryBush(false));
    public static final RegistryObject<Block> KITCHEN_TABLE=BLOCKS.register("kitchen_table",org.slavicmyths.block.KitchenTableBlock::new);
    public static final RegistryObject<Block> SKATERT=BLOCKS.register("skatert",org.slavicmyths.artifact.SkatertBlock::new);
    public static final RegistryObject<Block> FISHING_NET=BLOCKS.register("fishing_net",org.slavicmyths.water.FishingNetBlock::new);
    public static final RegistryObject<Block> REED=BLOCKS.register("reed",org.slavicmyths.water.ReedBlock::new);
    public static final RegistryObject<Block> WATER_GRASS=BLOCKS.register("water_grass",()->new net.minecraft.block.SeaGrassBlock(AbstractBlock.Properties.of(Material.WATER_PLANT).noCollission().instabreak().sound(SoundType.WET_GRASS)));
    public static final RegistryObject<Block> WHITE_LILY=BLOCKS.register("white_lily",()->new net.minecraft.block.LilyPadBlock(AbstractBlock.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.LILY_PAD)));
    public static final RegistryObject<Block> POOL_STONE=BLOCKS.register("pool_stone",org.slavicmyths.depth.PoolStoneBlock::new);
    public static final RegistryObject<Block> ARMORER_TABLE=BLOCKS.register("armorer_table",org.slavicmyths.armorer.ArmorerBlock::new);
    private ModBlocks() { }
}
