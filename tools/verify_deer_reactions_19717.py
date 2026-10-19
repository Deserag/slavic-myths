"""Focused deer route/resource contract; does not certify hearing or runtime."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path[:0]=[str(R/'tools'),str(R/'.tools/audio-libs')]
import numpy as np,soundfile as sf
from audio_groups_61_plan import PLAN,BLOCKED,DEFERRED
BASE='02c5b8837471090892b98480f66d1d59db50bca0';A=R/'src/main/resources/assets/slavicmyths'
EVENTS={'stag_hurt','stag_death','doe_hurt','doe_death'}
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 before=json.loads(old('src/main/resources/assets/slavicmyths/sounds.json'));current=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 assert set(before)==set(current)
 assert {e for e in current if before[e]!=current[e]}==EVENTS
 assert all(not before[e]['sounds'] and current[e]['sounds'] for e in EVENTS)
 assert not subprocess.check_output(['git','diff',BASE,'--','src/main/java'],cwd=R).strip()
 assert not set(BLOCKED)-set(DEFERRED)
 assert set(DEFERRED)=={'igosha_ambient','igosha_hurt','igosha_death'}
 paths=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE,'src/main/resources/assets/slavicmyths'],cwd=R,text=True).splitlines()
 # Every existing OGG, model, texture, recipe and blockstate must be unchanged.
 for path in paths:
  if any('/'+s+'/' in path for s in ['sounds','textures','models','blockstates','recipes']):assert (R/path).read_bytes()==old(path),path
 selection=json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8'))
 rows=[r for r in selection if r['event'] in EVENTS];assert len(rows)==6
 fingerprints=[]
 for row in rows:
  p=A/'sounds'/row['file'];assert sha(p)==row['sha256'];x,sr=sf.read(p)
  assert sr==44100 and x.ndim==1 and np.isfinite(x).all() and max(abs(x))<.8
  assert .015<np.sqrt(np.mean(x*x))<.08
  assert row['status']=='AGENT_SELECTED_OWNER_PENDING' and not row['owner_approved']
  assert row['hearing']=='NOT_LISTENED' and len(row['sources'])==1
  expected='stag__kvgarlic_snorts_816601.mp3' if row['event'].startswith('stag') else 'deer__juskiddink_startled_58128.mp3'
  assert row['sources'][0]['key']==expected
  fingerprints.append(hashlib.sha256(x.tobytes()).hexdigest())
  if row['event'].endswith('hurt'):assert len(x)/sr/.8<10/20
 assert len(set(fingerprints))==6
 for lang in ['ru_ru','en_us']:
  path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';b=json.loads(old(path));c=json.loads((R/path).read_text(encoding='utf-8'))
  allowed={current[e]['subtitle'] for e in EVENTS};assert set(b)==set(c)
  assert {k for k in b if b[k]!=c[k]}<=allowed
 # Canonical generation preserves these selections and all existing resources.
 snapshot={p:sha(p) for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 from audio_groups_61 import restore_selected
 restore_selected()
 assert snapshot=={p:sha(p) for p in (R/'src/main/resources').rglob('*') if p.is_file()}
 version=next(s.split('=',1)[1] for s in (R/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  credits=z.read('assets/slavicmyths/audio_licenses/project-61.txt').decode()
  assert 'kvgarlic' in credits and 'juskiddink' in credits and 'https://creativecommons.org/licenses/by/4.0/' in credits
  assert json.loads(z.read('assets/slavicmyths/sounds.json'))==current
  for row in rows:assert z.read('assets/slavicmyths/sounds/'+row['file'])==(A/'sounds'/row['file']).read_bytes()
 report=dict(status='PASS_OFFLINE_OWNER_PENDING',baseline=BASE,version=version,events=4,variants=6,active_source_gaps=0,owner_deferred=3,java='UNCHANGED',existing_art_and_audio='BYTE_IDENTICAL',restorer='BYTE_IDENTICAL',doe_source_sex='UNSPECIFIED_BY_AUTHOR',terminal_calls='GAME_INTERPRETATION_NOT_RECORDED_DEATH',fawn='EXISTING_SHARED_DOE_ROUTE_UNCHANGED',jar_sha256=sha(jar),runtime='NOT_RUN',hearing='NOT_LISTENED',owner_accepted=False)
 (R/'docs/audio-review/DEER_19717_GATES.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
