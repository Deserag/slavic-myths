package org.slavicmyths.world;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModBlocks;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class ModWorldGen {
    private static ConfiguredFeature<?, ?> flax;
    private static ConfiguredFeature<?, ?> wormwood;
    private static ConfiguredFeature<?, ?> perunite;
    private static ConfiguredFeature<?, ?> shrine;

    public static void registerFeatures() {
        shrine = register("ancient_shrine", org.slavicmyths.registry.ModFeatures.SHRINE.get()
                .configured(net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE).chance(96));
        flax = register("patch_flax", patch(ModBlocks.FLAX.get(), 24, 3));
        wormwood = register("patch_wormwood", patch(ModBlocks.WORMWOOD.get(), 16, 4));
        // One size-3 attempt per two chunks, origin Y=0..14 (vanilla diamond: size 8, every chunk).
        perunite = register("ore_perunite", Feature.ORE.configured(new OreFeatureConfig(
                OreFeatureConfig.FillerBlockType.NATURAL_STONE, ModBlocks.PERUNITE_ORE.get().defaultBlockState(), 3))
                .range(15).squared().chance(2));
    }

    private static ConfiguredFeature<?, ?> patch(Block block, int tries, int rarity) {
        return Feature.RANDOM_PATCH.configured(new BlockClusterFeatureConfig.Builder(
                new SimpleBlockStateProvider(block.defaultBlockState()), SimpleBlockPlacer.INSTANCE)
                .tries(tries).build()).decorated(Features.Placements.HEIGHTMAP_SQUARE).chance(rarity);
    }

    private static ConfiguredFeature<?, ?> register(String name, ConfiguredFeature<?, ?> feature) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,
                new ResourceLocation(SlavicMyths.MOD_ID, name), feature);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void biomeLoading(BiomeLoadingEvent event) {
        ResourceLocation id = event.getName();
        // Deliberately limit v0.2 generation to vanilla Overworld biomes.
        if (id == null || !"minecraft".equals(id.getNamespace())
                || event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND
                || event.getCategory() == Biome.Category.NONE) {
            return;
        }
        event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> perunite);
        if (event.getCategory() == Biome.Category.PLAINS || event.getCategory() == Biome.Category.FOREST) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> flax);
        }
        if (event.getCategory() == Biome.Category.PLAINS || event.getCategory() == Biome.Category.FOREST
                || event.getCategory() == Biome.Category.TAIGA) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> shrine);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> wormwood);
        }
    }

    private ModWorldGen() { }
}
