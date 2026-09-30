"""Structural integration checks against registrations and the production artifact."""
from pathlib import Path
import json, re, struct, zipfile, zlib
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT/'src/main/resources'
JAVA = ROOT/'src/main/java/org/slavicmyths'
VERSION = re.search(r"version = '([^']+)'", (ROOT/'build.gradle').read_text()).group(1)

def read(path):
    return json.loads(path.read_text(encoding='utf-8'))

def registered(file):
    source=(JAVA/file).read_text()
    found=set(re.findall(r'\.register\(\s*"([a-z_]+)"',source))
    if file == 'registry/ModItems.java': found.update(re.findall(r'\b(?:food|stew|egg)\(\s*"([a-z_]+)"',source))
    return found

items = registered('registry/ModItems.java')
blocks = registered('registry/ModBlocks.java')
assert not re.search(r'\.durability\([^)]*\)\.stacksTo\(', (JAVA/'registry/ModItems.java').read_text()), 'Durable items must not call stacksTo after durability'
assert len(items) >= 74 and len(blocks) >= 18
assert {'birch_bark_scroll', 'thunder_stone', 'warding_charm'} <= items
assert blocks - {"raspberry_bush", "blueberry_bush"} <= items
assert 'ItemGroup.TAB_MISC' not in (JAVA/'registry/ModItems.java').read_text()

def ref(value, folder, suffix='.json'):
    ns, name = value.split(':', 1)
    if ns == 'slavicmyths':
        assert (RES/folder/ns/name).with_suffix(suffix).is_file(), value

def item_ref(value):
    if value.startswith('slavicmyths:'):
        assert value.split(':')[1] in items, value

for path in RES.rglob('*.json'):
    read(path)
assert read(RES/'pack.mcmeta')['pack']['pack_format'] == 6
for item in items:
    assert (RES/f'assets/slavicmyths/models/item/{item}.json').is_file()
    for lang in ('ru_ru', 'en_us'):
        data = read(RES/f'assets/slavicmyths/lang/{lang}.json')
        assert ('block' if item in blocks else 'item')+'.slavicmyths.'+item in data
        assert 'itemGroup.slavicmyths' in data
for block in blocks:
    assert (RES/f'assets/slavicmyths/blockstates/{block}.json').is_file()
    assert (RES/f'data/slavicmyths/loot_tables/blocks/{block}.json').is_file()
for path in (RES/'assets/slavicmyths/models').rglob('*.json'):
    obj = read(path)
    parent = obj.get('parent', '')
    if parent.startswith('slavicmyths:'):
        assert (RES/('assets/slavicmyths/models/'+parent.split(':')[1]+'.json')).is_file()
    for texture in obj.get('textures', {}).values():
        if texture.startswith('slavicmyths:'):
            assert (RES/('assets/slavicmyths/textures/'+texture.split(':')[1]+'.png')).is_file()
for path in (RES/'assets/slavicmyths/blockstates').glob('*.json'):
    for variant in read(path)['variants'].values():
        assert (RES/('assets/slavicmyths/models/'+variant['model'].split(':')[1]+'.json')).is_file()
pngs = list(RES.rglob('*.png'))
assert len(pngs) >= 82
for path in pngs:
    png = path.read_bytes()
    assert png[:8] == b'\x89PNG\r\n\x1a\n'
    offset, compressed = 8, b''
    while offset < len(png):
        size = struct.unpack('!I', png[offset:offset+4])[0]
        kind, data = png[offset+4:offset+8], png[offset+8:offset+8+size]
        assert zlib.crc32(kind+data)&0xffffffff == struct.unpack('!I', png[offset+8+size:offset+12+size])[0]
        if kind == b'IHDR':
            width,height,depth,color,compression,filtering,interlace = struct.unpack('!IIBBBBB', data)
            expected = (64,64) if path.parent.name == 'entity' else ((64,32) if path.parent.name == 'armor' else (16,16))
            if path.parent.name == 'entity' and path.stem not in ('domovoy','leshy'):
                expected = (256,256)
            if path.parent.name == 'item':
                model_path = RES/f'assets/slavicmyths/models/item/{path.stem}.json'
                if model_path.exists() and read(model_path).get('elements'):
                    expected = (32,32)  # Native material atlas for physical weapon models.
            assert (width,height,depth,color,compression,filtering,interlace) == (*expected,8,6,0,0,0)
        if kind == b'IDAT':
            compressed += data
        offset += 12+size
    assert len(zlib.decompress(compressed)) == height*(1+width*4)
recipes = set()
for path in (RES/'data/slavicmyths/recipes').glob('*.json'):
    recipes.add(path.stem)
    obj = read(path)
    item_ref(obj['result']['item'] if isinstance(obj['result'],dict) else obj['result'])
    for ing in obj.get('ingredients', []) + list(obj.get('key', {}).values()):
        item_ref(ing['item'])
    if obj['type'] == 'minecraft:crafting_shaped':
        pattern = obj['pattern']
        assert 1 <= len(pattern) <= 3 and len({len(row) for row in pattern}) == 1
        assert 1 <= len(pattern[0]) <= 3
        assert set(''.join(pattern))-{' '} == set(obj['key'])
assert len(recipes) >= 52
parents = {}
visible = 0
for path in (RES/'data/slavicmyths/advancements').rglob('*.json'):
    obj = read(path)
    name = path.relative_to(RES/'data/slavicmyths/advancements').with_suffix('').as_posix()
    parent = obj.get('parent', '')
    if parent.startswith('slavicmyths:'):
        parents[name] = parent.split(':')[1]
        assert (RES/f'data/slavicmyths/advancements/{parents[name]}.json').is_file()
    if 'display' in obj:
        visible += 1
        item_ref(obj['display']['icon']['item'])
        for lang in ('ru_ru','en_us'):
            data = read(RES/f'assets/slavicmyths/lang/{lang}.json')
            for field in ('title','description'):
                assert obj['display'][field]['translate'] in data
    for criteria in obj['criteria'].values():
        for pred in criteria.get('conditions', {}).get('items', []):
            item_ref(pred['item'])
    for row in obj.get('requirements', []):
        assert set(row) <= set(obj['criteria'])
    for recipe in obj.get('rewards', {}).get('recipes', []):
        assert recipe.split(':')[1] in recipes
