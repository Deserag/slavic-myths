"""Compile the game cuboid sources and plot orthographic poses with Matplotlib."""
import argparse
import itertools
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/org/slavicmyths/client"
TEX = ROOT / "src/main/resources/assets/slavicmyths/textures/entity"

def geometry():
    with tempfile.TemporaryDirectory() as temp:
        old_source = JAVA / "AgneyaGeometryV7.java"
        new_source = JAVA / "LedeyaGeometry.java"
        old = Path(temp) / "agneya.json"
        if shutil.which("javac"):
            subprocess.run(["javac", "-d", temp, old_source, new_source], check=True)
            cmd = ["java", "-cp", temp]
            old_class = "org.slavicmyths.client.AgneyaGeometryV7"
            new_class = "org.slavicmyths.client.LedeyaGeometry"
        else:
            # Java 17 source-file mode compiles a single compilation unit.
            old_text = old_source.read_text()
            imports, geometry_class = old_text.split("public final class AgneyaGeometryV7", 1)
            model = new_source.read_text().split("public final class LedeyaGeometry", 1)[1]
            combined = Path(temp) / "LedeyaGeometry.java"
            combined.write_text(imports + "public final class LedeyaGeometry"
                                + model.replace("for (Box box", "for (AgneyaGeometryV7.Box box")
                                + "\nfinal class AgneyaGeometryV7" + geometry_class)
            cmd = ["java"]
            old_class, new_class = old_source, combined
        subprocess.run([*cmd, old_class, old], check=True)
        new = subprocess.check_output([*cmd, new_class], text=True)
        return json.loads(old.read_text())["cuboids"], new.splitlines()

def led_atlas():
    source = (JAVA / "LedeyaGeometry.java").read_text()
    materials = re.findall(r"([A-Z_]+)\(0x([0-9a-f]{6})\)", source)
    atlas = Image.new("RGB", (512, 512))
    for index, (_, color) in enumerate(materials):
        x, y = index % 4 * 128, index // 4 * 128
        atlas.paste("#" + color, (x, y, x + 128, y + 128))
    atlas.save(TEX / "ledeya.png")
    sprite = Image.new("RGBA", (64, 64))
    draw = ImageDraw.Draw(sprite)
    draw.polygon([(55, 1), (61, 5), (62, 10), (55, 16), (49, 12), (48, 6)],
                 fill="#3b87bd")
    draw.polygon([(55, 3), (60, 8), (55, 14), (51, 9)],
                 fill="#b8edff")
    draw.line([(55, 5), (55, 11)], fill="#ffffff", width=2)
    sprite.save(TEX / "ledeya_frost_bolt.png")

def rotation(pivot, x=0, y=0, z_offset=0):
    pivot = np.array(pivot)
    c, s = np.cos(x), np.sin(x)
    cy, sy = np.cos(y), np.sin(y)
    rx = np.array(((1, 0, 0), (0, c, -s), (0, s, c)))
    ry = np.array(((cy, 0, sy), (0, 1, 0), (-sy, 0, cy)))
    return pivot, ry @ rx, np.array([0, 0, z_offset])

