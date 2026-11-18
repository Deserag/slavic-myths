from pathlib import Path
from PIL import Image, ImageDraw

ATLAS_SIZE = 1024
TILE_SIZE = 256

# Change this directory if your Python script is not in the project root.
PROJECT_ROOT = Path(__file__).resolve().parent

OUTPUT_DIR = (
    PROJECT_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "slavicmyths"
    / "textures"
    / "entity"
)

OUTPUT_FILE = OUTPUT_DIR / "morovoy_zmey_color_atlas_v2.png"

# RGB values and UV tile positions matching MorovoyFireZmeyMesh.java
MATERIALS = {
    "OLIVE":    (0,   0,   (65, 72, 42)),
    "FOREST":   (256, 0,   (34, 85, 54)),
    "LIME":     (512, 0,   (165, 244, 44)),
    "CHARCOAL": (768, 0,   (36, 36, 44)),
    "OBSIDIAN": (0,   256, (67, 32, 71)),
    "MAGENTA":  (256, 256, (240, 68, 206)),
    "BONE":     (512, 256, (153, 140, 114)),
    "MOR_MEM":  (768, 256, (83, 112, 68)),
    "FIRE_MEM": (0,   512, (100, 52, 88)),
    "BELLY":    (256, 512, (98, 89, 81)),
}

def generate_atlas():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    atlas = Image.new(
        "RGB",
        (ATLAS_SIZE, ATLAS_SIZE),
        color=(48, 46, 48)
    )

    draw = ImageDraw.Draw(atlas)

    for name, (u, v, color) in MATERIALS.items():
        draw.rectangle(
            [
                u,
                v,
                u + TILE_SIZE - 1,
                v + TILE_SIZE - 1
            ],
            fill=color
        )
        print(f"{name}: UV=({u},{v}) RGB={color}")

    atlas.save(OUTPUT_FILE, format="PNG")

    # Validate generated atlas
    with Image.open(OUTPUT_FILE) as check:
        assert check.size == (1024, 1024)
        assert check.mode == "RGB"
        assert check.format == "PNG"

        for name, (u, v, color) in MATERIALS.items():
            actual = check.getpixel((u + 100, v + 100))
            assert actual == color, (
                f"{name}: expected {color}, got {actual}"
            )

    print()
    print("ATLAS GENERATION SUCCESSFUL")
    print(f"Saved to: {OUTPUT_FILE}")
    print(f"Size: {OUTPUT_FILE.stat().st_size} bytes")
    print("Resolution: 1024x1024")
    print("Materials: 10")
    print("Pixel validation: PASS")

if __name__ == "__main__":
    generate_atlas()