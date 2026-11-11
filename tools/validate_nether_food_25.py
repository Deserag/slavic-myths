#!/usr/bin/env python3
"""Strict SOURCE + production-JAR gate for issue #480. Not a Minecraft runtime test."""
import hashlib
import json
from pathlib import Path
import struct
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ASSET = ROOT / "src/main/resources/assets/slavicmyths"
DATA = ROOT / "src/main/resources/data/slavicmyths"
CROPS = ("heatroot", "ash_mushroom", "ash_horseradish", "coal_garlic")
GROWING = CROPS + ("sernica",)
DISHES = ("infernal_shchi", "druzhina_roast", "onion_pie", "garlic_flatbread", "infernal_mushroom_soup", "roasted_roots", "coal_plum_pie", "heat_berry_jam", "mushroom_onion_pie")
DRINKS = ("spicy_sbiten", "heat_berry_mors", "spiced_honey_drink", "spicy_beer", "heat_berry_beer", "coal_plum_beer")
PNG_SIGNATURE = bytes((137, 80, 78, 71, 13, 10, 26, 10))
evidence = set()


def require(path):
    if not path.is_file():
        raise AssertionError(f"Missing file: {path.relative_to(ROOT)}")
    if "src/main/resources/" in path.as_posix():
        evidence.add(path.relative_to(ROOT / "src/main/resources").as_posix())
    return path


def json_file(path):
    return json.loads(require(path).read_text(encoding="utf-8"))


def png(path):
    buf = require(path).read_bytes()
    if buf[:8] != PNG_SIGNATURE or buf[12:16] != b"IHDR":
        raise AssertionError(f"Invalid PNG: {path}")
    width, height, depth, kind = struct.unpack(">IIBB", buf[16:26])
    if (width, height, depth, kind) != (16, 16, 8, 6):
        raise AssertionError(f"Expected 16x16 RGBA8 PNG: {path}, got {(width, height, depth, kind)}")


def model(id, expected):
    obj = json_file(ASSET / "models/item" / (id + ".json"))
    if obj.get("parent") != "minecraft:item/generated" or obj.get("textures", {}).get("layer0") != expected:
        raise AssertionError(f"Wrong item model for {id}: {obj}")
    if not expected.startswith("slavicmyths:item/"):
        raise AssertionError(f"Unexpected texture namespace for {id}")
    png(ASSET / "textures/item" / (expected.split("/", 1)[1] + ".png"))


def crop(id):
    state = json_file(ASSET / "blockstates" / (id + "_crop.json"))["variants"]
    for age in range(4):
        want = f"slavicmyths:block/{id}_crop_stage{age}"
        if state.get(f"age={age}", {}).get("model") != want:
            raise AssertionError(f"Stage model mismatch {id}/{age}")
        obj = json_file(ASSET / "models/block" / f"{id}_crop_stage{age}.json")
        if obj.get("parent") != "minecraft:block/cross" or obj.get("textures", {}).get("cross") != f"slavicmyths:block/{id}_stage{age}":
            raise AssertionError(f"Stage texture ref mismatch {id}/{age}")
        png(ASSET / "textures/block" / f"{id}_stage{age}.png")
    table = json_file(DATA / "loot_table/blocks" / f"{id}_crop.json")
    if table.get("type") != "minecraft:block":
        raise AssertionError(f"Missing block loot type for {id}")
    for directory in ("configured_feature", "placed_feature"):
        json_file(DATA / "worldgen" / directory / f"patch_{id}_crop.json")
    model(id, f"slavicmyths:item/{id}")


