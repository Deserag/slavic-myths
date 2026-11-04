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

def old_boxes(boxes):
    source = (JAVA / "AgneyaModelV7.java").read_text()
    materials = re.findall(r'"(\w+)"', source.split("MATERIALS = {")[1].split("};")[0])
    atlas = Image.open(TEX / "agneya_v6.png").convert("RGB")
    for box in boxes:
        index = materials.index(box["material"])
        color = atlas.getpixel((index % 8 * 128 + 12, index // 8 * 128 + 12))
        start = [box[key] for key in ("x", "y", "z")]
        size = [box[key] for key in ("w", "h", "d")]
        mace = box["part"] == "mace"
        yield start, size, np.array(color) / 255, (7.95, 39, -1), -1.20 if mace else 0

def new_boxes(lines, step=0, cast=0):
    for line in lines:
        joint, color, *values = line.split()
        *bounds, px, py, pz, lift = map(float, values)
        bounds[1] += lift * abs(step)
        if joint.startswith("HEM_"):
            bounds[2] *= 1 + .34 * abs(step)
            bounds[5] *= 1 + .34 * abs(step)
        rgb = np.array([int(color[i:i+2], 16) for i in (0, 2, 4)]) / 255
        angle = {
            "LEFT_LEG": -.25 * step, "RIGHT_LEG": .25 * step,
            "LEFT_ARM": .18 * step + 1.24 * cast,
            "RIGHT_ARM": -.18 * step + 1.42 * cast,
        }.get(joint, 0)
        yaw = {"LEFT_ARM": .32 * cast, "RIGHT_ARM": -.32 * cast}.get(joint, 0)
        yield bounds[:3], bounds[3:], rgb, (px, py, pz), angle, yaw

FACES = ((0, 1, 3, 2), (4, 6, 7, 5), (0, 4, 5, 1),
         (2, 3, 7, 6), (0, 2, 6, 4), (1, 5, 7, 3))

def plot(ax, boxes, azimuth, title, elevation=0):
    faces, colors = [], []
    for start, size, rgb, pivot, angle, *extra in boxes:
        pivot = np.array(pivot)
        vertices = np.array(list(itertools.product((0, 1), repeat=3))) * size + start
        c, s = np.cos(angle), np.sin(angle)
        yaw = extra[0] if extra else 0
        cy, sy = np.cos(yaw), np.sin(yaw)
        rx = np.array(((1, 0, 0), (0, c, -s), (0, s, c)))
        ry = np.array(((cy, 0, sy), (0, 1, 0), (-sy, 0, cy)))
        vertices = (vertices - pivot) @ (ry @ rx).T + pivot
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
    for ax, az, view in zip(axes[0, :3], (-90, 0, 90), ("спереди", "сбоку", "сзади")):
        plot(ax, old_boxes(agneya), az, "Огнея · " + view)
    for ax, az, view in zip(axes[1, :3], (-90, 0, 90), ("спереди", "сбоку", "сзади")):
        plot(ax, new_boxes(ledeya), az, "Ледея · " + view)
    plot(axes[0, 3], new_boxes(ledeya, cast=1), -56, "Ледея · выпуск снаряда", 8)
    plot(axes[1, 3], new_boxes(ledeya, step=1), 0, "Ледея · шаг, подол поднят")
    fig.suptitle("Огнея и Ледея · игровые кубоиды Java · 16 единиц = 1 блок", fontsize=17)
    fig.savefig(args.output, dpi=150)

if __name__ == "__main__":
    main()
