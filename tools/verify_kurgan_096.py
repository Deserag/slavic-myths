"""0.9.6 acceptance: explicit authorized burial drift, baseline preservation and real artifact."""
from pathlib import Path
import argparse,collections,hashlib,io,json,re,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/kurgan-0.9.6';OUT.mkdir(parents=True,exist_ok=True)
def require(ok,message):
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
parser=argparse.ArgumentParser();parser.add_argument('--resources-only',action='store_true');args=parser.parse_args()
items=json.loads((ROOT/'art/kurgan-0.9.6/items/provenance.json').read_text(encoding='utf-8'));stones=json.loads((ROOT/'art/kurgan-0.9.6/provenance.json').read_text(encoding='utf-8'));require(len(items)==24 and len(stones)==11,'Authored asset inventory incomplete')
allowed={'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json','src/main/resources/data/minecraft/tags/block/walls.json','src/main/resources/assets/slavicmyths/lang/en_us.json','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/blockstates/pale_blue_kurgan_stone.json'}
for row in items:
 allowed.update('src/main/resources/assets/slavicmyths/'+kind+'/'+row['name']+ext for kind,ext in [('textures/item','.png'),('models/item','.json')])
for row in stones:allowed.update('src/main/resources/assets/slavicmyths/'+kind+'/'+row['name']+ext for kind,ext in [('textures/block','.png'),('models/block','.json')])
changed=[];preserved=0;baseline_assets={}
with zipfile.ZipFile(ROOT/'.tools/port-backups/slavicmyths-0.9.5-pre-0.9.6.zip') as z:
 for name in z.namelist():
  if name.endswith('/') or not name.startswith(('src/main/resources/','src/main/java/')):continue
  if name.startswith('src/main/resources/assets/') and name.endswith('.json'):baseline_assets[name]=json.loads(z.read(name))
  p=ROOT/name;require(p.is_file(),'Baseline file removed '+name);old=z.read(name)
  if p.read_bytes()==old:preserved+=1;continue
  if name.startswith('src/main/java/org/slavicmyths/kurgan/'):continue
  require(name in allowed,'Unexpected content drift '+name);changed.append(name)
  if '/lang/' in name:
   before=json.loads(old);after=json.loads(p.read_text(encoding='utf-8'));require(all(after.get(k)==v for k,v in before.items()),'Existing translation changed '+name)
  elif '/tags/' in name:
   before=json.loads(old);after=json.loads(p.read_text(encoding='utf-8'));require(set(map(str,before['values']))<=set(map(str,after['values'])) and not after.get('replace',False),'Tag membership removed '+name)
  elif '/models/item/' in name and json.loads(old).get('elements'):
   held=p.with_name(p.stem+'_held.json');require(json.loads(held.read_text(encoding='utf-8'))==json.loads(old),'Held model changed '+name)
assets=[]
for kind,rows in [('item',items),('block',stones)]:
 for row in rows:
  p=RES/'assets/slavicmyths/textures'/kind/(row['name']+'.png');im=Image.open(p);im.load();require(im.size==(256,256),'Incorrect runtime dimensions '+str(p))
  source=ROOT/row['authorSource'];original=Image.open(source);require(min(original.size)>=256,'Small authored source '+str(source))
  if kind=='item':require(im.mode=='RGBA' and im.getextrema()[3][0]==0,'Opaque icon background '+str(p))
  assets.append({'path':p.relative_to(ROOT).as_posix(),'runtimeDimensions':list(im.size),'sourceDimensions':list(original.size),'source':source.relative_to(ROOT).as_posix(),'sha256':sha(p)})
# Resolve every model/state reference against the pinned Minecraft assets and this mod.
vanilla=ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
with zipfile.ZipFile(vanilla) as z:vanilla_names=set(z.namelist())
refs=0;baseline_issues=[];active_file=None
def resource(value,folder,ext):
 global refs
 if value.startswith('#'):return
 ns,name=(value.split(':',1) if ':' in value else ('minecraft',value));name='assets/'+ns+'/'+folder+'/'+name+ext
 if ns=='minecraft' and name in ['assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json']:return
 if not ((RES/name).is_file() or name in vanilla_names):
  relative='src/main/resources/'+active_file.relative_to(RES).as_posix();original=baseline_assets.get(relative)
  if original is None and relative.endswith('_held.json'):original=baseline_assets.get(relative.replace('_held.json','.json'))
  require(original is not None and json.dumps(value) in json.dumps(original),'Missing NEW resource '+name+' in '+relative)
  baseline_issues.append({'model':relative,'missingReference':name,'scope':'unchanged 0.9.5 resource; outside burial rework'})
 refs+=1
