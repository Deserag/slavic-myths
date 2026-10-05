package org.slavicmyths.wood;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModFeatures;
/** Existing feature IDs remain registered; configured/placed features and biome assignment are data-driven. */
public final class WoodlandWorldgen {
 public static final Map<String,DeferredHolder<Feature<?>,WoodlandFeature>> TREES=new LinkedHashMap<>();
 static{for(String species:new String[]{"linden","rowan","willow","pine"})TREES.put(species,
  ModFeatures.FEATURES.register(species+"_tree",()->new WoodlandFeature(species)));}
 public static void init(){}
 private WoodlandWorldgen(){}
}
