"""Validate the desert flat-world preset and its natural structure registry."""
import json
from pathlib import Path

root = Path("src/main/resources")
def load(path):
    return json.loads((root / path).read_text(encoding="utf-8"))

id_ = "slavicmyths:slavic_desert"
preset = load("data/slavicmyths/worldgen/flat_level_generator_preset/slavic_desert.json")
settings = preset["settings"]
assert settings["biome"] == "minecraft:desert"
assert not settings["features"] and not settings["lakes"]
assert "structure_overrides" not in settings, "All biome-compatible structure sets must remain available"
assert sum(layer["height"] for layer in settings["layers"]) == 160
assert settings["layers"][0] == {"block": "minecraft:bedrock", "height": 1}
assert settings["layers"][-1] == {"block": "minecraft:sand", "height": 4}
assert id_ in load("data/minecraft/tags/worldgen/flat_level_generator_preset/visible.json")["values"]
for language in ("ru_ru", "en_us"):
    assert load(f"assets/minecraft/lang/{language}.json")["flat_world_preset.slavicmyths.slavic_desert"]
for name in ("sun_temple", "desert_trial_dungeon"):
    structure = load(f"data/slavicmyths/worldgen/structure/{name}.json")
    structure_set = load(f"data/slavicmyths/worldgen/structure_set/{name}.json")
    assert structure["biomes"] == "minecraft:desert"
    assert any(e["structure"] == f"slavicmyths:{name}" for e in structure_set["structures"])
print("Desert superflat preset resource checks passed")
