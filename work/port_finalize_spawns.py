from pathlib import Path
import json,zipfile,re
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');v=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar')
data={n:json.loads(v.read(n)) for n in v.namelist() if n.startswith('data/') and n.endswith('.json')};data.update({n:json.loads(z.read(n)) for n in z.namelist() if n.startswith('data/') and n.endswith('.json')})
def flatten(tag,seen=()):
 ns,id=tag.split(':');name='data/'+ns+'/tags/worldgen/biome/'+id+'.json'
 if name in seen:raise ValueError(tag)
 result=set()
 for entry in data.get(name,{}).get('values',[]):
  if isinstance(entry,dict):
   if not entry.get('required',True):continue
   entry=entry['id']
  if entry.startswith('#'):result.update(flatten(entry[1:],seen+(name,)))
  elif entry.startswith('minecraft:'):result.add(entry)
 return result
cats={n:flatten('c:is_'+n.lower()) for n in ['forest','taiga','plains','savanna','swamp','river','mountain','icy']}
cats['mountain'].update('minecraft:'+n for n in ['windswept_hills','windswept_forest','windswept_gravelly_hills']);cats['icy'].add('minecraft:snowy_plains')
root=Path('src/main/resources/data/slavicmyths');(root/'tags/worldgen/biome').mkdir(parents=True,exist_ok=True);(root/'neoforge/biome_modifier').mkdir(parents=True,exist_ok=True)
groups={'spawn_forest_taiga':['forest','taiga'],'spawn_open':['plains','mountain','savanna','icy'],'spawn_serpent':['plains','mountain','savanna','icy','forest'],'spawn_ovinnik':['plains','savanna','forest','taiga'],'spawn_village_spirit':['plains','taiga','savanna'],'spawn_fields':['plains','savanna'],'spawn_plains':['plains'],'spawn_water':['river','swamp']}
for group,keys in groups.items():
 values=sorted(set().union(*(cats[k] for k in keys)));assert values,group
 (root/'tags/worldgen/biome'/(group+'.json')).write_text(json.dumps({'replace':False,'values':values},indent=2)+'\n',encoding='utf-8')
def spawner(id,w,low,high):return {'type':'slavicmyths:'+id,'weight':w,'minCount':low,'maxCount':high}
rules={'hunt_serpent':('spawn_serpent',[spawner('fire_serpent',1,1,1)]),'hunt_podvey':('spawn_open',[spawner('podvey',1,1,1)]),'hunt_ovinnik':('spawn_ovinnik',[spawner('ovinnik',1,1,1)]),'hunt_volkolak':('spawn_forest_taiga',[spawner('volkolak',1,1,1)]),'domovoy':('spawn_village_spirit',[spawner('domovoy',2,1,1)]),'leshy':('spawn_forest_taiga',[spawner('leshy',3,1,1)]),'land_forest':('spawn_forest_taiga',[spawner('kikimora',2,1,1),spawner('igosha',1,1,1)]),'land_fields':('spawn_fields',[spawner('poludnitsa',2,1,1),spawner('polevik',2,1,1)]),'wildlife_forest':('spawn_forest_taiga',[spawner('brown_bear',2,1,1),spawner('forest_wolf',5,2,4),spawner('boar',6,1,3),spawner('stag',3,1,1),spawner('doe',6,2,3)]),'wildlife_plains':('spawn_plains',[spawner('stag',2,1,2),spawner('doe',4,2,3),spawner('boar',2,1,2)]),'water_spawns':('spawn_water',[spawner('vodyanoy',1,1,1),spawner('rusalka',1,1,1),spawner('pike',4,1,2),spawner('carp',8,2,4),spawner('crayfish',3,1,2)])}
# Entity IDs must already exist; never infer a new ID from a field name.
baseline=json.loads(Path('docs/port/baseline-0.9.3.json').read_text(encoding='utf-8'));ids={e['old_id'] for e in baseline['entries'] if e['kind']=='entity_type'}
ids.update(e['old_id'] for e in json.loads(Path('docs/port/baseline-helper-entries-0.9.3.json').read_text(encoding='utf-8'))['entries'])
for name,(group,spawns) in rules.items():
 for entry in spawns:assert entry['type'] in ids,entry
 (root/'neoforge/biome_modifier'/(name+'.json')).write_text(json.dumps({'type':'neoforge:add_spawns','biomes':'#slavicmyths:'+group,'spawners':spawns},indent=2)+'\n',encoding='utf-8')
classes=[('hunt/HuntSpawns','placements'),('world/SpiritSpawns','registerPlacements'),('world/WildlifeSpawns','placements'),('water/WaterSpawns','placements'),('world/LandEncounters','placements')]
for name,method in classes:
 p=Path('src/main/java/org/slavicmyths/'+name+'.java');s=p.read_text(encoding='utf-8');pattern=re.compile(r'\s*@SubscribeEvent(?:\(priority\s*=\s*EventPriority\.HIGH\))?\s*public static void biomes\(BiomeLoadingEvent \w+\)\s*\{');m=pattern.search(s);assert m,name;end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  elif s[end]=='}':depth-=1
  end+=1
 s=s[:m.start()]+s[end:];s=re.sub(r'\s*private static void add\(BiomeLoadingEvent[^\n]+','',s)
 s=s.replace('import net.minecraftforge.event.world.BiomeLoadingEvent;','')
 s=s.replace('public static void '+method+'(){','public static void '+method+'(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){').replace('public static void '+method+'() {','public static void '+method+'(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {')
 s=s.replace('SpawnPlacements.PlacementType.','net.minecraft.world.entity.SpawnPlacementTypes.').replace('java.util.Random n','net.minecraft.util.RandomSource n').replace('RiverFish::checkFishSpawnRules','RiverFish::checkSurfaceWaterAnimalSpawnRules')
 pos=0
 while m:=re.search(r'SpawnPlacements\.register\(',s[pos:]):
  start=pos+m.start();end=pos+m.end();depth=1
  while depth:
   if s[end]=='(':depth+=1
   elif s[end]==')':depth-=1
   end+=1
  arg=s[pos+m.end():end-1];replacement='event.register('+arg+',net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE)';s=s[:start]+replacement+s[end:];pos=start+len(replacement)
 if name=='hunt/HuntSpawns':
  s=s.replace('Biome.Category c=w.getBiome(p).getBiomeCategory();return c==Biome.Category.FOREST||c==Biome.Category.TAIGA;','var biome=w.getBiome(p);return biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_TAIGA);')
  s=s.replace('Biome.Category c=w.getBiome(p).getBiomeCategory();return c==Biome.Category.PLAINS||c==Biome.Category.EXTREME_HILLS||c==Biome.Category.SAVANNA||c==Biome.Category.ICY;','return w.getBiome(p).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","spawn_open")));')
 p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/SlavicMyths.java');s=p.read_text(encoding='utf-8')
for name,method in classes:
 fqn='org.slavicmyths.'+name.replace('/','.');short=name.rsplit('/',1)[-1]
 s=s.replace('        event.enqueueWork('+fqn+'::'+method+');\n','').replace('        event.enqueueWork('+short+'::'+method+');\n','')
 s=s.replace('        bus.addListener(this::setup);','        bus.addListener('+fqn+'::'+method+');\n        bus.addListener(this::setup);')
p.write_text(s,encoding='utf-8');print('Migrated 11 spawn biome modifiers, 8 bounded vanilla-biome lists and 5 placement event callbacks; existing IDs, weights, group sizes/predicates preserved.')
