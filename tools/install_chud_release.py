"""Install a gated Chud release into the existing supported testing instance."""
from pathlib import Path
import argparse, datetime, hashlib, json, shutil

parser = argparse.ArgumentParser()
parser.add_argument('--report', type=Path, required=True)
parser.add_argument('--development-candidate', action='store_true')
parser.add_argument('--offline-fix', action='store_true', help='Explicit untested runtime candidate; requires clean build, offline checks, and resource proof for this exact JAR')
args = parser.parse_args()
receipt = json.loads((args.report / 'resource-gates.json').read_text(encoding='utf-8'))
instance = Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing')
mods = instance / '.minecraft/mods'
source = Path(receipt['jar'])
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
assert receipt['production'] == receipt['resources'] == 'PASS'
assert receipt['version'] in {'1.5.1', '1.5.2', '1.5.2.1', '1.5.2.2', '1.5.3', '1.5.3.1', '1.5.4', '1.5.5'} and sha(source) == receipt['sha256']
if receipt['version'] in {'1.5.2', '1.5.2.1', '1.5.2.2', '1.5.3', '1.5.3.1', '1.5.4', '1.5.5'}:
    if args.offline_fix:
        assert args.development_candidate, 'Offline fixes may only be installed as development candidates'
        proof = json.loads((args.report / 'offline-gates.json').read_text(encoding='utf-8'))
        assert proof['build'] == proof['offline_checks'] == 'PASS' and proof['production_sha256'] == receipt['sha256']
        assert proof['runtime_validation'] == 'NOT_RUN' and proof['minecraft_launches'] == 0
        native = {'acceptance': 'OPEN_RUNTIME_NOT_RUN'}
    else:
        native = json.loads((args.report / 'native-gates.json').read_text(encoding='utf-8'))
        assert native['technical_status'] == 'PASS' and native['production_sha256'] == receipt['sha256'], 'Technical native gates are not complete for this JAR'
        assert native['acceptance'] in {'COMPLETE', 'TECHNICAL_COMPLETE_MANUAL_OPEN'} or args.development_candidate, 'Open acceptance requires an explicitly requested development candidate'
components = {c['uid']: c['version'] for c in json.loads((instance / 'mmc-pack.json').read_text())['components']}
assert components['net.minecraft'] == '1.21.1' and components['net.neoforged.neoforge'] == '21.1.255'
assert mods.is_dir()
old = list(mods.glob('slavicmyths-*.jar'))
assert old, 'No previously installed Slavic Myths JAR'
oldpaths = {p.resolve() for p in old}
before = {p.relative_to(instance).as_posix(): sha(p) for p in instance.rglob('*')
          if p.is_file() and p.resolve() not in oldpaths}
backup = instance / '.slavicmyths-backups' / ('pre-chud-' + datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
backup.mkdir(parents=True, exist_ok=False)
saved = []
for p in old:
    assert p.resolve().parent == mods.resolve()
    dest = backup / p.name
    shutil.copy2(p, dest)
    assert sha(dest) == sha(p)
    saved.append({'name': p.name, 'sha256': sha(p), 'backup': str(dest)})
target = mods / source.name
staged = mods / (source.name + '.new')
assert not staged.exists()
shutil.copy2(source, staged)
assert sha(staged) == receipt['sha256']
for p in old:
    if p != target:
        p.unlink()
staged.replace(target)
assert list(mods.glob('slavicmyths-*.jar')) == [target]
assert sha(target) == receipt['sha256']
for name, digest in before.items():
    assert (instance / name).is_file() and sha(instance / name) == digest, name
record = {'status': 'PASS', 'version': receipt['version'], 'installed_jar': str(target),
          'development_candidate': args.development_candidate,
          'acceptance': native['acceptance'] if receipt['version'] in {'1.5.2', '1.5.2.1', '1.5.2.2', '1.5.3', '1.5.3.1', '1.5.4', '1.5.5'} else 'TECHNICAL_CHECKPOINT',
          'sha256': sha(target), 'previous_jars': saved, 'protected_files': len(before),
          'client_launches': 0, 'worlds_and_settings': 'Preserved byte-for-byte',
          'runtime_validation': 'NOT_RUN' if args.offline_fix else 'See matching native gates',
          'old_1_16_5_instance': 'Untouched'}
(args.report / 'polymc-installation.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Installed {target}; {len(before)} protected files unchanged')
