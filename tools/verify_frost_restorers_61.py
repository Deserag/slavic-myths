"""Exercise safe audio entry points; never run historical visual generators."""
from pathlib import Path
import hashlib,json,subprocess,sys
R=Path(__file__).resolve().parents[1]

def snapshot():
 return {p.relative_to(R).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main').rglob('*') if p.is_file()}

def main():
 before=snapshot()
 for script in ['morozko_assets.py','karachun_assets.py']:
  subprocess.run([sys.executable,str(R/'tools'/script),'--audio-only'],check=True,cwd=R)
  assert snapshot()==before,script+' changed canonical production resources'
 rows={r['event']:r for r in json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8'))}
 # Source-state budgets: stream owner expires at age>70, repeated blizzard80,
 # dark frost40 and last-hour marks40 ticks; NightContent min pitch .94.
 assert rows['morozko.staff_stream']['seconds']>71/20
 assert rows['morozko.blizzard_loop']['seconds']<80/20
 for event in ['karachun.dark_frost','karachun.last_hour_mark']:
  assert rows[event]['seconds']/.94<40/20,event
 from audio_groups_61_plan import BLOCKED
 whisper=json.loads((R/'src/main/resources/assets/slavicmyths/sounds.json').read_text(encoding='utf-8'))['karachun.voice_whisper']['sounds']
 if 'karachun.voice_whisper' in BLOCKED:assert not whisper
 else:
  selected=json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8'))
  selected=[r for r in selected if r['event']=='karachun.voice_whisper']
  assert len(selected)==2 and {v['name'] for v in whisper}=={'slavicmyths:'+r['file'].removesuffix('.ogg') for r in selected}
  for row in selected:
   assert {s['key'] for s in row['sources']}=={'whisper__sardin_3256.wav'}
   assert row['seconds']/.94<24/20
   assert hashlib.sha256((R/'src/main/resources/assets/slavicmyths/sounds'/row['file']).read_bytes()).hexdigest()==row['sha256']
 report=dict(status='PASS_OFFLINE',scripts=['morozko_assets.py --audio-only','karachun_assets.py --audio-only'],production_files=len(before),production_unchanged=True,legacy_synthesis_executed=False,geometry_generators_executed=False,duration_budgets='PASS_DECODED_FILES_NOT_RUNTIME',runtime='NOT_RUN',owner_accepted=False)
 (R/'docs/audio-review/FROST_RESTORER_GATES.json').write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps(report,indent=2))

if __name__=='__main__':main()
