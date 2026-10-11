"""Strict production/resource gate, separate from native geometry and gameplay checks."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
OUT = ROOT / 'docs/verification/chud-1.5.1'
args = argparse.ArgumentParser()
args.add_argument('--jar', type=Path, required=True)
args.add_argument('--version', required=True)
options = args.parse_args()
checks = 0
def check(condition, label):
    global checks
    checks += 1
    if not condition:
        raise AssertionError(label)

with zipfile.ZipFile(options.jar) as archive:
    names = set(archive.namelist())
    check(f'version="{options.version}"' in archive.read('META-INF/neoforge.mods.toml').decode(), 'version metadata')
    for name in names:
        check('/verify/' not in name and '/smoke/' not in name and not name.startswith('data/slavicmyths_chud'),
              'test artifact leaked: ' + name)
        if name.endswith('.class') and name.startswith('org/slavicmyths/'):
            check(int.from_bytes(archive.read(name)[6:8], 'big') == 65, 'Java 21: ' + name)
    for path in RES.rglob('*'):
        if not path.is_file() or path.suffix not in {'.json', '.png', '.ogg', '.nbt'}:
            continue
        if path.suffix == '.json':
            json.loads(path.read_text(encoding='utf-8'))
        if path.suffix == '.png':
            with Image.open(path) as image:
                image.verify()
        name = path.relative_to(RES).as_posix()
        check(archive.read(name) == path.read_bytes(), 'resource differs: ' + name)
    for expected in ['ChudStructure', 'ChudStructures', 'ChudPiece', 'ChudPlan', 'ChudGeometry',
                     'ChudCommands', 'ChudNavigation', 'ChudPlacement']:
        check(f'org/slavicmyths/chud/{expected}.class' in names, 'missing class: ' + expected)
    for relative in ['worldgen/structure/chud_city.json', 'worldgen/structure_set/chud_city.json',
                     'tags/worldgen/biome/has_structure/chud_city.json']:
        check('data/slavicmyths/' + relative in names, 'missing registration ' + relative)
source = '\n'.join(path.read_text(encoding='utf-8') for path in (ROOT / 'src/main/java/org/slavicmyths/chud').glob('*.java'))
for forbidden in ['Blocks.CHEST', 'Blocks.TRAPPED_CHEST', 'Blocks.BARREL', 'Blocks.HOPPER',
                  'addFreshEntity', 'DeferredRegister.create', 'LootTable', 'PlayerTickEvent', 'ServerTickEvent']:
    check(forbidden not in source, 'forbidden new content: ' + forbidden)
placement = json.loads((RES / 'data/slavicmyths/worldgen/structure_set/chud_city.json').read_text())['placement']
minimum_centers = (placement['separation'] + 1) * 16
minimum_boundary = minimum_centers - 278
check(minimum_boundary >= 2000, 'boundary spacing')
check('world.setBlock(pos,s,2)' in source and 'chunkClip' in source, 'native clipped placement')
for language in ['ru_ru', 'en_us']:
    check('structure.slavicmyths.chud_city' in json.loads((RES / f'assets/slavicmyths/lang/{language}.json').read_text(encoding='utf-8')), 'localized name ' + language)
report = {'checks': checks, 'version': options.version, 'jar': str(options.jar.resolve()),
          'sha256': hashlib.sha256(options.jar.read_bytes()).hexdigest(),
          'resources': 'PASS', 'production': 'PASS', 'spacing_lower_bound_blocks': minimum_boundary,
          'client_launches': 0, 'gameplay_visual_qa': 'NOT_TESTED'}
OUT.mkdir(parents=True, exist_ok=True)
(OUT / 'resource-gates.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
print(json.dumps(report, ensure_ascii=False))
