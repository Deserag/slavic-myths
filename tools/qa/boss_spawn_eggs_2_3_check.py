#!/usr/bin/env python3
"""Source-level regression guard for Creative-only boss spawn eggs."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[2]
base = root / "src/main"
assets = base / "resources/assets/slavicmyths"
raven = (base / "java/org/slavicmyths/morovoy/MorovoyRavenContent.java").read_text()
zmey = (base / "java/org/slavicmyths/morovoy/MorovoyFireZmeyContent.java").read_text()
boss = (base / "java/org/slavicmyths/morovoy/MorovoyFireZmey.java").read_text()
entrypoint = (base / "java/org/slavicmyths/SlavicMyths.java").read_text()
catalog = (assets / "creative_catalog.tsv").read_text()
for name, source, primary, secondary in (
    ("morovoy_raven", raven, "0x24272A", "0x62B538"),
    ("morovoy_fire_zmey", zmey, "0x571926", "0x7635B8"),
):
    item_id = name + "_spawn_egg"
    assert source.count('ITEMS.register("' + item_id + '"') == 1, item_id
    assert "DeferredSpawnEggItem(BOSS" in source, item_id
    assert primary in source and secondary in source, item_id
    assert catalog.count(item_id + "\tmobs\tboss") == 1, item_id
    model = json.loads((assets / "models/item" / (item_id + ".json")).read_text())
    assert model["parent"] == "minecraft:item/template_spawn_egg", item_id
    for locale in ("en_us", "ru_ru"):
        names = json.loads((assets / "lang" / (locale + ".json")).read_text())
        assert names["item.slavicmyths." + item_id], (locale, item_id)
assert "MobSpawnType.SPAWN_EGG" in boss and "nextState(WAKING)" in boss
assert entrypoint.count("MorovoyRavenContent.init(bus)") == 1
assert entrypoint.count("MorovoyFireZmeyContent.init(bus)") == 1
print("PASS: two distinct spawn eggs, tinted inventory models, creative placement, translations, egg-only wake")
