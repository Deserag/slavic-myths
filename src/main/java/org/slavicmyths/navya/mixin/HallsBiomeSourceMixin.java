package org.slavicmyths.navya.mixin;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slavicmyths.losthalls.*;
/** Only sources containing the registered Halls point are affected. No world/chunk access. */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class HallsBiomeSourceMixin {
 @Shadow private Climate.ParameterList<Holder<Biome>> parameters(){throw new AssertionError();}
 @Unique private volatile HallsClimate.Selection<Holder<Biome>> slavicmyths$halls;
 @Inject(method="getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",at=@At("RETURN"),cancellable=true)
 private void slavicmyths$regions(int x,int y,int z,Climate.Sampler sampler,CallbackInfoReturnable<Holder<Biome>> result){var source=parameters();var selection=slavicmyths$halls;if(selection==null||selection.source()!=source){synchronized(this){selection=slavicmyths$halls;if(selection==null||selection.source()!=source)slavicmyths$halls=selection=HallsClimate.selection(source,h->h.is(HallsContent.BIOME));}}if(selection.halls()!=null)result.setReturnValue(HallsClimate.choose(selection,result.getReturnValue(),x,y,z,sampler));}
}
