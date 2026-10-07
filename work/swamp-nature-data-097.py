from pathlib import Path
import json
p=Path('src/main/java/org/slavicmyths/registry/ModFeatures.java');s=p.read_text(encoding='utf-8').replace('    private ModFeatures()', '    public static final DeferredHolder<Feature<?>,org.slavicmyths.swamp.SwampNatureFeature> SWAMP_NATURE=FEATURES.register("swamp_nature",org.slavicmyths.swamp.SwampNatureFeature::new);\n    private ModFeatures()');p.write_text(s,encoding='utf-8')
R=Path('src/main/resources/data/slavicmyths')
def js(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
for kind,rarity in [('willow',3),('oak',8),('pine',12),('dead',10),('detail',1)]:
 name='swamp_'+kind;js(R/('worldgen/configured_feature/'+name+'.json'),{'type':'slavicmyths:swamp_nature','config':{'kind':kind}});js(R/('worldgen/placed_feature/'+name+'.json'),{'feature':'slavicmyths:'+name,'placement':[{'type':'minecraft:rarity_filter','chance':rarity},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'OCEAN_FLOOR_WG'},{'type':'minecraft:biome'}]})
js(R/'neoforge/biome_modifier/woodland_swamp.json',{'type':'neoforge:add_features','biomes':['minecraft:swamp'],'features':['slavicmyths:swamp_'+s for s in ['willow','oak','pine','dead','detail']],'step':'vegetal_decoration'})
