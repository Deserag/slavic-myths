#!/usr/bin/env python3
"""Deterministic 512x512 dark pixel-art material atlas for Morovoy Fire Zmey.

Usage from the project root:
    python -m pip install Pillow
    python tools/generate_morovoy_atlas.py

Each UV slot is 128x128.  Base skin stays predominantly charcoal,
obsidian, ash or slate; elemental pigment occurs only on dedicated
vein/eye slots and very sparse hairline fissures in the wing web.
"""
from __future__ import annotations

import math
from pathlib import Path
from PIL import Image

SIZE = 512
TILE = 128
SEED = 0x71F3A
ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "src/main/resources/assets/slavicmyths/textures/entity/morovoy_fire_zmey_albedo.png"

# Layout must agree with Skin in MorovoyFireZmeyMesh.java, row by row.
MATERIALS = (
    ("SLATE",      (52, 56, 63)),
    ("CHARCOAL",   (28, 30, 36)),
    ("OBSIDIAN",   (34, 31, 43)),
    ("ASH",        (69, 72, 76)),
    ("MOR_WEB",    (32, 39, 38)),
    ("FIRE_WEB",   (37, 32, 43)),
    ("BONE",       (79, 77, 74)),
    ("BELLY",      (47, 47, 51)),
    ("MOR_VEIN",   (103, 190, 61)),
    ("FIRE_CRACK", (190, 66, 150)),
    ("NEAR_BLACK", (19, 21, 27)),
    ("CREST",      (48, 43, 52)),
    ("MOR_EYE",    (163, 231, 93)),
    ("FIRE_EYE",   (238, 112, 208)),
    ("SHADOW",     (23, 24, 29)),
    ("EDGE",       (71, 74, 79)),
)

def hash01(x: int, y: int) -> float:
    """Stable 32-bit hash; independent of Python's random seed and platform."""
    h = (x * 374761393 + y * 668265263 + SEED) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    h ^= h >> 16
    return h / 0xFFFFFFFF

def pigment(x: int, y: int) -> tuple[int, int, int]:
    column, row = x // TILE, y // TILE
    index = row * 4 + column
    label, base = MATERIALS[index]
    u, v = x % TILE, y % TILE

    # Pixel groups and sharp banded ridges (Minecraft pixel art, no blur).
    plate = ((u // 7) * 11 + (v // 9) * 17) % 23
    ridge = 1.11 if plate in (2, 13) else .91 if plate in (7, 19) else 1.0
    macro = .955 + .055 * math.sin(u * .11 + (v // 8) * .62)

    # The membrane tiles are sampled at v + radial*3 by the mesh:
    # dark at the wrist (v near zero), lighter toward the finger tips.
    gradient = (.65 + .46 * v / 127.0) if label in ("MOR_WEB", "FIRE_WEB") else .88 + .14 * (1 - abs(v - 63.5) / 63.5)
    if label == "BELLY":
        gradient = .81 + .22 * (1 - abs(u - 63.5) / 63.5)

    # +/-12% per-pixel, plus 7x9 pixel plates for micro and macro contrast.
    factor = (.88 + .24 * hash01(x, y)) * ridge * macro * gradient
    color = [max(0, min(255, round(channel * factor))) for channel in base]

    # Narrow, sparse tinted micro-fissures confined to dark membrane.
    # Dedicated colored UVs remain the only strongly luminous materials.
    if label in ("MOR_WEB", "FIRE_WEB") and v > 9:
        offset = round(2.0 * math.sin(v * .11))
        if (u + offset) % 59 == 0 and (v // 8) % 4 != 0:
            tint = (49, 90, 40) if label == "MOR_WEB" else (92, 35, 72)
            color = [max(0, min(255, round(t * factor))) for t in tint]
    return tuple(color)

def main() -> None:
    image = Image.new("RGB", (SIZE, SIZE))
    pixels = image.load()
    for y in range(SIZE):
        for x in range(SIZE):
            pixels[x, y] = pigment(x, y)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUTPUT, format="PNG", optimize=True)
    with Image.open(OUTPUT) as check:
        assert check.format == "PNG" and check.mode == "RGB" and check.size == (512, 512)
        assert len(MATERIALS) == 16
        for row in range(4):
            for col in range(4):
                assert check.getpixel((col * 128 + 4, row * 128 + 4)) == pigment(col * 128 + 4, row * 128 + 4)
    print(f"PASS: dark 512x512 UV atlas, 16 materials: {OUTPUT}")

if __name__ == "__main__":
    main()
