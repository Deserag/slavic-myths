"""Scoped QA for three existing events; no repeat of the full audio inventory."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path[:0]=[str(R/'tools'),str(R/'.tools/audio-libs')]
import soundfile as sf
from audio_groups_61_plan import DEFERRED,BLOCKED
BASE='d7327451';A=R/'src/main/resources/assets/slavicmyths'

def main():
 old=json.loads(subprocess.check_output(['git','show',BASE+':src/main/resources/assets/slavicmyths/sounds.json'],cwd=R))
 current=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 assert set(current)==set(old)
 assert {e for e in old if old[e]!=current[e]}=={'karachun.voice_whisper','cub_hurt','cub_death'}
 assert not subprocess.check_output(['git','diff',BASE,'--','src/main/java'],cwd=R).strip(),'No Java/mechanics changes permitted'
 for event in old:
  if event.startswith('igosha'):assert current[event]==old[event],event
 assert set(DEFERRED)=={'igosha_ambient','igosha_hurt','igosha_death'}
 assert set(BLOCKED)-set(DEFERRED)=={'stag_hurt','stag_death','doe_hurt','doe_death'}
 paths=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE,'src/main/resources/assets/slavicmyths/sounds'],cwd=R,text=True).splitlines()
 for path in paths:
  if 'igosha' in path:assert (R/path).read_bytes()==subprocess.check_output(['git','show',BASE+':'+path],cwd=R),path
 selection=json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8'))
 rows=[r for r in selection if r['event'] in {'karachun.voice_whisper','cub_hurt','cub_death'}];assert len(rows)==5
 pcm=[]
 for row in rows:
  p=A/'sounds'/row['file'];x,sr=sf.read(p);assert sr==44100 and x.ndim==1 and max(abs(x))<.8
  pcm.append(hashlib.sha256(x.tobytes()).hexdigest())
  if row['event']=='karachun.voice_whisper':assert len(x)/sr/.94<24/20
  if row['event']=='cub_hurt':assert len(x)/sr/.8<10/20
 assert len(set(pcm))==5
 assert all('polar_bear' not in json.dumps(current[e]) for e in ['cub_hurt','cub_death'])
 version=next(s.split('=',1)[1] for s in (R/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  credit=z.read('assets/slavicmyths/audio_licenses/project-61.txt').decode('utf-8')
  assert 'YleArkisto' in credit and 'https://creativecommons.org/licenses/by/4.0/' in credit and 'Crop/resample/EQ/fade/gain/montage' in credit
  for row in rows:assert z.read('assets/slavicmyths/sounds/'+row['file'])==(A/'sounds'/row['file']).read_bytes()
 report=dict(status='PASS_OFFLINE_OWNER_PENDING',baseline=BASE,version=version,events=3,recorded_variants=5,active_source_gaps=4,owner_deferred=3,igosha='KEEP_CURRENT_BYTE_IDENTICAL',java='UNCHANGED',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),runtime='NOT_RUN',hearing='NOT_LISTENED',owner_accepted=False)
 (R/'docs/audio-review/WHISPER_CUB_19716_GATES.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))

if __name__=='__main__':main()
