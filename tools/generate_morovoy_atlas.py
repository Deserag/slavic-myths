#!/usr/bin/env python3
"""Generate textured Morovoy Fire Zmey albedo (512x512, matching current mesh UVs).

Run from any working directory: python tools/generate_morovoy_atlas.py
Optional: pip install pillow
"""
from __future__ import annotations

import math
import random
from pathlib import Path
from PIL import Image

SIZE = 512
SEED = 330311
PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUTPUT = PROJECT_ROOT / "src/main/resources/assets/slavicmyths/textures/entity/morovoy_fire_zmey_albedo.png"

OLIVE = (69, 81, 40)
FOREST = (27, 76, 45)
LIME = (153, 231, 43)
CHARCOAL = (36, 35, 45)
OBSIDIAN = (65, 34, 80)
MAGENTA = (221, 54, 188)
BONE = (161, 145, 113)
BELLY = (112, 101, 84)
MOR_MEM = (76, 118, 59)
FIRE_MEM = (111, 44, 104)

def pigment(x: int, y: int) -> tuple[int, int, int]:
    # Mesh: left (negative X) uses the 0..191 UV band;
    # right (positive X) uses 192..383. 384..511 is shared bone/detail.
    plague = x < 192
    if x >= 384:
        base = BONE
    elif 288 <= y < 383:
        base = MOR_MEM if plague else FIRE_MEM
    elif 416 <= y:
        base = FOREST if plague else OBSIDIAN
    elif 228 <= y < 288:
        base = FOREST if plague else OBSIDIAN
    elif 95 <= y < 228:
        base = OLIVE if plague else CHARCOAL
    elif 25 <= y < 95 and (x % 192) >= 80:
        base = BELLY
    else:
        base = OLIVE if plague else CHARCOAL

    # Patches of saturated colored scales and rough, semi-irregular plates.
    if x < 384 and 95 <= y < 288:
        cell = ((x // 7) * 13 + (y // 9) * 17) % 31
        if cell == 0 or cell == 9:
            base = LIME if plague else MAGENTA
        elif cell == 3 or cell == 14:
            base = FOREST if plague else OBSIDIAN

    # Gradients are local to the wing membrane and underside UV zones.
    if 288 <= y < 383:
        wing_t = (y - 288) / 95
        gradient = 0.79 + 0.31 * (1 - wing_t)  # shoulder brighter; distal edge darker
    elif 25 <= y < 95:
        belly_t = (y - 25) / 70
        gradient = 0.83 + .26 * (1 - abs(2 * belly_t - 1))  # center brighter
    else:
        gradient = .94 + .07 * math.sin(y * .085)

    # Micro-relief: fixed-seed +/- 12% brightness perturbation per pixel.
    roughness = 1.0 + random.uniform(-.12, .12)
    factor = gradient * roughness
    return tuple(max(0, min(255, round(channel * factor))) for channel in base)

def main() -> None:
    global random
    random.seed(SEED)
    image = Image.new("RGB", (SIZE, SIZE))
    pixels = image.load()
    for y in range(SIZE):
        for x in range(SIZE):
            pixels[x, y] = pigment(x, y)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUTPUT, "PNG", optimize=True)
    with Image.open(OUTPUT) as check:
        assert check.size == (SIZE, SIZE) and check.mode == "RGB" and check.format == "PNG"
    print("PASS: shaded boss atlas 512x512")
    print(f"Saved: {OUTPUT}")

if __name__ == "__main__":
    main()
