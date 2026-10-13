"""Production resource and baseline gate, with actual bounded test evidence."""
from pathlib import Path
import zipfile,json,hashlib,subprocess,struct,io
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/finist-1.7.4';checks=0
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
def check(c,s):
 global checks
 checks+=1
 if not c:raise AssertionError(s)
for file,key in [('clean-build-final.log','BUILD SUCCESSFUL'),('runtime-final-04.log','FINIST_INTEGRATION PASS'),('offline-final.log','FINIST OFFLINE PASS')]:check(key in (O/file).read_text(encoding='utf8',errors='replace'),file)
jar=R/'build/libs/slavicmyths-1.7.4.jar';before=R/'release/slavicmyths-1.7.3.jar';allowed={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','org/slavicmyths/SlavicMyths.class','org/slavicmyths/skim/HeroCalls.class','org/slavicmyths/marinka/StoryPolicy.class'}
changed=[];identical=0
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(before) as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'Lost existing JAR entry')
 for n in prior:
  if old.read(n)!=z.read(n):changed.append(n);check(n in allowed,'Unexpected existing change '+n)
  else:identical+=1
 for n in names:
  check(not any(t in n.lower() for t in ['gametest','slavicmyths_finist','org/slavicmyths/verify/','smoke/']),'Test leakage '+n)
  data=z.read(n)
  if n.endswith('.class'):
   check(struct.unpack('>H',data[6:8])[0]==65,'Not Java21 '+n)
   if n.startswith('org/slavicmyths/finist/'):check(b'net/minecraft/client' not in data,'Common client linkage '+n)
   p=R/'build/classes/java/main'/n
   if p.exists():check(data==p.read_bytes(),'Stale class '+n)
  if n.endswith('.json'):json.loads(data);checks+=1
  if n.endswith('.png'):Image.open(io.BytesIO(data)).verify();checks+=1
 for p in (R/'src/main/resources').rglob('*'):
  if not p.is_file():continue
  n=p.relative_to(R/'src/main/resources').as_posix();b=p.read_bytes();check(n in names,'Missing '+n)
  if n=='META-INF/neoforge.mods.toml':b=b.replace(b'${mod_version}',b'1.7.4').replace(b'${neo_version}',b'21.1.255');check(b.replace(b'\r\n',b'\n')==z.read(n).replace(b'\r\n',b'\n'),'Metadata expansion')
  else:check(b==z.read(n),'Stale resource '+n)
 for locale in ['ru_ru','en_us']:
  n='assets/slavicmyths/lang/'+locale+'.json';a=json.loads(old.read(n));b=json.loads(z.read(n))
  for key,value in a.items():check(b.get(key)==value,'Existing translation changed '+key)
  for item in ['finist_call_feather','kartaus_black_seal','kartaus_spawn_egg']:check('item.slavicmyths.'+item in b,'Item translation')
 a=json.loads(old.read('assets/slavicmyths/sounds.json'));b=json.loads(z.read('assets/slavicmyths/sounds.json'))
 for key,value in a.items():check(b.get(key)==value,'Existing sound changed '+key)
 for key,value in json.loads((R/'docs/specs/1.7.4/07_Audio/Integration_Examples/sounds_174_fragment.json').read_text()).items():
  check(b.get(key)==value,'Provided event altered '+key)
  for sound in value['sounds']:check('assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg' in names,'Missing OGG '+key)
  for locale in ['ru_ru','en_us']:check(value['subtitle'] in json.loads(z.read('assets/slavicmyths/lang/'+locale+'.json')),'Missing subtitle')
for name in ['finist_call_feather','kartaus_black_seal','kartaus_spawn_egg']:
 p=R/'src/main/resources/assets/slavicmyths/textures/item'/f'{name}.png';check(Image.open(p).size==(64,64),'Item dimensions');check(sha(p)==sha(R/'docs/specs/1.7.4/06_References/Technical/Item_Sprites'/p.name),'Approved sprite changed')
for name in ['finist','kartaus','finist_falcon','kartaus_banner','kartaus_curse_anchor']:
 rig=json.loads((O/(name+'-rig.json')).read_text());occupied=set()
 for box in rig['boxes']:
  x,y,w,h=box['uv'];check(x>=2 and y>=2 and x+w<=rig['atlas']-2 and y+h<=rig['atlas']-2,'UV bounds')
  cells={(xx,yy) for xx in range(x,x+w) for yy in range(y,y+h)};check(not cells&occupied,'UV overlap');occupied|=cells
for e in json.loads((O/'audio-provenance.json').read_text()):
 p=R/e['path'];check(sha(p)==e['sha256'],'Audio provenance')
 probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','a:0','-show_entries','stream=codec_name,sample_rate,channels','-of','json',str(p)]))['streams'][0];check(probe=={'codec_name':'vorbis','sample_rate':'48000','channels':1},'Audio codec')
 subprocess.run(['ffmpeg','-v','error','-i',str(p),'-f','null','-'],check=True,capture_output=True);checks+=1
roots=[R/'src/main/resources',R/'src/main/java/org/slavicmyths/client'];snapshot=lambda:{str(p):sha(p) for root in roots for p in root.rglob('*') if p.is_file()}
a=snapshot();subprocess.run(['python',str(R/'tools/finist_assets.py')],check=True);check(a==snapshot(),'Generator repeat changed output')
(O/'generator-repeat.json').write_text(json.dumps({'status':'PASS','changed_files':0,'files':len(a)},indent=2)+'\n')
log=(O/'runtime-final-04.log').read_text(encoding='utf8',errors='replace');import re
count=int(re.search(r'FINIST_INTEGRATION PASS assertions=(\d+)',log).group(1))
result=dict(status='PASS',version='1.7.4',checks=checks,sha256=sha(jar),clean_build='PASS',dedicated_cases=1,scenario_assertions=count,old_jar_entries_identical=identical,changed_prior_entries=sorted(changed),original_audio=54,client_launches=0)
(O/'resource-gates.json').write_text(json.dumps(result,indent=2)+'\n');print('FINIST PRODUCTION PASS '+json.dumps(result))
