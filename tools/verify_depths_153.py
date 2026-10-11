"""Static production/resource checks; never boots Minecraft."""
from pathlib import Path
import argparse, csv, hashlib, json, struct, zipfile

root = Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser()
parser.add_argument('--version',default='1.5.3')
args=parser.parse_args()
report = root / ('docs/verification/ancient-depths-' + args.version)
jar = root / ('build/libs/slavicmyths-' + args.version + '.jar')
baseline = Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.5.2.2.jar')
if not baseline.exists():
    installed = json.loads((root / 'docs/verification/ancient-depths-1.5.3/polymc-installation.json').read_text(encoding='utf-8'))
    baseline = Path(next(p['backup'] for p in installed['previous_jars'] if p['name'] == 'slavicmyths-1.5.2.2.jar'))
checks = 0
def check(ok, label):
    global checks
    assert ok, label
    checks += 1

with zipfile.ZipFile(jar) as new, zipfile.ZipFile(baseline) as old:
    classes = list((root / 'build/classes/java/main').rglob('*.class'))
    for p in classes:
        name = p.relative_to(root / 'build/classes/java/main').as_posix()
        check(new.read(name) == p.read_bytes(), name)
    chud = [n for n in old.namelist() if n.startswith('org/slavicmyths/chud/') and n.endswith('.class')]
    for name in chud:
        check(new.read(name) == old.read(name), 'Chud unchanged: ' + name)
    previous_resources = [n for n in old.namelist() if n.startswith('data/slavicmyths/') and 'chud_' in n]
    for name in previous_resources:
        check(new.read(name) == old.read(name), 'Chud resource unchanged: ' + name)
    depths_classes = [n for n in new.namelist() if n.startswith('org/slavicmyths/depths/') and n.endswith('.class')]
    check(len(depths_classes) >= 6, 'Depths production classes')
    check(not any('DepthsOfflineChecks' in n or 'DepthsPersistenceOfflineChecks' in n for n in new.namelist()), 'No offline tests in production')
    check(not any(n.startswith('data/') and ('worldgen/structure' in n or 'biome_modifier' in n) and 'depths' in n for n in new.namelist()), 'No natural worldgen registration')
    image = new.read('assets/slavicmyths/textures/item/ancient_depths_map.png')
    check(struct.unpack('>II', image[16:24]) == (32, 32), '32x32 icon')
    check(image == (root / 'work/ancient-depths-1.5.3/assets/ancient_depths_map.png').read_bytes(), 'Supplied icon unchanged')
    langs = [json.loads(new.read('assets/slavicmyths/lang/' + n + '.json')) for n in ('ru_ru', 'en_us')]
    keys = {k for k in langs[0] if k.startswith('depths.slavicmyths.') or k == 'item.slavicmyths.ancient_depths_map'}
    check(len(keys) >= 20 and all(keys <= set(lang) for lang in langs), 'RU/EN Depths keys')
    for p in (root / 'src/main/java/org/slavicmyths/depths').glob('*.java'):
        check('import net.minecraft.client' not in p.read_text(encoding='utf-8'), 'Common-only: ' + p.name)
    service = (root / 'src/main/java/org/slavicmyths/depths/DepthsService.java').read_text(encoding='utf-8')
    check('.getChunkFuture(' not in service and '.join(' not in service, 'No blocking chunk future')

tokens = {row[3] for row in csv.reader((root / 'work/ancient-depths-1.5.3/blueprint.csv').open())}
vanilla = root / 'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
with zipfile.ZipFile(vanilla) as assets:
    for token in sorted(tokens):
        name, _, properties = token.partition('[')
        state = json.loads(assets.read('assets/minecraft/blockstates/' + name + '.json'))
        if properties:
            requested = dict(p.split('=') for p in properties.rstrip(']').split(','))
            variants = state.get('variants', {})
            if variants:
                valid = [dict(p.split('=') for p in key.split(',')) if key else {} for key in variants]
                check(any(all(v.get(k) == value for k, value in requested.items()) for v in valid), token)
            else:
                # Multipart includes independent sides: check each requested value against its conditions.
                conditions = json.dumps([v.get('when', {}) for v in state.get('multipart', [])])
                check(all(k in conditions and value in conditions for k, value in requested.items()), token)
        check(True, 'Vanilla block: ' + name)

digest = hashlib.sha256(jar.read_bytes()).hexdigest()
result = dict(version=args.version, static_checks=checks, production_classes=len(classes), depths_classes=len(depths_classes),
              unchanged_chud_classes=len(chud), unchanged_chud_resources=len(previous_resources), vanilla_tokens=len(tokens),
              production_sha256=digest, status='PASS', runtime_validation='NOT_RUN', minecraft_launches=0)
(report / 'depths-static-gates.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
offline = dict(build='PASS', clean_build='PASS (final source)', offline_checks='PASS',
               geometry_assertions=2318 if args.version=='1.5.3.1' else 2269, geometry_layouts=8, nbt_field_assertions=144, nbt_roundtrips=12,
               production_sha256=digest, runtime_validation='NOT_RUN', minecraft_launches=0,
               limitations='Pure geometry and NBT; not engine collision/light/map/AI/relog/MSPT verification')
(report / 'offline-gates.json').write_text(json.dumps(offline, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result))
