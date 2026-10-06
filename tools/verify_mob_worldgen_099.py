"""Current resource integrity; release coverage acceptance is a separate mandatory gate."""
from pathlib import Path
import argparse, hashlib, json, re, zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
OUT = ROOT / 'docs/verification/mob-worldgen-0.9.9'
parser = argparse.ArgumentParser()
parser.add_argument('--jar', type=Path)
args = parser.parse_args()
issues, refs = [], 0

def require(ok, message):
    if not ok:
        raise AssertionError(message)

with zipfile.ZipFile(ROOT / '.tools/port-backups/slavicmyths-0.9.7-pre-0.9.9.zip') as archive:
    baseline = {name: archive.read(name) for name in archive.namelist() if name.startswith('src/main/resources/')}
for name in baseline:
    require((ROOT / name).is_file(), 'Removed existing resource: ' + name)
native = zipfile.ZipFile(ROOT / 'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar')
native_names = set(native.namelist())
provided = {'assets/minecraft/models/builtin/generated.json', 'assets/minecraft/models/builtin/entity.json'}

def check_reference(value, folder, suffix, source):
    global refs
    if value.startswith('#'):
        return
    namespace, name = value.split(':', 1) if ':' in value else ('minecraft', value)
    target = f'assets/{namespace}/{folder}/{name}{suffix}'
    refs += 1
    if (RES / target).is_file() or target in native_names or target in provided:
        return
    key = 'src/main/resources/' + source.relative_to(RES).as_posix()
    require(baseline.get(key) == source.read_bytes(), 'Missing NEW/changed reference: ' + target)
    issues.append({'source': key, 'reference': target, 'status': 'pre-existing unchanged asset issue'})

def walk(value, source):
    if isinstance(value, list):
        for item in value:
            walk(item, source)
    elif isinstance(value, dict):
        for key, item in value.items():
            if key in ['parent', 'model'] and isinstance(item, str):
                check_reference(item, 'models', '.json', source)
            elif key == 'textures' and isinstance(item, dict):
                for texture in item.values():
                    if isinstance(texture, str):
                        check_reference(texture, 'textures', '.png', source)
            else:
                walk(item, source)

json_count, images = 0, 0
for path in RES.rglob('*.json'):
    data = json.loads(path.read_text(encoding='utf-8'))
    json_count += 1
    if '/models/' in path.as_posix() or '/blockstates/' in path.as_posix():
        walk(data, path)
for path in (RES / 'assets/slavicmyths').rglob('*.png'):
    with Image.open(path) as bitmap:
        bitmap.verify()
    images += 1
sounds = json.loads((RES / 'assets/slavicmyths/sounds.json').read_text(encoding='utf-8'))
with zipfile.ZipFile(ROOT / 'build/moddev/artifacts/neoforge-21.1.255-sources.jar') as source_archive:
    native_sound_source = source_archive.read('net/minecraft/sounds/SoundEvents.java').decode('utf-8')
native_sounds = set(re.findall(r'register\w*\(\s*"([a-z0-9_.]+)"', native_sound_source))
for key, definition in sounds.items():
    for sound in definition.get('sounds', []):
        name = sound if isinstance(sound, str) else sound['name']
        namespace, name = name.split(':', 1) if ':' in name else ('slavicmyths', name)
        kind = sound.get('type', 'sound') if isinstance(sound, dict) else 'sound'
        if kind == 'event':
            require(name in (native_sounds if namespace == 'minecraft' else sounds), 'Missing sound event ' + name)
        elif namespace == 'slavicmyths':
            require((RES / f'assets/slavicmyths/sounds/{name}.ogg').is_file(), 'Missing sound file ' + name)
report = {'status': 'PASS', 'jsonFiles': json_count, 'pngFiles': images, 'assetReferences': refs, 'soundEvents': len(sounds), 'existingUnchangedAssetIssues': issues, 'releaseAccepted': False, 'clientLaunched': False}
if args.jar:
    with zipfile.ZipFile(args.jar) as archive:
        names = set(archive.namelist())
        require(not any('GameTests' in name or name.startswith(('org/slavicmyths/verify/', 'xaero/', 'mezz/')) for name in names), 'Bundled tests or optional dependencies')
        require(not any(name.startswith('data/slavicmyths_placement/') or name.startswith('data/slavicmyths_water_placement/') or name.startswith('data/slavicmyths_terrain_placement/') or name.startswith('data/slavicmyths_sequential_placement/') or name.startswith('data/slavicmyths_coverage/') or name=='data/minecraft/worldgen/world_preset/flat.json' or name=='data/slavicmyths/structure/port_empty.nbt' for name in names), 'Bundled test fixture')
        for name in names:
            if name.endswith('.class'):
                require(int.from_bytes(archive.read(name)[6:8], 'big') == 65, 'Not Java21: ' + name)
        for path in RES.rglob('*'):
            if path.is_file():
                name = path.relative_to(RES).as_posix()
                require(name in names, 'Resource omitted: ' + name)
                if name != 'META-INF/neoforge.mods.toml':
                    require(archive.read(name) == path.read_bytes(), 'Stale JAR resource: ' + name)
    report['jarSha256'] = hashlib.sha256(args.jar.read_bytes()).hexdigest()
OUT.mkdir(parents=True, exist_ok=True)
(OUT / 'resource-check.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'PASS resource integrity: JSON={json_count}, PNG={images}, references={refs}, sounds={len(sounds)}, unchanged old asset issues={len(issues)}; release coverage gate remains separate')
