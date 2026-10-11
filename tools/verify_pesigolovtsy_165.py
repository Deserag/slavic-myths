"""Production artifact and frozen-input gates; no gameplay claims."""
from pathlib import Path
import json,zipfile,hashlib,io,math,csv
from PIL import Image
R=Path(__file__).resolve().parents[1];S=Path(json.loads((R/'work/pesigolovtsy/snapshot-location.json').read_text())['path']);out=R/'docs/verification/pesigolovtsy-1.6.5';out.mkdir(parents=True,exist_ok=True);J=R/'build/libs/slavicmyths-1.6.5.jar';checks=0
def need(b,s):
 global checks;checks+=1
 if not b:raise AssertionError(s)
log=(R/'work/pesigolovtsy/final-build.log').read_text(encoding='utf8',errors='replace');need('BUILD SUCCESSFUL' in log,'final clean build and all offline tests')
for marker in ['DOGHEAD OFFLINE PASS','NOCHNITSA OFFLINE PASS:','EARTH OFFLINE PASS:','SIVKA OFFLINE PASS:','Firebird offline PASS: 200020','VISUAL OFFLINE PASS: 15']:need(marker in log,marker)
old=zipfile.ZipFile(R/'work/pesigolovtsy/baseline-1.6.4.jar');new=zipfile.ZipFile(J);names=set(new.namelist());modified=[];preserved=0
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in names,'preserve old entry '+n)
 if old.read(n)==new.read(n):preserved+=1;continue
 modified.append(n)
 if n in ['META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml']:continue
 if n=='org/slavicmyths/SlavicMyths.class':continue
 need(n in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'],'unexpected old change '+n)
 a=json.loads(old.read(n));b=json.loads(new.read(n))
 for k,v in a.items():need(b.get(k)==v,'preserve old key '+n+' '+k)
for n in names:
 need(not any(t in n for t in ['Checks.class','/smoke/','offline-classes','kuzya']),'no test/future-module artifacts '+n)
 if n.endswith('.class'):need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java21 '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):Image.open(io.BytesIO(new.read(n))).verify();checks+=1
 if n.startswith('org/slavicmyths/pesigolovtsy/') and n.endswith('.class'):need(b'net/minecraft/client/' not in new.read(n),'common/server client dependency '+n)
for p in (S/'src/main/resources').rglob('*'):
 if p.is_file() and any(t in p.relative_to(S/'src/main/resources').as_posix() for t in ['pesigolovtsy','pesigolovets']):need(new.read(p.relative_to(S/'src/main/resources').as_posix())==p.read_bytes(),'resource packaged '+str(p))
langs={l:json.loads(new.read('assets/slavicmyths/lang/'+l+'.json')) for l in ['ru_ru','en_us']}
sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));events={k:v for k,v in sounds.items() if k.startswith('doghead.')};need(len(events)==27,'21 sound families + six role hurt/death aliases')
files=[]
for key,event in events.items():
 for language in langs.values():need(event['subtitle'] in language,'subtitle '+key)
 for sound in event['sounds']:
  n='assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg';need(n in names and new.read(n)[:4]==b'OggS','OGG data '+n);files.append(n)
need(len(set(files))==70,'70 originals, aliases only reuse correct role variants')
audio=json.loads((R/'docs/pesigolovtsy/source/audio-manifest.json').read_text())['files'];need(len(audio)==70,'70 decoded QA entries')
for row in audio:
 n='assets/slavicmyths/sounds/'+row['file'];need(hashlib.sha256(new.read(n)).hexdigest()==row['sha256'],'decoded audio SHA '+n);need(row['peak_db']<=-1 and row['rms_db']>-46,'decoded waveform '+n)
for role in ['hunter','scout','leader']:
 need(Image.open(io.BytesIO(new.read('assets/slavicmyths/textures/entity/pesigolovtsy/'+role+'.png'))).size==(256,256),'native 256 atlas '+role)
 need(json.loads(new.read('assets/slavicmyths/models/item/pesigolovets_'+role+'_spawn_egg.json'))['parent']=='minecraft:item/template_spawn_egg','egg '+role)
 for language in langs.values():need('entity.slavicmyths.pesigolovets_'+role in language and 'item.slavicmyths.pesigolovets_'+role+'_spawn_egg' in language,'role translation '+role)
 rig=json.loads((R/f'docs/pesigolovtsy/source/{role}-rig.json').read_text());rects=[]
 for bone in rig['bones']:
  for box in bone['cubes']:
   x,y=box['uv'];w,h,d=box['size'];rect=(x-2,y-2,x-2+math.ceil(2*(w+d))+4,y-2+math.ceil(h+d)+4)
   need(all(rect[2]<=q[0] or q[2]<=rect[0] or rect[3]<=q[1] or q[3]<=rect[1] for q in rects),'UV islands + gutter overlap '+role);rects.append(rect)
 need(len({b['name'] for b in rig['bones']})==len(rig['bones']),'unique rig bones '+role)
need(json.loads(new.read('data/slavicmyths/neoforge/biome_modifier/pesigolovtsy.json'))['type']=='slavicmyths:doghead_spawns','spawn codec')
need(len(json.loads(new.read('data/slavicmyths/loot_table/entities/pesigolovets.json'))['pools'])==3,'real ordinary loot')
need(new.read('org/slavicmyths/client/DogheadClient.class') and new.read('org/slavicmyths/pesigolovtsy/DogheadEntity.class'),'real client/server classes')
need(b'1.6.5' in new.read('META-INF/neoforge.mods.toml'),'actual release version')
need(not any('DogheadGameTests' in n or 'DogheadSceneProbe' in n for n in names),'runtime fixtures excluded from production')
runtime_path=out/'runtime-results.json'
runtime=json.loads(runtime_path.read_text()) if runtime_path.exists() else {'minecraft_launches':0,'status':'NOT TESTED'}
report={'version':'1.6.5','status':'PASS','checks':checks,'clean_build':'PASS','offline':'Doghead/Nochnitsa/Earth/Sivka/Firebird/Visual PASS','jar':str(J),'sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'baseline_entries_preserved':preserved,'modified_baseline_entries':modified,'minecraft_launches':runtime['minecraft_launches'],'gameplay_visual_audio_mp':runtime['status'],'build_snapshot':str(S),'excluded_parallel_work':'Kuzya additions only in build snapshot; checkout preserved'}
(out/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n');(out/'build.log').write_text(log,encoding='utf8');print(json.dumps(report))
