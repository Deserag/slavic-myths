#!/usr/bin/env python3
"""Static blueprint + bindings check. NOT a substitute for NeoForge runtime or client QA."""
import importlib.util
import json
from collections import Counter
from pathlib import Path

root = Path(__file__).resolve().parents[2]
concept = root / "docs/concepts/nether-settlement-2.2"
spec = importlib.util.spec_from_file_location("veche", concept / "2.2_VECHE_HALL_VOXELS_R1.py")
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)
veche = mod.decode()
assert len(veche) == 3598
assert len({(v["city_x"], v["y"], v["city_z"]) for v in veche}) == 3598
assert all(42 <= v["city_x"] <= 70 and 12 <= v["city_z"] <= 34 and 0 <= v["y"] <= 17 for v in veche)
payload = (root / "src/main/resources/data/slavicmyths/posad/veche_hall_r1.gz.base64").read_text().strip()
assert payload == mod.ENCODED

house = json.loads((root / "src/main/resources/data/slavicmyths/posad/communal_house_r1.json").read_text())
original = json.loads((concept / "communal-house-2.2.3/2.2_COMMUNAL_HOUSE_MODEL_R1.json").read_text())
assert house == original
rows = house["rows_y_z_x"]
assert len(rows) == 17 and all(len(layer) == 15 and all(len(row) == 23 for row in layer) for layer in rows)
vox = {(13 + x, y, 15 + z): symbol for y, layer in enumerate(rows) for z, row in enumerate(layer) for x, symbol in enumerate(row) if symbol != "."}
assert len(vox) == 1992
assert Counter(vox.values()) == Counter(house["by_symbol"])
assert all(12 <= x <= 37 and 12 <= z <= 37 for x, _, z in vox)
assert all((x, z) not in {(v["city_x"], v["city_z"]) for v in veche} for x, _, z in vox)
chests = {(x, y, z) for (x, y, z), symbol in vox.items() if symbol == "H"}
assert chests == {(16, 1, 18), (32, 1, 18), (21, 1, 26), (32, 7, 25)}
assert not any("minecraft:bed" in v for v in house["palette"].values())

loot_dir = root / "src/main/resources/data/slavicmyths/loot_table/chests"
source_dir = concept / "communal-house-2.2.3/loot_drafts"
for name in ("pantry", "utility", "personal", "elder"):
    n = "nether_communal_" + name + ".json"
    data = json.loads((loot_dir / n).read_text())
    assert data == json.loads((source_dir / n).read_text())
    assert data["type"] == "minecraft:chest"
    for pool in data["pools"]:
        for entry in pool["entries"]:
            assert entry["type"] == "minecraft:item"
            assert entry["name"] not in ("minecraft:bed", "minecraft:diamond", "minecraft:netherite_ingot")
elder = json.loads((loot_dir / "nether_communal_elder.json").read_text())
assert elder["pools"][1]["conditions"][0]["chance"] == 0.2
assert elder["pools"][1]["entries"][0]["name"] == "slavicmyths:ancient_coin"
assert all("ancient_coin" not in (loot_dir / ("nether_communal_" + n + ".json")).read_text() for n in ("pantry", "utility", "personal"))
for turns in range(4):
    rotate = lambda x, z: ((x, z), (112 - z, x), (112 - x, 112 - z), (z, 112 - x))[turns]
    a, b = {rotate(x, z) for x, _, z in vox}, {rotate(v["city_x"], v["city_z"]) for v in veche}
    assert not a & b
    assert len(a) <= 23 * 15 and len(b) <= 29 * 23
print("PASS: veche 3598, communal 1992, 4 chests, source equality, all city rotations and 4 loot drafts")
