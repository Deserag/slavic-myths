"""Check the actual production artifact against the accepted 1.6.8 baseline.

This is a packaging/static/audio gate, not a client or performance test.
"""
from pathlib import Path
import array,csv,hashlib,io,json,math,re,subprocess,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1]
O=R/'docs/verification/bayun-book-final'
J=R/'build/libs/slavicmyths-1.6.9.jar'
checks=0
def need(value,message):
 global checks
 checks+=1
 if not value:raise AssertionError(message)
def read(p):return p.read_text(encoding='utf8',errors='strict')
log=read(R/'work/bayun/clean-build-final.log')
need('BUILD SUCCESSFUL' in log and '> Task :clean' in log,'completed clean build')
for marker in ('BAYUN OFFLINE PASS','KUZYA OFFLINE PASS','DOGHEAD OFFLINE PASS'):
 need(marker in log,'native fixture '+marker)
server=read(R/'work/bayun/gates-final.log')
need('All 15 required tests passed' in server and 'BUILD SUCCESSFUL' in server,'latest dedicated server suite')
restart=read(R/'work/bayun/restart-read3.log')
need('BAYUN_RESTART_READ PASS' in restart,'actual disk-world restart')
old=zipfile.ZipFile(R/'release/slavicmyths-1.6.8.jar');new=zipfile.ZipFile(J)
names=set(new.namelist());modified=[];preserved=0
allowed={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/textures/item/lore_book.png',
 'data/minecraft/tags/damage_type/bypasses_armor.json',
 'data/slavicmyths/tags/entity_type/combat_protected.json','data/slavicmyths/tags/entity_type/control_immune.json'}
for n in old.namelist():
 if n.endswith('/'):continue
 need(n in names,'existing resource/class removed: '+n)
 a,b=old.read(n),new.read(n)
 if a==b:preserved+=1;continue
 modified.append(n)
 if n in allowed:continue
 if n.endswith('.class') and any(n==p+'.class' or n.startswith(p+'$') for p in ('org/slavicmyths/SlavicMyths','org/slavicmyths/item/LoreBookItem')):continue
 if n.startswith('data/slavicmyths/advancement/'):
  a,b=json.loads(a),json.loads(b)
  for key in ('criteria','requirements','rewards'):need(a.get(key)==b.get(key),'legacy advancement '+key+' '+n)
  if 'display' in a:need(a['display']==b.get('display'),'legacy display changed '+n)
  need(set(b)-set(a)<= {'display','parent'},'unexpected advancement field '+n)
  continue
 need(n in ('assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'),'unexpected baseline change '+n)
 a,b=json.loads(a),json.loads(b)
 for k,v in a.items():
  if n.endswith('/ru_ru.json') and k in {'entity.slavicmyths.podzemnik','entity.slavicmyths.podzemnik_miner','entity.slavicmyths.kladovik','entity.slavicmyths.large_podzemnik'}:need(b.get(k)==v.encode('cp1251').decode('utf8'),'exact repaired legacy entity name '+k)
  else:need(b.get(k)==v,'legacy locale/sound changed '+k)
for n in names:
 need(not any(v in n for v in ('/verify/','/smoke/','BayunChecks','BayunGameTests','BayunRestart','slavicmyths_bayun/','offline-classes')),'test artifact leaked '+n)
 if n.endswith('.json'):json.loads(new.read(n));checks+=1
 if n.endswith('.png'):Image.open(io.BytesIO(new.read(n))).verify();checks+=1
 if n.endswith('.class'):
  need(int.from_bytes(new.read(n)[6:8],'big')==65,'Java 21 '+n)
  if n.startswith(('org/slavicmyths/bayun/','org/slavicmyths/tales/')):need(b'net/minecraft/client/' not in new.read(n),'server/client boundary '+n)
for p in (R/'src/main/resources').rglob('*'):
 if not p.is_file():continue
 expected=p.read_bytes()
 if p.name=='neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.6.9').replace(b'${neo_version}',b'21.1.255').replace(b'\r\n',b'\n')
 need(new.read(p.relative_to(R/'src/main/resources').as_posix())==expected,'current resource not packaged '+str(p))
for p in (R/'build/classes/java/main').rglob('*.class'):
 need(new.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes(),'current class not packaged '+str(p))
before=json.loads(read(O/'registry-audit.json'));after=json.loads(read(O/'registry-after.json'))
delta={}
for k,a in before.items():
 if k not in after or not isinstance(a,list):continue
 ids=lambda values:{x['id'] if isinstance(x,dict) else x for x in values}
 need(ids(a)<=ids(after[k]),'removed registered IDs '+k)
 delta[k]=sorted(ids(after[k])-ids(a))
expected={'items':['slavicmyths:kot_bayun_spawn_egg'],'entity_type':['slavicmyths:kot_bayun'],
 'worldgen/feature':['slavicmyths:bayun_nook'],'worldgen/configured_feature':['slavicmyths:bayun_nook'],
 'worldgen/placed_feature':['slavicmyths:bayun_nook']}
