"""Regression check for custom door inventory proportions.

Generated-item models map the full source texture to a square model. Tall 1:2
door textures therefore need an X scale of 0.5 in GUI, while square item
icons must keep the default transform.
"""
from __future__ import annotations
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
DOORS = ("apple", "blackwood", "darkened", "linden", "navya", "pine", "rowan", "willow")

def png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise AssertionError(f"not a PNG: {path}")
    return struct.unpack(">II", data[16:24])

for wood in DOORS:
    texture = ASSETS / "textures/item" / f"{wood}_door.png"
    model_path = ASSETS / "models/item" / f"{wood}_door.json"
    width, height = png_size(texture)
    model = json.loads(model_path.read_text(encoding="utf-8"))
    assert model.get("parent") == "minecraft:item/generated", f"{wood}: unexpected parent"
    assert model.get("textures", {}).get("layer0") == f"slavicmyths:item/{wood}_door", f"{wood}: wrong layer0"
    scale = model.get("display", {}).get("gui", {}).get("scale")
    if height == width * 2:
        assert scale == [0.5, 1.0, 1.0], f"{wood}: {width}x{height} requires GUI X scale 0.5, got {scale}"
    elif height == width:
        assert scale is None, f"{wood}: square {width}x{height} icon must keep default GUI scale, got {scale}"
    else:
        raise AssertionError(f"{wood}: unsupported item texture aspect {width}x{height}")

print("PASS: door item models match PNG aspect ratios")
