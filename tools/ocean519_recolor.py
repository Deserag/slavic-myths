#!/usr/bin/env python3
"""Derive #519 icon recolors from vanilla 1.21.1 item PNGs, keeping original silhouettes."""
import io
import pathlib
import zipfile
from PIL import Image

SOURCES = {
    "pharaohka_tide_pearl": ("heart_of_the_sea", (44, 197, 224)),
    "deep_sea_serpent_scale": ("prismarine_shard", (48, 109, 157)),
    "ocean_zmey_storm_heart": ("amethyst_shard", (61, 140, 225)),
    "stratim_bird_feather": ("feather", (181, 203, 246)),
    "tsar_pike_fang": ("bone", (208, 170, 103)),
    "zhelva_shell_plate": ("turtle_scute", (77, 157, 116)),
    "pharaohka_victory_token": ("nautilus_shell", (29, 189, 210)),
    "deep_sea_serpent_victory_token": ("prismarine_crystals", (45, 107, 208)),
    "ocean_zmey_victory_token": ("echo_shard", (115, 86, 235)),
    "stratim_bird_victory_token": ("phantom_membrane", (181, 189, 243)),
    "tsar_pike_victory_token": ("rabbit_foot", (224, 166, 77)),
    "zhelva_victory_token": ("heart_of_the_sea", (93, 177, 91)),
}

def find_vanilla_jar() -> pathlib.Path:
    paths = list(pathlib.Path("build/moddev/artifacts").glob("*.jar"))
    paths += list(pathlib.Path("build").rglob("*minecraft*.jar"))
    for jar in paths:
        try:
            with zipfile.ZipFile(jar) as z:
                if "assets/minecraft/textures/item/feather.png" in z.namelist():
                    return jar
        except (OSError, zipfile.BadZipFile):
            continue
    raise RuntimeError("No Minecraft vanilla PNGs in extracted NeoForge client jar")

def colorize(source: Image.Image, rgb: tuple[int, int, int]) -> Image.Image:
    source = source.convert("RGBA")
    pixels = []
    for r, g, b, alpha in source.getdata():
        if alpha == 0:
            pixels.append((0, 0, 0, 0))
            continue
        shade = (0.21*r+0.72*g+0.07*b) / 255.
        multiplier = 0.22 + 0.92*shade
        # Lightest original pixels retain a subtle cool/white glint.
        glint = max(0.0, shade-0.68)*0.48
        p = tuple(min(255, max(0, int(c*multiplier + (255-c)*glint))) for c in rgb)
        pixels.append((*p, alpha))
    out = Image.new("RGBA", source.size)
    out.putdata(pixels)
    return out

def main() -> None:
    jar = find_vanilla_jar()
    out = pathlib.Path("build/ocean519-recolor")
    out.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(jar) as z:
        for name, (vanilla, tint) in SOURCES.items():
            path = f"assets/minecraft/textures/item/{vanilla}.png"
            if path not in z.namelist():
                raise RuntimeError(f"Vanilla source texture missing: {path}")
            image = colorize(Image.open(io.BytesIO(z.read(path))), tint)
            image.save(out / f"{name}.png")
            print(f"RECOLORED {name}: {vanilla}, {image.width}x{image.height}")
    print(f"RECOLOR_COUNT={len(SOURCES)}; JAR={jar}")

if __name__ == "__main__":
    main()
