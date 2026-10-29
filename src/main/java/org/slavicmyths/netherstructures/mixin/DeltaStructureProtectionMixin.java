package org.slavicmyths.netherstructures.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla protects nether-brick fortresses, but did not know our crafted
 * blackstone floor palette. A neighbouring delta could turn it into lava. */
@Mixin(DeltaFeature.class)
public abstract class DeltaStructureProtectionMixin {
    @Unique private static final TagKey<Block> SLAVICMYTHS$PROTECTED=TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("slavicmyths","nether_structure_protected"));
    @Inject(method="isClear",at=@At("HEAD"),cancellable=true)
    private static void slavicmyths$protectCraftedFloor(LevelAccessor world,BlockPos pos,
            DeltaFeatureConfiguration config,CallbackInfoReturnable<Boolean> result) {
        if(world.getBlockState(pos).is(SLAVICMYTHS$PROTECTED))result.setReturnValue(false);
    }
}
