"""Owner-authorized file-only HELL update. Never launches any application/game.

Requires the scoped build/QA receipts and committed production source. Only the
explicit Minecraft1.21.1 HELL instance is touched; backup lives outside mods.
"""
from pathlib import Path
import datetime,json,hashlib,subprocess,zipfile,shutil
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/items286-20261026'
INSTANCE=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def run():
    assert subprocess.check_output(['git','branch','--show-current'],cwd=R,text=True).strip()=='hell'
    subprocess.run(['git','diff','--quiet','HEAD','--','src/main','gradle.properties'],cwd=R,check=True)
    qa=json.loads((O/'source-jar.json').read_text());resources=json.loads((O/'current-resources.json').read_text())
    source=Path(qa['jar']);assert source.is_file() and sha(source)==qa['sha256']==resources['sha256']
    assert qa['version']=='2.0.2-HELL' and len(qa['jar_delta'])==4
    pack=json.loads((INSTANCE/'mmc-pack.json').read_text(encoding='utf8'));components={x['uid']:x['version'] for x in pack['components']}
    assert components['net.minecraft']=='1.21.1' and components['net.neoforged.neoforge']=='21.1.255',components
    mods=INSTANCE/'.minecraft/mods';assert mods.is_dir()
    # Match metadata as well as filenames: renamed duplicate mod JARs count too.
    old=[]
    for p in mods.glob('*.jar'):
        with zipfile.ZipFile(p) as z:
            if 'META-INF/neoforge.mods.toml' in z.namelist():
                import tomllib
                meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode('utf8'))
                if any(x.get('modId')=='slavicmyths' for x in meta.get('mods',[])):old.append(p)
    assert len(old)==1,old
    oldfile=old[0];oldhash=sha(oldfile);target=mods/source.name
    if target.exists() and target!=oldfile:raise RuntimeError('Target exists; preserve it and inspect manually')
    timestamp=datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    backup=Path('D:/slavic-myths-backups')/('items286-'+timestamp);backup.mkdir(parents=True,exist_ok=False)
    saved=backup/oldfile.name;shutil.copy2(oldfile,saved);assert sha(saved)==oldhash
    shutil.copy2(INSTANCE/'mmc-pack.json',backup/'mmc-pack.json')
    pending=mods/(source.name+'.pending');assert not pending.exists();shutil.copy2(source,pending);assert sha(pending)==qa['sha256']
    # Rename the previous JAR into its external backup directory only after the
    # independent backup has been verified, then publish the staged new bytes.
    moved=backup/(oldfile.name+'.original');shutil.move(str(oldfile),str(moved))
    try:pending.rename(target)
    except BaseException:
        shutil.copy2(moved,oldfile)
        raise
    assert sha(target)==qa['sha256'] and sha(moved)==oldhash
    final=[]
    for p in mods.glob('*.jar'):
        with zipfile.ZipFile(p) as z:
            if 'META-INF/neoforge.mods.toml' in z.namelist():
                import tomllib
                if any(x.get('modId')=='slavicmyths' for x in tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode('utf8')).get('mods',[])):final.append(str(p))
    assert final==[str(target)],final
    receipt=dict(status='INSTALLED_TEST_CANDIDATE_OWNER_PENDING',version=qa['version'],source_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),production_baseline=qa['source_baseline'],built_jar=str(source),installed_jar=str(target),sha256=sha(target),backup=str(saved),original_moved_to=str(moved),previous_sha256=oldhash,installed_mod_jars=final,instance_components=components,minecraft_launches=0,polymc_launches=0,gameplay='NOT_RUN / OWNER_PENDING',time_utc=timestamp)
    (O/'delivery.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print(json.dumps(receipt,ensure_ascii=False))
if __name__=='__main__':run()
