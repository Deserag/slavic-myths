"""0.9.7: preserve all non-swamp content, check target assets/templates and production JAR."""
from pathlib import Path
import argparse,hashlib,json,zipfile,sys
from PIL import Image
from verify_swamp_073 import read_nbt
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';OUT=R/'docs/verification/swamp-0.9.7';OUT.mkdir(exist_ok=True);parser=argparse.ArgumentParser();parser.add_argument('--resources-only',action='store_true');args=parser.parse_args()
def require(ok,message):
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
allowed_java=['compat/xaero/XaeroNavigationBridge.java','navigation/client/NavigationClient.java','compat/JeiRituals.java','swamp/SwampStructure.java','swamp/SwampPiece.java','swamp/SwampStructures.java','swamp/SwampCommands.java','water/WaterSpawns.java','water/WaterSpirit.java','client/ClientSetup.java','registry/ModItems.java','registry/ModEntities.java','registry/ModFeatures.java']
allowed={'src/main/java/org/slavicmyths/'+s for s in allowed_java};allowed.update('src/main/resources/assets/slavicmyths/'+s for s in ['lang/ru_ru.json','lang/en_us.json','blockstates/linden_door.json',*[f'models/block/linden_door_{s}.json' for s in ['bottom','bottom_hinge','top','top_hinge']]])
ids=['swamp_hut','abandoned_settlement','bog_causeway','flooded_shrine','fishing_camp','underwater_ruins','swamp_remnants']
allowed.update('src/main/resources/data/slavicmyths/'+s for s in ['neoforge/biome_modifier/woodland_swamp.json',*[f'tags/worldgen/biome/structures/{i}.json' for i in ids],*[f'worldgen/structure_set/{i}.json' for i in ['bog_causeway','swamp_remnants','fishing_camp','swamp_hut','flooded_shrine','abandoned_settlement']]])
changed=[];preserved=0;baseline={}
with zipfile.ZipFile(R/'.tools/port-backups/slavicmyths-0.9.6-pre-0.9.7.zip') as z:
 for n in z.namelist():
  if not n.startswith(('src/main/java/','src/main/resources/')):continue
  p=R/n;require(p.is_file(),'Removed baseline '+n);raw=z.read(n)
  if '/assets/' in n and n.endswith('.json'):baseline[n]=json.loads(raw)
  if p.read_bytes()==raw:preserved+=1;continue
  require(n in allowed,'Unrelated content changed '+n);changed.append(n)
  if '/lang/' in n:before=json.loads(raw);after=json.loads(p.read_text(encoding='utf-8'));require(all(after.get(k)==v for k,v in before.items()),'Existing translation changed')
vanilla=zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');native_names=set(vanilla.namelist());refs=0;issues=[];active=None
builtin={'assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json'}
def reference(v,folder,suffix):
 global refs
 if v.startswith('#'):return
 ns,name=v.split(':',1) if ':' in v else ('minecraft',v);target=f'assets/{ns}/{folder}/{name}{suffix}';refs+=1
 if not (RES/target).is_file() and target not in native_names and target not in builtin:
  original=baseline.get('src/main/resources/'+active.relative_to(RES).as_posix());require(original is not None and v in json.dumps(original),'Missing NEW asset '+target)
  issues.append({'model':active.relative_to(RES).as_posix(),'missingReference':target,'scope':'pre-existing non-linden door; unchanged'})
def walk(v):
 if isinstance(v,list):
  for x in v:walk(x)
 elif isinstance(v,dict):
  for key,x in v.items():
   if key in ['parent','model'] and isinstance(x,str):reference(x,'models','.json')
   elif key=='textures' and isinstance(x,dict):
    for t in x.values():
     if isinstance(t,str):reference(t,'textures','.png')
   else:walk(x)
for folder in ['models','blockstates']:
 for p in (RES/'assets/slavicmyths'/folder).rglob('*.json'):active=p;walk(json.loads(p.read_text(encoding='utf-8')))
