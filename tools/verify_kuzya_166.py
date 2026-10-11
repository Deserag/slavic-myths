"""Verify shipped resources, decoded supplied audio, preservation and production isolation."""
from pathlib import Path
import json,zipfile,hashlib,io,csv,subprocess,array,math,re
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/kuzya-1.6.6';O.mkdir(parents=True,exist_ok=True)
J=R/'build/libs/slavicmyths-1.6.6.jar';checks=0
def need(b,s):
 global checks;checks+=1
 if not b:raise AssertionError(s)
log=(R/'work/kuzya/clean-build.log').read_text(encoding='utf8',errors='replace')
need('BUILD SUCCESSFUL' in log and '> Task :clean' in log,'actual clean build')
need('KUZYA OFFLINE PASS:' in log and 'DOGHEAD OFFLINE PASS' in log,'native regression fixtures')
old=zipfile.ZipFile(R/'release/slavicmyths-1.6.5.jar');new=zipfile.ZipFile(J);names=set(new.namelist());preserved=0;modified=[]
allowed=['org/slavicmyths/SlavicMyths','org/slavicmyths/yaga/YagaHut','org/slavicmyths/yaga/YagaHutPlan','org/slavicmyths/yaga/YagaData']
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in names,'preserve old entry '+n)
 if old.read(n)==new.read(n):preserved+=1;continue
 modified.append(n)
 if n in ['META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml']:continue
 if n.endswith('.class') and any(n==p+'.class' or n.startswith(p+'$') for p in allowed):continue
 if '/kuzya/' in n or n.endswith('/kuzya.png'):continue
 need(n in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','data/slavicmyths/tags/entity_type/combat_protected.json','data/slavicmyths/tags/entity_type/control_immune.json'],'unexpected old modification '+n)
 a=json.loads(old.read(n));b=json.loads(new.read(n))
 if n.endswith('immune.json') or n.endswith('protected.json'):
  for v in a['values']:need(v in b['values'],'old protected type '+str(v))
 else:
  for k,v in a.items():
   if not k.startswith(('kuzya.','subtitles.slavicmyths.kuzya.','entity.slavicmyths.kuzya')):need(b.get(k)==v,'old JSON key '+k)
for n in names:
 need(not any(x in n for x in ['/smoke/','KuzyaChecks','KuzyaRuntime','/slavicmyths_kuzya/','offline-classes']),'no test resources in production '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):Image.open(io.BytesIO(new.read(n))).verify();checks+=1
 if n.endswith('.class'):need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java 21 bytecode '+n)
 if n.startswith('org/slavicmyths/kuzya/') and n.endswith('.class'):need(b'net/minecraft/client/' not in new.read(n),'common/client isolation '+n)
for p in (R/'src/main/resources').rglob('*'):
 if p.is_file():
  expected=p.read_bytes()
  if p.name=='neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.6.6').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
  need(new.read(p.relative_to(R/'src/main/resources').as_posix())==expected,'source resource packed '+str(p))
for p in (R/'build/classes/java/main').rglob('*.class'):need(new.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes(),'current class packed '+str(p))
need(Image.open(io.BytesIO(new.read('assets/slavicmyths/textures/entity/kuzya.png'))).size==(256,256),'real atlas')
sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));cues={k:v for k,v in sounds.items() if k.startswith('kuzya.')};source=(R/'src/main/java/org/slavicmyths/kuzya/KuzyaAssets.java').read_text();audiofiles=[]
for cue,value in cues.items():
 need(json.dumps(cue[6:]) in source,'registered sound '+cue)
 for lang in ['ru_ru','en_us']:need(value['subtitle'] in json.loads(new.read(f'assets/slavicmyths/lang/{lang}.json')),'subtitle '+cue)
 for s in value['sounds']:
  path='assets/slavicmyths/sounds/'+s['name'].split(':')[1]+'.ogg';need(path in names and new.read(path)[:4]==b'OggS','physical audio '+path);audiofiles.append(path)
need(len(set(audiofiles))==48,'48 ready OGGs')
S=R/'docs/kuzya/spec/Slavic_Myths_1.6.4_Kuzya_Codex_TZ';audio=[]
for relative in sorted(set(audiofiles)):
 p=R/'src/main/resources'/relative;original=S/'resources_overlay'/relative;need(p.read_bytes()==original.read_bytes(),'supplied audio preserved '+relative)
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','s16le','-ac','1','-ar','22050','-']);v=array.array('h',raw);rms=math.sqrt(sum(x*x for x in v)/len(v));need(len(v)>200 and rms>1,'decoded non-silent '+relative);audio.append({'file':relative,'seconds':len(v)/22050,'rms':rms})
loot=json.loads(new.read('data/slavicmyths/loot_table/chests/baba_yaga_household_supplies.json'));entries=loot['pools'][0]['entries'];need(loot['pools'][0]['rolls']['min']==4 and loot['pools'][0]['rolls']['max']==7,'4-7 modest groups');need(all(e['name'].startswith('minecraft:') and e['name']!='minecraft:golden_apple' for e in entries),'verified vanilla household items')
server=(R/'work/kuzya/server10.log').read_text(encoding='utf8',errors='replace');need('All 1 required tests passed' in server and 'BUILD SUCCESSFUL' in server,'final dedicated GameTest')
report={'status':'PASS','version':'1.6.6','checks':checks,'clean_build':'PASS','native_offline':re.search(r'KUZYA OFFLINE PASS: (\d+)',log).group(1),'doghead_regression':'PASS','final_game_test_server':'PASS','minecraft_server_launches':10,'minecraft_client_launches':0,'in_game_visual_audio_mp_relog_mspt':'NOT TESTED','jar':str(J),'sha256':hashlib.sha256(J.read_bytes()).hexdigest(),'old_entries_preserved':preserved,'modified_old_entries':modified,'audio_files':48,'registered_cues':len(cues)}
(O/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n');(O/'audio-qa.json').write_text(json.dumps(audio,indent=2)+'\n');(O/'build.log').write_text(log,encoding='utf8');(O/'server-final.log').write_text(server,encoding='utf8');print(json.dumps({k:v for k,v in report.items() if k!='modified_old_entries'}))
