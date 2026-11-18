"""Static consistency check for new natural Serpent Hill rarity (not a Minecraft worldgen test)."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
data = json.loads((ROOT / "src/main/resources/data/slavicmyths/worldgen/structure_set/serpent_hill.json").read_text(encoding="utf-8"))
placement = data["placement"]
assert placement["type"] == "minecraft:random_spread"
assert (placement["spacing"], placement["separation"], placement["salt"]) == (512, 384, 1951013)
assert placement["spread_type"] == "linear"
assert placement["exclusion_zone"] == {"other_set": "minecraft:strongholds", "chunk_count": 12}
assert data["structures"] == [{"structure": "slavicmyths:serpent_hill", "weight": 1}]
source = (ROOT / "src/main/java/org/slavicmyths/serpenthill/SerpentHillStructure.java").read_text(encoding="utf-8")
assert "MIN_ORIGIN_DISTANCE_SQUARED=5000L*5000L" in source
assert "x*x+z*z<MIN_ORIGIN_DISTANCE_SQUARED" in source
assert "Optional.empty()" in source and "new GenerationStub" in source
generator = (ROOT / "tools/hill_assets.py").read_text(encoding="utf-8")
assert "'spacing':512,'separation':384,'salt':1951013" in generator
assert (512 - (512 - 384 - 1)) * 16 >= 6000  # adjacent-cell closest candidate centers
print("SERPENT_HILL_RARITY_STATIC_PASS: 8192-block grid, >=6160 blocks between candidates, beyond origin radius 5000")
