package org.slavicmyths.farming;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Natural placement is limited to the prevalidated seven-block tree footprint. */
public final class CoalPlumTreeFeature extends Feature<NoneFeatureConfiguration> {
    public CoalPlumTreeFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return CoalPlumTree.grow(context.level(), context.origin(), context.random(), true);
    }
}
