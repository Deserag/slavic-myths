"""Verify and install the explicitly requested test snapshot; never launch Minecraft."""
from pathlib import Path
import datetime, hashlib, json, shutil, subprocess, tempfile, zipfile
from PIL import Image
from decor_all_woods import targets, encoded
import trophy110_art, trophy111_art

R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/delivery-19749'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()

def main():
    O.mkdir(parents=True,exist_ok=True)
    assert 'BUILD SUCCESSFUL' in (R/'build-delivery49.log').read_text()
    jar=R/'build/libs/slavicmyths-1.9.7.49.jar'
    resources=R/'src/main/resources'
    count=0
    with zipfile.ZipFile(jar) as z:
        assert 'version="1.9.7.49"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
        for p in resources.rglob('*'):
            if not p.is_file():continue
            rel=p.relative_to(resources).as_posix()
            expected=R/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
            assert z.read(rel)==expected.read_bytes(),rel
            if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'))
            if p.suffix=='.png':
                with Image.open(p) as im:im.verify()
            count+=1
        assert not any(n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist())
    textures=resources/'assets/slavicmyths/textures'
    for rel in targets():assert (textures/rel).read_bytes()==encoded(rel),rel
    with tempfile.TemporaryDirectory() as td:
        dest=Path(td)/'textures/item'
        for module,ids in [(trophy110_art,trophy110_art.IDS),(trophy111_art,trophy111_art.FIX)]:
            for name in ids:
                module.write(name,dest)
                assert (dest/(name+'.png')).read_bytes()==(textures/'item'/(name+'.png')).read_bytes(),name
    instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
    pack=json.loads((instance/'mmc-pack.json').read_text())
    versions={c['uid']:c['version'] for c in pack['components']}
    assert versions['net.minecraft']=='1.21.1' and versions['net.neoforged.neoforge']=='21.1.255'
    mods=(instance/'.minecraft/mods').resolve();assert mods.is_relative_to(instance)
    old=list(mods.glob('slavicmyths-*.jar'));assert len(old)==1 and old[0].name=='slavicmyths-1.9.7.48.jar',old
    others={p.name:sha(p) for p in mods.glob('*.jar') if p!=old[0]}
    backup=instance/'codex-backups'/('before-1.9.7.49-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
    backup.mkdir(parents=True)
    saved=backup/old[0].name;shutil.copy2(old[0],saved);assert sha(saved)==sha(old[0])
    installed=mods/jar.name;assert not installed.exists()
    shutil.copy2(jar,installed);assert sha(installed)==sha(jar)
    assert old[0].resolve().parent==mods and sha(old[0])==sha(saved)
    old[0].unlink()
    assert list(mods.glob('slavicmyths-*.jar'))==[installed]
    assert others=={p.name:sha(p) for p in mods.glob('*.jar') if p!=installed}
    result=dict(version='1.9.7.49',status='INSTALLED_TEST_BUILD_IN_PROGRESS_OWNER_PENDING',source_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),installed_jar=str(installed),sha256=sha(installed),backup=str(saved),backup_sha256=sha(saved),production_resources=count,wood_pngs=len(targets()),other_mods_preserved=others,duplicate_mod_jars=False,runtime_launches=0,final_release=False)
    (O/'installed.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))

if __name__=='__main__':main()
