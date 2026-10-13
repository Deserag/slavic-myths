"""Release gate for the production NeoForge artifact, original assets and actual test evidence."""
from pathlib import Path
import json,hashlib,zipfile,struct,re,subprocess,shutil
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/skim-1.7.3';checks=0
sha=lambda b:hashlib.sha256(b).hexdigest()
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
def log(name,needle):
 text=(O/name).read_text(encoding='utf8',errors='replace');check(needle in text,name+' missing '+needle);return text
log('clean-build-final.log','BUILD SUCCESSFUL');log('runtime-final-02.log','All 44 required tests passed');log('marinka-regression-02.log','All 36 required tests passed');log('ilya-regression.log','All 48 required tests passed');log('difficulty-01.log','All 1 required tests passed');log('restart-read-01.log','SKIM_RESTART_READ PASS');log('restart-write-01.log','SKIM_RESTART_WRITE');log('offline-final.log','SKIM OFFLINE PASS')
jar=R/'build/libs/slavicmyths-1.7.3.jar';baseline=R/'release/slavicmyths-1.7.2.jar';check(jar.is_file(),'JAR absent')
allowed={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','org/slavicmyths/SlavicMyths.class','org/slavicmyths/marinka/StoryPolicy.class','org/slavicmyths/marinka/DobrynyaEntity.class','org/slavicmyths/marinka/MarinkaEntity.class','org/slavicmyths/bogatyr/IlyaEntity.class'}
changed=[];same=0
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'Lost prior JAR entries')
 for name in sorted(prior):
  if z.read(name)!=old.read(name):changed.append(name);check(name in allowed or name in {'org/slavicmyths/bogatyr/IlyaEntity$1.class','org/slavicmyths/marinka/DobrynyaEntity$1.class','org/slavicmyths/marinka/MarinkaEntity$1.class'},'Unexpected changed baseline '+name)
  else:same+=1
 for name in names:
  check(not any(t in name.lower() for t in ('gametest','slavicmyths_skim','org/slavicmyths/verify/','smoke/','testmatrix','restart-checkpoint')),'Test leakage '+name)
  b=z.read(name)
  if name.endswith('.class'):
   check(struct.unpack('>H',b[6:8])[0]==65,'Java21 '+name)
   if name.startswith('org/slavicmyths/skim/'):check(b'net/minecraft/client' not in b,'Client linked in common '+name)
   source=R/'build/classes/java/main'/name
   if source.exists():check(source.read_bytes()==b,'Stale class '+name)
  elif name.endswith('.json'):json.loads(b);checks+=1
  elif name.endswith('.png'):
   import io
   Image.open(io.BytesIO(b)).verify();checks+=1
 for file in (R/'src/main/resources').rglob('*'):
  if not file.is_file():continue
  name=file.relative_to(R/'src/main/resources').as_posix();check(name in names,'Missing resource '+name)
  expected=file.read_bytes()
  if name=='META-INF/neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.7.3').replace(b'${neo_version}',b'21.1.255')
  check((expected.replace(b'\r\n',b'\n')==z.read(name).replace(b'\r\n',b'\n')) if name=='META-INF/neoforge.mods.toml' else expected==z.read(name),'Stale resource '+name)
 for lang in ('ru_ru','en_us'):
  before=json.loads(old.read('assets/slavicmyths/lang/'+lang+'.json'));after=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'))
  for k,v in before.items():check(after.get(k)==v,'Existing localization overwritten '+k)
 before=json.loads(old.read('assets/slavicmyths/sounds.json'));sounds=json.loads(z.read('assets/slavicmyths/sounds.json'))
 for k,v in before.items():check(sounds.get(k)==v,'Existing sound overwritten '+k)
 for k in ('alyosha_popovich','skim_beast'):
  name='assets/slavicmyths/textures/entity/'+k+'.png';check(Image.open(R/'src/main/resources'/name).size==(256,256),'Atlas size '+k)
  rig=json.loads((O/(k+'-rig.json')).read_text(encoding='utf8'));occupied=set()
  for box in rig['boxes']:
   x,y,w,h=box['uv'];check(x>=2 and y>=2 and x+w<=254 and y+h<=254,'UV bounds')
   cells={(xx,yy) for xx in range(x,x+w) for yy in range(y,y+h)};check(not cells&occupied,'UV overlap');occupied.update(cells)
 for k in ('alyosha_call_whistle','ilya_call_token','dobrynya_call_emblem','beast_challenge_horn','skim_beast_spawn_egg','idolishche_poganoe_spawn_egg','marinka_spawn_egg'):
  check(Image.open(R/'src/main/resources/assets/slavicmyths/textures/item'/f'{k}.png').size==(64,64),'Item size '+k)
  for lang in ('ru_ru','en_us'):check('item.slavicmyths.'+k in json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json')),'Item locale')
 for e in json.loads((O/'audio-provenance.json').read_text()):
  file=R/e['path'];check(sha(file.read_bytes())==e['sha256'],'Audio source changed '+e['event']);event=sounds[e['event']];check(event['sounds'][0]['name'].replace('slavicmyths:','assets/slavicmyths/sounds/')+'.ogg' in names,'Sound path')
  for lang in ('ru_ru','en_us'):check(event['subtitle'] in json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json')),'Subtitle absent')
  probe=subprocess.run(['ffprobe','-v','error','-select_streams','a:0','-show_entries','stream=codec_name,sample_rate,channels','-of','json',str(file)],capture_output=True,text=True,check=True)
  st=json.loads(probe.stdout)['streams'][0];check(st=={'codec_name':'vorbis','sample_rate':'48000','channels':1},'Codec mismatch '+e['event'])
  subprocess.run(['ffmpeg','-v','error','-i',str(file),'-f','null','-'],capture_output=True,check=True);checks+=1
# Resource generator must reproduce current output without touching any older assets.
roots=[R/'src/main/resources',R/'src/main/java/org/slavicmyths/client']
before={str(p):sha(p.read_bytes()) for root in roots for p in root.rglob('*') if p.is_file()}
subprocess.run(['python',str(R/'tools/skim_assets.py')],check=True)
after={str(p):sha(p.read_bytes()) for root in roots for p in root.rglob('*') if p.is_file()}
check(before==after,'Generator changed checked sources/resources')
(O/'generator-repeat.json').write_text(json.dumps({'status':'PASS','changed_files':0,'files':len(before)},indent=2)+'\n',encoding='utf8')
result={'status':'PASS','version':'1.7.3','checks':checks,'sha256':sha(jar.read_bytes()),'clean_build':'PASS','dedicated_cases':44,'regression_cases':84,'difficulty':'PASS','actual_disk_restart':'PASS','client_launches':0,'old_jar_entries_identical':same,'changed_prior_entries':changed}
(O/'resource-gates.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf8');print('SKIM PRODUCTION PASS '+json.dumps(result))
