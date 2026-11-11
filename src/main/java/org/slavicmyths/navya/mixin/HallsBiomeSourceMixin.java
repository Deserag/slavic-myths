package org.slavicmyths.navya.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slavicmyths.losthalls.*;
import org.slavicmyths.navya.NetherBiomeRuntime;

/** Repair Nether climate sources constructed before runtime biome holders became available. */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class HallsBiomeSourceMixin {
 @Shadow private Climate.ParameterList<Holder<Biome>> parameters(){throw new AssertionError();}
 @Unique private volatile HallsClimate.Selection<Holder<Biome>> slavicmyths$halls;
 @Unique private volatile List<Holder<Biome>> slavicmyths$runtime=List.of();
 @Unique private HallsClimate.Selection<Holder<Biome>> slavicmyths$filtered;
 @Unique private Holder<Biome> slavicmyths$islands;
 @Unique private Climate.ParameterList<Holder<Biome>> slavicmyths$original;

 @Inject(method="getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",at=@At("RETURN"),cancellable=true)
 private void slavicmyths$regions(int x,int y,int z,Climate.Sampler sampler,CallbackInfoReturnable<Holder<Biome>> result){
  var source=parameters();
  var runtime=NetherBiomeRuntime.holders();
  var selection=slavicmyths$halls;
  if(selection==null||selection.source()!=source||slavicmyths$runtime!=runtime){
   synchronized(this){
    selection=slavicmyths$halls;
    if(selection==null||selection.source()!=source||slavicmyths$runtime!=runtime){
     var extended=NetherBiomeRuntime.enriched(source,runtime);
     var entries=new ArrayList<>(extended.values());
     slavicmyths$islands=null;
     for(var pair:extended.values())if(pair.getSecond().is(org.slavicmyths.burningislands.IslandsContent.BIOME)){
      slavicmyths$islands=pair.getSecond();
      entries.remove(pair);
     }
     slavicmyths$original=new Climate.ParameterList<>(entries);
     var chosen=slavicmyths$filtered=HallsClimate.selection(slavicmyths$original,h->h.is(HallsContent.BIOME));
     slavicmyths$runtime=runtime;
     slavicmyths$halls=selection=new HallsClimate.Selection<>(source,chosen.fallback(),chosen.halls());
    }
   }
  }
  var original=slavicmyths$original.findValue(sampler.sample(x,y,z));
  var halls=slavicmyths$filtered;
  if(halls.halls()!=null)original=HallsClimate.choose(halls,original,x,y,z,sampler);
  var ocean=((org.slavicmyths.burningislands.OceanTerrain.SamplerAccess)(Object)sampler).slavicmyths$ocean();
  if(slavicmyths$islands!=null&&ocean!=null&&ocean.contains(x,y,z))original=slavicmyths$islands;
  result.setReturnValue(original);
 }
}
