"""Production resources and preservation of the installed 1.6.3 baseline."""
from pathlib import Path
import json,zipfile,hashlib,io,csv
from PIL import Image
R=Path(__file__).resolve().parents[1];out=R/'docs/verification/nochnitsa-1.6.4';J=R/'build/libs/slavicmyths-1.6.4.jar';checks=0
def need(b,s):
 global checks;checks+=1
 if not b:raise AssertionError(s)
log=(R/'work/nochnitsa/build.log').read_text(encoding='utf8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build and offline tests')
for s in ['NOCHNITSA OFFLINE PASS:','EARTH OFFLINE PASS:','SIVKA OFFLINE PASS:','Firebird offline PASS: 200020','VISUAL OFFLINE PASS: 15']:need(s in log,s)
old=zipfile.ZipFile(R/'work/nochnitsa/baseline-1.6.3.jar');new=zipfile.ZipFile(J);modified=[];preserved=0
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in new.namelist(),'preserve '+n)
 if old.read(n)==new.read(n):preserved+=1;continue
 modified.append(n)
 if n in ['META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml']:continue
 if n.endswith('.class') and any(n==p+'.class' or n.startswith(p+'$') for p in ['org/slavicmyths/SlavicMyths','org/slavicmyths/earth/EarthConfig']):continue
 need(n in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'],'unexpected old entry modification '+n)
 a=json.loads(old.read(n));b=json.loads(new.read(n))
 for k,v in a.items():need(b.get(k)==v,'preserve '+n+' '+k)
for n in new.namelist():
 need(not any(t in n for t in ['Checks.class','/smoke/','offline-classes']),'no test fixtures '+n)
 if n.endswith('.class'):need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java21 '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):Image.open(io.BytesIO(new.read(n))).verify();checks+=1
 if n.startswith('org/slavicmyths/nochnitsa/') and n.endswith('.class'):need(b'net/minecraft/client/' not in new.read(n),'common client dependency '+n)
for p in (R/'src/main/resources').rglob('*'):
 if p.is_file() and 'nochnitsa' in p.as_posix():need(new.read(p.relative_to(R/'src/main/resources').as_posix())==p.read_bytes(),'packaged '+str(p))
for lang in ['ru_ru','en_us']:
 v=json.loads(new.read('assets/slavicmyths/lang/'+lang+'.json'));need(v.get('entity.slavicmyths.nochnitsa') and v.get('item.slavicmyths.nochnitsa_spawn_egg'),'localized names')
sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));events={k:v for k,v in sounds.items() if k.startswith('nochnitsa.')};need(len(events)==12,'12 registered sound families');files=[]
for k,v in events.items():
 for lang in ['ru_ru','en_us']:need(v['subtitle'] in json.loads(new.read('assets/slavicmyths/lang/'+lang+'.json')),'subtitle '+k)
 for s in v['sounds']:
  p='assets/slavicmyths/sounds/'+s['name'].split(':')[1]+'.ogg';need(p in new.namelist() and new.read(p)[:4]==b'OggS','real audio '+p);files.append(p)
need(len(files)==len(set(files))==31,'31 distinct OGG assets');need(json.loads((out/'audio-qa.json').read_text())['status']=='PASS','audio decoded QA')
for name in ['nochnitsa','nochnitsa_emissive']:need(Image.open(io.BytesIO(new.read('assets/slavicmyths/textures/entity/'+name+'.png'))).size==(128,128),'128 atlas '+name)
need(json.loads(new.read('data/slavicmyths/loot_table/entities/nochnitsa.json'))['pools']==[],'empty ordinary loot')
need(json.loads(new.read('assets/slavicmyths/models/item/nochnitsa_spawn_egg.json'))['parent']=='minecraft:item/template_spawn_egg','real existing-tab egg model')
need(json.loads(new.read('data/slavicmyths/neoforge/biome_modifier/nochnitsa.json'))['type']=='slavicmyths:nochnitsa_spawns','registered biome modifier')
report={'version':'1.6.4','status':'PASS','checks':checks,'clean_build':'PASS','offline':'Nochnitsa/Earth/Sivka/Firebird/Visual PASS','jar':str(J),'sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'baseline_entries_preserved':preserved,'modified_baseline_entries':modified,'minecraft_launches':0,'gameplay_visual_audio_mp':'NOT TESTED'}
(out/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n');(out/'build.log').write_text(log,encoding='utf8')
spec=R/'work/nochnitsa-spec/Slavic_Myths_1.6.2_Nochnitsa_Codex_TZ/manifests/acceptance_tests.csv';rows=list(csv.DictReader(spec.read_text(encoding='utf-8-sig').splitlines()))
for row in rows:
 row['status']='PASS' if row['test_id'] in ['SND01','BUILD01'] else 'NOT TESTED IN GAME'
 row['evidence']={'SND01':'audio-qa.json: 31 actual decoded Vorbis assets','BUILD01':'build.log and resource-gates.json'}.get(row['test_id'],'Static audit / offline checks available; no client, dedicated server or multiplayer launch')
with (out/'acceptance_tests.csv').open('w',encoding='utf8',newline='') as f:
 writer=csv.DictWriter(f,fieldnames=rows[0].keys());writer.writeheader();writer.writerows(rows)
print(json.dumps({k:v for k,v in report.items() if k!='modified_baseline_entries'}))
