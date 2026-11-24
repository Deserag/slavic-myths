package org.slavicmyths.winter;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** Explicitly recognizes the opt-in winter test preset, not normal worlds. */
public final class WinterWorld {
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS =
        ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath("slavicmyths", "winter_flat"));
    public static boolean isTest(ServerLevel world) {
        return world.dimension().equals(Level.OVERWORLD)
            && world.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator noise
            && noise.generatorSettings().is(SETTINGS);
    }
    private WinterWorld() {}
}
