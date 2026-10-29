package org.slavicmyths.netherstructures.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BasaltColumnsFeature.class)
public abstract class BasaltStructureProtectionMixin {
    @Unique private static final TagKey<Block> SLAVICMYTHS$PROTECTED=TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("slavicmyths","nether_structure_protected"));
    @Inject(method="canPlaceAt",at=@At("HEAD"),cancellable=true)
    private static void slavicmyths$noColumnOnFloor(LevelAccessor w,int seaLevel,BlockPos.MutableBlockPos p,CallbackInfoReturnable<Boolean> out) {
        if(w.getBlockState(p.below()).is(SLAVICMYTHS$PROTECTED))out.setReturnValue(false);
    }
    @Inject(method="findAir",at=@At("HEAD"),cancellable=true)
    private static void slavicmyths$noClimbingThroughRoof(LevelAccessor w,BlockPos.MutableBlockPos p,int budget,CallbackInfoReturnable<BlockPos> out) {
        for(int dy=0;dy<budget&&p.getY()+dy<w.getMaxBuildHeight();dy++)
            if(w.getBlockState(p.above(dy)).is(SLAVICMYTHS$PROTECTED)){out.setReturnValue(null);return;}
    }
}
