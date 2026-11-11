#!/usr/bin/env python3
"""Strict SOURCE + production-JAR gate for issue #480. Not a Minecraft runtime test."""
import hashlib
import json
import re
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
# No vanilla texture aliases are permitted for newly approved food ingredients.
# Missing art is a release-blocking error, not an excuse to pass CI with a placeholder.
OWN_INGREDIENT_MODELS = (
    "garlic", "garlic_clove", "horseradish", "horseradish_cutting",
    "black_radish", "black_radish_seeds", "mustard_seeds", "onion", "onion_set",
    "ground_mustard", "grated_horseradish", "ground_hot_pepper",
    "ground_black_pepper", "black_pepper", "roasted_hot_pepper",
)
# Products with a separate planting unit must not duplicate its function.
NONPLANTABLE_FOODS = ("garlic", "horseradish", "black_radish", "onion",
                      "heat_berries", "coal_plum", "sernica")
PLANTING_MATERIALS = {
    "garlic_clove": "GARLIC_CROP", "horseradish_cutting": "HORSERADISH_CROP",
    "black_radish_seeds": "BLACK_RADISH_CROP", "mustard_seeds": "MUSTARD_CROP",
    "onion_set": "ONION_CROP", "heat_berry_cutting": "HEAT_BERRY_BUSH",
    "coal_plum_sapling": "COAL_PLUM_SAPLING", "sernica_seeds": "SERNICA_CROP",
    # The original five Nether crops intentionally use the edible unit as seed.
    "heatroot": "HEATROOT_CROP", "ash_mushroom": "ASH_MUSHROOM_CROP",
    "ash_horseradish": "ASH_HORSERADISH_CROP",
    "coal_garlic": "COAL_GARLIC_CROP", "crimson_pepper": "CRIMSON_PEPPER_CROP",
}
NETHER_SUBSTRATES = {
    "heatroot": "SOUL_SAND", "ash_mushroom": "SOUL_SOIL",
    "ash_horseradish": "SOUL_SAND", "coal_garlic": "SOUL_SOIL",
    "sernica": "SOUL_SOIL",
}
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


# Issue #468: all named new boss/sister materials, without changing their approved designs.
SISTER_INGOTS = ("tryaseya_ingot", "gneteya_ingot", "ledeya_ingot", "agneya_ingot", "neveya_ingot", "lomeya_ingot")
SISTER_SHARDS = ("tryaseya_shard", "gneteya_shard", "ledeya_shard", "agneya_shard", "neveya_shard", "lomeya_shard")
SISTER_OTHER = ("nether_soul_iv", "nether_soul_v", "hunter_bow_core", "sisters_ritual_core")
BOSS_MATERIALS = (
    "morova_thread", "faded_rite_seal", "ancestor_bone_charm", "nameless_ashes",
    "broken_watch_horn", "darkened_waymark", "mourning_circlet", "charred_signal_ribbon",
    "legendary_soul", "scroll_of_waning", "scroll_of_last_call",
    "rune_morova_lingering", "rune_trizna_ember_ward",
    "amulet_of_the_faded", "last_watch_horn", "morova_silence_pendant",
    "fallen_druzhina_horn", "mytar_chain", "bauk_wool", "bauk_hide",
    "nine_mor_heart", "nine_mor_scale", "nine_mor_fang", "pechat_devyati_morov",
)


