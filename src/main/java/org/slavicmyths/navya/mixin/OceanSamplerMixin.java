package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.*;
import org.slavicmyths.burningislands.OceanTerrain;
@Mixin(net.minecraft.world.level.biome.Climate.Sampler.class)
public abstract class OceanSamplerMixin implements OceanTerrain.SamplerAccess {
 @Unique private OceanTerrain slavicmyths$ocean;
 public OceanTerrain slavicmyths$ocean(){return slavicmyths$ocean;}
 public void slavicmyths$ocean(OceanTerrain value){slavicmyths$ocean=value;}
}