def walk(value):
 if isinstance(value,list):
  for v in value:walk(v)
 elif isinstance(value,dict):
  for key,v in value.items():
   if key in ['parent','model'] and isinstance(v,str):resource(v,'models','.json')
   elif key=='textures' and isinstance(v,dict):
    for tex in v.values():
     if isinstance(tex,str):resource(tex,'textures','.png')
   else:walk(v)
for folder in ['models','blockstates']:
 for p in (RES/'assets/slavicmyths'/folder).rglob('*.json'):
  active_file=p;walk(json.loads(p.read_text(encoding='utf-8')))
native=json.loads((ROOT/'tools/templates/minecraft-1.21.1-stone-brick-stairs.json').read_text(encoding='utf-8'))
for row in stones:
 name=row['name']+'_stairs';file=RES/'assets/slavicmyths/blockstates'/(name+'.json')
 if file.exists():
  expected={'variants':{key:{**value,'model':value['model'].replace('minecraft:block/stone_brick_stairs','slavicmyths:block/'+name)} for key,value in native['variants'].items()}}
  require(json.loads(file.read_text(encoding='utf-8'))==expected,'Stair rotation differs from real Minecraft 1.21.1 '+name)
additions=json.loads((ROOT/'docs/kurgan/registry-additions-0.9.6.json').read_text(encoding='utf-8'))['entries'];require(len(additions)==52 and len({(r['kind'],r['target_id']) for r in additions})==52,'New IDs drift')
for row in additions:
 ns,name=row['target_id'].split(':');folder='blockstates' if row['kind']=='block' else 'models/item';require((RES/'assets'/ns/folder/(name+'.json')).exists(),'No model for '+row['target_id'])
for locale in ['en_us','ru_ru']:
 lang=json.loads((RES/'assets/slavicmyths/lang'/(locale+'.json')).read_text(encoding='utf-8'))
 for row in additions:
  if row['kind']=='block':require('block.slavicmyths.'+row['target_id'].split(':')[1] in lang,'Missing translation')
report={'status':'PASS','scope':'authored assets, model references, explicit burial changes, preserved non-burial content','changedExistingResources':changed,'existingBaselineAssetIssues':baseline_issues,'preservedBaselineFiles':preserved,'resourceReferencesChecked':refs,'assets':assets,'registryAdditions':additions,'clientLaunches':0,'manualVisualQA':False}
(OUT/'resource-check.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if not args.resources_only:
 jar=ROOT/'build/libs/slavicmyths-0.9.6.jar';require(jar.is_file(),'No production JAR')
 with zipfile.ZipFile(jar) as z:
  names=z.namelist();require('version="0.9.6"' in z.read('META-INF/neoforge.mods.toml').decode(),'Wrong mod version')
  require(not any(n.startswith(('org/slavicmyths/verify/','xaero/','mezz/','top/theillusivec4/')) for n in names),'Test/dependency code bundled');require('data/slavicmyths/structure/port_empty.nbt' not in names,'Test template bundled')
  for n in names:
   if n.endswith('.class'):require(int.from_bytes(z.read(n)[6:8],'big')==65,'Non Java21 class')
  for p in RES.rglob('*'):
   if p.is_file():
    name=p.relative_to(RES).as_posix();require(name in names,'Resource omitted from artifact '+name)
    if name!='META-INF/neoforge.mods.toml':require(z.read(name)==p.read_bytes(),'Stale packaged resource '+name)
 acceptance=json.loads((OUT/'acceptance.json').read_text(encoding='utf-8'));require(acceptance['jar_sha256']==sha(jar),'Gated JAR changed')
 for name,digest in acceptance['evidence_sha256'].items():require(sha(OUT/name)==digest,'Evidence changed '+name)
 report['jarSha256']=sha(jar);report['scope']='production artifact and recorded final gates';(OUT/'final-check.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('PASS: new IDs=52 authored textures=35 model references='+str(refs)+' preserved baseline files='+str(preserved)+'; pre-existing missing model references='+str(len(baseline_issues))+'; client not launched')
