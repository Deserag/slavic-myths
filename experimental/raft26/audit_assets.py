#!/usr/bin/env python3
"""Static resource QA for the 2.6 shipbuilding system; no Minecraft runtime needed."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[2]
assets = root / "src/main/resources/assets/slavicmyths/models/item"
recipes = root / "src/main/resources/data/slavicmyths/recipe"

rafts = ["raft_2511_test", "raft_3x5", "raft_5x5", "raft_5x7"]
variants = ([f"{id}_sail" for id in rafts] +
            ["raft_5x7_double_sail"] +
            [f"{id}_rails" for id in rafts] +
            [f"{id}_sail_rails" for id in rafts] +
            ["raft_5x7_double_sail_rails"])
parts = ["ship_deck_section", "ship_rail_module", "ship_sail_module",
         "ship_helm_module", "ship_oar_module", "shipyard_key", "shipyard_workbench"]
names = rafts + variants + parts
count = 0


def check(condition, detail):
    global count
    count += 1
    if not condition:
        raise AssertionError(detail)


for name in names:
    file = assets / f"{name}.json"
    check(file.is_file(), f"Missing inventory model: {name}")
    model = json.loads(file.read_text(encoding="utf-8"))
    check(model.get("gui_light") == "front", f"{name}: unstable GUI lighting")
    check(model.get("ambientocclusion") is False, f"{name}: ambient occlusion mismatch")
    display = model.get("display", {})
    for view in ("gui", "ground", "thirdperson_righthand"):
        transform = display.get(view)
        check(isinstance(transform, dict), f"{name}: {view} transform missing")
        scale = transform.get("scale", [])
        check(len(scale) == 3 and all(0 < n <= 1 for n in scale),
              f"{name}: invalid {view} scale")
    for element in model.get("elements", []):
        f, t = element.get("from"), element.get("to")
        check(len(f or []) == 3 and len(t or []) == 3,
              f"{name}: malformed cuboid")
        check(all(0 <= f[i] < t[i] <= 16 for i in range(3)),
              f"{name}: cuboid outside 0..16 or inverted")
    for override in model.get("overrides", []):
        target = override["model"]
        check(target.startswith("slavicmyths:item/"), f"{name}: unknown override target")
        check((assets / f"{target.split('/')[-1]}.json").is_file(),
              f"{name}: missing sail override model {target}")

for name in ["shipyard_workbench", "ship_deck_section", "ship_rail_module",
             "ship_sail_module", "ship_helm_module", "ship_oar_module",
             "shipyard_key"]:
    file = recipes / f"{name}.json"
    check(file.is_file(), f"Missing shipbuilding recipe: {name}")
    recipe = json.loads(file.read_text(encoding="utf-8"))
    pattern, key = recipe["pattern"], recipe["key"]
    check(1 <= len(pattern) <= 3 and 1 <= len(pattern[0]) <= 3,
          f"{name}: oversized crafting pattern")
    check(all(len(row) == len(pattern[0]) for row in pattern),
          f"{name}: ragged crafting pattern")
    check(set("".join(pattern)) - {" "} == set(key),
          f"{name}: recipe key mapping mismatch")
    check(recipe["result"]["id"] == f"slavicmyths:{name}",
          f"{name}: unexpected recipe output")

check((root / "src/main/resources/assets/slavicmyths/blockstates/shipyard_workbench.json").is_file(),
      "Shipyard bench blockstate missing")
print(f"SHIPBUILDING ASSET AUDIT: PASS {count} assertions across {len(names)} item models and 7 recipes.")