for node in parents:
    seen = set()
    while node in parents:
        assert node not in seen, 'Advancement cycle'
        seen.add(node)
        node = parents[node]
assert visible >= 28
assert not (RES/'data/minecraft/advancements').exists(), 'Do not override vanilla advancements'
assert not (RES/'data/minecraft/loot_tables').exists(), 'Do not overwrite vanilla loot'
global_loot = read(RES/'data/forge/loot_modifiers/global_loot_modifiers.json')
assert global_loot['replace'] is False
for entry in global_loot['entries']:
    obj = read(RES/('data/slavicmyths/loot_modifiers/'+entry.split(':')[1]+'.json'))
    assert obj['type'].split(':')[1] in registered('registry/ModLoot.java')
jar = ROOT/f'build/libs/slavicmyths-{VERSION}.jar'
with zipfile.ZipFile(jar) as archive:
    assert archive.testzip() is None
    assert not any('smoke' in n or n.startswith('mezz/') or n.endswith('.jar') for n in archive.namelist()), 'Bundled test/third-party content'
    for path in RES.rglob('*'):
        if path.is_file():
            name = path.relative_to(RES).as_posix()
            packaged = archive.read(name)
            if name != 'META-INF/mods.toml':
                assert packaged == path.read_bytes(), f'Stale resource {name}'
    metadata = archive.read('META-INF/mods.toml').decode('utf-8')
    assert '${' not in metadata and f'version="{VERSION}"' in metadata
    classes = [n for n in archive.namelist() if n.endswith('.class')]
    assert classes
    for name in classes:
        assert struct.unpack('!H', archive.read(name)[6:8])[0] == 52
    # Official development method names must be remapped in the distributed mod.
    assert b'func_78016_d' in archive.read('org/slavicmyths/registry/ModItemGroup$1.class')
print(f'PASS: {len(items)} item registrations, {len(blocks)} block registrations, {len(pngs)} PNGs, {len(recipes)} recipes, {visible} advancements, JSON references, JAR contents, Java 8 and reobfuscation.')
print('Structural checks do not replace Minecraft gameplay tests.')

entities = registered('registry/ModEntities.java')
wildlife=set(re.findall(r'wildlife\("([a-z_]+)"',(JAVA/'registry/ModEntities.java').read_text()))
entities.update(wildlife)
assert {'domovoy','leshy','kikimora','poludnitsa','polevik','bannik','igosha','ovinnik','hot_stone'} <= entities
for entity in entities - {'hot_stone'}:
    if entity in {'flying_broom','flying_mortar'}:
        assert (RES/f'assets/slavicmyths/models/item/{entity}.json').is_file()
        assert (JAVA/'client/FlightRenderer.java').is_file() and (JAVA/'flight/FlyingVessel.java').is_file()
        continue
    assert list((RES/'assets/slavicmyths/textures/entity').glob(entity+'*.png'))
    if entity in wildlife:
        loot={'brown_bear':'bear','bear_cub':'cub','forest_wolf':'wolf'}.get(entity,entity)
        assert (RES/f'data/slavicmyths/loot_tables/entities/{loot}.json').is_file()
        assert (JAVA/'client/WildlifeRenderer.java').is_file() and (JAVA/'client/WildlifeModel.java').is_file()
        continue
    assert (RES/f'data/slavicmyths/loot_tables/entities/{entity}.json').is_file()
    renderer = (JAVA/f'client/{entity.capitalize()}Renderer.java').read_text()
    assert 'textures/entity/' in renderer and entity in renderer
    model = (JAVA/f'client/{entity.capitalize()}Model.java').read_text()
    assert 'texWidth = 64; texHeight = 64' in model or 'texWidth=256;texHeight=256' in model
for path in (RES/'data/slavicmyths/loot_tables/entities').glob('*.json'):
    assert read(path)['type'] == 'minecraft:entity'
import sys
sys.path.insert(0,str(ROOT/'.tools/audio-libs'))
import soundfile as sf
import numpy as np
sounds = read(RES/'assets/slavicmyths/sounds.json')
assert len(sounds) >= 37
registered_sounds = set(re.findall(r'sound\("([a-z_]+)"', (JAVA/'registry/ModSounds.java').read_text()))
registered_sounds.update(a+'_'+e for a in ('bear','cub','wolf','boar','stag','doe') for e in ('ambient','hurt','death','alert','roar','attack','impact'))
assert set(sounds) == registered_sounds
for name,event in sounds.items():
    for lang in ('ru_ru','en_us'):
        assert event['subtitle'] in read(RES/f'assets/slavicmyths/lang/{lang}.json')
    for entry in event['sounds']:
        if entry.get('type') == 'event' or entry['name'].startswith('minecraft:'): continue
        path = RES/('assets/slavicmyths/sounds/'+entry['name'].split(':')[1]+'.ogg')
        info = sf.info(path)
        data,rate = sf.read(path)
        assert info.format == 'OGG' and info.subtype == 'VORBIS' and info.channels == 1
        assert rate == 22050 and len(data)>0 and np.isfinite(data).all() and np.max(np.abs(data)) < 1.0
print('PASS: entity/model/renderer resources, decoded mono Vorbis sounds, subtitles, no vanilla advancement overrides or bundled JEI/test code.')
