#!/usr/bin/env python3
"""Regression check for integrated creative foods, assets and the two sisters."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
JAVA = ROOT / "src/main/java/org/slavicmyths"
errors: list[str] = []

catalog: dict[str, str] = {}
for row in (ASSETS / "creative_catalog.tsv").read_text(encoding="utf-8").splitlines():
    if not row.strip() or row.startswith("#"):
        continue
    parts = row.split("\t")
    if len(parts) != 3:
        errors.append(f"Invalid catalog row: {row}")
        continue
    catalog[parts[0]] = parts[1]

food_items = [name for name, tab in catalog.items() if tab == "food"]
kitchen = (JAVA / "kitchen/KitchenII.java").read_text(encoding="utf-8")
declared_dishes = set(re.findall(r'food(?:Effect)?\("([a-z0-9_]+)"', kitchen))
for name in sorted(declared_dishes):
    if catalog.get(name) != "food":
        errors.append(f"Registered kitchen dish absent from food creative tab: {name}")

for name in food_items:
    model_path = ASSETS / "models/item" / (name + ".json")
    if not model_path.is_file():
        errors.append(f"Missing food item model: {model_path.relative_to(ROOT)}")
        continue
    try:
        model = json.loads(model_path.read_text(encoding="utf-8"))
    except (ValueError, OSError) as exc:
        errors.append(f"Invalid food item model {name}: {exc}")
        continue
    for layer, texture in model.get("textures", {}).items():
        if not isinstance(texture, str) or not texture.startswith("slavicmyths:"):
            continue
        relative = texture.split(":", 1)[1]
        file = ASSETS / "textures" / (relative + ".png")
        if not file.is_file():
            errors.append(f"Missing food texture: {name} {layer} -> {texture}")

renderer = (JAVA / "client/SistersRenderer.java").read_text(encoding="utf-8")
sisters = re.findall(r'"textures/entity/(tryaseya[^"]*|gneteya[^"]*)"', renderer)
if len(sisters) < 2:
    errors.append("Cannot find both sisters' texture paths in SistersRenderer")
for texture in sisters:
    file = ASSETS / "textures/entity" / texture
    if not file.is_file():
        errors.append(f"Missing active sister texture: {file.relative_to(ROOT)}")
for name in ("SistersModel.java", "SistersRenderer.java", "SistersVisuals.java"):
    if not (JAVA / "client" / name).is_file():
        errors.append(f"Missing sister class: {name}")

if errors:
    print("OCEAN_INTEGRATION_AUDIT_FAIL")
    for issue in errors:
        print(" -", issue)
    raise SystemExit(1)
print(f"OCEAN_INTEGRATION_AUDIT_OK food_models={len(food_items)} kitchen_dishes={len(declared_dishes)} sisters_textures={len(sisters)}")
