#!/usr/bin/env python3
"""Slavic Myths 2.5 resource validation and real-geometry preview generator."""
from __future__ import annotations
import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
JAVA = ROOT / "src/main/java/org/slavicmyths"
ARMOR = ("ledeya_helmet", "gneteya_chestplate", "agneya_leggings", "tryaseya_boots")
BOWS = ("infernal_bow", "empowered_infernal_bow")
ERRORS = []

def check(condition, message):
    if not condition:
        ERRORS.append(message)

def load(path):
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except (ValueError, OSError) as error:
        ERRORS.append(f"{path}: {error}")
        return {}

def validate_model(name):
    path = ASSETS / "models/item" / (name + ".json")
    model = load(path)
    elements = model.get("elements", [])
    check(len(elements) >= 10, f"{name}: empty/unfinished item geometry")
    for key, resource in model.get("textures", {}).items():
        if isinstance(resource, str) and resource.startswith("slavicmyths:"):
            relative = resource.split(":", 1)[1]
            texture_file = ASSETS / "textures" / (relative + ".png")
            check(texture_file.is_file(), f"{name}: missing texture {key}: {texture_file}")
            if texture_file.is_file():
                header = texture_file.read_bytes()[:24]
                check(len(header) == 24 and header[:8] == b"\x89PNG\r\n\x1a\n", f"{name}: invalid PNG {texture_file}")
    for index, part in enumerate(elements):
        lo, hi = part.get("from"), part.get("to")
        check(isinstance(lo, list) and isinstance(hi, list) and len(lo) == len(hi) == 3, f"{name}:{index}: invalid cuboid")
        if not (isinstance(lo, list) and isinstance(hi, list) and len(lo) == len(hi) == 3):
            continue
        check(all(isinstance(a, (int, float)) and isinstance(b, (int, float)) and a < b for a, b in zip(lo, hi)), f"{name}:{index}: inverted/zero cuboid")
        check(all(-64 <= v <= 64 for v in lo + hi), f"{name}:{index}: impractical coordinate")
        textures = model.get("textures", {})
        for face in part.get("faces", {}).values():
            texture = face.get("texture", "")
            if texture.startswith("#"):
                check(texture[1:] in textures, f"{name}:{index}: unresolved {texture}")
    return model

def validate():
    ru = load(ASSETS / "lang/ru_ru.json")
    en = load(ASSETS / "lang/en_us.json")
    check(len(ru) >= 6500, f"ru_ru.json unexpectedly shrunk: {len(ru)} keys")
    missing = [key for key in en if key not in ru]
    if missing:
        print(f"Translation parity: {len(missing)} untranslated keys (first 15: {missing[:15]})")
    for name in ARMOR:
        validate_model(name)
        armor_texture = ASSETS / "textures/armor/sisters" / (name + ".png")
        check(armor_texture.is_file(), f"{name}: missing equipped armor texture")
        if armor_texture.is_file():
            check(armor_texture.read_bytes()[:8] == b"\x89PNG\r\n\x1a\n", f"{name}: invalid equipped-armor PNG")
    for bow in BOWS:
        base = validate_model(bow)
        mapping = base.get("overrides", [])
        check(len(mapping) == 3, f"{bow}: missing draw stage overrides")
        for index in range(3):
            state = f"{bow}_pulling_{index}"
            validate_model(state)
            check(any(item.get("model") == "slavicmyths:item/" + state for item in mapping), f"{bow}: missing predicate for {state}")
    properties = (JAVA / "client/FieldBowClient.java").read_text(encoding="utf-8")
    check(all('"' + bow + '"' in properties for bow in BOWS), "A bow is missing pull/pulling registration")
    draw = (JAVA / "client/BowDrawClient.java").read_text(encoding="utf-8")
    check(all('"' + bow + '"' in draw for bow in BOWS), "A bow is missing hand animation registration")
    armor = (JAVA / "sisters/client/SistersArmorClient.java").read_text(encoding="utf-8")
    check(all(token in armor for token in ('EquipmentSlot.HEAD', 'EquipmentSlot.CHEST', 'EquipmentSlot.LEGS', 'EquipmentSlot.FEET')), "Missing equipped armor geometry")
    for name in ARMOR:
        check(name in (JAVA / "sisters/SistersArmor.java").read_text(encoding="utf-8"), f"{name}: not registered")
    for name in ARMOR + BOWS:
        print(f"Validated model: {name}")
    return not ERRORS

def preview(destination):
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt
        from mpl_toolkits.mplot3d.art3d import Poly3DCollection
    except ImportError as error:
        ERRORS.append(f"Preview needs matplotlib: {error}")
        return
    color = {"metal": "#565060", "bone": "#c6bda5", "glow": "#b04ae6", "ice": "#62a7e7", "magma": "#f46b27"}
    destination.mkdir(parents=True, exist_ok=True)
    for name in ARMOR:
        data = validate_model(name)
        fig = plt.figure(figsize=(12, 4))
        for view, (az, el) in enumerate(((270, 15), (90, 15), (0, 15)), start=1):
            ax = fig.add_subplot(1, 3, view, projection="3d")
            for cube in data.get("elements", []):
                x, y, z = cube["from"]
                X, Y, Z = cube["to"]
                vertices = [(x,z,y),(X,z,y),(X,z,Y),(x,z,Y),(x,Z,y),(X,Z,y),(X,Z,Y),(x,Z,Y)]
                faces = [[vertices[i] for i in face] for face in ((0,1,2,3),(4,5,6,7),(0,1,5,4),(2,3,7,6),(1,2,6,5),(0,3,7,4))]
                material = next(iter(cube.get("faces", {}).values()), {}).get("texture", "#metal").lstrip("#")
                ax.add_collection3d(Poly3DCollection(faces, facecolors=color.get(material, "#777777"), edgecolors="#19191c", linewidths=.22, alpha=1))
            ax.set(xlim=(-1,18), ylim=(-2,16), zlim=(-1,20))
            ax.set_box_aspect((19,18,21))
            ax.view_init(elev=el, azim=az)
            ax.set_title(("Front", "Back", "Side")[view-1])
            ax.set_axis_off()
        fig.suptitle(f"{name} - actual item model JSON cuboids")
        fig.tight_layout()
        fig.savefig(destination / (name + "_three_views.png"), dpi=160)
        plt.close(fig)
    print(f"Geometry previews saved to {destination}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", action="store_true")
    parser.add_argument("--out", default="build/armor-25-previews")
    args = parser.parse_args()
    valid = validate()
    if args.preview and valid:
        preview(ROOT / args.out)
    for error in ERRORS:
        print("ERROR:", error, file=sys.stderr)
    sys.exit(0 if valid and not ERRORS else 1)
