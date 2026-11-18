#!/usr/bin/env python3
"""Technical previews of the ACTUAL infernal bow JSON cuboids (not concept art)."""
from __future__ import annotations

import argparse
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/slavicmyths/models/item"
BOWS = ("infernal_bow", "empowered_infernal_bow")

def model(name):
    return json.loads((MODELS / (name + ".json")).read_text(encoding="utf-8"))

def draw(ax, data, base_count, state):
    from matplotlib.patches import Rectangle
    elements = sorted(enumerate(data["elements"]), key=lambda pair: pair[1]["from"][2], reverse=True)
    for i, part in elements:
        x, y, _ = part["from"]
        X, Y, _ = part["to"]
        if i >= base_count:
            color, edge = "#e3c89e", "#55412d"
        elif i >= base_count - 48:
            color, edge = "#d2c7b2", "#77716b"
        else:
            color, edge = "#642e2d", "#1c1820"
        ax.add_patch(Rectangle((x, y), X-x, Y-y, facecolor=color, edgecolor=edge, linewidth=.28))
    ax.set_xlim(-15, 26)
    ax.set_ylim(-17, 33)
    ax.set_aspect("equal")
    ax.grid(alpha=.16)
    ax.set_title(state, fontsize=11)
    ax.set_xlabel("X (Minecraft model coordinates)", fontsize=8)

def render(dest):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib.animation import FuncAnimation, PillowWriter

    dest.mkdir(parents=True, exist_ok=True)
    stages = ("idle", "pulling_0", "pulling_1", "pulling_2")
    for bow in BOWS:
        variants = [model(bow if stage == "idle" else bow + "_" + stage) for stage in stages]
        base_count = len(variants[0]["elements"])
        figure, axes = plt.subplots(1, 4, figsize=(16, 6))
        for ax, data, stage in zip(axes, variants, stages):
            draw(ax, data, base_count, stage)
        figure.suptitle(f"{bow}: actual in-repository JSON geometry", fontsize=15)
        figure.tight_layout()
        figure.savefig(dest / (bow + "_four_stages.png"), dpi=170)
        plt.close(figure)

        gif, axis = plt.subplots(figsize=(5, 6))
        frames = (0, 1, 2, 3, 3, 2, 1, 0)
        def update(index):
            axis.clear()
            draw(axis, variants[frames[index]], base_count, stages[frames[index]])
        animation = FuncAnimation(gif, update, frames=len(frames), interval=220, repeat=True)
        animation.save(dest / (bow + "_draw.gif"), writer=PillowWriter(fps=5))
        plt.close(gif)
    print("Bow geometry previews:", dest)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", default="build/bow-25-previews")
    args = parser.parse_args()
    render(ROOT / args.out)
