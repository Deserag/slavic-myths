#!/usr/bin/env python3
"""Verify the concrete 2.5 trophy/weapon resource graph and preview actual JSON geometry."""
from __future__ import annotations

import argparse
import itertools
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
DATA = ROOT / "src/main/resources/data/slavicmyths"
MODELS = ASSETS / "models"
TEXTURES = ASSETS / "textures"
BLOCKSTATES = ASSETS / "blockstates"
WEAPONS = ("nine_mor_spear_held", "nine_mor_axe_held", "nine_mor_shield_held")
PIXELS = ("bauk_fur", "bauk_jaw", "bauk_bone", "bauk_eye",
          "nine_mor_graphite", "nine_mor_bone", "nine_mor_violet")
INVALID_VANILLA = {"minecraft:block/polished_blackstone", "minecraft:block/bone_block_side",
                   "minecraft:block/crying_obsidian"}
ERRORS: list[str] = []


def check(condition: bool, description: str) -> None:
    if not condition:
        ERRORS.append(description)


def read(path: Path) -> dict:
    try:
        obj = json.loads(path.read_text(encoding="utf-8-sig"))
        check(isinstance(obj, dict), f"{path}: expected JSON object")
        return obj if isinstance(obj, dict) else {}
    except (OSError, ValueError) as exc:
        ERRORS.append(f"{path}: {exc}")
        return {}


def texture_path(resource: str) -> Path | None:
    if not resource.startswith("slavicmyths:"):
        return None
    return TEXTURES / (resource.split(":", 1)[1] + ".png")


def model(name: str, kind: str) -> dict:
    item = read(MODELS / kind / (name + ".json"))
    textures = item.get("textures", {})
    for key, ref in textures.items():
        if not isinstance(ref, str) or ref.startswith("#"):
            continue
        target = texture_path(ref)
        if target is not None:
            check(target.is_file(), f"{kind}/{name}: missing {key} texture {ref}")
            if target.is_file():
                try:
                    with Image.open(target) as im:
                        im.verify()
                except (OSError, ValueError) as exc:
                    ERRORS.append(f"{kind}/{name}: invalid {ref}: {exc}")
    for index, element in enumerate(item.get("elements", [])):
        bounds = element.get("from"), element.get("to")
        check(all(isinstance(arr, list) and len(arr) == 3 for arr in bounds),
              f"{kind}/{name} element {index}: coordinates must contain three numbers")
        if not all(isinstance(arr, list) and len(arr) == 3 for arr in bounds):
            continue
        check(all(isinstance(a, (int, float)) and isinstance(b, (int, float)) and a <= b
                  for a, b in zip(*bounds)),
              f"{kind}/{name} element {index}: invalid cuboid")
        check(all(-16 <= v <= 32 for arr in bounds for v in arr),
              f"{kind}/{name} element {index}: out-of-range cuboid coordinates (Minecraft -16..32)")
        for face in element.get("faces", {}).values():
            tex = face.get("texture", "")
            if tex.startswith("#"):
                check(tex[1:] in textures, f"{kind}/{name} element {index}: unresolved {tex}")
    return item


