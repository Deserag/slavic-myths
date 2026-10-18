"""Scoped #59/#53 audio/native/JAR gates. All runtime acceptance remains NOT_RUN."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile,ast
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import soundfile as sf,numpy as np
from PIL import Image
BASE='307419b3';checks=[]
def check(value,label):
 if not value:raise AssertionError(label)
 checks.append(label)
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def main():
 a=R/'src/main/resources/assets/slavicmyths';audio=R/'art/audio/rusalka-59';canonical=json.loads((audio/'events.json').read_text(encoding='utf-8'));events=json.loads((a/'sounds.json').read_text(encoding='utf-8'))
 for k,v in canonical['events'].items():
  expected=dict(v);expected['sounds']=[];check(events[k]==expected,'Pending-owner mapping remains quarantined '+k)
 for k,v in json.loads(old('src/main/resources/assets/slavicmyths/sounds.json')).items():
  if not k.startswith('rusalka_'):check(events[k]==v,'Other sound unchanged '+k)
 names=[]
 for entry in json.loads((audio/'outputs.json').read_text(encoding='utf-8')):
  p=a/'sounds'/entry['file'];data,sr=sf.read(p);names.append(p.stem)
  check(hashlib.sha256(p.read_bytes()).hexdigest()==entry['sha256'],'Approved edit bytes '+p.name)
  check(sr==44100 and data.ndim==1 and len(data)>sr/2,'Valid mono Vorbis '+p.name)
  check(np.max(abs(data))<.7 and -30<entry['integrated_lufs']<-15,'Peak / integrated loudness '+p.name)
  check(p.read_bytes()[:4]==b'OggS','True OGG container '+p.name)
 check(len(names)==12,'Twelve real edited recordings')
 check(len({x['sha256'] for x in json.loads((audio/'outputs.json').read_text(encoding='utf-8'))})==12,'No identical edited event files')
 for spec in json.loads((audio/'sources.json').read_text(encoding='utf-8')):
  check(spec['license']=='CC0-1.0' and spec['author'] and spec['page'].startswith('https://freesound.org/people/'),'Attribution and source '+spec['name'])
  check(hashlib.sha256((audio/spec['file']).read_bytes()).hexdigest()==spec['sha256'],'Recorded input integrity '+spec['name'])
 for p in (a/'sounds/water').glob('rusalka_*.ogg'):check(p.read_bytes()==old(p.relative_to(R).as_posix()),'Legacy baseline preserved '+p.name)
 loop,sr=sf.read(a/'sounds/water/rusalka/song_loop.ogg');freq=[]
 for start in [.45,2.13,3.81,5.49]:
  y=loop[int(sr*start):int(sr*(start+.35))];spec=abs(np.fft.rfft(y*np.hanning(len(y))));hz=np.fft.rfftfreq(len(y),1/sr);band=(hz>=190)&(hz<=330);freq.append(float(hz[band][np.argmax(spec[band])]))
 check(245<freq[0]<280 and 280<freq[1]<315 and 205<freq[2]<240 and 280<freq[3]<315,'Recorded C-D-A-D motif frequencies '+str(freq))
 check(abs(loop[-1]-loop[0])<.001,'Loop pause/seam')
 import rusalka_audio_59 as generator
 temp=R/'build/quality-corrections/regen';temp.mkdir(parents=True,exist_ok=True)
 generator.generate(temp,temp/'outputs.json')
 for name in names:
  x,sr=sf.read(temp/(name+'.ogg'));y,_=sf.read(a/'sounds/water/rusalka'/(name+'.ogg'));check(x.shape==y.shape and np.max(abs(x-y))<1e-6,'Generator PCM reproducibility '+name)
 before=(a/'sounds.json').read_bytes();generator.restore_events();check((a/'sounds.json').read_bytes()==before,'Event restoration is idempotent')
 tree=ast.parse((R/'tools/water_070.py').read_text(encoding='utf-8'));soundfn=next(x for x in tree.body if isinstance(x,ast.FunctionDef) and x.name=='sounds');check(not any(isinstance(x,ast.For) for x in ast.walk(soundfn)) and 'sf.write' not in ast.unparse(soundfn),'Legacy water generator never synthesizes voices')
 check('restore_events(A)' in (R/'tools/water_070.py').read_text(encoding='utf-8'),'Legacy importer restores recorded vocal mappings')
 mesh=json.loads((R/'build/quality-corrections/meshes.json').read_text());tex=Image.open(a/'textures/entity/polevik.png').convert('RGBA')
 for key,faces in mesh.items():
  for face in faces:
   for point in face:check(all(np.isfinite(point)) and 0<=point[3]<=1 and 0<=point[4]<=1,'Native finite mesh/UV '+key)
   if key.startswith('polevik'):
    u=sum(p[3] for p in face)/4;v=sum(p[4] for p in face)/4;check(tex.getpixel((min(255,int(u*256)),min(255,int(v*256))))[3]>0,'Polevik face alpha '+key)
 for path in ['src/main/java/org/slavicmyths/water/VodyanoyEntity.java','src/main/java/org/slavicmyths/water/WaterSpirit.java','src/main/java/org/slavicmyths/entity/PolevikEntity.java','src/main/java/org/slavicmyths/registry/ModEntities.java','src/main/resources/assets/slavicmyths/textures/entity/polevik.png']:
  check((R/path).read_bytes().replace(b'\r\n',b'\n')==old(path).replace(b'\r\n',b'\n'),'Unchanged mechanics / ID / texture '+path)
 common=(R/'src/main/java/org/slavicmyths/water/RusalkaEntity.java').read_text(encoding='utf-8');client=(R/'src/main/java/org/slavicmyths/client/RusalkaSound.java').read_text(encoding='utf-8')
 check('APPROVED_VOCALS_AVAILABLE=false' in client,'Production vocal gate remains off pending owner')
 for token in ['songElapsed()','previewing()','if(previewing())','owner.isCreative()','getPlayerByUUID','RUSALKA_SONG_BREAK','RUSALKA_GRAB','encounter.hostile'] :check(token in common,'Encounter/preview callsite '+token)
 for token in ['RUSALKA_SONG_INTRO','RUSALKA_SONG_LOOP','RUSALKA_SONG_INTENSE','manager.isActive(sound)','getSoundManager().stop(this)','ACTIVE.remove(singer.getUUID(),this)','ClientTickEvent.Post','stage!=RusalkaEncounter.Vocal.INTRO']:check(token in client,'Managed vocal lifecycle '+token)
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    name=p.relative_to(R/'src/main/resources').as_posix()
    if name!='META-INF/neoforge.mods.toml':check(z.read(name)==p.read_bytes(),'JAR resource '+name)
  check(not any('org/slavicmyths/verify/' in n or '/smoke/' in n for n in z.namelist()),'No fixtures in production')
 installed=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods')
 prior=[dict(file=p.name,sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in installed.glob('slavicmyths-*.jar')]
 check(len(prior)==1,'Read-only installed profile has one Slavic Myths JAR')
 report=dict(status='IN_PROGRESS',checks=len(checks),jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),recorded_notes_hz=freq,new_ogg_count=12,installed_polymc_read_only=prior,installation='NOT_UPDATED_PENDING_AUDIO_ACCEPTANCE_59',runtime_launches=0,in_game_audio='NOT_RUN',in_game_visual='NOT_RUN',gameplay='NOT_RUN')
 out=R/'docs/implementation/quality-59-53';out.mkdir(parents=True,exist_ok=True);(out/'gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
