"""Trace legacy procedural audio by exact hashes, without executing generators."""
from pathlib import Path
import json,hashlib,csv,collections
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/audio-review'
def trace():
 evidence=collections.defaultdict(list)
 for name in ['docs/firebird/audio-manifest.json','docs/nochnitsa/audio-manifest.json','docs/pesigolovtsy/source/audio-manifest.json']:
  f=R/name;data=json.loads(f.read_text(encoding='utf-8'));rows=data['files'] if isinstance(data,dict) else data
  for row in rows:
   if row.get('sha256'):evidence[row['sha256']].append(name+'; '+row.get('source',data.get('provenance','') if isinstance(data,dict) else ''))
 packs=[('docs/specs/1.7.3/07_Audio','manifest.json'),('docs/specs/1.7.4/07_Audio','MANIFEST_RIGHTS_AUDIO.json'),('docs/specs/1.7.5/07_Audio','MANIFEST_RIGHTS_AUDIO.json'),('docs/specs/1.7.6/08_Audio','MANIFEST_RIGHTS.json'),('docs/specs/1.7.2/06_Audio','../08_Legal_and_Resources/01_PROVENANCE_LICENSE_AND_AUDIO_INTEGRATION.md')]
 for root,rights in packs:
  folder=R/root;declaration=folder/rights;assert declaration.exists(),declaration
  text=declaration.read_text(encoding='utf-8');assert any(s in text.lower() for s in ['procedural','algorithmically','oscillators','synthetic']),declaration
  for p in folder.rglob('*.ogg'):evidence[hashlib.sha256(p.read_bytes()).hexdigest()].append(declaration.resolve().relative_to(R).as_posix()+'; exact packaged source '+p.relative_to(R).as_posix())
 result=[]
 for f in (A/'sounds').rglob('*.ogg'):
  h=hashlib.sha256(f.read_bytes()).hexdigest()
  if h in evidence:result.append(dict(asset=f.relative_to(R).as_posix(),sha256=h,evidence=' | '.join(evidence[h]),author='Historical procedural project output',license='PROJECT_OUTPUT_RIGHTS_DECLARATION_NOT_CC0',status='HASH_MATCHED_LEGACY_PROCEDURAL_NOT_VOICE_ACCEPTED',hearing='NOT_LISTENED',final_voice_accepted=False))
 return result
if __name__=='__main__':
 rows=trace()
 with (O/'LEGACY_PROVENANCE.csv').open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
 print(len(rows),'legacy files traced by exact hash; no generators executed; no recorded voice/CC0/owner-acceptance claim')