def validate() -> dict[str, dict]:
    results = {}
    for name in PIXELS:
        target = TEXTURES / "block" / (name + ".png")
        check(target.is_file(), f"Missing original block texture: {name}")
        if target.is_file():
            try:
                with Image.open(target) as im:
                    check(im.size == (16, 16), f"{name}: expected 16x16 pixels, got {im.size}")
                    im.verify()
            except (OSError, ValueError) as exc:
                ERRORS.append(f"{name}: invalid PNG: {exc}")
    for pose in ("closed", "open"):
        name = "bauk_head_" + pose
        data = model(name, "block")
        check(len(data.get("elements", [])) >= 10, f"{name}: incomplete cuboid geometry")
        check(all(item.get("from", [0, 0, 0])[1] >= 0 and item.get("to", [0, 0, 0])[1] <= 16
                  for item in data.get("elements", [])),
              f"{name}: cuboids leave vertical reserved space")
        check(all(item.get("from", [0, 0, 0])[2] >= -16 and item.get("to", [0, 0, 0])[2] <= 16
                  for item in data.get("elements", [])),
              f"{name}: cuboids leave two-cell horizontal footprint")
        check(not INVALID_VANILLA.intersection(data.get("textures", {}).values()),
              f"{name}: temporary vanilla placeholder survives")
        results[name] = data

    placed = read(BLOCKSTATES / "bauk_head.json").get("variants", {})
    check(len(placed) == 16, f"Bauk trophy has {len(placed)} variants, expected 16")
    for direction, front, open_ in itertools.product(("north", "east", "south", "west"), ("true", "false"), ("true", "false")):
        key = f"facing={direction},front={front},jaw_open={open_}"
        check(key in placed, f"Bauk variant missing: {key}")
    baukkit = model("bauk_head", "item")
    check(baukkit.get("parent") == "slavicmyths:block/bauk_head_closed",
          "Bauk item no longer inherits actual skull geometry")
    for transform in ("gui", "ground", "fixed", "firstperson_righthand",
                      "firstperson_lefthand", "thirdperson_righthand", "thirdperson_lefthand"):
        check(transform in baukkit.get("display", {}),
              f"Bauk head: missing {transform} transform")
    bauk_drop = read(DATA / "loot_table/blocks/bauk_head.json")
    pools = bauk_drop.get("pools", [])
    check(any(condition.get("condition") == "minecraft:block_state_property"
              and condition.get("properties", {}).get("front") == "false"
              for pool in pools for condition in pool.get("conditions", [])),
          "Bauk front cell is not excluded from item drops")

    empty_reserve = model("nine_mor_reserve", "block")
    check(not empty_reserve.get("elements"), "Nine Mor reserve cell must not render a second trophy")
    reserved = read(BLOCKSTATES / "nine_mor_reserve.json").get("variants", {})
    check(len(reserved) == 125, f"Nine Mor upper cells: {len(reserved)}, expected 125")
    for cell, height in itertools.product("abcdefghijklmnopqrstuvwxy", range(1, 6)):
        check(f"cell={cell},y_offset={height}" in reserved,
              f"Nine Mor upper cell missing: {cell}@{height}")
    head = read(DATA / "loot_table/blocks/nine_mor_serpent_head.json")
    check(any(c.get("properties", {}).get("cell") == "a"
              for p in head.get("pools", []) for c in p.get("conditions", [])),
          "Nine Mor head anchor loot restriction missing")

    for name in WEAPONS:
        data = model(name, "item")
        check(len(data.get("elements", [])) >= 20, f"{name}: 3D shape unexpectedly absent")
        check(not INVALID_VANILLA.intersection(data.get("textures", {}).values()),
              f"{name}: temporary vanilla surface remains")
        for part in ("m", "bone", "accent"):
            check(data.get("textures", {}).get(part, "").startswith("slavicmyths:block/"),
                  f"{name}: {part} should use the block atlas")
        for transform in ("gui", "ground", "fixed", "firstperson_righthand",
                          "firstperson_lefthand", "thirdperson_righthand", "thirdperson_lefthand"):
            check(transform in data.get("display", {}), f"{name}: missing {transform} transform")
        results[name] = data
    poloz_item = model("poloz_head", "item")
    check(poloz_item.get("parent") == "minecraft:builtin/entity",
          "Poloz trophy item must activate the registered 3D item renderer")
    with Image.open(TEXTURES / "mob_effect" / "fracture.png") as fracture:
        check(fracture.size == (18, 18), "Fracture status effect icon must be 18 x 18 pixels")
    blocking = model("nine_mor_shield_blocking", "item")
    check(blocking.get("parent") == "slavicmyths:item/nine_mor_shield_held",
          "Nine Mor blocking shield is not linked to its normal geometry")
    for name in ("morovoy_staff", "nine_mor_amulet_held"):
        data = model(name, "item")
        check(len(data.get("elements", [])) >= 10, f"{name}: missing held geometry")
        check(not INVALID_VANILLA.intersection(data.get("textures", {}).values()),
              f"{name}: unrelated vanilla material")
        for transform in ("gui", "ground", "fixed", "firstperson_righthand",
                          "firstperson_lefthand", "thirdperson_righthand", "thirdperson_lefthand"):
            check(transform in data.get("display", {}), f"{name}: missing {transform} view")
        results[name] = data

    poloz = (ROOT / "src/main/java/org/slavicmyths/poloz/GreatPoloz.java").read_text(encoding="utf-8")
    for name in ("windupFx", "ringFx", "fxNearby", "tailSweep", "summon"):
        check(name in poloz, f"Great Poloz missing particle event {name}")
    print(f"Checked {len(PIXELS)} PNG textures, {len(WEAPONS)} weapon shapes, 16 Bauk states and 125 Nine Mor upper states.")
    return results


