#!/usr/bin/env python3
"""Technical GIFs of existing Slavic Myths boss weapon JSON cuboids.

Requires: python -m pip install matplotlib pillow numpy
Run from the repository: python tools/boss_reward_technical_preview.py
Outputs GIFs beside this script. Motion is an engineering approximation, not game capture.
"""
import json
import math
from io import BytesIO
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/slavicmyths/models/item"
COLORS = {"#handle": "#60473a", "#wood": "#60473a", "#metal": "#868e96", "#bone": "#c0c6cc",
          "#gold": "#b8a074", "#purple": "#675185", "#rune": "#67b9c5"}
SCENES = ("mytar_shackled_sickle", "neveya_scythe", "lomeya_hammer")


def cuboids(name):
    source = json.loads((MODELS / (name + ".json")).read_text(encoding="utf-8"))
    elements = []
    for element in source["elements"]:
        lower = np.minimum(element["from"], element["to"])
        upper = np.maximum(element["from"], element["to"])
        texture = next(iter(element["faces"].values())).get("texture", "#metal")
        elements.append((lower, upper, COLORS.get(texture, "#84888c")))
    return elements


def rotation_x(angle):
    c, s = math.cos(angle), math.sin(angle)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def rotation_y(angle):
    c, s = math.cos(angle), math.sin(angle)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def model_faces(lower, upper, pivot, transform, scale):
    vertices = np.array([[x, y, z] for x in (lower[0], upper[0])
                         for y in (lower[1], upper[1]) for z in (lower[2], upper[2])])
    vertices = ((vertices - np.array([8, 8, 8])) / 16.0 * scale) @ transform.T + pivot
    return [[vertices[j] for j in ids] for ids in ((0, 1, 3, 2), (4, 5, 7, 6),
                                                     (0, 1, 5, 4), (2, 3, 7, 6),
                                                     (0, 2, 6, 4), (1, 3, 7, 5))]


def draw_model(ax, elements, pivot, transform, scale):
    groups = {}
    for lower, upper, color in elements:
        groups.setdefault(color, []).extend(model_faces(lower, upper, pivot, transform, scale))
    for color, faces in groups.items():
        ax.add_collection3d(Poly3DCollection(faces, facecolor=color, edgecolor="#30343a", linewidth=.13))


def frame(scene, elements, index, count):
    t = index / (count - 1)
    fig = plt.figure(figsize=(6, 5.4), dpi=85)
    ax = fig.add_subplot(111, projection="3d")
    if scene == "mytar_shackled_sickle":
        hand = np.array([0.1, 1.5, 0.0])
        if t < 1 / 3:
            phase = "THROW"
            blade = np.array([.4 + 3.3 * (t * 3), 1.4, 0])
        elif t < 2 / 3:
            phase = "PULL"
            blade = np.array([3.7 - 2.1 * ((t - 1 / 3) * 3), 1.4, 0])
        else:
            phase = "RETURN"
            blade = np.array([1.6 - 1.4 * ((t - 2 / 3) * 3), 1.4, 0])
        draw_model(ax, elements, blade, rotation_y(-math.pi / 2) @ rotation_x(math.radians(115)), .72)
        vector = blade - hand
        length = float(np.linalg.norm(vector))
        if length > .01:
            forward = vector / length
            side = np.cross(forward, [0, 0, 1])
            side /= max(float(np.linalg.norm(side)), .001)
            up = np.cross(forward, side)
            segments = min(72, max(1, math.ceil(length / .20)))
            for j in range(segments):
                center = hand + vector * ((j + .5) / segments)
                circle = np.linspace(0, 2 * math.pi, 10)
                normal = side if j % 2 == 0 else up
                points = np.array([center + .065 * math.cos(a) * normal + .1 * math.sin(a) * forward for a in circle])
                ax.plot(points[:, 0], points[:, 1], points[:, 2], color="#8f969b", linewidth=1.1)
        ax.set_xlim(-1, 5)
        ax.set_ylim(0, 3)
        ax.set_zlim(-2, 2)
    elif scene == "neveya_scythe":
        phase = "CIRCULAR SWEEP"
        draw_model(ax, elements, np.array([.3, 1.1, 0]), rotation_y(-1.1 + 2.2 * t) @ rotation_x(.45), .61)
        theta = np.linspace(-math.pi, math.pi, 60)
        ax.plot(1.6 * np.cos(theta), np.ones_like(theta) * .7, 1.6 * np.sin(theta),
                linestyle="--", color="#9070ad", linewidth=1)
        ax.set_xlim(-2, 2)
        ax.set_ylim(0, 3)
        ax.set_zlim(-2, 2)
    else:
        phase = "ALTERNATING HAND SWINGS"
        angle = math.sin(2 * math.pi * t)
        draw_model(ax, elements, np.array([.45, 1.3, 0]), rotation_y(-.15) @ rotation_x(-.4 + 1.2 * angle), .55)
        draw_model(ax, elements, np.array([-.45, 1.3, 0]), rotation_y(.15) @ rotation_x(-.4 - 1.2 * angle), .55)
        ax.set_xlim(-2, 2)
        ax.set_ylim(0, 3)
        ax.set_zlim(-2, 2)
    ax.view_init(elev=23, azim=-63)
    ax.set_xlabel("X / blocks")
    ax.set_ylabel("Y / blocks")
    ax.set_zlabel("Z / blocks")
    ax.set_title(f"{scene.upper()} — {phase}\nTechnical geometry preview: {index + 1}/{count}", fontsize=10)
    fig.text(.5, .035, "Actual JSON cuboids • simulated motion • not Minecraft footage", ha="center", fontsize=7)
    plt.tight_layout(rect=(0, .06, 1, .94))
    output = BytesIO()
    fig.savefig(output, format="png")
    plt.close(fig)
    output.seek(0)
    return Image.open(output).convert("P", palette=Image.Palette.ADAPTIVE)


def main():
    for scene in SCENES:
        elements = cuboids(scene)
        images = [frame(scene, elements, i, 18) for i in range(18)]
        destination = Path(__file__).resolve().parent / (scene + "_technical.gif")
        images[0].save(destination, save_all=True, append_images=images[1:], duration=160, loop=0, optimize=True)
        print(destination)


if __name__ == "__main__":
    main()
