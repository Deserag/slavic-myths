"""Reproduce Posad cot resources from the existing authored two-part linen bed geometry."""
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
A = ROOT / "assets/slavicmyths"
D = ROOT / "data/slavicmyths"
def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
def copy(source, target):
    data = json.loads(source.read_text(encoding="utf-8").replace("linen_bed", "posad_cot"))
    write(target, data)
for part in ("head", "foot", "inventory"):
    source = A / "models/block" / f"linen_bed_{part}.json"
    data = json.loads(source.read_text(encoding="utf-8"))
    data["textures"]["wood"] = "minecraft:block/polished_blackstone"
    data["textures"]["particle"] = "minecraft:block/polished_blackstone"
    write(A / "models/block" / f"posad_cot_{part}.json", data)
copy(A / "blockstates/linen_bed.json", A / "blockstates/posad_cot.json")
copy(A / "models/item/linen_bed.json", A / "models/item/posad_cot.json")
copy(D / "loot_table/blocks/linen_bed.json", D / "loot_table/blocks/posad_cot.json")
write(D / "recipe/posad_cot.json", {"type":"minecraft:crafting_shaped", "category":"misc", "pattern":["WWW","BBB"], "key":{"W":{"item":"minecraft:red_wool"},"B":{"item":"minecraft:polished_blackstone"}}, "result":{"id":"slavicmyths:posad_cot","count":1}})
for tag in ("beds", "mineable/pickaxe"):
    path = ROOT / "data/minecraft/tags/block" / (tag + ".json")
    data = json.loads(path.read_text(encoding="utf-8"))
    if "slavicmyths:posad_cot" not in data["values"]: data["values"].append("slavicmyths:posad_cot")
    write(path, data)
