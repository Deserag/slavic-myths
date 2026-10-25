package org.slavicmyths.navya.mixin;
import java.util.ArrayList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Extend only the vanilla Nether preset, including new chunks in existing saves. */
@Mixin(MultiNoiseBiomeSourceParameterList.class)
public abstract class NetherParametersMixin {
    @Shadow @Final @Mutable private Climate.ParameterList<Holder<Biome>> parameters;
    @Inject(method="<init>",at=@At("RETURN"))
    private void slavicmyths$navya(MultiNoiseBiomeSourceParameterList.Preset preset,HolderGetter<Biome> biomes,CallbackInfo ci){
        if(preset!=MultiNoiseBiomeSourceParameterList.Preset.NETHER)return;
        var key=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","navya_grove"));
        biomes.get(key).ifPresent(holder->{
            if(parameters.values().stream().anyMatch(pair->pair.getSecond().is(key)))return;
            var values=new ArrayList<>(parameters.values());values.add(Pair.of(Climate.parameters(-.3F,.4F,0,0,0,0,.175F),holder));parameters=new Climate.ParameterList<>(values);
        });
    }
}
