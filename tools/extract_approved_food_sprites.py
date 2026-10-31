"""Extract owner-approved 16x16 food sprites, without redrawing or filtering.

Run after fetching the design branch:
  python -m pip install Pillow
  git fetch origin design/2.2.9-food-expansion-20261030
  python tools/extract_approved_food_sprites.py
"""
from __future__ import annotations

import io
import subprocess
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = "design/2.2.9-food-expansion-20261030"
ART = "docs/concepts/nether-settlement-2.2/food-expansion-2.2.9/art"
NAMES = ("roasted_hot_pepper", "ground_hot_pepper", "ground_mustard", "grated_horseradish", "black_pepper", "ground_black_pepper", "garlic", "garlic_clove", "horseradish", "black_radish", "black_radish_seeds", "mustard_seeds", "onion", "onion_set")
CROPS = {"garlic": "garlic", "horseradish": "horseradish", "black_radish": "radish", "mustard": "mustard", "onion": "onion"}

def git_file(path: str) -> bytes:
    return subprocess.check_output(["git", "show", f"origin/{SOURCE}:{ART}/{path}"], cwd=ROOT)

def main() -> None:
    source = Image.open(io.BytesIO(git_file("approved_16x_atlas_lossless.webp"))).convert("RGBA")
    assert source.size == (160, 128), source.size
    entries: dict[str, tuple[int, int, int, int]] = {}
    for line in git_file("ATLAS_MANIFEST.tsv").decode("utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        name, x, y, w, h = line.split("\t")
        entries[name] = (int(x), int(y), int(w), int(h))
    destination = ROOT / "src/main/resources/assets/slavicmyths/textures/item"
    destination.mkdir(parents=True, exist_ok=True)
    for name in NAMES:
        x, y, w, h = entries[name]
        assert (w, h) == (16, 16)
        image = source.crop((x, y, x + w, y + h))
        image.save(destination / f"{name}.png", optimize=False)
        print(f"{name}: approved 16x16 RGBA")
    block_destination = ROOT / "src/main/resources/assets/slavicmyths/textures/block"
    block_destination.mkdir(parents=True, exist_ok=True)
    for crop, art in CROPS.items():
        for stage in range(4):
            x, y, w, h = entries[f"{art}_stage{stage}"]
            source.crop((x, y, x + w, y + h)).save(block_destination / f"{art}_stage{stage}.png", optimize=False)
    # The approved atlas contains no separate cutting; use the existing root sprite until a distinct approved cutting is supplied.
    (destination / "horseradish_cutting.png").write_bytes((destination / "horseradish.png").read_bytes())
    print("Do not generate heat_berry_mash/coal_plum_mash from unrelated sprites.")

if __name__ == "__main__":
    main()
