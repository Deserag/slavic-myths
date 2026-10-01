"""Read packaged NBT independently; verify template bounds, markers, links and reproducibility.
This is resource verification, not a Minecraft run or visual review.
"""
from pathlib import Path
import gzip
import hashlib
import io
import json
import re
import struct
import zipfile
import swamp_073

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'


def read_nbt(raw):
    stream = io.BytesIO(gzip.decompress(raw))
    def number(fmt): return struct.unpack('>' + fmt, stream.read(struct.calcsize('>' + fmt)))[0]
    def string(): return stream.read(number('H')).decode('utf-8')
    def value(kind):
        if kind == 3: return number('i')
        if kind == 8: return string()
        if kind == 9:
            item, length = number('B'), number('i')
            return [value(item) for _ in range(length)]
        if kind == 10:
            result = {}
            while True:
                child = number('B')
                if not child: return result
                key = string()
                result[key] = value(child)
        raise AssertionError('Unexpected NBT type ' + str(kind))
    assert number('B') == 10
    string()
    result = value(10)
    assert not stream.read()
    return result


def main():
    templates = sorted((RES / 'data/slavicmyths/structures/swamp').glob('*.nbt'))
    assert len(templates) == 23
    hashes = {p: hashlib.sha256(p.read_bytes()).digest() for p in templates}
    localized = {p: p.read_bytes() for p in (RES / 'assets/slavicmyths/lang').glob('*.json')}
    swamp_073.generate()
    assert hashes == {p: hashlib.sha256(p.read_bytes()).digest() for p in templates}, 'Non-reproducible templates'
    assert all(p.read_bytes() == data for p, data in localized.items()), 'Non-reproducible localization'
    definitions = {}
    for path in templates:
        doc = read_nbt(path.read_bytes())
        assert doc['DataVersion'] == 2586 and not doc['entities']
        assert len(doc['blocks']) > 2
        positions = set()
        for block in doc['blocks']:
            pos = tuple(block['pos'])
            assert pos not in positions
            positions.add(pos)
            assert all(0 <= x < limit for x, limit in zip(pos, doc['size']))
            state = doc['palette'][block['state']]
            assert re.fullmatch(r'(minecraft|slavicmyths):[a-z_]+', state['Name'])
            if 'nbt' in block:
                assert state['Name'] == 'minecraft:structure_block'
                data = block['nbt']
                assert data['mode'] == 'DATA'
                marker = data['metadata']
                if marker.startswith('loot:'):
                    assert (RES / ('data/slavicmyths/loot_tables/chests/' + marker[5:] + '.json')).is_file()
                else: assert marker in ('encounter:kikimora', 'encounter:vodyanoy', 'encounter:rusalka')
        definitions[path.stem] = doc
    for family in ('hut', 'ruin'):
        assert len({hashes[RES / f'data/slavicmyths/structures/swamp/{family}_{i}.nbt'] for i in range(3)}) == 3
    for id in ('hut_0', 'hut_1', 'hut_2', 'settlement_small', 'settlement_main', 'settlement_flooded'):
        blocks = {tuple(b['pos']): definitions[id]['palette'][b['state']]['Name'] for b in definitions[id]['blocks']}
        assert 'minecraft:furnace' in blocks.values()
        assert any(y >= 8 and name == 'minecraft:cobblestone_wall' for (_, y, _), name in blocks.items())
        assert sum(name == 'minecraft:spruce_stairs' for name in blocks.values()) > 25
    java = (ROOT / 'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(encoding='utf-8')
    items = set(re.findall(r'(?:register|food|stew|egg)\(\s*"([a-z_]+)"', java))
    for path in (RES / 'data/slavicmyths/loot_tables/chests').glob('swamp_*.json'):
        for pool in json.loads(path.read_text(encoding='utf-8'))['pools']:
            for entry in pool['entries']:
                if entry.get('name', '').startswith('slavicmyths:'):
                    assert entry['name'][12:] in items, entry['name']
    ids = ['swamp_hut', 'abandoned_settlement', 'bog_causeway', 'flooded_shrine', 'fishing_camp', 'underwater_ruins']
    for id in ids:
        doc = json.loads((RES / f'data/slavicmyths/advancements/explore_{id}.json').read_text())
        assert doc['criteria']['visit']['conditions']['location']['feature'] == 'slavicmyths:' + id
    with zipfile.ZipFile(ROOT / 'build/libs/slavicmyths-0.7.3.jar') as jar:
        for path in templates:
            assert jar.read(path.relative_to(RES).as_posix()) == path.read_bytes()
        for name in ('SwampStructure', 'SwampStructures', 'SwampPiece', 'SwampCommands'):
            assert jar.read('org/slavicmyths/swamp/' + name + '.class')[6:8] == b'\x00\x34'
    print('PASS: 23 independently decoded NBT templates, bounds, deterministic generation, 7 loot tables, 6 discovery links, Java 8 classes and packaged templates.')


if __name__ == '__main__': main()