def old_boxes(boxes, pose="idle"):
    source = (JAVA / "AgneyaModelV7.java").read_text()
    materials = re.findall(r'"(\w+)"', source.split("MATERIALS = {")[1].split("};")[0])
    atlas = Image.open(TEX / "agneya_v6.png").convert("RGB")
    upper, fore, twist = {"idle": (0, -.07, 0), "raise": (-2.25, -.32, -.1),
                           "strike": (-.8, -.13, -.24), "walk": (-.12, -.07, 0)}[pose]
    for box in boxes:
        index = materials.index(box["material"])
        color = atlas.getpixel((index % 8 * 128 + 12, index // 8 * 128 + 12))
        start = [box[key] for key in ("x", "y", "z")]
        size = [box[key] for key in ("w", "h", "d")]
        part = box["part"]
        transforms = []
        if part == "mace":
            transforms.append(rotation((7.95, 39, -1), x=-1.2))
        if part in {"mace", "right_hand", "right_forearm"}:
            transforms.append(rotation((7.3, 47.2, -.1), x=-fore))
        if part in {"mace", "right_hand", "right_forearm", "right_arm"}:
            transforms.append(rotation((7.25, 58.5, -.2), x=-upper))
        if part.endswith("_leg"):
            if pose == "walk":
                side = -1 if part == "left_leg" else 1
                transforms.append(rotation((side * 2.75, 38, 0), x=side * .28))
        else:
            transforms.append(rotation((0, 43, 0), y=twist))
        yield start, size, np.array(color) / 255, transforms

def new_boxes(lines, step=0, draw=0, rain=False, ready=0, nocked=False):
    left = .18 * step + (2.32 if rain else 1.44) * ready
    right = -.18 * step + (2.18 if rain else 1.43) * ready - .10 * draw
    bow = -left + (1.17 * ready if rain else 0)
    for line in lines:
        joint, color, *values = line.split()
        *bounds, px, py, pz, lift = map(float, values)
        pivot = (px, py, pz)
        bounds[1] += lift * abs(step)
        if joint.startswith("HEM_"):
            bounds[2] *= 1 + .34 * abs(step)
            bounds[5] *= 1 + .34 * abs(step)
        rgb = np.array([int(color[i:i+2], 16) for i in (0, 2, 4)]) / 255
        transforms = []
        if joint.startswith("ARROW"):
            if not nocked or (not rain and joint != "ARROW_CENTER"):
                continue
            transforms.append(rotation(pivot, z_offset=2.2 * draw))
        if joint.startswith("STRING"):
            sign = -1 if joint == "STRING_UP" else 1
            transforms.append(rotation(pivot, x=sign * .24 * draw, z_offset=2.2 * draw))
        if joint == "BOW" or joint.startswith(("STRING", "ARROW")):
            transforms.append(rotation((-7.25, 36.8, -2.1), x=bow))
        if joint == "LEFT_ARM" or joint == "BOW" or joint.startswith(("STRING", "ARROW")):
            transforms.append(rotation((-7.25, 58.5, -.2), x=left))
        if joint == "RIGHT_ARM":
            transforms.append(rotation(pivot, x=right, y=.68 * ready))
        if joint.endswith("_LEG"):
            transforms.append(rotation(pivot, x=(-.25 if joint == "LEFT_LEG" else .25) * step))
        yield bounds[:3], bounds[3:], rgb, transforms

FACES = ((0, 1, 3, 2), (4, 6, 7, 5), (0, 4, 5, 1),
         (2, 3, 7, 6), (0, 2, 6, 4), (1, 5, 7, 3))

def plot(ax, boxes, azimuth, title, elevation=0):
    faces, colors = [], []
    for start, size, rgb, transforms in boxes:
        vertices = np.array(list(itertools.product((0, 1), repeat=3))) * size + start
        for pivot, matrix, offset in transforms:
            vertices = (vertices - pivot) @ matrix.T + pivot + offset
        vertices = vertices[:, [0, 2, 1]]
        for face, shade in zip(FACES, (.79, .9, .70, 1, 1, .84)):
            faces.append(vertices[list(face)])
            colors.append(rgb * shade)
    ax.add_collection3d(Poly3DCollection(faces, facecolors=colors, linewidth=.15,
                                        edgecolors=(.07, .12, .19, .24)))
    ax.set(xlim=(-23, 23), ylim=(-23, 23), zlim=(-2, 82),
           xlabel="X", ylabel="Z", zlabel="Y", title=title)
    ax.set_box_aspect((46, 46, 84), zoom=1.12)
    ax.set_proj_type("ortho")
    ax.view_init(elev=elevation, azim=azimuth)
    if elevation == 0:
        hidden = ax.xaxis if azimuth == 0 else ax.yaxis
        hidden.set_ticks([])
        hidden.label.set_visible(False)
    ax.set_zticks(range(0, 81, 16))
    ax.grid(True)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    parser.add_argument("--atlas", action="store_true")
    args = parser.parse_args()
    if args.atlas:
        led_atlas()
    agneya, ledeya = geometry()
    assert min(float(line.split()[3]) for line in ledeya) == 0
    assert max(float(row[3]) + float(row[6]) for row in map(str.split, ledeya)) <= 4.65 * 16
    fig, axes = plt.subplots(2, 4, figsize=(15, 10), subplot_kw={"projection": "3d"},
                             layout="constrained")
    plot(axes[0, 0], old_boxes(agneya), -90, "Огнея · пропорции спереди")
    plot(axes[0, 1], old_boxes(agneya, "walk"), 0, "Огнея · шаг и подошва")
    plot(axes[0, 2], old_boxes(agneya, "raise"), -40, "Огнея · замах", 8)
    plot(axes[0, 3], old_boxes(agneya, "strike"), -40, "Огнея · удар", 8)
    plot(axes[1, 0], new_boxes(ledeya, step=1), 0, "Ледея · ходьба с луком")
    plot(axes[1, 1], new_boxes(ledeya, draw=.94, ready=1, nocked=True), -38, "Ледея · одиночный", 8)
    plot(axes[1, 2], new_boxes(ledeya, draw=.94, ready=1, rain=True, nocked=True), -43,
         "Ледея · три стрелы вверх", 8)
    plot(axes[1, 3], new_boxes(ledeya, draw=.33, ready=1, nocked=True), -38,
         "Ледея · перезарядка серии", 8)
    fig.suptitle("Огнея и Ледея · игровые кубоиды Java · 16 единиц = 1 блок", fontsize=17)
    fig.savefig(args.output, dpi=150)

if __name__ == "__main__":
    main()
