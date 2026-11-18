package org.slavicmyths.navya;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModFeatures;

public final class NavyaFeatures {
    public static final DeferredHolder<Feature<?>,NavyaTreeFeature> TREE=ModFeatures.FEATURES.register("navya_tree",NavyaTreeFeature::new);
    public static final DeferredHolder<Feature<?>,NavyaGeology> GEOLOGY=ModFeatures.FEATURES.register("navya_geology",NavyaGeology::new);
    public static final DeferredHolder<Feature<?>,NavyaNature> NATURE=ModFeatures.FEATURES.register("navya_nature",NavyaNature::new);
    public static void init() { }
    private NavyaFeatures() { }
}
