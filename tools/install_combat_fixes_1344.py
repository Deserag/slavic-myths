"""Install verified 1.3.4.4 into the existing 1.21.1 test instance without launching it."""
from pathlib import Path
import datetime,hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[1]
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 gate=json.loads((ROOT/'docs/verification/combat-fixes-1.3.4.4.json').read_text(encoding='utf-8'));source=ROOT/'build/libs/slavicmyths-1.3.4.4.jar'
 assert gate['build']=='clean build PASS' and gate['automated_server_tests']==61 and digest(source)==gate['sha256']
 pack=json.loads((INSTANCE/'mmc-pack.json').read_text(encoding='utf-8'));versions={c['uid']:c['version']for c in pack['components']};assert versions['net.minecraft']=='1.21.1'and versions['net.neoforged.neoforge']=='21.1.255'
 mods=INSTANCE/'.minecraft/mods';target=(mods/source.name).resolve();prior=(mods/'slavicmyths-1.3.4.3.jar').resolve();active=list(mods.glob('slavicmyths-*.jar'))
 if active==[target]and digest(target)==gate['sha256']:print('Verified 1.3.4.4 is already installed');return
 assert len(active)==1 and active[0].resolve()==prior,active
 backup=(INSTANCE/'.slavicmyths-backups'/('pre-1.3.4.4-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))).resolve()
 assert all(p.is_relative_to(INSTANCE)for p in [target,prior,backup]);assert not target.exists()
 protected={p.relative_to(INSTANCE).as_posix():digest(p)for p in INSTANCE.rglob('*')if p.is_file()and not p.is_relative_to(INSTANCE/'.slavicmyths-backups')and p!=prior}
 oldhash=digest(prior);backup.mkdir(parents=True);saved=backup/prior.name;shutil.move(str(prior),str(saved))
 try:
  shutil.copy2(source,target);assert digest(target)==gate['sha256']and digest(saved)==oldhash;assert len(list(mods.glob('slavicmyths-*.jar')))==1
  after={p.relative_to(INSTANCE).as_posix():digest(p)for p in INSTANCE.rglob('*')if p.is_file()and not p.is_relative_to(INSTANCE/'.slavicmyths-backups')and p!=target}
  assert after==protected,'Other instance files changed'
 except Exception:
  if target.exists():target.unlink()
  shutil.move(str(saved),str(prior));raise
 receipt={'version':'1.3.4.4','installed':str(target),'source':str(source),'sha256':gate['sha256'],'previous':'1.3.4.3','previous_sha256':oldhash,'backup':str(saved),'protected_files_unchanged':len(protected),'client_launches_for_installation':0,'active_slavic_jars':1,'minecraft':'1.21.1','neoforge':'21.1.255'}
 (ROOT/'docs/verification/polymc-1.3.4.4-installation.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(receipt,ensure_ascii=False))
if __name__=='__main__':main()
