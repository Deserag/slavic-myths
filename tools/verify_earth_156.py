"""Offline production/resource proof for the exact Earth Spirit JAR. Never launches Minecraft."""
from pathlib import Path
import hashlib,json,zipfile,re,sys,subprocess
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';SPEC=ROOT/'work/earth-spirit-v2-spec';OUT=ROOT/'docs/verification/earth-1.5.6';OUT.mkdir(parents=True,exist_ok=True)
JAR=ROOT/'build/libs/slavicmyths-1.5.6.jar'
BASE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.5.5.jar')
if not BASE.exists():BASE=Path(json.loads((OUT/'polymc-installation.json').read_text())['previous_jars'][0]['backup'])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
checks=0
def check(ok,label):
 global checks
 checks+=1
 assert ok,label
for row in json.loads((SPEC/'FILES_MANIFEST.json').read_text()):
 p=SPEC/row['path'];check(p.stat().st_size==row['bytes'] and sha(p)==row['sha256'],'archive manifest '+str(p))
changed=['SlavicMyths','rpg/Runes','rpg/RuneDefinition','rpg/RuneRework','rpg/RuneRuntime','progression/Knowledge','client/LoreScreen','depths/DepthsService']
def allowed(n):return any(n=='org/slavicmyths/'+x+'.class' or n.startswith('org/slavicmyths/'+x+'$') for x in changed)
preserved_classes=[];preserved_resources=0
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(BASE) as old:
 names=set(new.namelist())
 for p in (ROOT/'build/classes/java/main').rglob('*.class'):
  n=p.relative_to(ROOT/'build/classes/java/main').as_posix();check(new.read(n)==p.read_bytes(),'packaged class '+n);check(int.from_bytes(new.read(n)[6:8],'big')==65,'Java21 '+n)
 check(not any('/verify/' in n or '/smoke/' in n or 'EarthChecks' in n or 'GameTests.class' in n for n in names),'no offline/smoke/test classes')
 check('version="1.5.6"' in new.read('META-INF/neoforge.mods.toml').decode(),'production version')
 for n in old.namelist():
  if n.endswith('.class') and not allowed(n):check(new.read(n)==old.read(n),'preserved class '+n);preserved_classes.append(n)
  if n.endswith(('.json','.png','.nbt','.ogg')) and n.startswith(('assets/','data/')):
   if n in ['assets/slavicmyths/sounds.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json']:
    a=json.loads(old.read(n));b=json.loads(new.read(n));check(all(b.get(k)==v for k,v in a.items()),'old keys '+n)
   elif n in ['data/slavicmyths/tags/entity_type/bleed_immune.json','data/slavicmyths/tags/entity_type/ominous_aura_immune.json']:
    a=json.loads(old.read(n));b=json.loads(new.read(n));check(all(v in b['values'] for v in a['values']) and a.get('replace',False)==b.get('replace',False),'additive tag '+n)
   else:check(new.read(n)==old.read(n),'preserved resource '+n);preserved_resources+=1
 for p in RES.rglob('*'):
  if not p.is_file() or p.suffix not in ('.json','.png','.nbt','.ogg'):continue
  n=p.relative_to(RES).as_posix();check(new.read(n)==p.read_bytes(),'packaged resource '+n)
  if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
  if p.suffix=='.png':
   with Image.open(p) as im:im.verify()
 for n in names:
  if n.startswith('org/slavicmyths/earth/') and n.endswith('.class'):check(b'net/minecraft/client/' not in new.read(n),'server isolation '+n)
 sounds=json.loads(new.read('assets/slavicmyths/sounds.json'));earth={k:v for k,v in sounds.items() if k.startswith('earth_spirit.')};check(len(earth)==30,'30 cues')
 for lang in ('ru_ru','en_us'):
  data=json.loads(new.read(f'assets/slavicmyths/lang/{lang}.json'))
  for k in ['entity.slavicmyths.earth_spirit','entity.slavicmyths.earth_servant','item.slavicmyths.rune_earth_heart_iii','book.slavicmyths.earth_heart.title','book.slavicmyths.earth_heart.text','advancement.slavicmyths.earth_heart.title']:
   check(bool(data.get(k)),lang+' '+k)
  for cue in earth.values():check(bool(data.get(cue['subtitle'])),lang+' subtitle '+cue['subtitle'])
 for name,size in [('phase_1',256),('phase_2',256),('phase_3',256),('servant_0',64),('servant_1',64),('rock',64),('pillar',128)]:
  with Image.open(RES/f'assets/slavicmyths/textures/entity/earth_spirit/{name}.png') as im:check(im.size==(size,size),'atlas '+name)
 with Image.open(RES/'assets/slavicmyths/textures/item/rune_earth_heart_iii.png') as im:check(im.size==(64,64) and im.mode=='RGBA' and im.getextrema()[3][0]==0,'rune icon')
 for k,event in earth.items():
  for sound in event['sounds']:
   name=sound if isinstance(sound,str) else sound['name'];ns,path=name.split(':');check(ns=='slavicmyths' and 'assets/slavicmyths/sounds/'+path+'.ogg' in names,'audio reference '+k)
 for name in ['earth_heart_seal','earth_pillar_stone','earth_shelter_stone','earth_gate_stone']:
  for n in [f'assets/slavicmyths/blockstates/{name}.json',f'assets/slavicmyths/models/item/{name}.json',f'data/slavicmyths/loot_table/blocks/{name}.json']:check(n in names,'block asset '+n)
 for p in (RES/'assets/slavicmyths/models').rglob('*.json'):
  if 'earth' not in p.stem:continue
  obj=json.loads(p.read_text());parent=obj.get('parent','')
  if parent.startswith('slavicmyths:'):check((RES/'assets/slavicmyths/models'/f"{parent.split(':')[1]}.json").is_file(),'model parent '+str(p))
  for texture in obj.get('textures',{}).values():
   if texture.startswith('slavicmyths:'):check((RES/'assets/slavicmyths/textures'/f"{texture.split(':')[1]}.png").is_file(),'model texture '+str(p))
 check('data/slavicmyths/advancement/earth_heart.json' in names,'victory advancement')
 check('assets/slavicmyths/textures/mob_effect/earth_imbalance.png' in names,'brief balance effect icon')
 check('assets/slavicmyths/models/item/earth_spirit_spawn_egg.json' not in names,'boss egg replaced by binding stone')
 check(b'activate' in new.read('org/slavicmyths/earth/EarthSeal.class'),'binding stone attack hook compiled')
