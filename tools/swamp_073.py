"""Reproducible, authored 1.16.5 structure NBT. No random block-deletion processors.

Coordinates describe intact carpentry first; each damage rectangle is intentional.
No Minecraft installation or third-party Python packages are required.
"""
from pathlib import Path
import gzip
import json
import struct

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
OUT = RES / 'data/slavicmyths/structures/swamp'


def string(s):
    raw = s.encode('utf-8')
    return struct.pack('>H', len(raw)) + raw


def tag(value):
    if isinstance(value, str): return 8, string(value)
    if isinstance(value, int): return 3, struct.pack('>i', value)
    if isinstance(value, list):
        children = [tag(v) for v in value]
        kind = children[0][0] if children else 10
        assert all(k == kind for k, _ in children)
        return 9, bytes([kind]) + struct.pack('>i', len(children)) + b''.join(b for _, b in children)
    if isinstance(value, dict):
        return 10, b''.join(bytes([tag(v)[0]]) + string(k) + tag(v)[1] for k, v in value.items()) + b'\0'
    raise TypeError(value)


class Blueprint:
    def __init__(self, name, size):
        self.name, self.size, self.blocks = name, size, {}

    def put(self, x, y, z, block, **props):
        assert all(0 <= v < s for v, s in zip((x, y, z), self.size)), (self.name, x, y, z)
        state = {'Name': block if ':' in block else 'minecraft:' + block}
        if props: state['Properties'] = {k: str(v).lower() for k, v in props.items()}
        self.blocks[x, y, z] = (state, None)

    def box(self, a, b, block, **props):
        for x in range(a[0], b[0] + 1):
            for y in range(a[1], b[1] + 1):
                for z in range(a[2], b[2] + 1): self.put(x, y, z, block, **props)

    def loot(self, x, y, z, category):
        self.put(x, y, z, 'structure_block', mode='data')
        state, _ = self.blocks[x, y, z]
        self.blocks[x, y, z] = (state, {'id': 'minecraft:structure_block', 'mode': 'DATA', 'metadata': 'loot:swamp_' + category})

    def encounter(self, x, y, z, kind):
        self.put(x, y, z, 'structure_block', mode='data')
        state, _ = self.blocks[x, y, z]
        self.blocks[x, y, z] = (state, {'id': 'minecraft:structure_block', 'mode': 'DATA', 'metadata': 'encounter:' + kind})

    def save(self):
        palette, lookup, blocks = [], {}, []
        for pos, (state, nbt) in sorted(self.blocks.items(), key=lambda entry: (entry[0][1], entry[0][2], entry[0][0])):
            key = json.dumps(state, sort_keys=True)
            if key not in lookup:
                lookup[key] = len(palette)
                palette.append(state)
            block = {'pos': list(pos), 'state': lookup[key]}
            if nbt: block['nbt'] = nbt
            blocks.append(block)
        document = {'DataVersion': 2586, 'size': list(self.size), 'palette': palette, 'blocks': blocks, 'entities': []}
        OUT.mkdir(parents=True, exist_ok=True)
        (OUT / (self.name + '.nbt')).write_bytes(gzip.compress(b'\x0a\0\0' + tag(document)[1], mtime=0))
        return {'size': self.size, 'blocks': len(blocks), 'containers': sum(n is not None and n.get('metadata', '').startswith('loot:') for _, n in self.blocks.values())}


def stair(b, x, y, z, facing, material='spruce', **props):
    b.put(x, y, z, material + '_stairs', facing=facing, half='bottom', shape='straight', waterlogged='false', **props)


def slab(b, x, y, z, material='spruce', top=False, wet=False):
    b.put(x, y, z, material + '_slab', type='top' if top else 'bottom', waterlogged=wet)


def log(b, x, y, z, axis='y', stripped=False):
    b.put(x, y, z, ('stripped_' if stripped else '') + 'spruce_log', axis=axis)


