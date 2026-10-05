from pathlib import Path
import json,zipfile,re
root=Path('src/main/resources/data/slavicmyths');z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');v=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');all_data={n:json.loads(v.read(n)) for n in v.namelist() if n.startswith('data/') and n.endswith('.json')};all_data.update({n:json.loads(z.read(n)) for n in z.namelist() if n.startswith('data/') and n.endswith('.json')})
def flatten(tag,seen=()):
 ns,key=tag.split(':');name='data/'+ns+'/tags/worldgen/biome/'+key+'.json';assert name not in seen
 out=set()
 for e in all_data.get(name,{}).get('values',[]):
  if isinstance(e,dict):
   if not e.get('required',True):continue
   e=e['id']
  if e.startswith('#'):out.update(flatten(e[1:],seen+(name,)))
  elif e.startswith('minecraft:'):out.add(e)
 return out
cats={k:flatten('c:is_'+k) for k in ['plains','forest','taiga','savanna','river','swamp']}
def write(folder,name,data):p=root/folder/(name+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
custom={'deep_pool':240,'water_patch':3,'berry_patch':6,'path_shrine':80,'bathhouse':160,'old_barn':220,'ancient_shrine':96}
for name,rarity in custom.items():
 write('worldgen/configured_feature',name,{'type':'slavicmyths:'+name,'config':{}})
 write('worldgen/placed_feature',name,{'feature':'slavicmyths:'+name,'placement':[{'type':'minecraft:rarity_filter','chance':rarity},{'type':'minecraft:biome'}]})
herbs={'flax':(24,3),'wormwood':(16,4),'st_johns_wort':(12,6),'nettle':(12,5),'fireweed':(12,5),'juniper':(8,8)}
for name,(tries,rarity) in herbs.items():
 id='patch_'+name;block='slavicmyths:'+('juniper_berries' if name=='juniper' else name)
 write('worldgen/configured_feature',id,{'type':'minecraft:random_patch','config':{'tries':tries,'xz_spread':7,'y_spread':3,'feature':{'feature':{'type':'minecraft:simple_block','config':{'to_place':{'type':'minecraft:simple_state_provider','state':{'Name':block}}}},'placement':[{'type':'minecraft:block_predicate_filter','predicate':{'type':'minecraft:matching_blocks','blocks':['minecraft:air']}}]}}})
 write('worldgen/placed_feature',id,{'feature':'slavicmyths:'+id,'placement':[{'type':'minecraft:rarity_filter','chance':rarity},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'},{'type':'minecraft:biome'}]})
for name,size,top,count,rare in [('perunite',3,14,None,2),('silver',5,47,2,None)]:
 id='ore_'+name;write('worldgen/configured_feature',id,{'type':'minecraft:ore','config':{'size':size,'discard_chance_on_air_exposure':0.0,'targets':[{'target':{'predicate_type':'minecraft:tag_match','tag':'minecraft:stone_ore_replaceables'},'state':{'Name':'slavicmyths:'+name+'_ore'}}]}})
 placement=[{'type':'minecraft:count','count':count}] if count else [{'type':'minecraft:rarity_filter','chance':rare}]
 placement += [{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':0},'max_inclusive':{'absolute':top}}},{'type':'minecraft:biome'}]
 write('worldgen/placed_feature',id,{'feature':'slavicmyths:'+id,'placement':placement})
groups={'feature_water':['river','swamp','forest'],'feature_bathhouse':['plains','taiga'],'feature_fields':['plains','savanna'],'feature_plains_forest':['plains','forest'],'feature_plains_forest_taiga':['plains','forest','taiga']}
for name,keys in groups.items():write('tags/worldgen/biome',name,{'replace':False,'values':sorted(set().union(*(cats[k] for k in keys)))})
write('tags/worldgen/biome','feature_overworld',{'replace':False,'values':sorted(flatten('minecraft:is_overworld')-{'minecraft:the_void'})})
rules={'deep_pool':('spawn_water',['deep_pool'],'surface_structures'),'water_patch':('feature_water',['water_patch'],'vegetal_decoration'),'bathhouse':('feature_bathhouse',['bathhouse'],'surface_structures'),'old_barn':('feature_fields',['old_barn'],'surface_structures'),'path_shrine':('feature_plains_forest',['path_shrine'],'surface_structures'),'berry_patch':('spawn_forest_taiga',['berry_patch'],'vegetal_decoration'),'ores':('feature_overworld',['ore_perunite','ore_silver'],'underground_ores'),'flax':('feature_plains_forest',['patch_flax'],'vegetal_decoration'),'shrine':('feature_plains_forest_taiga',['ancient_shrine'],'surface_structures'),'herbs':('feature_plains_forest_taiga',['patch_'+x for x in herbs if x!='flax'],'vegetal_decoration')}
for name,(group,features,step) in rules.items():write('neoforge/biome_modifier','core_'+name,{'type':'neoforge:add_features','biomes':'#slavicmyths:'+group,'features':['slavicmyths:'+x for x in features],'step':step})
# These were runtime-registered configured IDs in the original source, omitted by its scanner.
p=Path('docs/port/baseline-helper-entries-0.9.3.json');helper=json.loads(p.read_text(encoding='utf-8'));old=zipfile.ZipFile('.tools/port-backups/slavicmyths-0.9.3-pre-port.zip').read('src/main/java/org/slavicmyths/world/ModWorldGen.java').decode('utf-8');original_ids=re.findall(r'(?:=\s*register\(|=register\()"([^\"]+)"',old);assert len(original_ids)==15,original_ids
for name in original_ids:helper['entries'].append({'kind':'configured_feature','old_id':'slavicmyths:'+name,'target_id':'slavicmyths:'+name,'source_file':'src/main/java/org/slavicmyths/world/ModWorldGen.java'})
p.write_text(json.dumps(helper,indent=2)+'\n',encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/SlavicMyths.java');s=p.read_text(encoding='utf-8').replace('import org.slavicmyths.world.ModWorldGen;','').replace('        event.enqueueWork(ModWorldGen::registerFeatures);\n','');p.write_text(s,encoding='utf-8')
print('Created 15 preserved configured IDs, matching placed features and 10 biome modifiers; obsolete Java bootstrap can now be removed.')