def check_source():
    blocks = require(ROOT / "src/main/java/org/slavicmyths/registry/ModBlocks.java").read_text()
    items = require(ROOT / "src/main/java/org/slavicmyths/registry/ModItems.java").read_text()
    for id in CROPS:
        crop(id)
        if blocks.count(f'BLOCKS.register("{id}_crop"') != 1 or items.count(f'ITEMS.register("{id}"') != 1:
            raise AssertionError(f"Missing or duplicated item/block registration: {id}")
    crop("sernica")
    for id in ("heat_berries", "heat_berry_cutting", "coal_plum", "coal_plum_sapling", "sernica", "sernica_seeds", "heat_berry_mash", "coal_plum_mash"):
        model(id, f"slavicmyths:item/{id}")
    for id in ("heat_berry_bush", "coal_plum_sapling", "coal_plum_leaves"):
        json_file(ASSET / "blockstates" / (id + ".json"))
        json_file(DATA / "loot_table/blocks" / (id + ".json"))
    for age in range(4):
        png(ASSET / "textures/block" / f"berries_stage{age}.png")
        png(ASSET / "textures/block" / f"plum_stage{age}.png")
    for id in ("heat_berry_bush", "coal_plum_tree"):
        json_file(DATA / "worldgen/configured_feature" / (("patch_" + id if id == "heat_berry_bush" else id) + ".json"))
        json_file(DATA / "worldgen/placed_feature" / (("patch_" + id if id == "heat_berry_bush" else id) + ".json"))
    for id in DISHES:
        recipe = json_file(DATA / "recipe/kitchen" / (id + ".json"))
        if recipe.get("type") != "slavicmyths:kitchen" or len(recipe.get("ingredients", [])) > 4 or recipe.get("mode") not in ("ROLLING", "POT"):
            raise AssertionError(f"Invalid kitchen recipe {id}")
        model(id, f"slavicmyths:item/{id}")
    for id in DRINKS:
        alcohol = id.endswith("_beer")
        recipe = json_file(DATA / "recipe/vat" / (id + ("_wort" if alcohol else "") + ".json"))
        if recipe.get("type") != "slavicmyths:vat" or recipe.get("mode") != ("WORT" if alcohol else "INFUSION") or len(recipe.get("ingredients", [])) > 4:
            raise AssertionError(f"Invalid vat recipe {id}")
        model(id + "_mug", f"slavicmyths:item/{id}")
        if alcohol:
            model(id + "_wort", f"slavicmyths:item/{id}")
    for locale in ("ru_ru", "en_us"):
        lang = json_file(ASSET / "lang" / (locale + ".json"))
        for id in CROPS:
            if not lang.get("item.slavicmyths." + id) or not lang.get("block.slavicmyths." + id + "_crop"):
                raise AssertionError(f"Missing localization {id}/{locale}")
    json_file(DATA / "neoforge/biome_modifier/nether_food_crops.json")
    json_file(DATA / "neoforge/biome_modifier/coal_plum_tree.json")
    return len(evidence)


def check_jar():
    candidates = []
    for path in (ROOT / "build/libs").glob("*.jar"):
        with zipfile.ZipFile(path) as archive:
            if "assets/slavicmyths/models/item/heatroot.json" in archive.namelist():
                candidates.append(path)
    if len(candidates) != 1:
        raise AssertionError(f"Expected one production mod JAR containing crops, found {candidates}")
    path = candidates[0]
    with zipfile.ZipFile(path) as jar:
        names = set(jar.namelist())
        for resource in sorted(evidence):
            if resource not in names:
                raise AssertionError(f"Missing production JAR resource: {resource}")
            disk = (ROOT / "src/main/resources" / resource).read_bytes()
            if jar.read(resource) != disk:
                raise AssertionError(f"Production JAR resource differs from source: {resource}")
        for cl in ("NetherFoodCrop", "NetherBerryBush", "CoalPlumTree", "CoalPlumSapling", "CoalPlumLeaves", "CoalPlumTreeFeature"):
            entry = f"org/slavicmyths/farming/{cl}.class"
            if entry not in names:
                raise AssertionError(f"Missing compiled class in JAR: {entry}")
    sha = hashlib.sha256(path.read_bytes()).hexdigest()
    print(f"JAR SOURCE-ASSET PASS: {path.relative_to(ROOT)} sha256={sha}; files={len(evidence)}")


if __name__ == "__main__":
    count = check_source()
    print(f"RESOURCE SOURCE-CHECK PASS: {count} assets, recipes and models")
    if "--jar" in sys.argv:
        check_jar()
    print("NOT_RUN: GameTest, vanilla bone-meal interaction, survival, player relog, multiplayer, GUI/hand/ground owner visual acceptance")
