#!/usr/bin/env python3
"""Static marine rewards regression guard. Does not replace Minecraft GameTests."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
resources = root / "src/main/resources/assets/slavicmyths"
code = root / "src/main/java/org/slavicmyths"
materials = {
    "pharaohka_tide_pearl": 3,
    "deep_sea_serpent_scale": 5,
    "ocean_zmey_storm_heart": 3,
    "stratim_bird_feather": 4,
    "tsar_pike_fang": 3,
    "zhelva_shell_plate": 6,
}
tokens = {
    "pharaohka_victory_token": 18,
    "deep_sea_serpent_victory_token": 16,
    "ocean_zmey_victory_token": 22,
    "stratim_bird_victory_token": 20,
    "tsar_pike_victory_token": 12,
    "zhelva_victory_token": 22,
}
ids = set(materials) | set(tokens)
catalog = (resources / "creative_catalog.tsv").read_text(encoding="utf-8")
rows = [line.split("\t") for line in catalog.splitlines() if line and not line.startswith("#")]
for item in ids:
    match = [row for row in rows if row[0] == item]
    assert len(match) == 1, f"Creative catalog missing or duplicate: {item}"
    assert match[0][1] == "resources"
    path = resources / "models/item" / (item + ".json")
    model = json.loads(path.read_text(encoding="utf-8"))
    assert model["parent"] == "minecraft:item/generated"
    sprite = "slavicmyths:item/ocean_rewards/" + item
    assert model["textures"]["layer0"] == sprite, f"Custom icon missing: {item}"
    texture = resources / "textures/item/ocean_rewards" / (item + ".png")
    assert texture.is_file(), f"Custom sprite missing: {item}"
    assert texture.read_bytes().startswith(b"\\x89PNG\\r\\n\\x1a\\n"), f"Invalid PNG: {item}"
for lang in ("ru_ru", "en_us"):
    data = json.loads((resources / "lang" / (lang + ".json")).read_text(encoding="utf-8"))
    for item in ids:
        assert data.get("item.slavicmyths." + item), f"Missing {item} in {lang}"
source = (code / "marine/MarineRewards.java").read_text(encoding="utf-8")
trade = (code / "buyan/BuyanTrades.java").read_text(encoding="utf-8")
loader = (code / "SlavicMyths.java").read_text(encoding="utf-8")
for item in ids:
    assert ('register("' + item + '"') in source, f"Unregistered item: {item}"
for count in materials.values():
    assert (", " + str(count) + ", ") in source
for token, price in tokens.items():
    assert (", " + str(price) + ", l);") in trade, f"Trade price missing: {token}"
assert "pharaohka.leader()" in source
assert "MarineRewards.init();" in loader
assert "princeToken(o, MarineRewards." in trade
food = (
    "infernal_shchi", "druzhina_roast", "onion_pie", "garlic_flatbread",
    "infernal_mushroom_soup", "roasted_roots", "coal_plum_pie",
    "heat_berry_jam", "mushroom_onion_pie"
)
kitchen = (code / "kitchen/KitchenII.java").read_text(encoding="utf-8")
for item in food:
    assert len([row for row in rows if row[0] == item and row[1] == "food"]) == 1, (
        f"Restored dish absent from Food and Drinks: {item}")
    assert ('"' + item + '"') in kitchen, f"Dish unregistered in KitchenII: {item}"
    assert (resources / "models/item" / (item + ".json")).is_file(), (
        f"Dish model missing: {item}")
print("Marine rewards static checks PASS: 12 unique sprites, 2 languages, 6 trades; "
      "9 previous dishes retained in Food and Drinks.")
