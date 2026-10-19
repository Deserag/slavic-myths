"""Curated CC0 recorded-mouth candidates. No production writes or game launch."""
from pathlib import Path
import sys, json, io, zipfile, hashlib, urllib.request
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode, RATE, stable_text
P=R/'art/audio/pack-a-60/voice-rework'
URL='https://opengameart.org/sites/default/files/monster_-_starninjas.zip'
ARCHIVE='7542446ebc69892e4267d8b3abd3167193505833692da9624c01c8da392d9065'
specs=json.loads((P/'sources.json').read_text(encoding='utf-8'));archive=None;report=[]
for number,take in [(1,2),(2,7)]:
 source=P/f'sources/starninjas_monster_{take}.ogg';member=f'monster.{take}.ogg'
 if not source.exists():
  if archive is None:
   b=urllib.request.urlopen(URL,timeout=30).read();assert hashlib.sha256(b).hexdigest()==ARCHIVE;archive=zipfile.ZipFile(io.BytesIO(b))
  source.write_bytes(archive.read(member))
 b=source.read_bytes();entry=next((r for r in specs if r.get('name')==f'starninjas_monster_{take}'),None)
 if entry:assert hashlib.sha256(b).hexdigest()==entry['sha256']
 else:
  entry=dict(name=f'starninjas_monster_{take}',file=source.relative_to(P).as_posix(),author='StarNinjas',page='https://opengameart.org/content/16-monster-growls',license='CC0-1.0',license_url='https://creativecommons.org/publicdomain/zero/1.0/',download_url=URL,download_sha256=ARCHIVE,archive_member=member,sha256=hashlib.sha256(b).hexdigest(),source_description='Author states mouth-performed growls/grunts, barely edited',verified_date='2026-10-18');specs.append(entry)
 x,sr=sf.read(source,always_2d=True);x=x.mean(axis=1)
 # Source rate must match; preserve pitch, do not invent monster voice via tones.
 assert sr==RATE;x=x[:3*RATE].copy();x-=x.mean();fade=min(220,len(x)//8);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade);x*=min(.16/max(np.sqrt(np.mean(x*x)),1e-12),.8/max(np.max(abs(x)),1e-12))
 dest=P/f'alternatives/ovinnik_rage_candidate_{number}.ogg';encode(dest,x);y,rate=sf.read(dest)
 assert rate==RATE and np.isfinite(y).all() and max(abs(y))<.95
 report.append(dict(mob='Овинник',role='Враждебный дух овина',action='Вариант голоса ярости / HOWL',layer='Voice, not fire',source=entry['file'],file=dest.relative_to(P).as_posix(),sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(y)/rate,processing='mono, max3s crop, DC removal, 5ms fade, gain; pitch unchanged',status='CANDIDATE_UNCERTAIN_NOT_WIRED',hearing='NOT_LISTENED',owner_approved=False))
stable_text(P/'sources.json',json.dumps(specs,ensure_ascii=False,indent=2)+'\n')
stable_text(P/'alternatives/ovinnik_candidates.json',json.dumps(report,ensure_ascii=False,indent=2)+'\n')
assert report[0]['sha256']!=report[1]['sha256']
print('Two distinct recorded CC0 source takes preserved; candidates only. Archive held in memory, no full-pack files left behind.')