def audit_material(id):
    obj = json_file(ASSET / "models/item" / (id + ".json"))
    textures = obj.get("textures", {})
    if not textures or not (obj.get("parent") or obj.get("elements")):
        raise AssertionError(f"Unbound ingredient model: {id}")
    if id in SISTER_INGOTS and (obj.get("parent") != "minecraft:item/generated" or textures.get("layer0") != "slavicmyths:item/" + id):
        raise AssertionError(f"Wrong sister ingot texture reference: {id}")
    for slot, target in textures.items():
        if not isinstance(target, str):
            raise AssertionError(f"Invalid texture target {id}/{slot}")
        if target.startswith("slavicmyths:item/"):
            path = ASSET / "textures/item" / (target[len("slavicmyths:item/"):] + ".png")
            raw = require(path).read_bytes()
            if raw[:8] != PNG_SIGNATURE or raw[12:16] != b"IHDR":
                raise AssertionError(f"Invalid material PNG: {id} -> {path}")
        elif id in ("lomeya_shard", "neveya_shard") and target.startswith("minecraft:"):
            raise AssertionError(f"Vanilla placeholder in approved new shard {id}/{slot}: {target}")
        elif not target.startswith("minecraft:"):
            raise AssertionError(f"Unresolved material texture namespace: {id}/{slot}: {target}")
    for element in obj.get("elements", []):
        for prop in ("from", "to"):
            coords = element[prop]
            if len(coords) != 3 or not all(isinstance(v, (int, float)) and 0 <= v <= 16 for v in coords):
                raise AssertionError(f"Invalid 3D bounds for {id}/{prop}")
        for face in element.get("faces", {}).values():
            ref = face.get("texture", "")
            if ref.startswith("#") and ref[1:] not in textures:
                raise AssertionError(f"Missing texture slot {ref} in {id}")
            uv = face.get("uv")
            if uv is not None and (len(uv) != 4 or not all(isinstance(v, (int, float)) and 0 <= v <= 16 for v in uv)):
                raise AssertionError(f"Out-of-range item UV {id}/{uv}")


def check_creative_catalog():
    catalog = {}
    for line in require(ASSET / "creative_catalog.tsv").read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) != 3 or fields[0] in catalog:
            raise AssertionError(f"Invalid or duplicate creative catalog row: {line}")
        catalog[fields[0]] = (fields[1], fields[2])
    expected = {id: ("food", "ingredients") for id in CROPS}
    expected.update({id + "_mug": ("food", "drinks") for id in DRINKS})
    expected.update({id + "_wort": ("hidden", "brewing_batches") for id in DRINKS if id.endswith("_beer")})
    for id, category in expected.items():
        if catalog.get(id) != category:
            raise AssertionError(f"Creative inventory category mismatch for {id}: {catalog.get(id)} != {category}")
    food = [line.split("\t")[0] for line in require(ASSET / "creative_catalog.tsv").read_text(encoding="utf-8").splitlines()
            if "\tfood\t" in line]
    groups = [
        ("garlic", "garlic_clove", "horseradish", "horseradish_cutting", "black_radish",
         "black_radish_seeds", "mustard_seeds", "onion", "onion_set"),
        ("heatroot", "ash_mushroom", "crimson_pepper", "ash_horseradish", "coal_garlic",
         "sernica", "sernica_seeds", "heat_berries", "heat_berry_cutting", "coal_plum", "coal_plum_sapling"),
        ("roasted_hot_pepper", "ground_hot_pepper", "ground_mustard", "grated_horseradish",
         "black_pepper", "ground_black_pepper", "heat_berry_mash", "coal_plum_mash"),
    ]
    for group in groups:
        indices = [food.index(item) for item in group]
        if indices != list(range(indices[0], indices[0] + len(indices))):
            raise AssertionError(f"Creative crops/ingredients must be adjacent and ordered: {group}")
    if not max(food.index(item) for group in groups for item in group) < food.index("pea_soup"):
        raise AssertionError("Crops/ingredients must precede cooked meals")
    if not food.index("pea_soup") < food.index("spicy_sbiten_mug"):
        raise AssertionError("Ready meals must precede drinks")




