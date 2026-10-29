package org.slavicmyths.navya.mixin;
import java.util.ArrayList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.slavicmyths.navya.*;
@Mixin(MultiNoiseBiomeSourceParameterList.class)
public abstract class NetherParametersMixin implements NavyaPresetAccess {
 @Shadow @Final @Mutable private Climate.ParameterList<Holder<Biome>> parameters;
 @Override @Unique public void slavicmyths$append(Holder<Biome> holder){
  var key=holder.unwrapKey().orElseThrow();
  if(parameters.values().stream().anyMatch(pair->pair.getSecond().is(key)))return;
  var values=new ArrayList<>(parameters.values());
  boolean cinder=holder.is(org.slavicmyths.blackcinder.CinderWorldgen.BIOME);
  boolean islands=holder.is(org.slavicmyths.burningislands.IslandsContent.BIOME);
  boolean halls=holder.is(org.slavicmyths.losthalls.HallsContent.BIOME);
  values.add(Pair.of(islands?Climate.parameters(.9F,.9F,0,0,0,0,1F):halls?org.slavicmyths.losthalls.HallsClimate.POINT:cinder?Climate.parameters(.7F,-.4F,0,0,0,0,.175F):Climate.parameters(-.3F,.4F,0,0,0,0,.175F),holder));parameters=new Climate.ParameterList<>(values);
 }
}
