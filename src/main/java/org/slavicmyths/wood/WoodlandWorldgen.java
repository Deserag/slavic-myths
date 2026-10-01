package org.slavicmyths.wood;
import java.util.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.*;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slavicmyths.registry.ModFeatures;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WoodlandWorldgen {
 public static final Map<String,RegistryObject<WoodlandFeature>> TREES=new LinkedHashMap<>();
 private static final Map<String,ConfiguredFeature<?,?>> FEATURES=new HashMap<>();
 static {for(String s:new String[]{"linden","rowan","willow","pine"})TREES.put(s,ModFeatures.FEATURES.register(s+"_tree",()->new WoodlandFeature(s)));}
 public static void init(){}
 public static void setup(){for(String s:TREES.keySet())for(int rarity:new int[]{2,4,6,8,12,24}){String key=s+"_"+rarity;FEATURES.put(key,Registry.register(WorldGenRegistries.CONFIGURED_FEATURE,new ResourceLocation("slavicmyths",key),TREES.get(s).get().configured(NoFeatureConfig.INSTANCE).chance(rarity)));}}
 private static void add(BiomeLoadingEvent e,String s,int rarity){e.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).add(()->FEATURES.get(s+"_"+rarity));}
 @SubscribeEvent public static void biomes(BiomeLoadingEvent e){
  if(e.getName()==null||!e.getName().getNamespace().equals("minecraft"))return;String b=e.getName().getPath();
  if(b.contains("forest")){add(e,"linden",4);add(e,"rowan",8);add(e,"pine",24);add(e,"willow",12);}
  else if(b.contains("taiga")){add(e,"pine",2);add(e,"rowan",12);}
  else if(b.equals("plains")||b.equals("sunflower_plains")){add(e,"rowan",12);add(e,"linden",24);}
  else if(b.contains("swamp")){add(e,"willow",2);add(e,"rowan",12);}
  else if(b.equals("river")){add(e,"willow",4);add(e,"rowan",24);}
 }
}