# Contract for each renewable crop: actual planted block, maturity-gated produce,
# seed return, capped Fortune for produce only, and each rendered growth state.
GROWTH_CONTRACT = {
    "garlic_crop": ("garlic_clove", "garlic", "garlic"),
    "horseradish_crop": ("horseradish_cutting", "horseradish", "horseradish"),
    "black_radish_crop": ("black_radish_seeds", "black_radish", "radish"),
    "mustard_crop": ("mustard_seeds", "mustard_seeds", "mustard"),
    "onion_crop": ("onion_set", "onion", "onion"),
    "heatroot_crop": ("heatroot", "heatroot", "heatroot"),
    "ash_mushroom_crop": ("ash_mushroom", "ash_mushroom", "ash_mushroom"),
    "crimson_pepper_crop": ("crimson_pepper", "crimson_pepper", "crimson_pepper"),
    "ash_horseradish_crop": ("ash_horseradish", "ash_horseradish", "ash_horseradish"),
    "coal_garlic_crop": ("coal_garlic", "coal_garlic", "coal_garlic"),
    "sernica_crop": ("sernica_seeds", "sernica", "sernica"),
    "heat_berry_bush": ("heat_berry_cutting", "heat_berries", "berries"),
    "coal_plum_leaves": (None, "coal_plum", "plum"),
}


def age_three(conditions, block):
    return any(c.get("condition") == "minecraft:block_state_property"
               and c.get("block") == "slavicmyths:" + block
               and c.get("properties", {}).get("age") == "3"
               for c in (conditions or []))


def check_growth_and_loot():
    for block, (seed, produce, texture) in GROWTH_CONTRACT.items():
        variants = json_file(ASSET / "blockstates" / (block + ".json"))["variants"]
        fruit_leaves = block == "coal_plum_leaves"
        expected_keys = ({f"age={age},fruiting={value}" for age in range(4) for value in ("true", "false")}
                         if fruit_leaves else {f"age={age}" for age in range(4)})
        if set(variants) != expected_keys:
            raise AssertionError(f"Incomplete blockstate stages: {block}: {set(variants) ^ expected_keys}")
        for age in range(4):
            name = f"{block}_stage{age}"
            if variants["age=" + str(age) + (",fruiting=true" if fruit_leaves else "")].get("model") != "slavicmyths:block/" + name:
                raise AssertionError(f"Wrong model stage for {block}/{age}")
            obj = json_file(ASSET / "models/block" / (name + ".json"))
            if obj.get("parent") != "minecraft:block/cross" or obj.get("textures", {}).get("cross") != f"slavicmyths:block/{texture}_stage{age}":
                raise AssertionError(f"Wrong texture for {block}/{age}")
            png(ASSET / "textures/block" / f"{texture}_stage{age}.png")
            if fruit_leaves and variants[f"age={age},fruiting=false"].get("model") != "slavicmyths:block/coal_plum_leaves_stage0":
                raise AssertionError("Non-fruiting coal plum leaves must not show berries")
        table = json_file(DATA / "loot_table/blocks" / (block + ".json"))
        if table.get("type") != "minecraft:block":
            raise AssertionError(f"Wrong loot type for {block}")
        entries = [entry for pool in table.get("pools", []) for entry in pool.get("entries", [])]
        if any(entry.get("type") != "minecraft:item" or entry.get("name") not in
               {"slavicmyths:" + produce, "slavicmyths:" + seed if seed else "slavicmyths:" + produce}
               for entry in entries):
            raise AssertionError(f"Unexpected crop drop for {block}")
        ripe = [entry for entry in entries if entry.get("name") == "slavicmyths:" + produce
                and (age_three(entry.get("conditions"), block) or
                     any(age_three(fn.get("conditions"), block) for fn in entry.get("functions", [])))]
        if len(ripe) != 1:
            raise AssertionError(f"Need one mature produce entry for {block}, got {len(ripe)}")
        mature = ripe[0]
        counts = [fn for fn in mature.get("functions", []) if fn.get("function") == "minecraft:set_count"]
        if len(counts) != 1:
            raise AssertionError(f"Need bounded mature yield for {block}")
        count = counts[0]["count"]
        if isinstance(count, dict) and (count.get("type") != "minecraft:uniform" or
                                        count.get("min", 0) < 1 or count.get("max", 999) > 4):
            raise AssertionError(f"Unbounded yield for {block}: {count}")
        if block != "mustard_crop":
            fortune = [fn for fn in mature.get("functions", []) if fn.get("function") == "slavicmyths:capped_crop_fortune"]
            if len(fortune) != 1 or (not age_three(mature.get("conditions"), block)
                                    and not age_three(fortune[0].get("conditions"), block)):
                raise AssertionError(f"Missing maturity-guarded Fortune for {block}")
        if seed and seed != produce:
            seedlings = [entry for entry in entries if entry.get("name") == "slavicmyths:" + seed]
            if len(seedlings) != 1 or seedlings[0].get("conditions"):
                raise AssertionError(f"No guaranteed immature/replant drop for {block}")
            if any(fn.get("function") == "slavicmyths:capped_crop_fortune"
                   for fn in seedlings[0].get("functions", [])):
                raise AssertionError(f"Fortune must not multiply planting items: {block}")
        elif seed == produce and block != "mustard_crop":
            if len(entries) != 1 or not age_three(counts[0].get("conditions"), block):
                raise AssertionError(f"Edible planting unit must return once when immature: {block}")

    blocks = require(ROOT / "src/main/java/org/slavicmyths/registry/ModBlocks.java").read_text()
    items = require(ROOT / "src/main/java/org/slavicmyths/registry/ModItems.java").read_text()
    for name, source, registrar in (("block", blocks, "BLOCKS"), ("item", items, "ITEMS")):
        ids = re.findall(r'\b' + registrar + r'\.register\("([a-z0-9_]+)"', source)
        if len(ids) != len(set(ids)):
            raise AssertionError(f"Duplicate {name} registry IDs")
    for cls, soil in (("CrimsonPepperCrop", "CRIMSON_NYLIUM"),
                      ("NetherBerryBush", "CRIMSON_NYLIUM"),
                      ("CoalPlumSapling", "CRIMSON_NYLIUM")):
        code = require(ROOT / "src/main/java/org/slavicmyths/farming" / (cls + ".java")).read_text()
        if f"Blocks.{soil}" not in code or "Blocks.FARMLAND" in code or "canSurvive" not in code:
            raise AssertionError(f"Incorrect Nether placement and survival logic: {cls}")
    nether = require(ROOT / "src/main/java/org/slavicmyths/farming/NetherFoodCrop.java").read_text()
    if "state.is(substrate)" not in nether or "is(substrate)" not in nether or "Blocks.FARMLAND" in nether:
        raise AssertionError("Nether crops must honor the configured substrate, not farmland")


