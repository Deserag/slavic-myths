#!/usr/bin/env python3
"""Build exact approved R1 structures #350/#351 and compact runtime assets.

Requires only Python 3 stdlib. Never changes city source drawings or loot weights.
Outputs are DATA ONLY; no new standalone StructureSet or migration.
"""
import base64
import gzip
import json
import shutil
from collections import Counter
from pathlib import Path
from generate_posad_buildings import ROOT, DOC, OUT, assemble

RESOURCE = ROOT / "src/main/resources/data/slavicmyths/posad"
LOOT = ROOT / "src/main/resources/data/slavicmyths/loot_table/chests"


def publish(name, source, size, blocks, markers, docs, expected_count):
    assert len(blocks) == expected_count, (name, len(blocks), expected_count)
    assert all(len(p) == 3 and all(0 <= p[i] < size[i] for i in range(3)) for p in blocks)
    assert len({p for p in blocks}) == expected_count
    assert len(markers) == 5 and len({p for p in markers}) == 5
    assert sum(s.startswith("minecraft:chest") for s in blocks.values()) == 5
    assert all(blocks[p].startswith("minecraft:chest") for p in markers), name
    for table in set(markers.values()):
        assert table.startswith("slavicmyths:chests/"), table
        filename = table.split(":", 1)[1].split("/", 1)[1] + ".json"
        original = docs / "loot_drafts" / filename
        loot = json.loads(original.read_text(encoding="utf8"))
        assert loot["type"] == "minecraft:chest", filename
        LOOT.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(original, LOOT / filename)
    assemble(name, size, blocks, {p: table for p, table in markers.items()})
    RESOURCE.mkdir(parents=True, exist_ok=True)
    packed = gzip.compress(source.read_bytes(), compresslevel=9, mtime=0)
    (RESOURCE / (name.replace("_2_2_", "_r1_") + ".gz.base64")).write_text(base64.b64encode(packed).decode("ascii") + "\n", encoding="ascii")
    print(f"PASS {name}: blocks={len(blocks)}, palette={len(set(blocks.values()))}, chests={len(markers)}, runtime bytes={len(packed)}")


def druzhina():
    docs = DOC / "druzhinny-dvor-2.2.4"
    source = docs / "2.2_DRUZHINNY_DVOR_MODEL_R1.json"
    data = json.loads(source.read_text(encoding="utf8"))
    size = tuple(data["local_extent"]["size"])
    assert size == (28, 18, 25)
    assert data["city_bbox"] == {"city_x_min": 12, "city_x_max": 39, "city_z_min": 75, "city_z_max": 99}
    rows = data["rows_y_z_x"]
    assert len(rows) == size[1]
    blocks = {}
    for y, layer in enumerate(rows):
        assert len(layer) == size[2]
        for z, row in enumerate(layer):
            assert len(row) == size[0]
            for x, symbol in enumerate(row):
                if symbol == ".":
                    continue
                assert symbol in data["palette"], symbol
                blocks[(x, y, z)] = data["palette"][symbol]
    assert Counter(sym for layer in rows for row in layer for sym in row if sym != ".") == data["by_symbol"]
    markers = {tuple(m["local"]): m["table"] for m in data["loot_markers"]}
    assert all((12 + p[0], 75 + p[2]) == tuple(m["city"]) [::2] for p, m in zip(markers, data["loot_markers"]))
    publish("druzhinny_dvor_2_2_4", source, size, blocks, markers, docs, 2440)


def kuznya():
    docs = DOC / "kuznechny-dvor-2.2.5"
    source = docs / "2.2_KUZNECHNY_DVOR_MODEL_R1.json"
    data = json.loads(source.read_text(encoding="utf8"))
    assert data["site"]["city_x"] == [12, 37] and data["site"]["city_z"] == [44, 69]
    size = (26, 18, 26)
    blocks = {}
    for v in data["voxels"]:
        pos = (v["u"], v["y"], v["v"])
        assert pos not in blocks, pos
        blocks[pos] = v["block"]
    assert Counter(s.split("[", 1)[0] for s in blocks.values()) == data["material_counts"]
    markers = {(m["u"], m["y"], m["v"]): m["loot_table"] for m in data["chests"]}
    assert all((12 + m["u"], 44 + m["v"]) == (m["x_city"], m["z_city"]) for m in data["chests"])
    publish("kuznechny_dvor_2_2_5", source, size, blocks, markers, docs, 1901)


if __name__ == "__main__":
    druzhina()
    kuznya()
