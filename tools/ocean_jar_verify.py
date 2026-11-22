#!/usr/bin/env python3
"""Validate actual built JAR assets, not just files in the source directory."""
import json
import pathlib
import zipfile

weapons = ("storm_anchor", "abyss_staff", "storm_harpoon")
accessories = ("tide_ring", "abyss_necklace", "storm_bracelet",
               "tailwind_feather", "tsar_pike_belt", "zhelva_amulet")
artifacts = ("marine_compass", "depths_lantern", "breathing_bowl")
materials = ("pharaohka_tide_pearl", "deep_sea_serpent_scale", "ocean_zmey_storm_heart",
             "stratim_bird_feather", "tsar_pike_fang", "zhelva_shell_plate")
tokens = ("pharaohka_victory_token", "deep_sea_serpent_victory_token",
          "ocean_zmey_victory_token", "stratim_bird_victory_token",
          "tsar_pike_victory_token", "zhelva_victory_token")
new_items = weapons + accessories + artifacts
all_items = new_items + materials + tokens
jar_paths = sorted(pathlib.Path("build/libs").glob("*.jar"),
                   key=lambda path: path.stat().st_size, reverse=True)
assert jar_paths, "No mod JAR built"
selected = None
for path in jar_paths:
    with zipfile.ZipFile(path) as candidate:
        if "assets/slavicmyths/lang/en_us.json" in candidate.namelist():
            selected = path
            break
assert selected is not None, "No production JAR with Slavic Myths resources"
with zipfile.ZipFile(selected) as jar:
    names = set(jar.namelist())
    base = "assets/slavicmyths/"
    translations = {name: json.loads(jar.read(base + f"lang/{name}.json"))
                    for name in ("ru_ru", "en_us")}
    catalog = jar.read(base + "creative_catalog.tsv").decode("utf-8")
    for item in all_items:
        model_file = base + f"models/item/{item}.json"
        assert model_file in names, f"Missing item model in JAR: {item}"
        model = json.loads(jar.read(model_file))
        layer = model["textures"]["layer0"]
        assert layer.startswith("slavicmyths:item/"), f"Not a bundled icon: {item}: {layer}"
        namespace, texture = layer.split(":", 1)
        image = f"assets/{namespace}/textures/{texture}.png"
        assert image in names, f"Missing texture in JAR: {image}"
        png = jar.read(image)
        assert png[:8] == bytes.fromhex("89504e470d0a1a0a"), f"Invalid PNG: {item}"
        for name, data in translations.items():
            assert data.get(f"item.slavicmyths.{item}"), f"Missing {name} locale for {item}"
        assert any(row.startswith(item + "\t") for row in catalog.splitlines()), f"Missing creative catalog: {item}"
    for item in new_items:
        recipe_file = f"data/slavicmyths/recipe/{item}.json"
        assert recipe_file in names, f"Missing packed recipe: {item}"
        recipe = json.loads(jar.read(recipe_file))
        assert recipe["result"]["id"] == f"slavicmyths:{item}", f"Wrong packed recipe: {item}"
        for name, data in translations.items():
            assert data.get(f"item.slavicmyths.{item}.desc"), f"Missing {name} tooltip for {item}"
    for model in ("storm_anchor_cast", "storm_harpoon_throwing"):
        assert base + f"models/item/{model}.json" in names, model
    for cls in ("MarineWeapons", "MarineAccessories", "MarineArtifacts", "MarineRewards"):
        assert f"org/slavicmyths/marine/{cls}.class" in names, f"Missing Java class: {cls}"
print(f"PACKAGED_OCEAN_ITEMS_PASS={len(all_items)} recipes={len(new_items)} jar={selected}")
