"""Owner-authorized file-only PolyMC update. Never starts an application."""
from pathlib import Path
import datetime,hashlib,json,shutil,subprocess,tomllib,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/loot276-all-20261026'
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def mod(p):
    with zipfile.ZipFile(p) as z:
        for name in ['META-INF/neoforge.mods.toml','META-INF/mods.toml']:
            if name in z.namelist():
                for m in tomllib.loads(z.read(name).decode()).get('mods',[]):
                    if m.get('modId')=='slavicmyths':return m
def main():
    assert subprocess.check_output(['git','branch','--show-current'],cwd=R,text=True).strip()=='hell'
    assert not subprocess.check_output(['git','diff','HEAD','--name-only','--','src/main','gradle.properties','build.gradle'],cwd=R,text=True).strip(),'production must be committed'
    commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip()
    receipt=json.loads((O/'package-receipt.json').read_text());jar=Path(receipt['jar'])
    assert receipt['version']=='2.0.4-HELL' and sha(jar)==receipt['sha256'] and mod(jar)['version']=='2.0.4-HELL'
    components={v['uid']:v['version'] for v in json.loads((INSTANCE/'mmc-pack.json').read_text())['components']}
    assert components['net.minecraft']=='1.21.1' and components['net.neoforged.neoforge']=='21.1.255'
    mods=INSTANCE/'.minecraft/mods';old=[p for p in mods.glob('*.jar') if mod(p)]
    assert len(old)==1,('ambiguous installed mod JARs',old)
    old=old[0];assert old.name=='slavicmyths-2.0.2-HELL.jar' and sha(old)=='d4583f90d8c4531e1e0d78d20cf4ac11c24868af6ecfea40531d1d0e4951c6bb'
    backup=Path('D:/slavic-myths-backups')/('loot276-all-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ'))
    backup.mkdir(parents=True,exist_ok=False);copy=backup/old.name;shutil.copy2(old,copy);assert sha(copy)==sha(old)
    shutil.copy2(INSTANCE/'mmc-pack.json',backup/'mmc-pack.json')
    target=mods/jar.name;pending=mods/(jar.name+'.pending');assert not target.exists() and not pending.exists()
    oldhash=sha(old);shutil.copy2(jar,pending);assert sha(pending)==receipt['sha256']
    # Verify complete backup before moving only the explicitly identified old JAR.
    try:
        shutil.move(str(old),str(backup/('removed-'+old.name)));pending.rename(target)
        installed=[p for p in mods.glob('*.jar') if mod(p)]
        assert installed==[target] and sha(target)==receipt['sha256']
    except BaseException:
        if target.exists():target.unlink()
        if not old.exists():shutil.copy2(copy,old)
        raise
    result=dict(status='INSTALLED_FILE_ONLY_OWNER_PENDING',version='2.0.4-HELL',installed_jar=str(target),sha256=sha(target),source_commit=commit,backup=str(copy),backup_sha256=oldhash,old_version='2.0.2-HELL',mod_jar_count=1,instance_components=components,game_launches=0,polymc_launches=0,worlds_configs_untouched=True)
    (O/'installation-receipt.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print(json.dumps(result,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
