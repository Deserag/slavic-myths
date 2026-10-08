"""Install the verified local JAR into the existing testing instance; never launch it."""
from pathlib import Path
import json, hashlib, shutil, datetime
root=Path(__file__).resolve().parents[1]
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
mods=instance/'.minecraft/mods'
source=root/'build/libs/slavicmyths-1.3.2.1.jar'
gate=json.loads((root/'docs/verification/weapons-1.3.2.1-resources.json').read_text())
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
assert digest(source)==gate['sha256']
target=mods/source.name
old=list(mods.glob('slavicmyths-*.jar'))
assert len(old)==1 and old[0].name=='slavicmyths-1.3.1.jar',old
prior=old[0].resolve();assert prior.is_relative_to(instance)
backup=(instance/'.slavicmyths-backups'/('pre-1.3.2.1-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))).resolve()
assert backup.is_relative_to(instance)
protected={p.relative_to(instance).as_posix():digest(p) for p in instance.rglob('*') if p.is_file() and not p.is_relative_to(instance/'.slavicmyths-backups') and p!=prior}
previous_hash=digest(prior)
backup.mkdir(parents=True)
saved=backup/prior.name
shutil.move(str(prior),str(saved))
try:
 shutil.copy2(source,target)
 assert digest(target)==gate['sha256']
 assert digest(saved)==previous_hash
 assert len(list(mods.glob('slavicmyths-*.jar')))==1
 for name,sha in protected.items():assert digest(instance/name)==sha,name
except Exception:
 if target.exists():target.unlink()
 shutil.move(str(saved),str(prior))
 raise
report={'version':'1.3.2.1','instance':str(instance),'source':str(source),'installed':str(target),'sha256':digest(target),'size':target.stat().st_size,'backup':str(backup),'previous_jar':str(saved),'previous_sha256':previous_hash,'active_slavic_jars':1,'protected_files_unchanged':len(protected),'client_launches':0}
(root/'docs/verification/polymc-1.3.2.1-installation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
