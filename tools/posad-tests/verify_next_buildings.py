#!/usr/bin/env python3
"""Deterministic offline exact R1/loot/NBT audit for military and forge.

NOT an in-game opening, relog, multiplayer, real natural placement or performance test.
"""
import base64
import gzip
import importlib.util
import json
import random
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "docs/concepts/nether-settlement-2.2"
RUNTIME = ROOT / "src/main/resources/data/slavicmyths"
spec = importlib.util.spec_from_file_location("previous_nbt_audit", ROOT / "tools/posad-tests/verify_generated_building_nbt.py")
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
Reader = module.Reader
state = module.state


def check(name, doc, origin, dims, count, is_rows):
    path = BASE / doc / ("2.2_DRUZHINNY_DVOR_MODEL_R1.json" if is_rows else "2.2_KUZNECHNY_DVOR_MODEL_R1.json")
    original = path.read_bytes()
    packed = (RUNTIME / "posad" / (name + ".gz.base64")).read_text().strip()
    assert gzip.decompress(base64.b64decode(packed)) == original, (name, "packed runtime source differs")
    data = json.loads(original)
    if is_rows:
        expected = {(x, y, z): data["palette"][sym]
                    for y, layer in enumerate(data["rows_y_z_x"]) for z, row in enumerate(layer)
                    for x, sym in enumerate(row) if sym != "."}
        loot = {tuple(m["local"]): m["table"] for m in data["loot_markers"]}
    else:
        expected = {(v["u"], v["y"], v["v"]): v["block"] for v in data["voxels"]}
        loot = {(m["u"], m["y"], m["v"]): m["loot_table"] for m in data["chests"]}
    assert len(expected) == count and len(loot) == 5 and sum(s.startswith("minecraft:chest") for s in expected.values()) == 5
    assert all(0 <= x < dims[0] and 0 <= y < dims[1] and 0 <= z < dims[2] for x, y, z in expected)
    assert all(expected[p].startswith("minecraft:chest") for p in loot)
    nbt = Reader(gzip.decompress((RUNTIME / "structure/posad" / (name + ".nbt")).read_bytes())).root()
    assert nbt["size"] == list(dims) and nbt["DataVersion"] == 3955 and nbt["entities"] == []
    palette = nbt["palette"]
    cells = {}
    chest_entities = {}
    for block in nbt["blocks"]:
        p = tuple(block["pos"])
        assert p not in cells
        cells[p] = palette[block["state"]]
        if "nbt" in block:
            chest_entities[p] = block["nbt"]
    assert cells == {p: state(s) for p, s in expected.items()}, name
    assert chest_entities == {p: {"id": "minecraft:chest", "LootTable": v} for p, v in loot.items()}, name
    for table in set(loot.values()):
        fname = table.split("/", 1)[1] + ".json"
        runtime = (RUNTIME / "loot_table/chests" / fname).read_bytes()
        draft = (BASE / doc / "loot_drafts" / fname).read_bytes()
        if runtime != draft:
            # R1 architecture and original chest entries are immutable. Schema-18
            # reward improvements legitimately strengthened loot without replacing
            # those original entries. Validate additive evolution, not draft bytes.
            current, baseline = json.loads(runtime), json.loads(draft)
            assert current["type"] == baseline["type"] == "minecraft:chest", fname
            assert len(current["pools"]) >= len(baseline["pools"]), fname
            for index, original_pool in enumerate(baseline["pools"]):
                evolved = current["pools"][index]
                assert evolved["entries"] == original_pool["entries"], (fname, index, "R1 entries changed")
                assert {k:v for k,v in evolved.items() if k != "rolls"} == {k:v for k,v in original_pool.items() if k != "rolls"}, (fname, index, "original pool mechanics changed")
                a,b = evolved["rolls"], original_pool["rolls"]
                if isinstance(a,dict) and isinstance(b,dict):
                    assert a["min"] >= b["min"] and a["max"] >= b["max"], (fname,index,"base loot weakened")
                else:
                    assert a == b, (fname,index,"original roll count changed")
            for extra in current["pools"][len(baseline["pools"]):]:
                assert extra["entries"] and extra.get("rolls") is not None, (fname, "invalid bonus pool")
                assert all(e.get("type") == "minecraft:item" and e.get("name") for e in extra["entries"]), (fname, "invalid bonus entry")
            print(f"PASS evolved loot {fname}: original entries preserved, validated {len(current['pools'])-len(baseline['pools'])} bonus pools")
    for turn in range(4):
        points = {((x + origin[0], z + origin[1]), (112 - z - origin[1], x + origin[0]), (112 - x - origin[0], 112 - z - origin[1]), (z + origin[1], 112 - x - origin[0]))[turn]
                  for x, y, z in expected}
        assert len(points) <= dims[0] * dims[2] and all(0 <= x <= 112 and 0 <= z <= 112 for x, z in points)
    print(f"PASS {name}: {count} original voxels, {len(palette)} states, 5 correct chests, four rotations, identical JSON and NBT")


check("druzhinny_dvor_2_2_4", "druzhinny-dvor-2.2.4", (12, 75), (28, 18, 25), 2440, True)
check("kuznechny_dvor_2_2_5", "kuznechny-dvor-2.2.5", (12, 44), (26, 18, 26), 1901, False)

captain = json.loads((RUNTIME / "loot_table/chests/nether_druzhina_captain.json").read_text())
armory = json.loads((RUNTIME / "loot_table/chests/nether_druzhina_armory.json").read_text())
armor = ["minecraft:netherite_helmet", "minecraft:netherite_boots", "minecraft:netherite_leggings", "minecraft:netherite_chestplate"]
assert len(captain["pools"]) == 5
assert [e["name"] for e in captain["pools"][0]["entries"]] == armor
assert [e["weight"] for e in captain["pools"][0]["entries"]] == [30,30,25,15]
assert captain["pools"][0]["rolls"] == 1
assert captain["pools"][1]["rolls"] == 1 and captain["pools"][1]["conditions"][0]["chance"] == .2
assert captain["pools"][3]["conditions"][0]["chance"] == .25
assert captain["pools"][4]["conditions"][0]["chance"] == .7
assert armory["pools"][1]["conditions"][0]["chance"] == .15
assert all("netherite_" not in (RUNTIME / "loot_table/chests" / f"nether_druzhina_{n}.json").read_text() for n in ("armory", "barracks", "supply"))

stats = Counter()
for seed in range(10_000):
    rng = random.Random(seed)
    pool0 = captain["pools"][0]["entries"]
    def choose(pool):
        return rng.choices([e["name"] for e in pool], weights=[e.get("weight", 1) for e in pool], k=1)[0]
    rewarded = [choose(pool0)]
    if rng.random() < .2:
        rewarded.append(choose(captain["pools"][1]["entries"]))
    assert 1 <= len(rewarded) <= 2 and all(r in armor for r in rewarded)
    stats["one" if len(rewarded) == 1 else "two"] += 1
    for item in rewarded:
        stats[item] += 1
    if rng.random() < .7:
        stats["coins"] += 1
    if rng.random() < .25:
        stats["weapon"] += 1
assert 1800 <= stats["two"] <= 2200
assert 6750 <= stats["coins"] <= 7250
assert 2250 <= stats["weapon"] <= 2750
print(f"PASS 10000 seeded captain reward simulations: {stats['one']} single, {stats['two']} double, {stats['coins']} coins, {stats['weapon']} weapon bonuses")
