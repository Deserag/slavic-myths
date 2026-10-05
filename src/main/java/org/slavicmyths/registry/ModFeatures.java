package org.slavicmyths.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slavicmyths.world.ShrineFeature;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "slavicmyths");
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SHRINE = FEATURES.register("ancient_shrine", ShrineFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> PATH_SHRINE=FEATURES.register("path_shrine",org.slavicmyths.world.PathShrineFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BERRY_PATCH=FEATURES.register("berry_patch",org.slavicmyths.world.BerryPatchFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> WATER_PATCH=FEATURES.register("water_patch",org.slavicmyths.water.WaterFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DEEP_POOL=FEATURES.register("deep_pool",org.slavicmyths.depth.DeepPoolFeature::new);
    private ModFeatures() { }
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BATHHOUSE = FEATURES.register("bathhouse", () -> new org.slavicmyths.world.HomesteadFeature(false));
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> OLD_BARN = FEATURES.register("old_barn", () -> new org.slavicmyths.world.HomesteadFeature(true));
}
