"""Install the gated JAR into the previously recorded test instance, without launching it."""
from pathlib import Path
import hashlib,json,shutil,datetime

ROOT=Path(__file__).resolve().parents[1]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
old_receipt=json.loads((ROOT/'docs/verification/polymc-1.2.5-installation.json').read_text())
gate=json.loads((ROOT/'docs/verification/classes-1.3.1-acceptance.json').read_text())
assert gate['clean_build']=='PASS' and gate['required_passed']==82 and gate['checks']>11000
source=Path(gate['jar']).resolve()
assert source.parent== (ROOT/'build/libs').resolve() and sha(source)==gate['sha256']
instance=Path(old_receipt['instance']).resolve()
mods=(instance/'.minecraft/mods').resolve()
assert mods.is_dir() and mods.is_relative_to(instance)
pack=json.loads((instance/'mmc-pack.json').read_text())
versions={c['uid']:c['version'] for c in pack['components']}
assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
active=list(mods.glob('slavicmyths-*.jar'))
assert len(active)==1 and active[0].name=='slavicmyths-1.2.5.jar', active
old=active[0].resolve()
assert old.parent==mods and sha(old)==old_receipt['sha256']
# Record every companion mod, config, instance setting and options file; worlds are never touched.
protected=[p for p in mods.iterdir() if p.is_file() and p!=old]
protected += [p for p in (instance/'.minecraft/config').rglob('*') if p.is_file()]
protected += [p for p in (instance/'instance.cfg',instance/'mmc-pack.json',instance/'.minecraft/options.txt') if p.is_file()]
before={str(p.relative_to(instance)):sha(p) for p in protected}
stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
backup=(instance/'.slavicmyths-backups'/f'pre-1.3.1-{stamp}').resolve()
assert backup.is_relative_to(instance/'.slavicmyths-backups')
backup.mkdir(parents=True,exist_ok=False)
saved=backup/old.name
shutil.copy2(old,saved)
assert sha(saved)==old_receipt['sha256']
target=mods/source.name
temporary=mods/(source.name+'.installing')
assert not target.exists() and not temporary.exists()
shutil.copy2(source,temporary)
assert sha(temporary)==gate['sha256']
# Move exactly one verified old file; no recursive deletion, dependency or world changes.
old.replace(backup/(old.name+'.original'))
try:
    temporary.replace(target)
    assert sha(target)==gate['sha256']
    assert {str(p.relative_to(instance)):sha(p) for p in protected}==before
    assert list(mods.glob('slavicmyths-*.jar'))==[target]
except BaseException:
    if target.exists():target.replace(backup/(target.name+'.failed'))
    (backup/(old.name+'.original')).replace(old)
    raise
receipt={
    'version':'1.3.1','instance':str(instance),'source':str(source),'installed':str(target),
    'sha256':sha(target),'size':target.stat().st_size,'backup':str(backup),
    'previous_sha256':sha(saved),'active_slavic_jars':1,'protected_files_unchanged':len(before),
    'protected_hashes':before,'worlds_modified':False,'client_launches':0,'polymc_launches':0,
}
(ROOT/'docs/verification/polymc-1.3.1-installation.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
gate['polymc_installation']='docs/verification/polymc-1.3.1-installation.json'
gate['polymc_updated']=True
(ROOT/'docs/verification/classes-1.3.1-acceptance.json').write_text(json.dumps(gate,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:receipt[k] for k in ('installed','sha256','backup','protected_files_unchanged')}))
