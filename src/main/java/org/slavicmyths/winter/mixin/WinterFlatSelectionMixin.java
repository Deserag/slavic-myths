package org.slavicmyths.winter.mixin;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.slavicmyths.winter.FrozenStoneBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The UI stays in Superflat. Only the exact winter template is promoted to the
 *  multibiome generator before the world is saved. Other flat presets are untouched. */
@Mixin(CreateWorldScreen.class)
public abstract class WinterFlatSelectionMixin {
    private static final ResourceKey<WorldPreset> WINTER_GENERATOR = ResourceKey.create(
        Registries.WORLD_PRESET, ResourceLocation.fromNamespaceAndPath("slavicmyths", "slavic_winter"));

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void slavicmyths$applyWinterTemplate(CallbackInfo ci) {
        var ui = ((CreateWorldScreen)(Object)this).getUiState();
        if (!(ui.getSettings().selectedDimensions().overworld() instanceof FlatLevelSource flat)) return;
        var layers = flat.settings().getLayersInfo();
        if (layers.size() != 5
            || !slavicmyths$layer(layers.get(0), 1, Blocks.BEDROCK)
            || !slavicmyths$layer(layers.get(1), 119, Blocks.STONE)
            || !slavicmyths$layer(layers.get(2), 4, FrozenStoneBlocks.ALL.get("frozen_stone").get())
            || !slavicmyths$layer(layers.get(3), 3, Blocks.DIRT)
            || !slavicmyths$layer(layers.get(4), 1, Blocks.GRASS_BLOCK)) return;
        ui.updateDimensions((registries, dimensions) -> {
            var generator = registries.registryOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(WINTER_GENERATOR).createWorldDimensions().overworld();
            return dimensions.replaceOverworldGenerator(registries, generator);
        });
    }

    private static boolean slavicmyths$layer(FlatLayerInfo layer, int count, net.minecraft.world.level.block.Block block) {
        return layer.getHeight() == count && layer.getBlockState().is(block);
    }
}
