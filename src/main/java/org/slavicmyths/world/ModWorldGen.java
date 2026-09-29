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
    private static ConfiguredFeature<?, ?> silver;
    private static ConfiguredFeature<?, ?> stJohnsWort, nettle, fireweed, juniper;
    private static ConfiguredFeature<?, ?> shrine;
    private static ConfiguredFeature<?, ?> bathhouse, oldBarn;

    public static void registerFeatures() {
        bathhouse = register("bathhouse", org.slavicmyths.registry.ModFeatures.BATHHOUSE.get().configured(net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE).chance(160));
        oldBarn = register("old_barn", org.slavicmyths.registry.ModFeatures.OLD_BARN.get().configured(net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE).chance(220));
        shrine = register("ancient_shrine", org.slavicmyths.registry.ModFeatures.SHRINE.get()
                .configured(net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE).chance(96));
        flax = register("patch_flax", patch(ModBlocks.FLAX.get(), 24, 3));
        wormwood = register("patch_wormwood", patch(ModBlocks.WORMWOOD.get(), 16, 4));
        // One size-3 attempt per two chunks, origin Y=0..14 (vanilla diamond: size 8, every chunk).
        perunite = register("ore_perunite", Feature.ORE.configured(new OreFeatureConfig(
                OreFeatureConfig.FillerBlockType.NATURAL_STONE, ModBlocks.PERUNITE_ORE.get().defaultBlockState(), 3))
                .range(15).squared().chance(2));
        silver = register("ore_silver", Feature.ORE.configured(new OreFeatureConfig(
                OreFeatureConfig.FillerBlockType.NATURAL_STONE, ModBlocks.SILVER_ORE.get().defaultBlockState(), 5))
                .range(48).squared().count(2));
        stJohnsWort = register("patch_st_johns_wort", patch(ModBlocks.ST_JOHNS_WORT.get(), 12, 6));
        nettle = register("patch_nettle", patch(ModBlocks.NETTLE.get(), 12, 5));
        fireweed = register("patch_fireweed", patch(ModBlocks.FIREWEED.get(), 12, 5));
        juniper = register("patch_juniper", patch(ModBlocks.JUNIPER_BERRIES.get(), 8, 8));
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
        if(event.getName()!=null && event.getName().getNamespace().equals("minecraft")) {
            if(event.getCategory()==Biome.Category.PLAINS || event.getCategory()==Biome.Category.TAIGA)
                event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> bathhouse);
            if(event.getCategory()==Biome.Category.PLAINS || event.getCategory()==Biome.Category.SAVANNA)
                event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> oldBarn);
        }
        ResourceLocation id = event.getName();
        // Deliberately limit v0.2 generation to vanilla Overworld biomes.
        if (id == null || !"minecraft".equals(id.getNamespace())
                || event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND
                || event.getCategory() == Biome.Category.NONE) {
            return;
        }
        event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> perunite);
        event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> silver);
        if (event.getCategory() == Biome.Category.PLAINS || event.getCategory() == Biome.Category.FOREST) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> flax);
        }
        if (event.getCategory() == Biome.Category.PLAINS || event.getCategory() == Biome.Category.FOREST
                || event.getCategory() == Biome.Category.TAIGA) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> shrine);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> wormwood);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> stJohnsWort);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> nettle);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> fireweed);
            event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(() -> juniper);
        }
    }

    private ModWorldGen() { }
}
