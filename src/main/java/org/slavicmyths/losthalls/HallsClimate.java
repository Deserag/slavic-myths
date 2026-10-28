package org.slavicmyths.losthalls;
/** Distinct third-biome point. It does not implement ocean selection for 2.0.5. */
public final class HallsClimate {
 public static final net.minecraft.world.level.biome.Climate.ParameterPoint POINT=net.minecraft.world.level.biome.Climate.parameters(-.7F,-.55F,0,0,0,0,.175F);
 public record Selection<T>(net.minecraft.world.level.biome.Climate.ParameterList<T> source,net.minecraft.world.level.biome.Climate.ParameterList<T> fallback,T halls){}
 public static <T> Selection<T> selection(net.minecraft.world.level.biome.Climate.ParameterList<T> source,java.util.function.Predicate<T> isHalls){var entries=new java.util.ArrayList<>(source.values());T halls=null;for(var pair:source.values())if(isHalls.test(pair.getSecond())){halls=pair.getSecond();entries.remove(pair);}return new Selection<>(source,halls==null?source:new net.minecraft.world.level.biome.Climate.ParameterList<>(entries),halls);}
 /** Align selection with the existing 12x12-chunk geometry plan. One region,
  * one classification at all Y; no tiny fragments whose planned arena lies in another biome. */
 public static <T> T choose(Selection<T> selection,T original,int quartX,int quartY,int quartZ,net.minecraft.world.level.biome.Climate.Sampler sampler){if(selection.halls()==null)return original;int centerX=Math.floorDiv(quartX,48)*48+24,centerZ=Math.floorDiv(quartZ,48)*48+24;T center=selection.source().findValue(sampler.sample(centerX,19,centerZ));if(center.equals(selection.halls()))return selection.halls();return original.equals(selection.halls())?selection.fallback().findValue(sampler.sample(quartX,quartY,quartZ)):original;}
 private HallsClimate(){}
}
