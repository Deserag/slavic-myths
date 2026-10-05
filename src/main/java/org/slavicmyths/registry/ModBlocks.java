package org.slavicmyths.registry;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.slavicmyths.SlavicMyths;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, SlavicMyths.MOD_ID);

    // Wild plants: no growth state, random ticks, tile entities or scanning.
    public static final DeferredHolder<Block, Block> FLAX = BLOCKS.register("flax",
            () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> WORMWOOD = BLOCKS.register("wormwood",
            () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> ST_JOHNS_WORT = BLOCKS.register("st_johns_wort", () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> NETTLE = BLOCKS.register("nettle", () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> FIREWEED = BLOCKS.register("fireweed", () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> JUNIPER_BERRIES = BLOCKS.register("juniper_berries", () -> new WildPlantBlock(plantProperties()));
    public static final DeferredHolder<Block, Block> PERUNITE_ORE = BLOCKS.register("perunite_ore",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 3.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> SILVER_ORE = BLOCKS.register("silver_ore",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));
    public static final DeferredHolder<Block, Block> CARVED_OAK_PILLAR = BLOCKS.register("carved_oak_pillar",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredHolder<Block, Block> CARVED_BIRCH_PLANKS = BLOCKS.register("carved_birch_planks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredHolder<Block, Block> CARVED_OAK_BLOCK = BLOCKS.register("carved_oak_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredHolder<Block, Block> STRAW_BLOCK = BLOCKS.register("straw_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.5F).sound(SoundType.GRASS)));
    public static final DeferredHolder<Block, Block> AMBER_BLOCK = BLOCKS.register("amber_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F).sound(SoundType.GLASS)));
    public static final DeferredHolder<Block, Block> RITUAL_CANDLE = BLOCKS.register("ritual_candle",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noOcclusion().noCollission().strength(0.2F)
                    .sound(SoundType.WOOL).lightLevel(s -> 10)));
    public static final DeferredHolder<Block, Block> WALL_CARVING = BLOCKS.register("wall_carving",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD)));

    private static BlockBehaviour.Properties plantProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS);
    }

    public static final DeferredHolder<Block, Block> HEARTH = BLOCKS.register("hearth", () -> new org.slavicmyths.block.HearthBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5F).sound(SoundType.STONE).lightLevel(s -> 7).noOcclusion()));
    public static final DeferredHolder<Block, Block> ANCIENT_IDOL = BLOCKS.register("ancient_idol", () -> new org.slavicmyths.block.IdolBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredHolder<Block, Block> ALTAR = BLOCKS.register("altar", () -> new org.slavicmyths.block.AltarBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE).noOcclusion()));

    public static final DeferredHolder<Block, Block> BATH_STOVE = BLOCKS.register("bath_stove", () -> new org.slavicmyths.block.BathStoveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3).sound(SoundType.STONE).noOcclusion().lightLevel(s -> 4)));
    public static final DeferredHolder<Block, Block> WOODEN_TUB = BLOCKS.register("wooden_tub", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredHolder<Block, Block> PATH_STONE = BLOCKS.register("path_stone", () -> new org.slavicmyths.rpg.RpgBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5F).sound(SoundType.STONE).noOcclusion(),false));
    public static final DeferredHolder<Block, Block> RUNIC_ANVIL = BLOCKS.register("runic_anvil", () -> new org.slavicmyths.rpg.RpgBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4F).sound(SoundType.ANVIL).noOcclusion(),true));
    public static final DeferredHolder<Block, Block> RASPBERRY_BUSH=BLOCKS.register("raspberry_bush",()->new org.slavicmyths.block.FolkBerryBush(true));
    public static final DeferredHolder<Block, Block> BLUEBERRY_BUSH=BLOCKS.register("blueberry_bush",()->new org.slavicmyths.block.FolkBerryBush(false));
    public static final DeferredHolder<Block, Block> KITCHEN_TABLE=BLOCKS.register("kitchen_table",org.slavicmyths.block.KitchenTableBlock::new);
    public static final DeferredHolder<Block, Block> SKATERT=BLOCKS.register("skatert",org.slavicmyths.artifact.SkatertBlock::new);
    public static final DeferredHolder<Block, Block> FISHING_NET=BLOCKS.register("fishing_net",org.slavicmyths.water.FishingNetBlock::new);
    public static final DeferredHolder<Block, Block> REED=BLOCKS.register("reed",org.slavicmyths.water.ReedBlock::new);
    public static final DeferredHolder<Block, Block> WATER_GRASS=BLOCKS.register("water_grass",()->new net.minecraft.world.level.block.SeagrassBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).noCollission().instabreak().sound(SoundType.WET_GRASS)));
    public static final DeferredHolder<Block, Block> WHITE_LILY=BLOCKS.register("white_lily",()->new net.minecraft.world.level.block.WaterlilyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.LILY_PAD)));
    public static final DeferredHolder<Block, Block> POOL_STONE=BLOCKS.register("pool_stone",org.slavicmyths.depth.PoolStoneBlock::new);
    public static final DeferredHolder<Block, Block> ARMORER_TABLE=BLOCKS.register("armorer_table",org.slavicmyths.armorer.ArmorerBlock::new);
    public static final DeferredHolder<Block, Block> BURIAL_COFFIN=BLOCKS.register("burial_log_coffin",org.slavicmyths.kurgan.BurialCoffinBlock::new);
    private static final class WildPlantBlock extends BushBlock {
        private static final com.mojang.serialization.MapCodec<WildPlantBlock> CODEC = simpleCodec(WildPlantBlock::new);
        private WildPlantBlock(BlockBehaviour.Properties properties) { super(properties); }
        @Override protected com.mojang.serialization.MapCodec<WildPlantBlock> codec() { return CODEC; }
    }
    private ModBlocks() { }
 public static final DeferredHolder<Block, Block> YAGA_CAULDRON=BLOCKS.register("yaga_cauldron",org.slavicmyths.yaga.YagaCauldronBlock::new);
 public static final DeferredHolder<Block, Block> YAGA_DRIED_HERBS=BLOCKS.register("yaga_dried_herbs",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(-1,3600000).noCollission().noOcclusion().noLootTable()));
 public static final DeferredHolder<Block, Block> YAGA_BONE_CHARM=BLOCKS.register("yaga_bone_charm",()->new Block(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(-1,3600000).noCollission().noOcclusion().noLootTable()));
 public static final DeferredHolder<Block, Block> YAGA_CHICKEN_LEG=BLOCKS.register("yaga_chicken_leg",org.slavicmyths.yaga.YagaLegBlock::new);
}
