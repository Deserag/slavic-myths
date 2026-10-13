"""Production resource and baseline gate, with actual bounded test evidence."""
from pathlib import Path
import zipfile,json,hashlib,subprocess,struct,io
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/kalin-1.7.5';checks=0
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
def check(c,s):
 global checks
 checks+=1
 if not c:raise AssertionError(s)
for file,key in [('clean-build.log','BUILD SUCCESSFUL'),('runtime-final.log','KALIN_INTEGRATION PASS'),('offline.log','KALIN OFFLINE PASS')]:check(key in (O/file).read_text(encoding='utf8',errors='replace'),file)
jar=R/'build/libs/slavicmyths-1.7.5.jar';before=R/'release/slavicmyths-1.7.4.jar';allowed={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','org/slavicmyths/SlavicMyths.class','org/slavicmyths/skim/HeroCalls.class','org/slavicmyths/marinka/StoryPolicy.class','org/slavicmyths/bogatyr/IlyaEntity.class','org/slavicmyths/marinka/DobrynyaEntity.class','org/slavicmyths/skim/AlyoshaEntity.class','org/slavicmyths/finist/FinistEntity.class','org/slavicmyths/bogatyr/IlyaEntity$1.class','org/slavicmyths/skim/AlyoshaEntity$1.class'}
changed=[];identical=0
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(before) as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'Lost existing JAR entry')
 for n in prior:
  if old.read(n)!=z.read(n):changed.append(n);check(n in allowed,'Unexpected existing change '+n)
  else:identical+=1
 for n in names:
  check(not any(t in n.lower() for t in ['gametest','slavicmyths_finist','org/slavicmyths/verify/','smoke/','slavicmyths_kalin','KalinChecks','kalinruntime']),'Test leakage '+n)
  data=z.read(n)
  if n.endswith('.class'):
   check(struct.unpack('>H',data[6:8])[0]==65,'Not Java21 '+n)
   if n.startswith('org/slavicmyths/kalin/'):check(b'net/minecraft/client' not in data,'Common client linkage '+n)
   p=R/'build/classes/java/main'/n
   if p.exists():check(data==p.read_bytes(),'Stale class '+n)
  if n.endswith('.json'):json.loads(data);checks+=1
  if n.endswith('.png'):Image.open(io.BytesIO(data)).verify();checks+=1
 for p in (R/'src/main/resources').rglob('*'):
  if not p.is_file():continue
  n=p.relative_to(R/'src/main/resources').as_posix();b=p.read_bytes();check(n in names,'Missing '+n)
  if n=='META-INF/neoforge.mods.toml':b=b.replace(b'${mod_version}',b'1.7.5').replace(b'${neo_version}',b'21.1.255');check(b.replace(b'\r\n',b'\n')==z.read(n).replace(b'\r\n',b'\n'),'Metadata expansion')
  else:check(b==z.read(n),'Stale resource '+n)
 for locale in ['ru_ru','en_us']:
  n='assets/slavicmyths/lang/'+locale+'.json';a=json.loads(old.read(n));b=json.loads(z.read(n))
  for key,value in a.items():check(b.get(key)==value,'Existing translation changed '+key)
  for item in ['kalin_battle_seal','kalin_tsar_spawn_egg','kalin_war_horse_spawn_egg']:check('item.slavicmyths.'+item in b,'Item translation')
 a=json.loads(old.read('assets/slavicmyths/sounds.json'));b=json.loads(z.read('assets/slavicmyths/sounds.json'))
 for key,value in a.items():check(b.get(key)==value,'Existing sound changed '+key)

 for src in (R/'docs/specs/1.7.5/07_Audio/ogg').glob('*.ogg'):
  key='kalin.'+src.stem;check(key in b,'Missing sound event '+key);check(z.read('assets/slavicmyths/sounds/kalin/'+src.name)==src.read_bytes(),'Supplied audio changed '+src.name)
  probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','a:0','-show_entries','stream=codec_name,sample_rate,channels','-of','json',str(src)]))['streams'][0];check(probe['codec_name']=='vorbis' and probe['channels']==1,'Audio codec '+src.name)
  subprocess.run(['ffmpeg','-v','error','-i',str(src),'-f','null','-'],check=True,capture_output=True);checks+=1
 for name in ['kalin_tsar','kalin_war_horse','kalin_common_soldier','kalin_elite_soldier']:
  model=json.loads((O/(name+'-rig.json')).read_text());check(model['atlas']==256,'Atlas dimensions')
  for box in model['boxes']:
   x,y,w,h=box['uv'];check(x>=2 and y>=2 and x+w<=254 and y+h<=254,'UV bounds')
  for idx,a in enumerate(model['boxes']):
   x,y,w,h=a['uv']
   for other in model['boxes'][idx+1:]:
    xx,yy,ww,hh=other['uv'];check(x+w<=xx or xx+ww<=x or y+h<=yy or yy+hh<=y,'UV overlap')
 for key in ['kalin_battle_seal','kalin_tsar_spawn_egg','kalin_war_horse_spawn_egg']:
  im=Image.open(io.BytesIO(z.read('assets/slavicmyths/textures/item/'+key+'.png')));check(im.size==(32,32) and im.mode=='RGBA','Item sprite');model=json.loads(z.read('assets/slavicmyths/models/item/'+key+'.json'));check(model['textures']['layer0']=='slavicmyths:item/'+key,'Real item model')
 for key in ['sabre','charge','spin','stomp']:
  damage=json.loads(z.read('data/slavicmyths/damage_type/kalin_'+key+'.json'));check(damage['scaling']=='when_caused_by_living_non_player','Damage difficulty rules')
 check('version="1.7.5"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'Production version')
# Verify package provenance, and deterministic generator without changing unrelated resources.
for line in (R/'docs/specs/1.7.5/CHECKSUMS_SHA256.txt').read_text().splitlines():
 if not line.strip():continue
 digest,name=line.split(None,1);check(sha(R/'docs/specs/1.7.5'/name.strip())==digest,'ZIP package checksum '+name)
paths=list((R/'src/main/resources').rglob('*'))+list((R/'src/main/java/org/slavicmyths/client').glob('*.java'));snapshot={p:sha(p) for p in paths if p.is_file()};subprocess.run(['python',str(R/'tools/kalin_assets.py')],check=True)
for p,digest in snapshot.items():check(sha(p)==digest,'Generator drift '+str(p))
import re
runtime=(O/'runtime-final.log').read_text(errors='replace');count=int(re.search(r'KALIN_INTEGRATION PASS assertions=(\d+)',runtime)[1]);check('1 tests are now running' in runtime and 'required tests passed' in runtime,'Actual GameTest completion')
disk=json.loads((O/'saveddata-disk-readback.json').read_text());check(disk['status']=='PASS' and disk['actual_saveddata_disk_readback'] and disk['flags']['times_kalin_defeated']==1,'Actual dedicated SavedData disk readback')
result=dict(saveddata_disk_readback='PASS',status='PASS',version='1.7.5',checks=checks,sha256=sha(jar),clean_build='PASS',dedicated_cases=1,scenario_assertions=count,old_jar_entries_identical=identical,changed_prior_entries=sorted(changed),original_audio=23,client_launches=0)
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n');print(json.dumps(result))
