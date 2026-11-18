#!/usr/bin/env python3
"""#369 offline checks. No Minecraft startup and no build claim."""
from pathlib import Path
import json
from generate import WOODS, KINDS, MODELS, PATTERNS
R = Path(__file__).resolve().parents[2]
A = R / "src/main/resources/assets/slavicmyths"
D = R / "src/main/resources/data/slavicmyths"
def read(path):
    assert path.is_file(), "Missing file: " + str(path)
    return json.loads(path.read_text(encoding="utf-8"))
def check():
    rows = [line.split("\t") for line in (A / "creative_catalog.tsv").read_text(encoding="utf-8").splitlines() if line and not line.startswith("#")]
    furniture = [row for row in rows if row[1] == "furniture"]
    assert len(furniture) == 72 and len({row[0] for row in furniture}) == 72
    assert [r[0] for r in furniture] == [w + "_" + k for k in KINDS for w in ("pine", "linden", *WOODS)]
    assert not any(r[1] == "decor" and r[0].endswith(tuple("_" + k for k in KINDS)) and r[0].startswith(("pine_", "linden_")) for r in rows)
    # Owner decision 31.10.2026: no charred_blackwood planks or furniture variants.
    excluded = {"charred_blackwood_" + kind for kind in KINDS}
    assert "charred_blackwood" not in WOODS
    assert not any(row[0] in excluded for row in rows)
    assert '"charred_blackwood"' not in (R / "src/main/java/org/slavicmyths/furniture/Furniture.java").read_text(encoding="utf-8")
    for item in excluded:
        for path in (A / "blockstates" / (item + ".json"), A / "models/item" / (item + ".json"), D / "recipe" / (item + ".json"), D / "loot_table/blocks" / (item + ".json")):
            assert not path.exists(), "Excluded charred wood furniture: " + str(path)
    for type_ in ("block", "item"):
        tags = read(D / "tags" / type_ / "furniture.json")["values"]
        assert len(tags) == len(set(tags))
        assert not any(value.removeprefix("slavicmyths:") in excluded for value in tags)
        for w in WOODS:
            for k in KINDS:
                assert "slavicmyths:" + w + "_" + k in tags
    for lang in ("ru_ru", "en_us"):
        names = read(A / "lang" / (lang + ".json"))
        assert names["itemGroup.slavicmyths.furniture"]
        for w in WOODS:
            for k in KINDS:
                assert names["block.slavicmyths." + w + "_" + k]
    assert "furniture" in (R / "src/main/java/org/slavicmyths/registry/CreativeCatalog.java").read_text()
    for suffix, file in (("_weapon_rack", "src/main/java/org/slavicmyths/furniture/Furniture.java"), ("_table", "src/main/java/org/slavicmyths/kitchen/KitchenII.java")):
        text = (R / file).read_text()
        assert "endsWith(\"" + suffix + "\")" in text and "toArray(" in text, file
    for w, beam in WOODS.items():
        for texture in (w + "_planks", w + "_log_top", beam):
            assert (A / "textures/block" / (texture + ".png")).is_file()
        for model in MODELS:
            definition = read(A / "models/block" / (w + "_" + model + ".json"))
            assert definition["parent"] == "slavicmyths:block/pine_" + model
            assert definition["textures"]["wood"] == "slavicmyths:block/" + w + "_planks"
            assert definition["textures"]["beam"] == "slavicmyths:block/" + beam
        for kind in KINDS:
            item = w + "_" + kind
            blockstate = read(A / "blockstates" / (item + ".json"))
            assert len(blockstate["variants"]) == (20 if kind == "table" else 8 if kind == "wardrobe" else 4)
            for val in blockstate["variants"].values():
                model = val["model"].split(":", 1)[1].removeprefix("block/")
                assert (A / "models/block" / (model + ".json")).is_file()
            model = read(A / "models/item" / (item + ".json"))
            assert model["parent"] == "slavicmyths:block/" + item + ("_lower" if kind == "wardrobe" else "")
            actual = read(D / "recipe" / (item + ".json"))
            baseline = read(D / "recipe" / ("pine_" + kind + ".json"))
            assert actual["type"] == baseline["type"] == "minecraft:crafting_shaped"
            assert actual["pattern"] == baseline["pattern"] == PATTERNS[kind]
            assert actual["key"]["P"] == {"item": "slavicmyths:" + w + "_planks"}
            assert actual["key"].get("L") == ({"item": "slavicmyths:" + w + "_log"} if kind == "wardrobe" else None)
            assert actual["key"].get("S") == baseline["key"].get("S")
            assert actual["result"]["id"] == "slavicmyths:" + item
            loot = read(D / "loot_table/blocks" / (item + ".json"))
            entry = loot["pools"][0]["entries"][0]
            assert entry["name"] == "slavicmyths:" + item
            if kind == "wardrobe":
                assert entry["conditions"][0]["properties"]["half"] == "lower"
    print("FURNITURE_369_OFFLINE_PASS variants=56 total_furniture=72 java_build=NOT_RUN game_starts=0")
if __name__ == "__main__":
    check()
