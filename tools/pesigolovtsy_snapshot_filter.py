"""Separate the parallel Kuzya additions ONLY in the frozen release copy."""
from pathlib import Path
import json,zipfile,hashlib
R=Path(__file__).resolve().parents[1];S=Path(json.loads((R/'work/pesigolovtsy/snapshot-location.json').read_text())['path']).resolve();assert S.is_relative_to((R/'work/pesigolovtsy').resolve()) and S!=R
res=S/'src/main/resources';baseline=zipfile.ZipFile(R/'work/pesigolovtsy/baseline-1.6.4.jar');removed=[]
for file in ['assets/slavicmyths/sounds.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json']:
 p=res/file;v=json.loads(p.read_text(encoding='utf8'));keys=[k for k in v if k.startswith('kuzya.') or '.kuzya' in k]
 for k in keys:removed.append(file+':'+k);del v[k]
 p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
for file in ['data/slavicmyths/tags/entity_type/combat_protected.json','data/slavicmyths/tags/entity_type/control_immune.json']:
 p=res/file;assert json.loads(p.read_text())['values']==json.loads(baseline.read(file))['values']+['slavicmyths:kuzya'];p.write_bytes(baseline.read(file));removed.append(file+':future tag member')
for p in res.rglob('*'):
 if p.is_file() and 'kuzya' in p.relative_to(res).as_posix():assert p.resolve().is_relative_to(res.resolve());removed.append(p.relative_to(S).as_posix());p.unlink()
manifest={p.relative_to(S).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for folder in ['src','gradle','tools'] for p in (S/folder).rglob('*') if p.is_file()}
for name in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']:manifest[name]=hashlib.sha256((S/name).read_bytes()).hexdigest()
(S/'snapshot-inputs.json').write_text(json.dumps(manifest,indent=2)+'\n');(S/'parallel-exclusions.json').write_text(json.dumps({'workspace_files_changed':0,'excluded_additions':removed},indent=2)+'\n');print('Separated',len(removed),'parallel additions in snapshot; checkout unchanged.')
