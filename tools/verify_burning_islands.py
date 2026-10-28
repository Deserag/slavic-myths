"""Static source/assets/production JAR gate. Does NOT test natural spawn, AI or gameplay."""
from pathlib import Path
import json,hashlib,re,zipfile
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/implementation/burning-islands-205'
def main():
 baseline=json.loads((O/'baseline-production.json').read_text());changed=[];missing=[]
 for rel,sha in baseline.items():
  p=R/rel
  if not p.is_file():missing.append(rel)
  elif hashlib.sha256(p.read_bytes()).hexdigest()!=sha:changed.append(rel)
 assert not missing,missing
 allowed={'src/main/java/org/slavicmyths/SlavicMyths.java','src/main/java/org/slavicmyths/rpg/Runes.java','src/main/java/org/slavicmyths/rpg/RuneDefinition.java','src/main/java/org/slavicmyths/trophy/TrophyHeads.java','src/main/java/org/slavicmyths/trophy/TrophyHeadBlock.java','src/main/java/org/slavicmyths/client/TrophyHeadsClient.java','src/main/resources/assets/slavicmyths/lang/en_us.json','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/creative_catalog.tsv'}
 assert set(changed)<=allowed,set(changed)-allowed
 # Existing Nether climate source, geology, worldgen and content are byte-preserved.
 ids=['heat_salamander','slag_crab','vent_golem','magma_lord']
 atlas_rects={}
 for name in ids:
  data=json.loads((A/f'burning_islands_rigs/{name}.json').read_text());rects=[];parts=set()
  for p in data['parts']:
   assert p['parent']=='root' or p['parent'] in parts,(name,p['name']);assert p['name'] not in parts;parts.add(p['name'])
   x,y,z=p['size'];u,v=p['uv'];rect=(u,v,u+2*(x+z),v+y+z);assert 0<=u<rect[2]<=data['atlas'] and 0<=v<rect[3]<=data['atlas']
   for other in rects:assert not(max(rect[0],other[0])<min(rect[2],other[2]) and max(rect[1],other[1])<min(rect[3],other[3])),('overlapping UV islands',name)
   rects.append(rect)
  atlas_rects[name]=len(rects)
 for n,extra,chance in [('heat_salamander','salamander_hide',.5),('slag_crab','crab_claw',.1),('vent_golem','magma_rune_fragment',.225)]:
  data=json.loads((D/f'loot_table/entities/{n}.json').read_text());assert len(data['pools'])==2
  assert data['pools'][0]['entries'][0]['name']=='slavicmyths:magma_clot'
  assert data['pools'][1]['entries'][0]['name']=='slavicmyths:'+extra and data['pools'][1]['conditions'][0]['chance']==chance
  assert 'looting' not in json.dumps(data).lower()
 for name,ingredients in [('magma_core',['magma_clot']*4),('rune_magma_1',['magma_rune_fragment','magma_core','salamander_hide','salamander_hide','crab_claw','blank_rune_1']),('rune_magma_2',['rune_magma_1']*2+['magma_core']*2)]:
  data=json.loads((D/f'recipe/{name}.json').read_text());assert [x['item'] for x in data['ingredients']]==['slavicmyths:'+i for i in ingredients];assert data['result']['id']=='slavicmyths:'+name
 mob=(R/'src/main/java/org/slavicmyths/burningislands/IslandsMob.java').read_text()
 assert 'addFreshEntity' not in mob and 'new IslandsMob' not in mob,'alarm must never spawn'
 runes=(R/'src/main/java/org/slavicmyths/burningislands/MagmaRunes.java').read_text()
 assert not any(x in runes for x in ['setBlock(', 'setBlockAndUpdate(', 'getChunk(']),'temporary magma must remain fail-closed'
 biome=json.loads((D/'worldgen/biome/burning_islands.json').read_text());assert all(not step for step in biome['features'])
 assert len(biome['spawners']['creature'])==3
 selector=(R/'src/main/java/org/slavicmyths/navya/mixin/NetherParametersMixin.java').read_text();assert 'burning_islands' not in selector and 'IslandsContent' not in selector,'no unproved climate append'
 jar=R/'build/libs/slavicmyths-2.0.8-HELL.jar'
 with zipfile.ZipFile(jar) as z:
  names=z.namelist();assert 'org/slavicmyths/burningislands/IslandsMob.class' in names
  assert not any('/verify/' in n or 'IslandsOffline' in n or 'OceanAudit' in n or 'Heads140Offline' in n for n in names)
  for n in names:
   if n.startswith('org/slavicmyths/burningislands/') and '/client/' not in n and n.endswith('.class'):
    data=z.read(n);assert b'net/minecraft/client/' not in data and b'org/slavicmyths/burningislands/client/' not in data,('client dependency in common',n)
 result={'scope':'Static source/assets/production JAR only; NOT gameplay or data codec under a registered NeoForge runtime','baselineFiles':len(baseline),'missingBaselineFiles':missing,'changedExistingFiles':changed,'uvIslands':atlas_rects,'naturalBiome':'BLOCKED_OCEAN_BIOME_ARCHITECTURE; not selected','temporaryMagma':'BLOCKED_TEMP_MAGMA_RESTORE; no mutations','audio':'BLOCKED_NEEDS_AUDIO_SOURCE','referenceMatch':'BLOCKED_REFERENCE_BINARY','jar':str(jar.resolve()),'bytes':jar.stat().st_size,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'minecraftLaunches':0}
 (O/'static-jar-results.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
if __name__=='__main__':main()
