"""Install gated 1.3.2.3 into the existing 1.21.1 instance; no launch."""
from pathlib import Path
import datetime,hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[1]
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
mods=instance/'.minecraft/mods'
source=ROOT/'build/libs/slavicmyths-1.3.2.3.jar'
gate=json.loads((ROOT/'docs/verification/classes-ui-1.3.2.3.json').read_text(encoding='utf-8'))
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
assert gate['build']=='clean build PASS' and digest(source)==gate['sha256']
assert gate['offline_rule_and_layout_checks']==3710
old=list(mods.glob('slavicmyths-*.jar'))
assert len(old)==1 and old[0].name=='slavicmyths-1.3.2.1.jar',old
prior=old[0].resolve();target=(mods/source.name).resolve()
backup=(instance/'.slavicmyths-backups'/('pre-1.3.2.3-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))).resolve()
assert all(p.is_relative_to(instance) for p in (prior,target,backup))
assert not target.exists()
protected={p.relative_to(instance).as_posix():digest(p) for p in instance.rglob('*') if p.is_file() and not p.is_relative_to(instance/'.slavicmyths-backups') and p!=prior}
previous_hash=digest(prior);backup.mkdir(parents=True);saved=backup/prior.name
shutil.move(str(prior),str(saved))
try:
 shutil.copy2(source,target)
 assert digest(target)==gate['sha256'] and digest(saved)==previous_hash
 assert len(list(mods.glob('slavicmyths-*.jar')))==1
 after={p.relative_to(instance).as_posix():digest(p) for p in instance.rglob('*') if p.is_file() and not p.is_relative_to(instance/'.slavicmyths-backups') and p!=target}
 assert after==protected,'Other instance files changed'
except Exception:
 if target.exists():target.unlink()
 shutil.move(str(saved),str(prior))
 raise
report={'version':'1.3.2.3','instance':str(instance),'source':str(source),'installed':str(target),'sha256':digest(target),'size':target.stat().st_size,'backup':str(saved),'previous_sha256':previous_hash,'active_slavic_jars':1,'protected_files_unchanged':len(protected),'client_launches':0,'server_launches':0}
(ROOT/'docs/verification/polymc-1.3.2.3-installation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
