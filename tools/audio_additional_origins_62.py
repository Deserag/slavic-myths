"""Hash-trace original bundles and legacy mix manifests. Never run generators.

A copied production backup proves no origin: only explicit source bundles and
declared historical mix manifests are eligible. License and hearing caveats stay.
"""
from pathlib import Path
import json,csv,hashlib,collections
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths/sounds';O=R/'docs/audio-review'

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def trace():
 evidence=collections.defaultdict(list)
 def add(h,path,author,license,status,note):
  evidence[h].append(dict(author=author,license=license,status=status,evidence=path+'; '+note))
 # Each manifest entry explicitly supplies its own source and redistribution grant.
 folder=next((R/'docs/specs/1.7.1').glob('05_*'));manifest=folder/'manifest.json'
 for row in json.loads(manifest.read_text(encoding='utf-8'))['files']:
  assert row['source']=='synthetic-original-2026' and 'may be redistributed with Slavic Myths' in row['rights']
  p=folder/row['file'];assert p.resolve().is_relative_to(folder.resolve()) and p.is_file()
  add(sha(p),manifest.relative_to(R).as_posix(),'Original supplied project package','PROJECT_OUTPUT_RIGHTS_DECLARATION_NOT_CC0','HASH_MATCHED_LEGACY_PROCEDURAL_NOT_VOICE_ACCEPTED','exact source '+p.relative_to(R).as_posix())
 bundles=[('docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ','09_ASSETS/audio','10_SOURCES/RIGHTS_AND_TECH.md'),('docs/kuzya/spec/Slavic_Myths_1.6.4_Kuzya_Codex_TZ','audio/ready','audio/README_RIGHTS.md')]
 for root,bank,rights in bundles:
  declaration=R/root/rights;text=declaration.read_text(encoding='utf-8');assert 'synthesi' in text.lower()
  for p in (R/root/bank).rglob('*.ogg'):
   tts='voice_draft' in p.parts
   add(sha(p),declaration.relative_to(R).as_posix(),'Original supplied project package','PACKAGE_TTS_ENGINE_RIGHTS_PENDING_NOT_CC0' if tts else 'PROJECT_OUTPUT_RIGHTS_DECLARATION_NOT_CC0','HASH_MATCHED_PACKAGE_TTS_OWNER_PROTECTED' if tts else 'HASH_MATCHED_LEGACY_PROCEDURAL_NOT_VOICE_ACCEPTED','exact source '+p.relative_to(R).as_posix()+'; existing Kuzya protection unchanged' if 'kuzya' in root else 'exact source '+p.relative_to(R).as_posix())
 for name,credits in [('volkolak_1.4.3','docs/verification/volkolak_1.4.3/SOUND_CREDITS.md'),('sivka-1.6.0','docs/sivka/audio_provenance.md')]:
  manifest=R/'docs/verification'/name/'audio-manifest.json';assert (R/credits).is_file()
  for row in json.loads(manifest.read_text(encoding='utf-8')):
   source=row['source'];procedural=source.startswith(('original foley','original synthesis'))
   add(row['sha256'],manifest.relative_to(R).as_posix(),'Historical project manifest; source authors in '+credits,'PROJECT_OUTPUT_RIGHTS_DECLARATION_NOT_CC0' if procedural else 'DECLARED_RECORDED_MIX_SOURCE_CHAIN_PENDING','HASH_MATCHED_LEGACY_PROCEDURAL_NOT_VOICE_ACCEPTED' if procedural else 'HASH_MATCHED_LEGACY_RECORDED_OR_MIX_MANIFEST','declared source '+source+'; '+credits+'; primary license pages rechecked 2026-10-19, no runtime acceptance or full original-byte-chain claim')
 result=[]
 for p in A.rglob('*.ogg'):
  h=sha(p)
  if h not in evidence:continue
  rows=evidence[h];classes={(r['license'],r['status']) for r in rows};assert len(classes)==1,(p,classes)
  result.append(dict(asset=p.relative_to(R).as_posix(),sha256=h,author=rows[0]['author'],license=rows[0]['license'],status=rows[0]['status'],evidence=' | '.join(r['evidence'] for r in rows),hearing='NOT_LISTENED',owner_accepted=False))
 return result

if __name__=='__main__':
 rows=trace()
 with (O/'ADDITIONAL_ORIGINS.csv').open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
 print(json.dumps(dict(count=len(rows),classes=collections.Counter(r['status'] for r in rows),production_writes=False,generators_executed=0),indent=2))
