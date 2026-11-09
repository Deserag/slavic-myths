"""Complete the classic 64x64 Posad player skin UVs without extra model layers."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/slavicmyths/textures/entity"


def fill(im, box, color):
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            im.putpixel((x, y), color)


for variant in range(5):
    path = ROOT / f"posad_resident_{variant}.png"
    image = Image.open(path).convert("RGBA")
    if image.size != (64, 64):
        raise ValueError(f"Unexpected skin size: {path}")
    dark = image.getpixel((20, 23))
    cloth = image.getpixel((23, 25))
    cuff = image.getpixel((20, 28))
    # Vanilla classic arm UV: top/bottom, then outer/front/inner/back faces.
    # Preserve the last two pixels of each arm as bare hands.
    for box in ((44, 16, 52, 20), (36, 48, 44, 52),
                (40, 20, 56, 29), (32, 52, 48, 61)):
        fill(image, box, cloth)
    fill(image, (40, 29, 56, 30), cuff)
    fill(image, (32, 61, 48, 62), cuff)
    # Four subtle vertical seams make all arm faces read as the same garment.
    for x in (40, 44, 48, 52):
        fill(image, (x, 20, x + 1, 29), dark)
    for x in (32, 36, 40, 44):
        fill(image, (x, 52, x + 1, 61), dark)
    # Hair is painted on the actual head top instead of an extra cap cube.
    hair = image.getpixel((8, 9))
    fill(image, (8, 0, 16, 8), hair)
    for x in (9, 12, 14):
        fill(image, (x, 1, x + 1, 5), dark)
    image.save(path)

# The ruler keeps the same player silhouette. A restrained gold circlet and
# woven tunic marks replace the separate crown and solid gold armor cubes.
leader = Image.open(ROOT / "posad_resident_0.png").convert("RGBA")
gold = (194, 145, 46, 255)
shade = (112, 70, 33, 255)
for x in range(0, 32):
    leader.putpixel((x, 8), gold if x % 4 else shade)
for y in range(20, 28):
    for x in (20, 27):
        leader.putpixel((x, y), shade)
    if y in (20, 21, 27):
        for x in range(21, 27):
            leader.putpixel((x, y), gold if x % 3 else shade)
for y in range(22, 27):
    for x in (23, 24):
        leader.putpixel((x, y), gold if y % 2 else shade)
leader.save(ROOT / "posad_resident_posadnik.png")
