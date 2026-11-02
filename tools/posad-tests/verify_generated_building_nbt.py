#!/usr/bin/env python3
"""Verify committed 1.21.1 structure NBT against exact approved R1 sources.

Offline structural evidence only: not a Minecraft placement, survival, or loot-opening test.
No third-party libraries required.
"""
import gzip
import hashlib
import json
import runpy
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONCEPT = ROOT / "docs/concepts/nether-settlement-2.2"
STRUCTURES = ROOT / "src/main/resources/data/slavicmyths/structure/posad"


class Reader:
    def __init__(self, content):
        self.content = content
        self.offset = 0

    def take(self, length):
        data = self.content[self.offset:self.offset + length]
        if len(data) != length:
            raise ValueError("Truncated structure NBT")
        self.offset += length
        return data

    def number(self):
        return struct.unpack(">i", self.take(4))[0]

    def utf8(self):
        length = struct.unpack(">H", self.take(2))[0]
        return self.take(length).decode("utf-8")

    def value(self, kind):
        if kind == 3:
            return self.number()
        if kind == 8:
            return self.utf8()
        if kind == 9:
            item_kind = self.take(1)[0]
            count = self.number()
            assert 0 <= count < 100_000, "Invalid list length"
            return [self.value(item_kind) for _ in range(count)]
        if kind == 10:
            result = {}
            while True:
                item_kind = self.take(1)[0]
                if item_kind == 0:
                    break
                name = self.utf8()
                assert name not in result, "Duplicate NBT compound key"
                result[name] = self.value(item_kind)
            return result
        raise ValueError(f"Unexpected NBT tag type: {kind}")

    def root(self):
        assert self.take(1) == b"\x0a", "Expected compound root"
        assert self.utf8() == "", "Unexpected named root"
        result = self.value(10)
        assert self.offset == len(self.content), "Trailing bytes"
        return result


def state(value):
    name, sep, props = value.partition("[")
    return {"Name": name, **({"Properties": dict(part.split("=", 1) for part in props[:-1].split(","))} if sep else {})}


def verify(name, dims, expected, loot):
    path = STRUCTURES / (name + ".nbt")
    raw = path.read_bytes()
    data = Reader(gzip.decompress(raw)).root()
    assert data["DataVersion"] == 3955, (name, "wrong Minecraft DataVersion")
    assert data["size"] == list(dims), (name, "wrong size")
    assert data["entities"] == [], (name, "unexpected entities")
    palette = data["palette"]
    assert len(palette) == len({json.dumps(p, sort_keys=True) for p in palette}), (name, "duplicated palette states")
    positions = {}
    chests = {}
    for entry in data["blocks"]:
        p = tuple(entry["pos"])
        assert len(p) == 3 and all(0 <= p[i] < dims[i] for i in range(3)), (name, "outside template")
        assert p not in positions, (name, "duplicate block")
        assert 0 <= entry["state"] < len(palette), (name, "invalid palette index")
        positions[p] = palette[entry["state"]]
        if "nbt" in entry:
            assert p in loot, (name, "unexpected NBT block entity", p)
            assert entry["nbt"] == {"id": "minecraft:chest", "LootTable": loot[p]}, (name, "invalid chest data", p)
            assert positions[p]["Name"] == "minecraft:chest"
            chests[p] = entry["nbt"]["LootTable"]
    assert positions == {p: state(s) for p, s in expected.items()}, (name, "source voxel/blockstate mismatch")
    assert chests == loot, (name, "missing/altered loot chests")
    print(f"PASS {name}: {len(positions)} exact blocks, {len(palette)} states, "
          f"{len(chests)} chest markers, sha256={hashlib.sha256(raw).hexdigest()}")


veche = runpy.run_path(str(CONCEPT / "2.2_VECHE_HALL_VOXELS_R1.py"))["decode"]()
hall = {(v["u"] + 1, v["y"], v["v"] + 1): v["block"] for v in veche}
assert len(hall) == 3598
verify("veche_hall_2_2_2", (29, 18, 23), hall, {})

house = json.loads((CONCEPT / "communal-house-2.2.3/2.2_COMMUNAL_HOUSE_MODEL_R1.json").read_text())
grid = house["grid"]
expected = {(x, y, z): house["palette"][symbol]
            for y, layer in enumerate(house["rows_y_z_x"])
            for z, row in enumerate(layer)
            for x, symbol in enumerate(row) if symbol != "."}
assert len(expected) == 1992
loot = {(m["pos"][0] - grid["x_min"], m["pos"][1] - grid["y_min"], m["pos"][2] - grid["z_min"]): m["loot_table"]
        for m in house["loot_markers"]}
assert len(loot) == 4
verify("communal_house_2_2_3", (23, 17, 15), expected, loot)
