from pathlib import Path
import json,zipfile
p=Path('src/main/java/org/slavicmyths/wood/WoodlandFeature.java');s=p.read_text(encoding='utf-8').replace('Feature<NoneFeatureConfiguration>','Feature<WoodlandFeature.Config>').replace('super(NoneFeatureConfiguration.CODEC)','super(Config.CODEC)').replace('FeaturePlaceContext<NoneFeatureConfiguration>','FeaturePlaceContext<Config>').replace('NoneFeatureConfiguration cfg=context.config();','Config cfg=context.config();if(cfg.sapling())return grow(w,r,origin);')
s=s.replace(' public final String species;', ''' public record Config(boolean sapling) implements net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration {
  public static final com.mojang.serialization.Codec<Config> CODEC=com.mojang.serialization.Codec.BOOL.optionalFieldOf("sapling",false).xmap(Config::new,Config::sapling).codec();
 }
 public final String species;''');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/wood/WoodlandGrower.java');p.write_text('''package org.slavicmyths.wood;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
/** The target grower resolves the existing custom tree geometry through a sapling configuration. */
public final class WoodlandGrower {
 public static TreeGrower create(String species){return new TreeGrower("slavicmyths:"+species,
  Optional.empty(),Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,
   ResourceLocation.fromNamespaceAndPath("slavicmyths",species+"_sapling"))),Optional.empty());}
 private WoodlandGrower(){}
}
''',encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/wood/WoodlandWorldgen.java');p.write_text('''package org.slavicmyths.wood;
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
''',encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/wood/Woodlands.java');s=p.read_text(encoding='utf-8').replace('new WoodlandGrower(n)','WoodlandGrower.create(n)').replace('WoodlandWorldgen.setup();','');p.write_text(s,encoding='utf-8')
root=Path('src/main/resources/data/slavicmyths')
for d in ['worldgen/configured_feature','worldgen/placed_feature','tags/worldgen/biome','neoforge/biome_modifier']:(root/d).mkdir(parents=True,exist_ok=True)
def write(d,name,value):(root/d/(name+'.json')).write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
for species in ['linden','rowan','willow','pine']:
 write('worldgen/configured_feature',species+'_sapling',{'type':'slavicmyths:'+species+'_tree','config':{'sapling':True}})
 for rarity in [2,4,6,8,12,24]:
  name=species+'_'+str(rarity);write('worldgen/configured_feature',name,{'type':'slavicmyths:'+species+'_tree','config':{'sapling':False}})
  write('worldgen/placed_feature',name,{'feature':'slavicmyths:'+name,'placement':[{'type':'minecraft:rarity_filter','chance':rarity},{'type':'minecraft:biome'}]})
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');prefix='data/minecraft/worldgen/biome/';biomes=sorted(n[len(prefix):-5] for n in z.namelist() if n.startswith(prefix) and n.endswith('.json'))
groups={k:[] for k in ['forest','taiga','plains','swamp','river']}
for biome in biomes:
 if 'forest' in biome:group='forest'
 elif 'taiga' in biome:group='taiga'
 elif biome in ['plains','sunflower_plains']:group='plains'
 elif 'swamp' in biome:group='swamp'
 elif biome=='river':group='river'
 else:continue
 groups[group].append('minecraft:'+biome)
rules={'forest':['linden_4','rowan_8','pine_24','willow_12'],'taiga':['pine_2','rowan_12'],'plains':['rowan_12','linden_24'],'swamp':['willow_2','rowan_12'],'river':['willow_4','rowan_24']}
for group,features in rules.items():
 write('tags/worldgen/biome','woodland_'+group,{'replace':False,'values':groups[group]})
 write('neoforge/biome_modifier','woodland_'+group,{'type':'neoforge:add_features','biomes':'#slavicmyths:woodland_'+group,'features':['slavicmyths:'+f for f in features],'step':'vegetal_decoration'})
print('Woodland features/growers: 4 existing registered IDs, 24 preserved configured IDs and 5 data-driven biome assignments; saplings resolve same custom shapes.')
