"""Check the packaged winter block resources without a running client."""
import json
import struct
from pathlib import Path
from zipfile import ZipFile

stones = ["ancient_sandstone", "sandstone_bricks", "cracked_sandstone_bricks", "dark_sandstone"]
decor = ["carved_solar_sandstone","carved_serpent_sandstone","carved_wind_sandstone","compacted_sand",
         "cracked_dry_earth","quicksand","salt_crust","sandstone_rubble","fossilized_bones","solar_seal","desert_serpent_seal","wind_seal"]
stone_ids = [n + suffix for n in stones for suffix in ("", "_stairs", "_slab", "_wall")] + decor
wood_suffixes = ["log","wood","stripped_log","stripped_wood","planks","leaves","sapling","stairs","slab","fence",
                 "fence_gate","door","trapdoor","pressure_plate","button","sign","hanging_sign"]
wood_ids = [n + "_" + s for n in ("date_palm","tamarisk") for s in wood_suffixes]
ids = stone_ids + wood_ids + ["hanging_date_palm_leaves"]
assets = "assets/slavicmyths/"
data = "data/slavicmyths/"
jars = [p for p in Path("build/libs").glob("slavicmyths-*.jar")
        if not p.name.endswith(("-sources.jar", "-javadoc.jar"))]
assert jars, "No production jar"
for jar in jars:
    with ZipFile(jar) as z:
        files = set(z.namelist())
        def read_json(path):
            assert path in files, f"{jar}: Missing {path}"
            return json.loads(z.read(path))
        def walk(value, key):
            if isinstance(value, dict):
                for k,v in value.items():
                    if k == key and isinstance(v, str):
                        yield v
                    yield from walk(v, key)
            elif isinstance(value, list):
                for v in value:
                    yield from walk(v, key)
        checked = set()
        def check_model(name):
            if not name.startswith("slavicmyths:") or name in checked:
                return
            checked.add(name)
            model = read_json(assets + "models/" + name.split(":",1)[1] + ".json")
            for parent in walk(model, "parent"):
                check_model(parent)
            for texture in model.get("textures", {}).values():
                if texture.startswith("slavicmyths:"):
                    p = assets + "textures/" + texture.split(":",1)[1] + ".png"
                    assert p in files, f"Missing texture {p}"
        en = read_json(assets + "lang/en_us.json")
        ru = read_json(assets + "lang/ru_ru.json")
        for n in ids:
            state = read_json(assets + "blockstates/" + n + ".json")
            for model in walk(state, "model"):
                check_model(model)
            check_model("slavicmyths:item/" + n)
            read_json(data + "loot_table/blocks/" + n + ".json")
            assert "block.slavicmyths." + n in en, n
            assert "block.slavicmyths." + n in ru, n
        for n in stones + decor:
            tex = z.read(assets + "textures/block/" + n + ".png")
            assert struct.unpack(">II", tex[16:24]) == (16, 16), n
            recipe = read_json(data + "recipe/" + n + ".json")
            assert recipe["result"]["id"] == "slavicmyths:" + n, n
        for n in ("date_palm", "tamarisk"):
            for tag in ("block", "item"):
                read_json(data + "tags/" + tag + "/" + n + "_logs.json")
        check_model("slavicmyths:item/date")
        assert "item.slavicmyths.date" in en and "item.slavicmyths.date" in ru
        for n in ("solar_seal","desert_serpent_seal","wind_seal"):
            states = read_json(assets + "blockstates/" + n + ".json")["variants"]
            assert set(states) == {"open=false", "open=true"}
            marker = read_json(assets + "models/block/" + n + "_open.json")["elements"][0]
            assert marker["from"] == [6,6,6] and marker["to"] == [10,10,10]
        print(f"{jar}: {len(ids)} desert block item models and loot tables, stone recipes and PNG dimensions checked")