def check_source():
    blocks = require(ROOT / "src/main/java/org/slavicmyths/registry/ModBlocks.java").read_text()
    items = require(ROOT / "src/main/java/org/slavicmyths/registry/ModItems.java").read_text()
    # Approved 2.5 food reference: check every PNG and its actual item model.
    reference_items = (
        "garlic", "garlic_clove", "horseradish", "horseradish_cutting", "black_radish",
        "black_radish_seeds", "mustard_seeds", "onion", "onion_set", "heatroot",
        "ash_mushroom", "crimson_pepper", "ash_horseradish", "coal_garlic",
        "heat_berries", "heat_berry_cutting", "coal_plum", "coal_plum_sapling",
        "sernica", "sernica_seeds", "infernal_shchi", "druzhina_roast",
        "onion_pie", "garlic_flatbread", "infernal_mushroom_soup", "roasted_roots",
        "coal_plum_pie", "heat_berry_jam", "mushroom_onion_pie", "spicy_sbiten",
        "heat_berry_mors", "spiced_honey_drink", "spicy_beer", "heat_berry_beer",
        "coal_plum_beer", "spicy_beer_wort", "heat_berry_beer_wort",
        "coal_plum_beer_wort", "heat_berry_mash", "coal_plum_mash",
        "black_pepper", "ground_black_pepper", "ground_hot_pepper",
        "ground_mustard", "grated_horseradish", "roasted_hot_pepper",
        "lomeya_shard", "neveya_shard",
    )
    drinks = {"spicy_sbiten", "heat_berry_mors", "spiced_honey_drink", "spicy_beer", "heat_berry_beer", "coal_plum_beer"}
    for item_id in reference_items:
        model_id = item_id + "_mug" if item_id in drinks else item_id
        model(model_id, f"slavicmyths:item/{item_id}")
    stage_names = ("garlic", "horseradish", "radish", "mustard", "onion", "hot_pepper",
                   "heatroot", "ash_mushroom", "ash_horseradish", "coal_garlic",
                   "sernica", "berries", "plum", "crimson_pepper")
    for plant_id in stage_names:
        digests = []
        for age in range(4):
            p = ASSET / "textures/block" / f"{plant_id}_stage{age}.png"
            png(p)
            digests.append(hashlib.sha256(p.read_bytes()).digest())
        if len(set(digests)) != 4:
            raise AssertionError(f"Indistinguishable / duplicated crop frames: {plant_id}")
    distinct = (("onion", "onion_set"), ("black_pepper", "ground_black_pepper"),
                ("garlic", "garlic_clove"), ("horseradish", "horseradish_cutting"),
                ("lomeya_shard", "neveya_shard"))
    for left, right in distinct:
        l = require(ASSET / "textures/item" / (left + ".png")).read_bytes()
        r = require(ASSET / "textures/item" / (right + ".png")).read_bytes()
        if l == r:
            raise AssertionError(f"Different ingredients share a placeholder sprite: {left}/{right}")
    for id in CROPS:
        crop(id)
        if blocks.count(f'BLOCKS.register("{id}_crop"') != 1 or items.count(f'ITEMS.register("{id}"') != 1:
            raise AssertionError(f"Missing or duplicated item/block registration: {id}")
    crop("sernica")
    check_growth_and_loot()
    for id in OWN_INGREDIENT_MODELS:
        model(id, f"slavicmyths:item/{id}")
    for id in NONPLANTABLE_FOODS:
        line = next((line for line in items.splitlines() if f'ITEMS.register("{id}"' in line), "")
        if "new Item(properties()" not in line or "ItemNameBlockItem" in line:
            raise AssertionError(f"Raw food must not be directly plantable: {id}")
    for id, block in PLANTING_MATERIALS.items():
        line = next((line for line in items.splitlines() if f'ITEMS.register("{id}"' in line), "")
        if f"ItemNameBlockItem(ModBlocks.{block}.get()," not in line:
            raise AssertionError(f"Missing planting link: {id} -> {block}")
    for id, soil in NETHER_SUBSTRATES.items():
        line = next((line for line in blocks.splitlines() if f'BLOCKS.register("{id}_crop"' in line), "")
        if f"Blocks.{soil}" not in line:
            raise AssertionError(f"Wrong Nether growing soil: {id} should use {soil}")
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
            model(id + "_wort", f"slavicmyths:item/{id}_wort")
    check_creative_catalog()
    for locale in ("ru_ru", "en_us"):
        lang = json_file(ASSET / "lang" / (locale + ".json"))
        for id in CROPS:
            if not lang.get("item.slavicmyths." + id) or not lang.get("block.slavicmyths." + id + "_crop"):
                raise AssertionError(f"Missing localization {id}/{locale}")
    json_file(DATA / "neoforge/biome_modifier/nether_food_crops.json")
    json_file(DATA / "neoforge/biome_modifier/coal_plum_tree.json")
    json_file(DATA / "neoforge/biome_modifier/crimson_pepper_crop.json")
    for category in ("configured_feature", "placed_feature"):
        json_file(DATA / "worldgen" / category / "patch_crimson_pepper_crop.json")
    for cooking in ("smelting", "smoking", "campfire_cooking"):
        obj = json_file(DATA / "recipe" / ("roasted_hot_pepper_" + cooking + ".json"))
        if obj.get("result", {}).get("id") != "slavicmyths:roasted_hot_pepper":
            raise AssertionError("Missing survivor-accessible pepper roasting")
    for id in SISTER_INGOTS + SISTER_SHARDS + SISTER_OTHER + BOSS_MATERIALS:
        audit_material(id)
    print('ART_PENDING_OWNER: original food/plant pixel resources present; Minecraft GUI screenshots and owner reference review NOT_RUN.')
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