def roof(b, x, z, width, length, y, damage=None):
    # Ridge runs along X; 7-wide house gets four rising rows including the eave.
    for a in range(x - 1, x + length + 1):
        for d in range((width + 2) // 2):
            for side in (0, 1):
                zz = z - 1 + d if side == 0 else z + width - d
                if damage and damage[0] <= a <= damage[1] and damage[2] <= zz <= damage[3]: continue
                stair(b, a, y + d, zz, 'south' if side == 0 else 'north')
        center = z + width // 2
        if width % 2:
            slab(b, a, y + (width + 1) // 2, center)
    # Closed gables beneath roof, excluding the eaves.
    for a in (x, x + length - 1):
        for d in range(1, (width + 1) // 2):
            b.box((a, y + d - 1, z + d), (a, y + d - 1, z + width - 1 - d), 'spruce_planks')


def stove(b, x, z, roof_y=9):
    b.box((x, 2, z), (x + 1, 3, z + 1), 'cobblestone')
    b.put(x + 1, 2, z + 1, 'furnace', facing='south', lit=False)
    slab(b, x, 4, z, 'cobblestone')
    for y in range(4, roof_y + 1): b.put(x + 1, y, z, 'cobblestone_wall')


def house(name, length=9, width=7, variant=0, category='home', annex=True):
    # Main room LxW, one-block raised floor, three-block walls. Front faces south.
    b = Blueprint(name, (length + 4, 11, width + 7))
    x, z = 2, 1
    b.box((x, 1, z), (x + length - 1, 1, z + width - 1), 'spruce_planks')
    b.box((x, 2, z), (x + length - 1, 8, z + width - 1), 'air')
    for a in range(x, x + length):
        for zz in (z, z + width - 1):
            for y in range(2, 5): log(b, a, y, zz, 'x', stripped=(y == 3))
    for zz in range(z, z + width):
        for a in (x, x + length - 1):
            for y in range(2, 5): log(b, a, y, zz, 'z', stripped=(y == 3))
    for a in (x, x + length - 1):
        for zz in (z, z + width - 1):
            b.put(a, 0, zz, 'mossy_cobblestone')
            for y in range(1, 5): log(b, a, y, zz)
    for a in (x + 2, x + length - 3):
        log(b, a, 0, z + width // 2)
    for a, zz in ((x, z + 3), (x + length - 1, z + 2), (x + length - 3, z)):
        b.put(a, 3, zz, 'air')
    door = x + length - 3
    b.box((door, 2, z + width - 1), (door, 3, z + width - 1), 'air')
    damage = (x + 3, x + 4, z, z + 1) if variant == 0 else (x + length - 4, x + length - 2, z + width - 2, z + width)
    roof(b, x, z, width, length, 5, damage)
    stove(b, x + 1, z + 1)
    # Table, two benches, shelf and storage: front-right storage preserves circulation.
    b.put(x + length - 3, 2, z + 2, 'spruce_fence')
    b.put(x + length - 3, 3, z + 2, 'spruce_trapdoor', facing='north', half='bottom', open=False, powered=False, waterlogged=False)
    for a in range(x + 1, x + 3): stair(b, a, 2, z + width - 2, 'north')
    stair(b, x + length - 2, 2, z + 3, 'west')
    slab(b, x + 2, 3, z + 1, top=True)
    b.put(x + 3, 2, z + 1, 'flower_pot')
    b.loot(x + length - 2, 2, z + width - 2, category)
    if name == 'settlement_main': b.loot(x + length - 2, 2, z + 1, 'hidden')
    if annex:
        # Small vestibule: a roof lower than the main ridge, left partition and open doorway.
        b.box((door - 3, 1, z + width), (door + 1, 1, z + width + 1), 'spruce_planks')
        b.box((door - 3, 2, z + width), (door + 1, 3, z + width + 1), 'air')
        for a in (door - 3, door + 1):
            for zz in (z + width, z + width + 1):
                for y in range(2, 4): log(b, a, y, zz)
        for a in range(door - 2, door + 1):
            if a != door: b.box((a, 2, z + width + 1), (a, 3, z + width + 1), 'spruce_planks')
        for a in range(door - 3, door + 2):
            stair(b, a, 4, z + width + 1, 'north')
            slab(b, a, 4, z + width, top=True)
        porch_z = z + width + 2
    else: porch_z = z + width
    for a in range(door - 1, door + 2):
        for zz in range(porch_z, porch_z + 2): slab(b, a, 1, zz, top=True)
        stair(b, a, 0, porch_z + 2, 'north')
    log(b, door - 1, 0, porch_z + 1)
    log(b, door + 1, 0, porch_z + 1)
    if variant:
        # One connected collapse below the roof breach, never random pinholes.
        b.box((x + length - 3, 1, z + width - 3), (x + length - 2, 1, z + width - 2), 'water' if variant == 1 else 'coarse_dirt')
        b.box((x + length - 1, 2, z + width - 3), (x + length - 1, 3, z + width - 2), 'air')
        if variant == 1:
            b.box((x + length - 2, 0, z + 3), (x + length, 0, z + width - 1), 'water')
            b.loot(x + length - 2, 0, z + width - 2, 'hidden')
            slab(b, door + 1, 0, porch_z + 1, wet=True)
        else:
            for a, zz in ((x + length - 3, z + width - 3), (x + length - 2, z + width - 2)):
                b.put(a, 2, zz, 'brown_mushroom')
            b.box((x - 1, 0, z + 3), (x - 1, 0, z + 5), 'podzol')
            b.put(x - 1, 1, z + 4, 'fern')
            b.put(x + length - 1, 2, z + width - 1, 'vine', south=True)
            b.box((x + length - 1, 2, z + 2), (x + length - 1, 3, z + width - 2), 'air')
    if name.startswith('hut_'): b.encounter(x + 3, 2, z + 3, 'kikimora')
    return b


def causeway(name, fork=False):
    b = Blueprint(name, (13 if fork else 4, 4, 31 if fork else 25))
    for z in range(1, b.size[2] - 1):
        for x in (1, 2):
            log(b, x, 0, z, 'z')
            if z == 17 and x == 2: continue  # one missing plank; road remains passable
            slab(b, x, 1 if z not in (10, 11) else 0, z, top=True, wet=z in (10, 11))
    for z in (3, 8, 15, 22):
        for x in (1, 2): log(b, x, 0, z)
    if fork:
        for n in range(9):
            for d in (0, 1):
                log(b, 3 + n, 0, 18 + n // 2 + d, 'x')
                if n < 7: slab(b, 3 + n, 1, 18 + n // 2 + d, top=True)
    b.put(0, 0, 14, 'podzol'); b.put(0, 1, 14, 'fern')
    return b


def well():
    b = Blueprint('settlement_well', (9, 8, 9))
    for x in range(9):
        for z in range(9):
            if 1 < x + z < 15: b.put(x, 0, z, 'coarse_dirt' if (x + z) % 3 else 'grass_block')
    b.box((3, 0, 3), (5, 1, 5), 'mossy_cobblestone')
    b.put(4, 1, 4, 'water')
    for x in (3, 5):
        for y in range(2, 5): log(b, x, y, 4)
    roof(b, 3, 3, 3, 3, 4)
    b.put(2, 1, 6, 'fern')
    return b


def barn():
    b = Blueprint('settlement_barn', (8, 7, 8))
    b.box((1, 1, 1), (6, 1, 5), 'spruce_planks')
    b.box((1, 2, 1), (6, 4, 5), 'air')
    for x in (1, 6):
        for z in (1, 5):
            for y in range(4): log(b, x, y, z)
    b.box((2, 2, 1), (5, 3, 1), 'spruce_planks')
    b.box((1, 2, 2), (1, 3, 4), 'spruce_planks')
    b.box((6, 2, 2), (6, 2, 3), 'spruce_planks')
    roof(b, 1, 1, 5, 6, 3, (4, 6, 4, 6))
    b.loot(2, 2, 2, 'supplies'); b.put(3, 2, 2, 'barrel', facing='up', open=False)
    b.put(2, 2, 4, 'hay_block', axis='y'); b.put(5, 2, 2, 'crafting_table')
    return b


def paths(variant):
    b = Blueprint('settlement_paths_' + str(variant), (45, 3, 49))
    # L-shaped, offset links; never a perfectly crossed plaza. Sparse voids preserve terrain.
    routes = [((18, 12), (18, 19)), ((8, 23), (16, 23)), ((24, 22), (29, 22)), ((22, 25), (22, 31)), ((22, 43), (22, 48))]
    if variant: routes[-2:] = [((23, 25), (23, 34)), ((23, 34), (33, 34)), ((33, 34), (33, 47))]
    for (x, z), (xx, zz) in routes:
        for a in range(x, xx + 1):
            for c in range(z, zz + 1):
                b.put(a, 0, c, 'coarse_dirt'); b.put(a + 1, 0, c, 'coarse_dirt')
    for x, z, length in ((1, 30, 7), (30, 9, 8), (8, 4, 5)):
        for i in range(length):
            if i == length - 2: continue
            log(b, x + i, 0, z)
            b.put(x + i, 1, z, 'spruce_fence', east=i != length - 1, west=i != 0)
    return b


def shrine(variant):
    b = Blueprint('shrine_' + str(variant), (19, 10, 19))
    # Submerged 5x4 niche. Water replaces only this excavation and the platform footprint.
    b.box((6, 0, 6), (12, 0, 11), 'mossy_cobblestone')
    b.box((6, 1, 6), (12, 3, 11), 'water')
    for z in range(6, 12):
        for x in (6, 12): b.box((x, 1, z), (x, 3, z), 'mossy_cobblestone')
    b.box((7, 1, 6), (11, 3, 6), 'mossy_cobblestone')
    # Side entrance faces south; no sealed loot room.
    for x in (7, 11): b.box((x, 1, 11), (x, 3, 11), 'mossy_cobblestone')
    b.loot(8, 1, 7, 'shrine')
    b.encounter(10, 1, 9, 'vodyanoy')
    for x in range(6, 13):
        for z in range(6, 13):
            if x >= 11 and z >= 10: slab(b, x, 3, z, 'mossy_cobblestone', wet=True)
            else: b.put(x, 4, z, 'mossy_cobblestone' if (x + z) % 3 else 'stone')
    # Weathered broken idol: squat base and asymmetrical upper head, no light.
    b.put(9, 5, 8, 'mossy_cobblestone'); b.put(9, 6, 8, 'chiseled_stone_bricks')
    stair(b, 9, 7, 8, 'west', 'stone_brick')
    pillars = [(3, 4, 5), (9, 2, 7), (15, 4, 2), (16, 10, 6), (13, 15, 1), (4, 14, 5), (2, 9, 1)]
    for i, (x, z, height) in enumerate(pillars):
        if variant and i in (0, 3): height = 2 if i == 0 else 4
        b.put(x, 0, z, 'mossy_cobblestone')
        for y in range(1, height): log(b, x, y, z, stripped=True)
    for x in range(12, 16): log(b, x, 1, 14, 'x', True)
    for z in range(15, 18):
        for x in (8, 9): slab(b, x, 2, z, 'stone', wet=True)
    return b


def boat(b, x, y, z):
    # Seven-block hull, raised gunwales, tapered bow, open damaged stern.
    for zz in range(z + 1, z + 6):
        slab(b, x + 1, y, zz, top=True, wet=True)
        for xx in (x, x + 2): slab(b, xx, y + 1, zz, wet=True)
    stair(b, x + 1, y + 1, z, 'south')
    stair(b, x + 1, y + 1, z + 6, 'north')
    b.put(x + 2, y + 1, z + 4, 'water')
    slab(b, x + 1, y + 1, z + 2, wet=True)


def camp(variant):
    b = Blueprint('camp_' + str(variant), (23, 8, 18))
    b.box((2, 1, 2), (7, 1, 6), 'spruce_planks')
    b.box((2, 2, 2), (7, 4, 6), 'air')
    for x in (2, 7):
        for z in (2, 6):
            for y in range(5): log(b, x, y, z)
    for x in range(1, 9):
        for z in range(1, 8):
            slab(b, x, 5 if z <= 3 else 4, z, top=z > 3)
    b.loot(3, 2, 3, 'fishing'); b.put(4, 2, 3, 'barrel', facing='up', open=False)
    b.put(6, 2, 3, 'crafting_table')
    b.put(7, 0, 11, 'slavicmyths:fishing_net', filled=False)
    for z in range(7, 17):
        for x in (5, 6):
            if z > 13 and (x == 6 or z == 16): continue
            slab(b, x, 1, z, top=True)
            if z in (7, 10, 13): log(b, x, 0, z)
    boat(b, 9 if not variant else 12, 0, 10)
    for x in range(9, 15):
        log(b, x, 0, 4, 'x')
    if variant:
        b.box((2, 3, 2), (2, 4, 2), 'air')
        b.box((1, 5, 1), (3, 5, 2), 'air')
    return b


def ruin(variant):
    b = Blueprint('ruin_' + str(variant), (17, 7, 17))
    if variant == 2:
        # Old waterfront, not another room.
        for x in (2, 6, 10, 14):
            for y in range(4 if x != 10 else 2): log(b, x, y, 5)
        for z in range(6, 14):
            for x in (6, 7):
                if z not in (10, 13): slab(b, x, 2, z, wet=True)
        b.box((2, 0, 1), (7, 0, 4), 'mossy_cobblestone')
        b.loot(3, 1, 2, 'ruins'); boat(b, 11, 0, 8)
        b.encounter(9, 1, 9, 'rusalka')
        return b
    length, width = (9, 7) if variant == 0 else (11, 8)
    x, z = 2, 2
    b.box((x, 0, z), (x + length - 1, 0, z + width - 1), 'mossy_cobblestone')
    b.box((x, 1, z), (x + length - 1, 4, z + width - 1), 'water')
    b.box((x, 1, z), (x + length - 1, 1, z), 'spruce_planks')
    b.box((x, 1, z), (x, 2, z + width - 1), 'spruce_planks')
    b.box((x + 1, 2, z), (x + 3, 2, z), 'spruce_planks')
    for a, zz, h in ((x, z, 4), (x + length - 1, z, 4), (x, z + width - 1, 3), (x + length - 1, z + width - 1, 2)):
        for y in range(1, h + 1): log(b, a, y, zz)
    for a in range(x + 1, x + length - 1): log(b, a, 4, z, 'x')
    if variant == 0:
        b.put(x + 1, 1, z + 1, 'furnace', facing='south', lit=False)
        for a in range(5, 10):
            for zz in range(11, 14): slab(b, a, 1 + (zz == 12), zz, wet=True)
    else:
        for a in range(x, x + 5):
            for zz in range(z, z + 4): slab(b, a, 4 + (zz == z + 2), zz, wet=True)
        b.loot(9, 1, 3, 'supplies'); b.put(10, 1, 3, 'barrel', facing='up', open=False)
        for zz in range(3, 8): log(b, 14, 1, zz, 'z')
    b.loot(x + 2, 1, z + width - 2, 'ruins')
    b.encounter(x + 4, 1, z + 3, 'vodyanoy')
    return b


def poi(variant):
    b = Blueprint('poi_' + str(variant), (12, 5, 12))
    if variant == 0:
        b.put(4, 0, 4, 'coarse_dirt'); b.put(4, 1, 4, 'campfire', lit=False, signal_fire=False, waterlogged=False, facing='north')
        for x in range(2, 5): log(b, x, 1, 2, 'x')
        for z in range(4, 7): log(b, 6, 1, z, 'z')
        b.loot(2, 1, 6, 'remnants')
    elif variant == 1:
        # Block-composed cart: two round-ish stair wheels, axle, bed and one shaft.
        for x in (2, 6):
            stair(b, x, 0, 4, 'south', 'dark_oak'); stair(b, x, 0, 5, 'north', 'dark_oak')
            slab(b, x, 1, 4, 'dark_oak'); slab(b, x, 1, 5, 'dark_oak')
        for x in range(3, 6): log(b, x, 0, 4, 'x')
        b.box((3, 1, 3), (5, 1, 6), 'spruce_planks')
        for x in (3, 5):
            for z in range(3, 7): b.put(x, 2, z, 'spruce_trapdoor', half='bottom', open=True, facing='east' if x == 3 else 'west')
        for z in (7, 8, 9): b.put(4, 1, z, 'spruce_fence', north=True, south=True)
        b.loot(4, 2, 4, 'remnants')
    elif variant == 2:
        for z in range(1, 10):
            for x in (4, 5):
                if z > 7 and x == 5: continue
                log(b, x, 0, z, 'z'); slab(b, x, 1, z, top=True)
    else:
        b.put(4, 0, 4, 'mossy_cobblestone'); b.put(4, 1, 4, 'chiseled_stone_bricks')
        b.put(5, 0, 4, 'podzol'); b.put(5, 1, 4, 'fern')
    return b


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def generate():
    buildings = [house('hut_' + str(i), variant=i) for i in range(3)]
    buildings += [house('settlement_small', 7, 6, 2, annex=False), house('settlement_main', 10, 8), house('settlement_flooded', 8, 7, 1, 'hidden')]
    buildings += [barn(), well()] + [paths(i) for i in range(2)]
    buildings += [causeway('causeway_' + str(i), bool(i)) for i in range(2)]
    buildings += [shrine(i) for i in range(2)] + [camp(i) for i in range(2)]
    buildings += [ruin(i) for i in range(3)] + [poi(i) for i in range(4)]
    write(ROOT / 'docs/verification/structures-0.7.3.json', {b.name: b.save() for b in buildings})
    resources()


def resources():
    # Existing registry IDs only. Empty entries keep rare objects genuinely optional.
    groups = {
        'home': [('minecraft:bread', 12, 1, 3), ('slavicmyths:flour', 9, 1, 3), ('minecraft:bowl', 8, 1, 2), ('slavicmyths:wormwood', 7, 1, 2), ('minecraft:string', 8, 1, 4), ('slavicmyths:ancient_coin', 1, 1, 1)],
        'supplies': [('minecraft:spruce_planks', 12, 3, 9), ('minecraft:stick', 10, 2, 5), ('minecraft:iron_nugget', 8, 2, 6), ('minecraft:stone_axe', 2, 1, 1), ('minecraft:wheat', 6, 1, 4)],
        'hidden': [('slavicmyths:ancient_coin', 5, 1, 2), ('slavicmyths:silver_ingot', 3, 1, 2), ('minecraft:iron_ingot', 7, 1, 2), ('slavicmyths:ancient_water_sign', 1, 1, 1)],
        'fishing': [('minecraft:string', 12, 2, 6), ('minecraft:fishing_rod', 3, 1, 1), ('slavicmyths:fishing_net', 2, 1, 1), ('slavicmyths:raw_pike', 4, 1, 2), ('slavicmyths:raw_carp', 6, 1, 2), ('minecraft:bowl', 8, 1, 2)],
        'shrine': [('slavicmyths:ancient_coin', 10, 1, 3), ('slavicmyths:silver_ingot', 6, 1, 3), ('slavicmyths:wormwood', 10, 1, 3), ('slavicmyths:amber', 4, 1, 1), ('slavicmyths:ancient_water_sign', 2, 1, 1)],
        'ruins': [('slavicmyths:ancient_coin', 9, 1, 3), ('slavicmyths:silver_ingot', 5, 1, 2), ('minecraft:fishing_rod', 4, 1, 1), ('slavicmyths:amber', 2, 1, 1), ('slavicmyths:ancient_water_sign', 1, 1, 1)],
        'remnants': [('minecraft:stick', 12, 1, 3), ('minecraft:flint', 7, 1, 2), ('minecraft:string', 6, 1, 2), ('slavicmyths:ancient_coin', 1, 1, 1)]}
    for category, items in groups.items():
        entries = [{'type': 'minecraft:item', 'name': name, 'weight': weight, 'functions': [{'function': 'minecraft:set_count', 'count': {'min': lo, 'max': hi}}]} for name, weight, lo, hi in items]
        pools = [{'rolls': {'min': 2, 'max': 4}, 'entries': entries}]
        if category in ('ruins', 'shrine'):
            pools.append({'rolls': 1, 'entries': [{'type': 'minecraft:empty', 'weight': 49}, {'type': 'minecraft:item', 'name': 'slavicmyths:warding_charm', 'weight': 1}]})
        write(RES / f'data/slavicmyths/loot_tables/chests/swamp_{category}.json', {'type': 'minecraft:chest', 'pools': pools})
    ids = ['swamp_hut', 'abandoned_settlement', 'bog_causeway', 'flooded_shrine', 'fishing_camp', 'underwater_ruins']
    names = [('Болотная изба', 'Swamp hut'), ('Топь помнит', 'The bog remembers'), ('Старая гать', 'Old causeway'), ('Под чёрной водой', 'Beneath black water'), ('Промысловый стан', 'Fishing camp'), ('Что осталось на дне', 'What rests below')]
    icons = ['minecraft:spruce_log', 'minecraft:cauldron', 'minecraft:spruce_slab', 'minecraft:mossy_cobblestone', 'minecraft:fishing_rod', 'minecraft:barrel']
    for i, name in enumerate(ids):
        write(RES / f'data/slavicmyths/advancements/explore_{name}.json', {
            'parent': 'slavicmyths:traces_of_past',
            'display': {'icon': {'item': icons[i]}, 'title': {'translate': 'advancement.slavicmyths.explore_' + name + '.title'}, 'description': {'translate': 'advancement.slavicmyths.explore_' + name + '.description'}, 'frame': 'task', 'show_toast': True, 'announce_to_chat': True, 'hidden': False},
            'criteria': {'visit': {'trigger': 'minecraft:location', 'conditions': {'location': {'dimension': 'minecraft:overworld', 'feature': 'slavicmyths:' + name}}}}})
    for name, all_required in (('traces_of_past', False), ('bog_explorer', True)):
        criteria = {id: {'trigger': 'minecraft:location', 'conditions': {'location': {'dimension': 'minecraft:overworld', 'feature': 'slavicmyths:' + id}}} for id in ids}
        write(RES / f'data/slavicmyths/advancements/{name}.json', {'parent': 'slavicmyths:traces_of_past' if all_required else 'slavicmyths:root', 'display': {'icon': {'item': 'minecraft:map'}, 'title': {'translate': 'advancement.slavicmyths.' + name + '.title'}, 'description': {'translate': 'advancement.slavicmyths.' + name + '.description'}, 'frame': 'challenge' if all_required else 'task', 'show_toast': True, 'announce_to_chat': True, 'hidden': False}, 'criteria': criteria, 'requirements': [[id] for id in ids] if all_required else [ids]})
    texts = [
        ('Старый человеческий сруб поднят над сырой землёй. Печь и сени напоминают о прежних жильцах. Под повреждённой кровлей бывает вода; осматривай сохранившиеся хозяйственные уголки. Это игровая реконструкция, не точная историческая постройка.', 'An old human log house stands above damp soil. Stove and vestibule recall its inhabitants. Water may collect beneath damaged roofing; inspect the remaining household corners. This is a game reconstruction, not an exact historical building.'),
        ('Колодец связывает двор, дома и сарай. Один дом сохранился лучше, другой постепенно ушёл под воду. Дорожки показывают, как здесь жили; путь наружу теряется в топи. Место покинуто, но не обязательно занято враждебными духами.', 'A well connects the yard, houses and barn. One house survived better; another slowly sank. Paths show how people lived here, while the way out disappears into the bog. Abandoned does not necessarily mean occupied by hostile spirits.'),
        ('Продольные брёвна держат старый настил. Гать ещё помогает пройти мокрый участок, хотя отдельные доски просели. Разрушенная ветка могла когда-то вести к людям.', 'Longitudinal logs support the old walkway. The causeway still crosses wet ground despite sunken planks. A broken branch may once have led to people.'),
        ('Сломанные столбы очерчивают древнюю площадку. Повреждённый знак в центре давно не светится и не требует обряда. Осмотри края под водой: под камнями осталось небольшое пространство. Святилище — авторская игровая интерпретация, не утверждение о конкретном славянском обряде.', 'Broken posts outline an ancient platform. Its weathered central stone needs no ritual. Inspect its underwater edges: a small space remains beneath the stonework. This shrine is an original game interpretation, not a claim about a specific Slavic rite.'),
        ('Навес, бочки и рабочее место принадлежали рыбакам. Причал ещё различим, но его конец разрушен. Рядом под водой лежит деревянный корпус лодки. Здесь стоит искать следы старого промысла.', 'The canopy, barrels and work surface belonged to fishers. The pier remains recognizable, but its end is broken. A wooden boat hull rests underwater nearby. Look for remnants of the old fishing trade.'),
        ('Фундаменты и балки на дне бывают остатками дома, склада или старой пристани. Запомни путь к поверхности прежде, чем нырять. Еда и Амулет глубины помогают подготовиться, но не отменяют запас воздуха.', 'Foundations and beams below may belong to a home, warehouse or old landing. Remember the way to the surface before diving. Food and a Depth Amulet help prepare, but air is still limited.')]
    messages = {
        'overworld': ('Поиск доступен только в Верхнем мире.', 'Search is available only in the Overworld.'),
        'not_found': ('В пределах ограниченного поиска подходящее место не найдено.', 'No suitable location found within the bounded search.'),
        'no_safe': ('Болото найдено: %s, %s. Безопасной сухой поверхности рядом нет; телепортация отменена.', 'Swamp found: %s, %s. No safe dry surface nearby; teleport cancelled.'),
        'teleported': ('Болото: %s %s %s. Новизна чанков не гарантируется.', 'Swamp: %s %s %s. Previously ungenerated chunks are not guaranteed.'),
        'bad_id': ('Неизвестный ID структуры; используй подсказку команды.', 'Unknown structure ID; use command completion.'),
        'structure': ('%s: %s %s %s — подтверждённый старт структуры.', '%s: %s %s %s — confirmed structure start.')}
    for index, language in enumerate(('ru_ru', 'en_us')):
        path = RES / f'assets/slavicmyths/lang/{language}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        for i, id in enumerate(ids):
            data['book.slavicmyths.' + id + '.title'] = names[i][index]
            data['book.slavicmyths.' + id + '.text'] = texts[i][index]
            data['advancement.slavicmyths.explore_' + id + '.title'] = names[i][index]
            data['advancement.slavicmyths.explore_' + id + '.description'] = ('Исследовать: ' if index == 0 else 'Explore: ') + names[i][index]
        for id, titles, descriptions in [('traces_of_past', ('Следы прошлого', 'Traces of the past'), ('Найти первую водно-болотную постройку', 'Find your first wetland structure')), ('bog_explorer', ('Исследователь топей', 'Bog explorer'), ('Посетить шесть основных категорий построек', 'Visit all six major wetland structure categories'))]:
            data['advancement.slavicmyths.' + id + '.title'] = titles[index]
            data['advancement.slavicmyths.' + id + '.description'] = descriptions[index]
        for key, pair in messages.items(): data['swamp.command.' + key] = pair[index]
        write(path, data)


if __name__ == '__main__': generate()

