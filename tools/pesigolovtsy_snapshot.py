"""Freeze this release's build inputs without mutating parallel work in the checkout."""
from pathlib import Path
import hashlib,json,shutil,datetime
R=Path(__file__).resolve().parents[1];stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S');S=(R/'work/pesigolovtsy'/('snapshot-'+stamp)).resolve();assert S.is_relative_to(R/'work/pesigolovtsy') and not S.exists();S.mkdir()
manifest={}
def copy(path):
 rel=path.relative_to(R);data=path.read_bytes();target=S/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data);manifest[rel.as_posix()]=hashlib.sha256(data).hexdigest()
for name in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']:
 copy(R/name)
for folder in ['gradle','src']:
 for p in (R/folder).rglob('*'):
  if p.is_file() and '/kuzya/' not in p.as_posix():copy(p)
for role in ['pesigolovtsy','nochnitsa','earth','sivka','firebird','visual']:
 copy(R/f'tools/{role}-offline.gradle')
 for p in (R/f'tools/{role}-tests').rglob('*.java'):copy(p)
 (S/f'work/{role}/offline-classes').mkdir(parents=True,exist_ok=True)
assert 'Kuzya' not in (S/'src/main/java/org/slavicmyths/SlavicMyths.java').read_text(),'Parallel entrypoint already integrated: coordinate before freezing'
props=S/'gradle.properties';text=props.read_text();assert 'mod_version=1.6.5' in text,'Version changed: coordinate before release'
(S/'snapshot-inputs.json').write_text(json.dumps(manifest,indent=2)+'\n')
(R/'work/pesigolovtsy/snapshot-location.json').write_text(json.dumps({'path':str(S),'excluded_parallel_module':'org/slavicmyths/kuzya','input_files':len(manifest)},indent=2)+'\n')
print(S)
