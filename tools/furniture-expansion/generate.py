#!/usr/bin/env python3
"""Rebuild #369 new-wood furniture JSON, without touching legacy pine/linden or structures."""
import json
from pathlib import Path
R = Path(__file__).resolve().parents[2]
A = R / "src/main/resources/assets/slavicmyths"
D = R / "src/main/resources/data/slavicmyths"
WOODS = {"apple": "stripped_apple_log", "rowan": "rowan_stripped_log", "willow": "willow_stripped_log", "darkened": "darkened_stripped_log", "mor_oak": "mor_oak_log", "navya": "stripped_navya_log", "blackwood": "blackwood_log"}
KINDS = ("chair", "stool", "bench", "table", "bedside_cabinet", "shelf", "wardrobe", "weapon_rack")
MODELS = ("chair", "stool", "bench", "table", "table_pair_north", "table_pair_south", "table_pair_east", "table_pair_west", "bedside_cabinet", "shelf", "wardrobe_lower", "wardrobe_upper", "weapon_rack")
PATTERNS = {"chair": ["P  ", "PPP", "S S"], "stool": ["PP", "SS"], "bench": ["PPP", "S S"], "table": ["PPP", " S ", "S S"], "bedside_cabinet": ["PP", "PP", "SS"], "shelf": ["PPP", "S  "], "wardrobe": ["PPP", "PLP", "PPP"], "weapon_rack": ["S S", "PPP", "S S"]}
FACES = ("north", "east", "south", "west")
def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
def generate():
    for wood, beam in WOODS.items():
        for kind in KINDS:
            item = wood + "_" + kind
            variants = {}
            for i, facing in enumerate(FACES):
                if kind == "table":
                    for connection in ("none", "north", "south", "east", "west"):
                        model = "slavicmyths:block/" + item + ("" if connection == "none" else "_pair_" + connection)
                        variants["facing=" + facing + ",connection=" + connection] = {"model": model, **({"y": i * 90} if connection == "none" else {})}
                elif kind == "wardrobe":
                    for half in ("lower", "upper"):
                        variants["facing=" + facing + ",half=" + half] = {"model": "slavicmyths:block/" + item + "_" + half, "y": i * 90}
                else:
                    variants["facing=" + facing] = {"model": "slavicmyths:block/" + item, "y": i * 90}
            write(A / "blockstates" / (item + ".json"), {"variants": variants})
            key = {"P": {"item": "slavicmyths:" + wood + "_planks"}}
            if kind == "wardrobe":
                key["L"] = {"item": "slavicmyths:" + wood + "_log"}
            else:
                key["S"] = {"item": "minecraft:stick"}
            write(D / "recipe" / (item + ".json"), {"type": "minecraft:crafting_shaped", "pattern": PATTERNS[kind], "key": key, "result": {"id": "slavicmyths:" + item}})
            entry = {"type": "minecraft:item", "name": "slavicmyths:" + item}
            if kind == "wardrobe":
                entry["conditions"] = [{"condition": "minecraft:block_state_property", "block": "slavicmyths:" + item, "properties": {"half": "lower"}}]
            write(D / "loot_table/blocks" / (item + ".json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [entry], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
            write(A / "models/item" / (item + ".json"), {"parent": "slavicmyths:block/" + item + ("_lower" if kind == "wardrobe" else "")})
        for model in MODELS:
            tex = {"wood": "slavicmyths:block/" + wood + "_planks", "beam": "slavicmyths:block/" + beam, "iron": "slavicmyths:block/furniture_metal", "cloth": "slavicmyths:block/furniture_cloth", "rope": "slavicmyths:block/furniture_rope", "straw": "minecraft:block/hay_block_side", "log": "slavicmyths:block/" + wood + "_log_top", "particle": "slavicmyths:block/" + wood + "_planks"}
            write(A / "models/block" / (wood + "_" + model + ".json"), {"parent": "slavicmyths:block/pine_" + model, "textures": tex})
if __name__ == "__main__":
    generate()
