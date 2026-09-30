package org.slavicmyths.registry;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.world.ShrineFeature;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "slavicmyths");
    public static final RegistryObject<Feature<NoFeatureConfig>> SHRINE = FEATURES.register("ancient_shrine", ShrineFeature::new);
    public static final RegistryObject<Feature<NoFeatureConfig>> PATH_SHRINE=FEATURES.register("path_shrine",org.slavicmyths.world.PathShrineFeature::new);
    private ModFeatures() { }
    public static final RegistryObject<Feature<NoFeatureConfig>> BATHHOUSE = FEATURES.register("bathhouse", () -> new org.slavicmyths.world.HomesteadFeature(false));
    public static final RegistryObject<Feature<NoFeatureConfig>> OLD_BARN = FEATURES.register("old_barn", () -> new org.slavicmyths.world.HomesteadFeature(true));
}
