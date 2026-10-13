package org.slavicmyths.garden;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
public final class AppleTreeFeature extends Feature<NoneFeatureConfiguration>{
    public AppleTreeFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return AppleTreeShape.grow(c.level(),c.origin(),c.random(),!c.level().getBlockState(c.origin()).is(Gardens.block("apple_sapling")));}
}
