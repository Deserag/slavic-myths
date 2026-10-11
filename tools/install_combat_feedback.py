"""Install verified combat feedback build; never launch any game process."""
from pathlib import Path
import datetime, hashlib, json, shutil, zipfile

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / 'docs/verification/combat-feedback-2026-10-09'
INSTANCE = Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').resolve()
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
def is_mod(path):
    with zipfile.ZipFile(path) as archive:
        name = 'META-INF/neoforge.mods.toml'
        return name in archive.namelist() and 'modId="slavicmyths"' in ''.join(archive.read(name).decode().split())
def main():
    gate = json.loads((REPORT / 'resources.json').read_text())
    assert gate['status'] == 'PASS'
    source = Path(gate['jar']); assert digest(source) == gate['sha256'] and is_mod(source)
    versions = {c['uid']: c['version'] for c in json.loads((INSTANCE/'mmc-pack.json').read_text())['components']}
    assert versions['net.minecraft'] == '1.21.1' and versions['net.neoforged.neoforge'] == '21.1.255'
    mods = INSTANCE / '.minecraft/mods'
    active = [p.resolve() for p in mods.glob('*.jar') if is_mod(p)]
    target = (mods/source.name).resolve()
    assert len(active) == 1
    prior = active[0]
    assert prior.name == 'slavicmyths-1.5.1-boss-rework.jar', active
    assert digest(prior) == 'f5e947b9cffbceab3105cbf849aeaef6f27f325db8ad00ede71b32cf82c44b82'
    backup = (INSTANCE/'.slavicmyths-backups'/('pre-combat-feedback-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))).resolve()
    staged = target.with_suffix('.jar.pending')
    assert all(p.is_relative_to(INSTANCE) for p in (target, prior, backup, staged))
    assert not target.exists() and not staged.exists()
    def protected(excluded):
        return {p.relative_to(INSTANCE).as_posix(): digest(p) for p in INSTANCE.rglob('*') if p.is_file() and not p.is_relative_to(INSTANCE/'.slavicmyths-backups') and p != excluded}
    before = protected(prior); old_hash = digest(prior)
    backup.mkdir(parents=True); saved = backup/prior.name
    shutil.copy2(prior, saved); assert digest(saved) == old_hash
    try:
        shutil.copy2(source, staged); assert digest(staged) == gate['sha256']
        prior.unlink(); staged.rename(target)
        assert digest(target) == gate['sha256'] and protected(target) == before
        assert len([p for p in mods.glob('*.jar') if is_mod(p)]) == 1
    except Exception:
        if target.exists(): target.unlink()
        if staged.exists(): staged.unlink()
        shutil.copy2(saved, prior)
        raise
    receipt = dict(installed=str(target), sha256=gate['sha256'], backup=str(saved), previous_sha256=old_hash, protected_files_unchanged=len(before), active_slavic_jars=1, client_launches_after_install=0, minecraft='1.21.1', neoforge='21.1.255')
    (REPORT/'polymc-installation.json').write_text(json.dumps(receipt, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(receipt, ensure_ascii=False))
if __name__ == '__main__': main()
