#!/usr/bin/env python3
"""Generate exact Minecraft 1.21.1 structure NBT for posad buildings #347/#348.
Run from repository root: python3 tools/generate_posad_buildings.py
Requires only Python 3 standard library. Does NOT place/retrofit world structures.
"""
import gzip
import json
import runpy
import struct
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "docs/concepts/nether-settlement-2.2"
OUT = ROOT / "src/main/resources/data/slavicmyths/structure/posad"
VE = DOC / "2.2_VECHE_HALL_VOXELS_R1.py"
HOUSE = DOC / "communal-house-2.2.3/2.2_COMMUNAL_HOUSE_MODEL_R1.json"

def tag(tag_id, name, payload):
    b = name.encode("utf8")
    return bytes([tag_id]) + struct.pack(">H", len(b)) + b + payload

def nstr(value):
    b = value.encode("utf8")
    return struct.pack(">H", len(b)) + b

def string(name, value):
    return tag(8, name, nstr(value))

def integer(name, value):
    return tag(3, name, struct.pack(">i", value))

def compound(name, parts):
    return tag(10, name, b"".join(parts) + b"\\x00")

def list_tag(name, element_type, parts):
    return tag(9, name, bytes([element_type]) + struct.pack(">i", len(parts)) + b"".join(parts))

def bare_compound(parts):
    return b"".join(parts) + b"\\x00"

def bare_int_list(numbers):
    return struct.pack(">i", len(numbers)) + b"".join(struct.pack(">i", n) for n in numbers)

def block_state(source):
    base, _, rest = source.partition("[")
    props = {}
    if rest:
        assert rest.endswith("]"), source
        props = dict(item.split("=", 1) for item in rest[:-1].split(","))
    return base, props

def assemble(name, size, blocks, loot=None):
    assert len(blocks) == len(set(blocks)), "Overlapping physical block coordinates"
    assert all(0 <= p[i] < size[i] for p in blocks for i in range(3)), "Out-of-range coordinate"
    palette = sorted(set(blocks.values()))
    indexes = {state: i for i, state in enumerate(palette)}
    palette_entries = []
    for state in palette:
        base, props = block_state(state)
        fields = [string("Name", base)]
        if props:
            fields.append(compound("Properties", [string(k, v) for k, v in sorted(props.items())]))
        palette_entries.append(bare_compound(fields))
    block_entries = []
    for pos, state in sorted(blocks.items(), key=lambda e: (e[0][1], e[0][2], e[0][0])):
        fields = [integer("state", indexes[state]), list_tag("pos", 3, [struct.pack(">i", v) for v in pos])]
        if loot and pos in loot:
            assert state.startswith("minecraft:chest["), "Loot marker must occupy actual chest"
            fields.append(compound("nbt", [string("id", "minecraft:chest"), string("LootTable", loot[pos])]))
        block_entries.append(bare_compound(fields))
    root = [integer("DataVersion", 3955),
            list_tag("size", 3, [struct.pack(">i", n) for n in size]),
            list_tag("palette", 10, palette_entries),
            list_tag("blocks", 10, block_entries),
            list_tag("entities", 10, [])]
    output = b"\\x0a\\x00\\x00" + bare_compound(root)
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / (name + ".nbt")
    with path.open("wb") as f:
        with gzip.GzipFile(filename="", mode="wb", fileobj=f, mtime=0) as g:
            g.write(output)
    print(f"{path.relative_to(ROOT)}: {len(blocks)} blocks, {len(palette)} states, {len(loot or {})} loot chests")

def veche():
    data = runpy.run_path(str(VE))
    entries = data["decode"]()
    assert len(entries) == 3598
    blocks = {(v["u"] + 1, v["y"], v["v"] + 1): v["block"] for v in entries}
    assert len(blocks) == 3598
    # Template origin city (42,12); dimensions X/Y/Z=(29,18,23).
    assert all(42 <= 42+x <= 70 and 12 <= 12+z <= 34 for x,y,z in blocks)
    assemble("veche_hall_2_2_2", (29, 18, 23), blocks)

def communal():
    data = json.loads(HOUSE.read_text(encoding="utf8"))
    grid = data["grid"]
    sx, sy, sz = grid["width"], grid["height"], grid["depth"]
    assert (sx,sy,sz) == (23,17,15)
    assert (grid["x_min"],grid["z_min"]) == (-1,-1)
    blocks = {}
    tally = Counter()
    for y, layer in enumerate(data["rows_y_z_x"]):
        assert len(layer) == sz
        for z, row in enumerate(layer):
            assert len(row) == sx
            for x, symbol in enumerate(row):
                assert symbol in data["palette"]
                if symbol == ".":
                    continue
                tally[symbol] += 1
                blocks[(x,y,z)] = data["palette"][symbol]
    assert len(blocks) == data["non_air_block_count"] == 1992
    assert dict(tally) == data["by_symbol"], "Blueprint symbol counts differ"
    loot = {}
    for marker in data["loot_markers"]:
        x,y,z = marker["pos"]
        pos = (x-grid["x_min"],y-grid["y_min"],z-grid["z_min"])
        assert pos in blocks
        assert pos not in loot
        loot[pos] = marker["loot_table"]
    assert len(loot) == 4
    assert all(12 <= 13+x <= 37 and 12 <= 15+z <= 37 for x,y,z in blocks)
    assemble("communal_house_2_2_3", (sx,sy,sz), blocks, loot)

if __name__ == "__main__":
    veche()
    communal()