def previews(models: dict[str, dict], folder: Path) -> None:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from mpl_toolkits.mplot3d.art3d import Poly3DCollection

    colors = {"fur": "#38343d", "jaw": "#4b2a31", "bone": "#c4b69c",
              "eye": "#c52c32", "m": "#4a4a56", "accent": "#703a9a"}
    folder.mkdir(parents=True, exist_ok=True)
    for name, item in models.items():
        fig = plt.figure(figsize=(13, 4))
        elements = item.get("elements", [])
        bounds = [(float(v), float(V)) for axis in range(3)
                  for v, V in [(min(e["from"][axis] for e in elements),
                                max(e["to"][axis] for e in elements))]]
        cx = sum(bounds[0]) / 2
        cy = sum(bounds[1]) / 2
        cz = sum(bounds[2]) / 2
        span = max(b[1] - b[0] for b in bounds) * .7 + 1
        for index, (azim, elev) in enumerate(((270, 15), (90, 15), (30, 30)), 1):
            ax = fig.add_subplot(1, 3, index, projection="3d")
            for cube in elements:
                x, y, z = cube["from"]
                X, Y, Z = cube["to"]
                vertices = [(x, z, y), (X, z, y), (X, Z, y), (x, Z, y),
                            (x, z, Y), (X, z, Y), (X, Z, Y), (x, Z, Y)]
                faces = [[vertices[k] for k in face] for face in
                         ((0, 1, 2, 3), (4, 5, 6, 7), (0, 1, 5, 4),
                          (2, 3, 7, 6), (1, 2, 6, 5), (0, 3, 7, 4))]
                marker = next(iter(cube.get("faces", {}).values()), {}).get("texture", "#m").lstrip("#")
                color = colors.get(marker, "#777777")
                ax.add_collection3d(Poly3DCollection(faces, facecolors=color,
                                                     edgecolors="#1b1a21", linewidths=.22))
            ax.set_xlim(cx - span, cx + span)
            ax.set_ylim(cz - span, cz + span)
            ax.set_zlim(cy - span, cy + span)
            ax.set_box_aspect((1, 1, 1))
            ax.view_init(elev=elev, azim=azim)
            ax.set_axis_off()
            ax.set_title(("Front", "Back", "Isometric")[index - 1])
        fig.suptitle(name + " — source cuboid geometry (offline only)")
        fig.tight_layout()
        fig.savefig(folder / (name + ".png"), dpi=130)
        plt.close(fig)
    print(f"Saved {len(models)} model previews to {folder}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", action="store_true")
    args = parser.parse_args()
    models = validate()
    if not ERRORS and args.preview:
        previews(models, ROOT / "build/visual-25-previews")
    for error in ERRORS:
        print("ERROR: " + error)
    if ERRORS:
        raise SystemExit(1)