expected=json.loads(vanilla.read('assets/minecraft/blockstates/oak_door.json'))
for v in expected['variants'].values():v['model']=v['model'].replace('minecraft:block/oak_door','slavicmyths:block/linden_door')
require(json.loads((RES/'assets/slavicmyths/blockstates/linden_door.json').read_text(encoding='utf-8'))==expected,'Door orientations mismatch native 1.21.1')
templates=json.loads((R/'docs/swamp/templates-0.9.7.json').read_text(encoding='utf-8'));require(len(templates)==21,'Template count');hashes={}
for name,meta in templates.items():
 p=RES/f'data/slavicmyths/structure/swamp/{name}.nbt';doc=read_nbt(p.read_bytes());require(doc['DataVersion']==3955,'Old template format');require(doc['size']==meta['size'],'Wrong size');require(len(doc['blocks'])==meta['blocks'],'Wrong blocks')
 positions={tuple(b['pos']) for b in doc['blocks']};require(len(positions)==len(doc['blocks']),'Duplicate cells')
 loot=0
 for b in doc['blocks']:
  require(all(0<=v<dim for v,dim in zip(b['pos'],doc['size'])),'Template bounds');state=doc['palette'][b['state']];block=state['Name'];ns,key=block.split(':');require((RES/f'assets/{ns}/blockstates/{key}.json').is_file() or f'assets/{ns}/blockstates/{key}.json' in native_names,'Missing template block '+block)
  marker=b.get('nbt',{}).get('metadata','')
  if marker.startswith('loot:'):loot+=1;require((RES/f'data/slavicmyths/loot_table/chests/{marker[5:]}.json').is_file(),'Missing marker loot')
 require(loot==meta['lootPoints'] and loot>=1,'Empty important structure');hashes[name]=sha(p)
for kind in ['fisher','abandoned','watchtower','shrine','boardwalk','landing','cluster']:require(len({h for n,h in hashes.items() if n.startswith('v097_'+kind+'_')})==3,'Repeated structure variants '+kind)
for ident in [*ids,'swamp_watchtower']:
 values=json.loads((RES/f'data/slavicmyths/tags/worldgen/biome/structures/{ident}.json').read_text(encoding='utf-8'))['values'];require(values==['minecraft:swamp'],'Non-vanilla-swamp target')
require(Image.open(RES/'assets/slavicmyths/textures/entity/bolotnik.png').size==(256,256),'Bog texture size')
report={'status':'PASS','preservedBaselineFiles':preserved,'authorizedChangedFiles':changed,'assetReferences':refs,'existingBaselineAssetIssues':issues,'nativeLindenDoorStates':len(expected['variants']),'templates':hashes,'clientLaunched':False,'manualVisualQA':False};(OUT/'resource-check.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if not args.resources_only:
 jar=R/'build/libs/slavicmyths-0.9.7.jar';require(jar.is_file(),'No JAR')
 with zipfile.ZipFile(jar) as z:
  names=set(z.namelist());require('version="0.9.7"' in z.read('META-INF/neoforge.mods.toml').decode(),'Wrong artifact version')
  require(not any(n.startswith(('org/slavicmyths/verify/','xaero/','mezz/','top/theillusivec4/')) for n in names),'Bundled dependencies/tests');require('org/slavicmyths/swamp/SwampGameTests.class' not in names,'Test code bundled');require('data/slavicmyths/structure/port_empty.nbt' not in names,'Test fixture bundled')
  for n in names:
   if n.endswith('.class'):require(int.from_bytes(z.read(n)[6:8],'big')==65,'Not Java21')
  for p in RES.rglob('*'):
   if p.is_file():
    n=p.relative_to(RES).as_posix();require(n in names,'Resource omitted '+n)
    if n!='META-INF/neoforge.mods.toml':require(z.read(n)==p.read_bytes(),'Stale artifact resource '+n)
 acceptance=json.loads((OUT/'acceptance.json').read_text(encoding='utf-8'));require(acceptance['jar_sha256']==sha(jar),'Ungated artifact')
 for n,digest in acceptance['evidence_sha256'].items():require(sha(OUT/n)==digest,'Evidence changed '+n)
 report['jarSha256']=sha(jar);(OUT/'final-check.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'PASS: 21 modern templates / 7 distinct families, native door {len(expected["variants"])} states, asset refs {refs}, old assets preserved {preserved}, pre-existing non-linden door issues {len(issues)}')
