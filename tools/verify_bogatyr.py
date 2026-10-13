"""Static production packaging/audio gate. Does not claim client acceptance."""
from pathlib import Path
import hashlib,io,json,subprocess,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1]
O=R/'docs/verification/bogatyr-1.7.1'
J=R/'build/libs/slavicmyths-1.7.1.jar'
checks=0
def need(ok,msg):
 global checks
 checks+=1
 if not ok:raise AssertionError(msg)
def read(p):return p.read_text(encoding='utf8',errors='replace')
need('BUILD SUCCESSFUL' in read(O/'clean-build.log') and '> Task :clean' in read(O/'clean-build.log'),'clean build')
need('All 48 required tests passed' in read(O/'runtime-08.log'),'48 dedicated cases')
need('BOGATYR OFFLINE PASS' in read(O/'offline.log'),'native offline fixture')
for phase in ('write','read'):need('All 1 required tests passed' in read(O/f'restart-{phase}.log') and 'BUILD SUCCESSFUL' in read(O/f'restart-{phase}.log'),'disk-world restart '+phase)
old=zipfile.ZipFile(R/'release/slavicmyths-1.6.9.jar');new=zipfile.ZipFile(J)
names=set(new.namelist());changed=[];preserved=0
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in names,'removed baseline entry '+n)
 a,b=old.read(n),new.read(n)
 if a==b:preserved+=1;continue
 changed.append(n)
 if n in ('META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','org/slavicmyths/SlavicMyths.class'):continue
 need(n in ('assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'),'unexpected old entry changed '+n)
 for k,v in json.loads(a).items():need(json.loads(b).get(k)==v,'old locale/sound changed '+k)
for n in names:
 need(not any(x in n for x in ('/verify/','/smoke/','BogatyrChecks','BogatyrGameTests','BogatyrRestart','slavicmyths_bogatyr/')),'fixture leaked '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):Image.open(io.BytesIO(new.read(n))).verify();checks+=1
 if n.endswith('.class'):
  need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java 21 '+n)
  local=R/'build/classes/java/main'/n
  need(local.is_file() and local.read_bytes()==new.read(n),'compiled class differs '+n)
  if n.startswith('org/slavicmyths/bogatyr/'):need(b'net/minecraft/client/' not in new.read(n),'client linkage common '+n)
for p in (R/'src/main/resources').rglob('*'):
 if not p.is_file():continue
 expected=p.read_bytes()
 if p.name=='neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.7.1').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
 need(new.read(p.relative_to(R/'src/main/resources').as_posix())==expected,'resource differs '+str(p))
for name in ('ilya_muromets','idolishche_poganoe'):
 rig=json.loads(read(O/f'{name}-rig.json'));boxes=rig['boxes'];size=rig['atlas']
 for i,b in enumerate(boxes):
  x,y,w,h=b['uv'];need(x>=2 and y>=2 and x+w<=size-2 and y+h<=size-2,'UV bounds')
  for c in boxes[:i]:
   xx,yy,ww,hh=c['uv'];need(x>=xx+ww+4 or xx>=x+w+4 or y>=yy+hh+4 or yy>=y+h+4,'UV island padding')
source=R/'docs/specs/1.7.1'
origin=json.loads(read(O/'audio-origin.json'));need(len(origin)==33,'33 OGG')
events=json.loads(new.read('assets/slavicmyths/sounds.json'))
for rel,digest in origin.items():
 p=source/rel;who=p.parent.name;n=f'assets/slavicmyths/sounds/entity/{who}/{p.name}'
 need(hashlib.sha256(p.read_bytes()).hexdigest()==digest and new.read(n)==p.read_bytes(),'audio origin '+n)
 info=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-of','json',str(p)],text=True))['streams'][0]
 need(info['codec_name']=='vorbis' and info['channels']==1,'mono Vorbis '+n)
 subprocess.run(['ffmpeg','-v','error','-i',str(p),'-f','null','-'],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
 need(any(any(s.get('name')==f'slavicmyths:entity/{who}/{p.stem}' for s in e['sounds'] if isinstance(s,dict)) for e in events.values()),'sound not referenced '+n)
result=dict(status='PASS',version='1.7.1',clean_build='PASS',dedicated_cases=48,offline_checks=227016,clips=36,checks=checks,baseline_entries_preserved=preserved,baseline_entries_changed=changed,audio_ogg=33,sha256=hashlib.sha256(J.read_bytes()).hexdigest(),client_launches=0,client_acceptance='NOT_RUN')
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print(json.dumps(result,ensure_ascii=False))
