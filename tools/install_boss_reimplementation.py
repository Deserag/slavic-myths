"""Install only after all recorded gates pass. Does not launch PolyMC or Minecraft."""
from pathlib import Path
import datetime,hashlib,json,shutil,zipfile
ROOT=Path(__file__).resolve().parents[1]
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    gate=json.loads((ROOT/'docs/verification/boss-reimplementation/gates.json').read_text())
    assert gate['build']=='clean build PASS' and gate['boss_server_tests']==40 and gate['combat_server_tests']==63
    assert gate['native_model_checks']==9176 and gate['rare_rune_checks']==20491 and gate['resources']=='PASS'
    source=ROOT/'build/libs'/('slavicmyths-'+gate['version']+'.jar');assert digest(source)==gate['sha256']
    with zipfile.ZipFile(source) as z:assert 'version="'+gate['version']+'"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
    pack=json.loads((INSTANCE/'mmc-pack.json').read_text());versions={c['uid']:c['version'] for c in pack['components']}
    assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
    mods=INSTANCE/'.minecraft/mods';target=(mods/source.name).resolve();prior=(mods/'slavicmyths-1.3.4.5.2.jar').resolve()
    active=[p.resolve() for p in mods.glob('slavicmyths-*.jar')]
    if active==[target] and digest(target)==gate['sha256']:print('Already installed and verified');return
    assert active==[prior],('Active test version changed; no downgrade or overwrite',active)
    backup=(INSTANCE/'.slavicmyths-backups'/('pre-'+gate['version']+'-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))).resolve()
    assert all(p.is_relative_to(INSTANCE) for p in [target,prior,backup]) and not target.exists()
    protected={p.relative_to(INSTANCE).as_posix():digest(p) for p in INSTANCE.rglob('*') if p.is_file() and not p.is_relative_to(INSTANCE/'.slavicmyths-backups') and p!=prior}
    oldhash=digest(prior);backup.mkdir(parents=True);saved=backup/prior.name;shutil.move(str(prior),str(saved))
    try:
        shutil.copy2(source,target);assert digest(target)==gate['sha256'] and digest(saved)==oldhash
        assert len(list(mods.glob('slavicmyths-*.jar')))==1
        after={p.relative_to(INSTANCE).as_posix():digest(p) for p in INSTANCE.rglob('*') if p.is_file() and not p.is_relative_to(INSTANCE/'.slavicmyths-backups') and p!=target}
        assert after==protected,'Other test instance files changed'
    except Exception:
        if target.exists():target.unlink()
        shutil.move(str(saved),str(prior));raise
    receipt={'version':gate['version'],'installed':str(target),'sha256':gate['sha256'],'backup':str(saved),'previous_sha256':oldhash,'protected_files_unchanged':len(protected),'client_launches':0,'active_slavic_jars':1,'minecraft':'1.21.1','neoforge':'21.1.255'}
    (ROOT/'docs/verification'/('polymc-'+gate['version']+'-installation.json')).write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(receipt,ensure_ascii=False))
if __name__=='__main__':main()
