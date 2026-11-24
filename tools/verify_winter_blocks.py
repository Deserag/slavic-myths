"""Check the packaged winter block resources without a running client."""
import json
import struct
from pathlib import Path
from zipfile import ZipFile

stones = ["frozen_cobblestone", "frozen_stone", "smooth_frozen_stone",
          "frozen_stone_bricks", "frozen_granite", "frozen_diorite",
          "frozen_andesite", "frozen_deepslate", "frozen_cobbled_deepslate",
          "frozen_deepslate_bricks"]
decor = ["chiseled_frozen_stone", "cracked_frozen_stone_bricks",
         "frozen_stone_pillar", "frozen_paving"]
stone_ids = [n + suffix for n in stones for suffix in ("", "_stairs", "_slab", "_wall")] + decor
wood_suffixes = ["log", "wood", "stripped_log", "stripped_wood", "planks",
                 "leaves", "sapling", "stairs", "slab", "fence", "fence_gate",
                 "door", "trapdoor", "pressure_plate", "button", "sign", "hanging_sign"]
wood_ids = [n + "_" + s for n in ("ice_pine", "frost_willow") for s in wood_suffixes]
ids = stone_ids + wood_ids + ["hanging_frost_willow_leaves"]
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
            assert recipe["result"]["count"] == 4, n
            assert recipe["key"]["I"]["item"] == "minecraft:ice", n
        for n in ("ice_pine", "frost_willow"):
            for tag in ("block", "item"):
                read_json(data + "tags/" + tag + "/" + n + "_logs.json")
        print(f"{jar}: {len(ids)} winter block item models and loot tables, stone recipes and PNG dimensions checked")
