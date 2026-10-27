"""Owner-authorized test build installation; no launcher/game/world changes."""
from pathlib import Path
from datetime import datetime, timezone
from zipfile import ZipFile
import hashlib
import json
import os
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/implementation/stabilization269-205-20261026'
VERSION = '2.0.5-HELL'
MODS = Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods')
JAR = ROOT / f'build/libs/slavicmyths-{VERSION}.jar'
def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT)

assert git('branch', '--show-current').decode().strip() == 'hell'
assert 'BUILD SUCCESSFUL' in (OUT / 'clean-build.log').read_text(encoding='utf-8-sig')
resources = json.loads((ROOT / 'build/current-resources.json').read_text(encoding='utf-8'))
assert resources['version'] == VERSION and resources['sha256'] == sha(JAR)
runtime = json.loads((OUT / 'jar-regression/restart-results.json').read_text(encoding='utf-8'))
assert runtime['status'] == 'PASS' and runtime['mod_version'] == VERSION
assert len(runtime['checks']) == 415 and all(x['result'] == 'PASS' for x in runtime['checks'])
assert 'stabilization269-205-jar/mods/slavicmyths-2.0.5-HELL.jar' in runtime['production_code_origin']
with ZipFile(JAR) as archive:
    assert VERSION in archive.read('META-INF/neoforge.mods.toml').decode()
    assert hashlib.sha256(archive.read('org/slavicmyths/worldgen/StructureCandidates.class')).hexdigest() == runtime['production_collision_class_sha256']
    old_test = ROOT / 'work/stabilization269-jar-regression/mods/slavicmyths-2.0.4-HELL.jar'
    with ZipFile(old_test) as previous:
        classes = [n for n in archive.namelist() if n.endswith('.class')]
        assert len(classes) == 1950
        assert all(archive.read(n) == previous.read(n) for n in classes), 'Unexpected gameplay class change during version bump'

old_jars = list(MODS.glob('slavicmyths*.jar'))
assert len(old_jars) == 1 and old_jars[0].name == 'slavicmyths-2.0.4-HELL.jar'
old = old_jars[0]
old_sha = sha(old)
assert old_sha == '1c22bf186ab6886fee8a464fb44b123620354ef93ee0f990d1208660d3743010'
backup_dir = Path('D:/slavic-myths-backups') / ('stabilization269-205-' + datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ'))
backup_dir.mkdir(parents=True, exist_ok=False)
backup = backup_dir / old.name
shutil.copy2(old, backup)
assert sha(backup) == old_sha
patch = git('diff', '--binary', '--', 'gradle.properties', 'src/main')
(OUT / 'source-production.patch').write_bytes(patch)
pending = MODS / f'slavicmyths-{VERSION}.jar.pending'
installed = MODS / f'slavicmyths-{VERSION}.jar'
assert not pending.exists() and not installed.exists()
shutil.copy2(JAR, pending)
assert sha(pending) == sha(JAR)
try:
    os.replace(pending, old)
    old.rename(installed)
    assert sha(installed) == sha(JAR)
    assert list(MODS.glob('slavicmyths*.jar')) == [installed]
except BaseException:
    if not old.exists() and installed.exists():
        installed.rename(old)
    shutil.copy2(backup, old)
    raise

receipt = {'status': 'INSTALLED_TEST_BUILD_NOT_OWNER_ACCEPTED', 'version': VERSION,
           'installed_jar': str(installed), 'installed_sha256': sha(installed), 'mod_jar_count': 1,
           'source_commit': git('rev-parse', 'HEAD').decode().strip(), 'source_dirty': True,
           'source_production_patch': str((OUT / 'source-production.patch').resolve()),
           'source_production_patch_sha256': hashlib.sha256(patch).hexdigest(),
           'backup': str(backup), 'backup_sha256': sha(backup), 'previous_installed_sha256': old_sha,
           'java21_clean_build': 'PASS', 'resources': 'PASS', 'actual_jar_save_checks': 415,
           'gameplay_classes_identical_to_tested_204_snapshot': 1950,
           'worlds_modified_by_install': False, 'launcher_started_by_install': False,
           'timestamp_utc': datetime.now(timezone.utc).isoformat()}
(OUT / 'installation-receipt.json').write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(receipt, ensure_ascii=False, indent=2))
