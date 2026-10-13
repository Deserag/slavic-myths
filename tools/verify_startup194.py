"""Require a startup hotfix with precisely two bytecode changes and identical resources."""
from pathlib import Path
import zipfile,json,hashlib
r=Path(__file__).resolve().parents[1]
candidate=r/'build/libs/slavicmyths-1.9.4.jar'
baseline=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.4.jar')
baseline_hash='04a995f83146dd05a4c3c0c22950ec53fbe0da660026a890b916df8d2235f0f7'
if hashlib.sha256(baseline.read_bytes()).hexdigest()!=baseline_hash:
 baseline=next(p for p in (baseline.parents[2]/'.slavicmyths-backups').rglob('slavicmyths-1.9.4.jar') if hashlib.sha256(p.read_bytes()).hexdigest()==baseline_hash)
expected={'org/slavicmyths/sanctuary/SanctuaryBlock.class','org/slavicmyths/sanctuary/SanctuaryDecor.class'}
with zipfile.ZipFile(candidate) as new,zipfile.ZipFile(baseline) as old:
 a={n for n in new.namelist() if not n.endswith('/')};b={n for n in old.namelist() if not n.endswith('/')}
 assert a==b,'Added or removed production entries'
 changed={n for n in a if new.read(n)!=old.read(n)}
 assert changed==expected,sorted(changed)
 assert not any('serpenthill' in n for n in a),'Future content in JAR'
result={'status':'PASS','changed_entries':sorted(changed),'resources':'BYTE_IDENTICAL_TO_INSTALLED_1.9.4','sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'baseline_sha256':hashlib.sha256(baseline.read_bytes()).hexdigest(),'client_starts':0,'server_starts':0}
(r/'docs/verification/startup-fix-1.9.4/jar-comparison.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result))