import ctypes,math
# Decode directly with the project's libsndfile; no dependency on another Python ABI.
class SoundInfo(ctypes.Structure):
 _fields_=[('frames',ctypes.c_longlong),('samplerate',ctypes.c_int),('channels',ctypes.c_int),('format',ctypes.c_int),('sections',ctypes.c_int),('seekable',ctypes.c_int)]
sf=ctypes.CDLL(str(ROOT/'.tools/audio-libs/_soundfile_data/libsndfile_x64.dll'))
sf.sf_open.argtypes=[ctypes.c_char_p,ctypes.c_int,ctypes.POINTER(SoundInfo)];sf.sf_open.restype=ctypes.c_void_p
sf.sf_read_float.argtypes=[ctypes.c_void_p,ctypes.POINTER(ctypes.c_float),ctypes.c_longlong];sf.sf_read_float.restype=ctypes.c_longlong
sf.sf_close.argtypes=[ctypes.c_void_p]
audio=[]
for row in json.loads((SPEC/'SOUND_FILES_INDEX.json').read_text()):
 p=RES/'assets/slavicmyths/sounds/earth_spirit'/row['file'];check(sha(p)==sha(SPEC/'assets/slavicmyths/sounds/earth_spirit'/row['file']),'supplied OGG unchanged')
 info=SoundInfo();handle=sf.sf_open(str(p).encode('utf-8'),0x10,ctypes.byref(info));check(bool(handle),'open audio '+p.name)
 data=(ctypes.c_float*(info.frames*info.channels))();read=sf.sf_read_float(handle,data,len(data));sf.sf_close(handle)
 peak=max(abs(v) for v in data);duration=info.frames/info.samplerate
 check(info.format==0x200060 and info.channels==1 and info.samplerate==44100,'OGG format '+p.name)
 check(read==len(data) and all(math.isfinite(v) for v in data) and 0<peak<1 and abs(duration-row['duration_seconds'])<.001,'decoded audio '+p.name)
 audio.append({'file':p.name,'rate':info.samplerate,'channels':info.channels,'duration':duration,'peak':peak,'sha256':sha(p)})
# Reproduction must preserve the complete current resource tree, including older assets.
def resource_hashes():return {p.relative_to(RES).as_posix():sha(p) for p in RES.rglob('*') if p.is_file()}
before=resource_hashes();subprocess.run([sys.executable,str(ROOT/'tools/earth_resources.py')],check=True);check(before==resource_hashes(),'generator reproducibility/no old-resource rollback')
log=(ROOT/'work/earth/final-build.log').read_text(encoding='utf-8-sig');check('BUILD SUCCESSFUL' in log and 'EARTH OFFLINE PASS:' in log,'final clean build and offline checks')
offline=int(re.search(r'EARTH OFFLINE PASS: (\d+) checks',log)[1])
receipt={'version':'1.5.6','jar':str(JAR),'sha256':sha(JAR),'production':'PASS','resources':'PASS','checks':checks,'baseline':str(BASE),'baseline_sha256':sha(BASE),'preserved_classes':len(preserved_classes),'preserved_resources':preserved_resources,'audio':audio,'generator_reproducible':True}
(OUT/'resource-gates.json').write_text(json.dumps(receipt,indent=2),encoding='utf-8')
(OUT/'offline-gates.json').write_text(json.dumps({'build':'PASS','offline_checks':'PASS','checks':offline,'production_sha256':sha(JAR),'runtime_validation':'NOT_RUN','minecraft_launches':0,'limits':'Pure damage math, real SavedData codec, eight existing cave layout variants, native mesh/UV checks; not real combat, relog, animation or MSPT.'},indent=2),encoding='utf-8')
(OUT/'preserved-classes.json').write_text(json.dumps(preserved_classes,indent=2),encoding='utf-8')
print(f'EARTH RESOURCE/PRODUCTION PASS: {checks} checks; {len(preserved_classes)} old classes, {preserved_resources} old resources preserved; 30 OGG decoded; JAR SHA256 {sha(JAR)}')
