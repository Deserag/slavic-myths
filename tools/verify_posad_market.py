#!/usr/bin/env python3
"""Verify market parity, native structure NBT and eight original loot contracts."""
import gzip
import json
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
assert len(structure["palette"]) == len(compact["palette"])
assert len(structure["blocks"]) == 2203 and structure["entities"] == []
assert len({tuple(x["pos"]) for x in structure["blocks"]}) == 2203
for idx, (original, block) in enumerate(zip(design["blocks"], structure["blocks"])):
    assert block["pos"] == [original["u"], original["y"], original["v"]], idx
    state = structure["palette"][block["state"]]
    assert (state["Name"], state.get("Properties", {})) == canonical(original["id"]), idx
    xyz = tuple(block["pos"])
    if xyz in markers:
        assert block["nbt"]["LootTable"] == "slavicmyths:" + markers[xyz]
        assert block["nbt"]["id"] == "minecraft:chest"
    else:
        assert "nbt" not in block
print("PASS: 2203 identical voxels, 675 floor, 8 real NBT chests/tables, 20% chance, 0 extra coins")
