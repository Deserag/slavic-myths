#!/usr/bin/env python3
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
p=root/"src/main"
code=(p/"java/org/slavicmyths/posad/PosadShell.java").read_text(encoding="utf-8")
assert "tx == 42 ? EAST : WEST" in code and "groundDoor && y <= 2" in code
assert "SCHEMA_VERSION = 21" in (p/"java/org/slavicmyths/posad/PosadGeometry.java").read_text(encoding="utf-8")
f=(p/"java/org/slavicmyths/posad/PosadFinishes.java").read_text(encoding="utf-8")
assert "linden_table" in f and "pine_weapon_rack" in f and "nether_veche_treasury" in f
assert "z==33 && y<=3 && base==null" in f and "DoorHingeSide" in f
ru=json.loads((p/"resources/assets/slavicmyths/lang/ru_ru.json").read_text(encoding="utf-8"))
assert sum(k.endswith("_spawn_egg") for k in ru)>=90
for n in ("posad_barrel_market","posad_barrel_supplies","nether_veche_treasury","nether_veche_relics"):
    x=json.loads((p/"resources/data/slavicmyths/loot_table/chests"/(n+".json")).read_text(encoding="utf-8"))
    assert x["type"]=="minecraft:chest" and x["pools"]
m=json.loads((p/"resources/data/slavicmyths/loot_table/chests/nether_market_merchant_cache.json").read_text(encoding="utf-8"))
assert m["pools"][1]["conditions"][0]["chance"]==0.2
assert m["pools"][1]["entries"][0]["functions"][0]["count"]["max"]==2
print("PASS: schema6 static QA, translated eggs, reward tables")