for k,v in delta.items():need(v==expected.get(k,[]),'unexpected registry changes '+k)
entries={p.stem:json.loads(read(p)) for p in (R/'src/main/resources/data/slavicmyths/tales').glob('*.json')}
need(len(entries)==217,'catalogue count')
notes=json.loads(read(R/'docs/bayun/creature-notes.json'));need(len(notes)==58,'individually authored living profiles')
locale=json.loads(new.read('assets/slavicmyths/lang/ru_ru.json'))
for key in notes:
 for stage,(lo,hi) in zip(('rumor','discovered','studied','verified'),((45,95),(45,100),(60,130),(80,160))):
  need(lo<=len(locale['tales.entry.'+key+'.'+stage].split())<=hi,'authored Russian density '+key+' '+stage)
need(json.loads(read(O/'regeneration.json'))['changed']==[],'reproducible generation')
coverage=list(csv.DictReader((O/'CONTENT_COVERAGE.csv').open(encoding='utf8',newline='')))
need(len(coverage)==1666,'coverage count')
for row in coverage:
 need(row['book_entry_id'] and all(x.split(':',1)[1] in entries for x in row['book_entry_id'].split(';')),'coverage owner '+row['id'])
oldadv={k.replace('\\','/').split('/advancement/',1)[1][:-5] for k in json.loads(read(O/'before-advancements.json'))}
adv={p.relative_to(R/'src/main/resources/data/slavicmyths/advancement').as_posix()[:-5]:json.loads(read(p)) for p in (R/'src/main/resources/data/slavicmyths/advancement').rglob('*.json')}
need(len(adv)==175,'advancement count')
for key,value in adv.items():
 seen=set();at='slavicmyths:'+key
 while at.startswith('slavicmyths:'):
  need(at not in seen,'advancement cycle '+key);seen.add(at)
  node=adv.get(at.split(':',1)[1]);need(node is not None,'missing parent '+at)
  at=node.get('parent','')
 if key not in oldadv:need(not value.get('rewards',{}).get('experience',0),'new unearned XP '+key)
ru=json.loads(new.read('assets/slavicmyths/lang/ru_ru.json'));en=json.loads(new.read('assets/slavicmyths/lang/en_us.json'))
oldru=json.loads(old.read('assets/slavicmyths/lang/ru_ru.json'));olden=json.loads(old.read('assets/slavicmyths/lang/en_us.json'))
owned={k for k in ru if k not in oldru};need(owned=={k for k in en if k not in olden},'new locale key parity')
for k in owned:
 v=ru[k];need('\ufffd' not in v,'replacement character '+k)
 try:repaired=v.encode('cp1251').decode('utf8')
 except (UnicodeEncodeError,UnicodeDecodeError):repaired=v
 need(repaired==v,'mojibake '+k)
 need(bool(re.search('[А-Яа-яЁё]',v)) or k.startswith('tales.recipe.tag.'),'Russian primary '+k)
audio=[];sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));cues={k:v for k,v in sounds.items() if k.startswith('bayun.')}
need(len(cues)==19,'19 routed sound events')
spec=R/'docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ'
for cue,value in cues.items():
 need(value['subtitle'] in ru and value['subtitle'] in en,'subtitle '+cue)
 for sound in value['sounds']:
  n='assets/slavicmyths/sounds/'+sound['name'].split(':')[1]+'.ogg';p=R/'src/main/resources'/n
  originals=list(spec.rglob(p.name));need(len(originals)==1 and originals[0].read_bytes()==p.read_bytes(),'authoritative supplied audio '+n)
  metadata=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-of','json',str(p)]))['streams'][0]
  need(metadata['channels']==1,'mono positional sample '+n)
  pcm=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','s16le','-ac','1','-ar','22050','-']);v=array.array('h',pcm)
  rms=math.sqrt(sum(x*x for x in v)/len(v));peak=max(abs(x) for x in v)
  need(len(v)>200 and rms>1 and peak<32767,'non-silent unclipped decoded audio '+n)
  audio.append(dict(file=n,seconds=len(v)/22050,rms=rms,peak=peak,channels=1,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
need(len(audio)==19,'19 physical samples')
need(Image.open(io.BytesIO(new.read('assets/slavicmyths/textures/entity/kot_bayun/skin.png'))).size==(256,256),'dense authored atlas')
model=json.loads(read(R/'docs/bayun/kot_bayun.bbmodel'));need(len(model['elements'])==254 and len(model['animations'])==17,'editable authored model')
report=dict(status='PASS',version='1.6.9',checks=checks,jar=str(J),sha256=hashlib.sha256(J.read_bytes()).hexdigest(),
 clean_build='PASS',native_offline='PASS',dedicated_cases=15,actual_disk_restart='PASS',client_launches=0,
 client_visual_audio_multiplayer='NOT_RUN',mspt_fps='NOT_MEASURED',baseline_entries_preserved=preserved,
 modified_baseline_entries=modified,new_locale_keys=len(owned),catalogue_entries=len(entries),registry_delta=delta,
 content_editorial_density='PASS: 58 authored Russian living-creature profiles; technical projection grouped with Tugarin')
for file,data in [('resource-gates.json',report),('audio-qa.json',audio)]:
 (O/file).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(O/'build.log').write_text(log,encoding='utf8');(O/'server-final.log').write_text(server,encoding='utf8')
print(json.dumps({k:v for k,v in report.items() if k not in ('modified_baseline_entries',)},ensure_ascii=False))
