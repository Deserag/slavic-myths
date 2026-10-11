"""Grey Wolf production/resources/preservation gates. Never claims client QA."""
from pathlib import Path
import json,hashlib,zipfile,math,subprocess,struct
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/grey_wolf_1.4.1';checks=0

def check(ok,msg):
 global checks
 checks+=1
 if not ok:raise AssertionError(msg)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
version='1.4.1';jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
check('mod_version='+version in (ROOT/'gradle.properties').read_text(),'version')
allowed={'src/main/java/org/slavicmyths/'+x for x in ['client/ClientSetup.java','combat/RareBleedEffect.java','entity/WildlifeEntity.java','navigation/MarkerCategory.java','registry/ModEffects.java','registry/ModEntities.java','registry/ModItems.java','registry/ModSounds.java','compat/xaero/XaeroNavigationBridge.java']}
allowed|={'src/main/resources/assets/slavicmyths/'+x for x in ['sounds.json','lang/en_us.json','lang/ru_ru.json']}
base=json.loads((ROOT/'work/grey-wolf/baseline.json').read_text(encoding='utf-8'));changed=[];preserved=0
for name,sha in base.items():
 p=ROOT/name;check(p.is_file(),'missing baseline '+name)
 if digest(p)!=sha:check(name in allowed,'unexpected baseline change '+name);changed.append(name)
 else:preserved+=1
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));check(True,'JSON '+str(p))
for p in RES.rglob('*.png'):
 with Image.open(p) as im:im.verify()
 check(True,'PNG '+str(p))
geo=json.loads((RES/'assets/slavicmyths/grey_wolf_geometry.json').read_text());check(geo['texture_size']==[512,512],'atlas size');names=set();rect=[]
for p in geo['parts']:check(p['name'] not in names and (p['parent'] is None or p['parent'] in names),'hierarchy');names.add(p['name'])
for b in geo['boxes']:
 check(b['part'] in names and all(math.isfinite(n) for n in b['b']),'cube geometry');u,v=b['uv'];w,h,d=map(math.ceil,b['b'][3:]);r=(u,v,u+2*(w+d)+2,v+h+d+2)
 check(r[2]<=512 and r[3]<=512 and u>=0 and v>=0,'UV bounds')
 for q in rect:check(not (r[0]<q[2] and r[2]>q[0] and r[1]<q[3] and r[3]>q[1]),'overlapping packed UV')
 rect.append(r)
metrics=json.loads((OUT/'previews/measurements.json').read_text());ratios=[a/b for a,b in zip(metrics['wolf_neutral_render_geometry']['size_blocks'],metrics['bear_neutral_render_geometry']['size_blocks'])];check(all(1.9<r<2.1 for r in ratios),'wolf/bear ratio')
owned=json.loads((ROOT/'tools/grey_wolf_text.json').read_text(encoding='utf-8'))
for lang in ['ru_ru','en_us']:
 actual=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
 for k,v in owned[lang].items():check(actual.get(k)==v,'localized '+k)
for p in (ROOT/'src/main/java/org/slavicmyths/wolf').glob('*.java'):check('net.minecraft.client' not in p.read_text(),'common client dependency')
audio=[]
for name,seconds in [('summon_howl',3.95),('pack_answer',2.0)]:
 p=RES/f'assets/slavicmyths/sounds/grey_wolf/{name}.ogg';probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(p)]));stream=probe['streams'][0]
 check(stream['codec_name']=='vorbis' and stream['channels']==1 and stream['sample_rate']=='44100','Vorbis mono');check(abs(float(probe['format']['duration'])-seconds)<.03,'audio duration')
 pcm=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','f32le','-ac','1','pipe:1']);values=struct.unpack('<'+'f'*(len(pcm)//4),pcm);peak=max(map(abs,values));rms=math.sqrt(sum(x*x for x in values)/len(values));check(0.001<rms<.7 and .05<peak<=1,'audio energy/clipping');audio.append({'name':name,'duration':float(probe['format']['duration']),'peak':peak,'rms':rms,'sha256':digest(p)})
with zipfile.ZipFile(jar) as z:
 names=z.namelist();check(not any('/verify/' in n or 'slavicmyths_wolf/structure/' in n or 'slavicmyths_unique/structure/' in n for n in names),'test fixture leaked')
 check('version="1.4.1"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'production metadata')
 for p in RES.rglob('*'):
  if p.is_file() and p.suffix in ['.json','.png','.ogg']:
   n=p.relative_to(RES).as_posix();check(n in names,'missing JAR asset '+n);check(z.read(n)==p.read_bytes(),'different JAR asset '+n)
 for n in names:
  if n.endswith('.class') and n.startswith('org/slavicmyths/'):
   check(int.from_bytes(z.read(n)[6:8],'big')==65,'Java21 class')
 required=['org/slavicmyths/wolf/GreyWolf.class','org/slavicmyths/client/GreyWolfRenderer.class','assets/slavicmyths/models/item/grey_wolf_spawn_egg.json','assets/slavicmyths/textures/mob_effect/wolf_cut.png']
 for n in required:check(n in names,'production '+n)
report={'version':version,'checks':checks,'production_jar':str(jar),'sha256':digest(jar),'preserved_baseline_files':preserved,'changed_baseline_files':changed,'parts':len(geo['parts']),'cubes':len(geo['boxes']),'wolf_to_bear_visual_ratios':ratios,'audio':audio,'client_launches':0,'manual_qa_open':True}
OUT.mkdir(parents=True,exist_ok=True);(OUT/'resource_gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
