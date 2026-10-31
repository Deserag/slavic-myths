#!/usr/bin/env python3
"""Verify market parity, native structure NBT and eight original loot contracts."""
import gzip
import json
import re
import struct
from pathlib import Path

root = Path(__file__).resolve().parents[1]
design = json.loads((root / "docs/concepts/nether-settlement-2.2/menovye-ryady-2.2.6/2.2_6_MARKET_VOXELS_DRAFT_A.json").read_text("utf-8"))
compact = json.loads((root / "src/main/resources/data/slavicmyths/posad/menovye_ryady_2_2_6.json").read_text("utf-8"))
assert compact["origin"] == [75, 75] and compact["size"] == [27, 12, 25]
assert len(design["blocks"]) == len(compact["blocks"]) == 2203
assert sum(b["y"] == 0 for b in design["blocks"]) == 675

def canonical(value):
    if "[" not in value:
        return value, {}
    name, tail = value.split("[", 1)
    return name, dict(pair.split("=", 1) for pair in tail[:-1].split(","))

positions = set()
for original, row in zip(design["blocks"], compact["blocks"]):
    xyz = (original["u"], original["y"], original["v"])
    assert row[:3] == list(xyz) and compact["palette"][row[3]] == original["id"]
    assert 0 <= xyz[0] <= 26 and 0 <= xyz[1] <= 11 and 0 <= xyz[2] <= 24
    assert xyz not in positions
    positions.add(xyz)
for v in range(25):
    for u in range(11, 16):
        for y in (1, 2):
            assert (u, y, v) not in positions

markers = {tuple(row[:3]): row[3] for row in compact["chests"]}
assert len(markers) == 8
assert sum(b["id"].startswith("minecraft:chest") for b in design["blocks"]) == 8
for chest in design["chests"]:
    xyz = chest["u"], chest["y"], chest["v"]
    assert markers[xyz] == chest["loot_table"].removeprefix("slavicmyths:")
    assert chest["X_city"] == 75 + xyz[0] and chest["Z_city"] == 75 + xyz[2]
    loot_path = root / "src/main/resources/data/slavicmyths/loot_table" / (markers[xyz] + ".json")
    loot = json.loads(loot_path.read_text("utf-8"))
    assert loot["type"] == "minecraft:chest"
    contains_coins = "slavicmyths:ancient_coin" in json.dumps(loot)
    assert contains_coins == (chest["name"] == "merchant_cache")
    if chest["name"] == "merchant_cache":
        rare = loot["pools"][1]
        assert rare["rolls"] == 1 and len(rare["entries"]) == 1
        assert rare["conditions"] == [{"condition": "minecraft:random_chance", "chance": 0.2}]
        assert rare["entries"][0]["functions"][0]["count"] == {"type": "minecraft:uniform", "min": 1, "max": 2}

# Preserve the immutable original source, but compare the manual NBT and
# current native renderer to the schema-9 wall-side chest positions.
renovation = (root / "src/main/java/org/slavicmyths/posad/PosadMarketRenovation.java").read_text("utf-8")
moves = re.findall(r"relocate\((\d+),(\d+),(\d+),(\d+)\)", renovation)
moves = {(int(x), 1, int(z)): (int(nx), 1, int(nz)) for x,z,nx,nz in moves}
assert len(moves) == 8 and len(set(moves.values())) == 8
original_by_pos = {tuple(row[:3]): compact["palette"][row[3]] for row in compact["blocks"]}
for old, new in moves.items():
    assert old in markers and original_by_pos[old].startswith("minecraft:chest")
    assert new not in original_by_pos and new[0] in (3,23)
    assert original_by_pos.get((new[0],0,new[2])) == "minecraft:crimson_planks"

furnishings = re.findall(r'furnish\((\d+),(\d+),"([^"]+)"\)', renovation)
lanterns = re.findall(r'(?<!void )lamp\((\d+),(\d+)\)', renovation)
assert len(furnishings) == 12 and len(lanterns) == 6
occupied = set(moves.values())
for sx, sz, name in furnishings:
    x,z = int(sx),int(sz)
    assert name.startswith(("pine_","linden_"))
    assert (x,1,z) not in occupied and (x,1,z) not in original_by_pos
    assert (x,2,z) not in original_by_pos
    assert original_by_pos.get((x,0,z)) == "minecraft:crimson_planks"
    occupied.add((x,1,z))
for sx,sz in lanterns:
    x,z=int(sx),int(sz)
    assert (x,1,z) not in occupied and (x,1,z) not in original_by_pos
    assert original_by_pos.get((x,0,z)) == "minecraft:crimson_planks"
    occupied.add((x,1,z))

for x0 in (2,16):
    for x in range(x0,x0+9):
        for z in list(range(2,9))+list(range(11,17))+list(range(19,24)):
            assert any((x,y,z) in original_by_pos for y in range(5,12)), ("roof",x,z)

nbt_path = root / "src/main/resources/data/slavicmyths/structure/posad/menovye_ryady_2_2_6.nbt"
data = memoryview(gzip.decompress(nbt_path.read_bytes()))
offset = 0

def take(format_):
    global offset
    width = struct.calcsize(format_)
    result = struct.unpack_from(format_, data, offset)[0]
    offset += width
    return result

def text():
    global offset
    size = take(">H")
    result = bytes(data[offset:offset + size]).decode("utf-8")
    offset += size
    return result

def payload(kind):
    if kind == 1:
        return take(">b")
    if kind == 2:
        return take(">h")
    if kind == 3:
        return take(">i")
    if kind == 4:
        return take(">q")
    if kind == 8:
        return text()
    if kind == 9:
        inner = take(">B")
        count = take(">i")
        return [payload(inner) for _ in range(count)]
    if kind == 10:
        output = {}
        while True:
            element = take(">B")
            if element == 0:
                return output
            name = text()
            assert name not in output
            output[name] = payload(element)
    raise AssertionError(f"Unsupported NBT tag {kind}")

assert take(">B") == 10 and text() == ""
structure = payload(10)
assert offset == len(data)
assert structure["size"] == [27, 12, 25]
assert len(structure["palette"]) == len(compact["palette"]) + 2
assert len(structure["blocks"]) == 2203 and structure["entities"] == []
assert len({tuple(x["pos"]) for x in structure["blocks"]}) == 2203
for idx, (original, block) in enumerate(zip(design["blocks"], structure["blocks"])):
    old = (original["u"], original["y"], original["v"])
    new = moves.get(old, old)
    assert block["pos"] == list(new), idx
    expected = original["id"]
    if old in moves:
        facing = "east" if new[0] == 3 else "west"
        expected = f"minecraft:chest[facing={facing},type=single,waterlogged=false]"
    state = structure["palette"][block["state"]]
    assert (state["Name"], state.get("Properties", {})) == canonical(expected), idx
    if old in markers:
        assert block["nbt"]["LootTable"] == "slavicmyths:" + markers[old], idx
        assert block["nbt"]["id"] == "minecraft:chest", idx
    else:
        assert "nbt" not in block, idx
assert {tuple(row["pos"]) for row in structure["blocks"] if "nbt" in row} == set(moves.values())
assert all(tuple(row["pos"]) not in moves for row in structure["blocks"])
print("PASS: 2203 original blocks + native/manual NBT moves of 8 chests, 12 furniture/6 lamps, 20% max2 coins, roof coverage")
