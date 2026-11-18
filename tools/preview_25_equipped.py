#!/usr/bin/env python3
"""Render Minecraft Y-up equipped cuboids extracted from SistersArmorClient.java.
The source itself is parsed; these are not manually invented armor pieces.
"""
from __future__ import annotations
import argparse
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/org/slavicmyths/sisters/client/SistersArmorClient.java"
SLOTS = ("HEAD", "CHEST", "LEGS", "FEET")
COLORS = {
    "HEAD": ("#97b2c5", "#c2d8e5", "#71bcf4"),
    "CHEST": ("#392b40", "#bcad97", "#b14de1"),
    "LEGS": ("#693037", "#ad4a33", "#f78c30"),
    "FEET": ("#5c4276", "#5d9ac6", "#ce4bed")
}

def balanced(text, at):
    depth = 0
    for pos in range(at, len(text)):
        if text[pos] == "{":
            depth += 1
        elif text[pos] == "}":
            depth -= 1
            if depth == 0:
                return text[at + 1:pos], pos + 1
    raise ValueError("Unbalanced Java braces")

def expression(expr, variables):
    expr = re.sub(r"(?<=\d)F\b", "", expr.strip())
    inside = re.compile(r"\(\s*(side\s*[<>]\s*0)\s*\?\s*([^():]+?)\s*:\s*([^():]+?)\s*\)")
    while "?" in expr:
        found = inside.search(expr)
        if found:
            selected = found.group(2) if eval(found.group(1), {"__builtins__": {}}, variables) else found.group(3)
            expr = expr[:found.start()] + "(" + selected.strip() + ")" + expr[found.end():]
            continue
        outer = re.fullmatch(r"\s*(side\s*[<>]\s*0)\s*\?\s*(.+?)\s*:\s*(.+?)\s*", expr)
        if not outer:
            raise ValueError("Unsupported source expression: " + expr)
        expr = outer.group(2) if eval(outer.group(1), {"__builtins__": {}}, variables) else outer.group(3)
    return float(eval(expr, {"__builtins__": {}}, variables))

def boxes(body, slot, side=None):
    variables = {} if side is None else {"side": side}
    for var, formula in re.findall(r"float\s+(sx|x)\s*=\s*([^;]+);", body):
        variables[var] = expression(formula, variables)
    built = []
    for line in re.findall(r"box\(([^;]+)\);", body):
        fields = [piece.strip() for piece in line.split(",")]
        if len(fields) != 10:
            raise ValueError("Invalid box argument count: " + line)
        parent, identifier = fields[:2]
        if (parent in ("arm", "leg")) and side is None:
            raise ValueError("A sided bone was parsed outside its loop")
        u = expression(fields[2], variables)
        v = expression(fields[3], variables)
        x,y,z,w,h,d = [expression(f, variables) for f in fields[4:]]
        offset_x = (5 if side > 0 else -5) if parent == "arm" else (1.9 if side > 0 else -1.9) if parent == "leg" else 0
        offset_y = 2 if parent == "arm" else 12 if parent == "leg" else 0
        # Convert Minecraft model's downward-positive Y into conventional upward Z.
        a,b,c = x + offset_x, z, 24 - (y + offset_y + h)
        X,Y,Z = a + w, b + d, c + h
        built.append((a,b,c,X,Y,Z,slot,u,v))
    return built

def extract():
    content = JAVA.read_text(encoding="utf-8")
    pieces = []
    loop = re.compile(r"for\s*\(int\s+side\s*:\s*new\s+int\[\]\s*\{[^}]+\}\)\s*\{")
    for slot in SLOTS:
        marker = re.search(r"if\s*\(slot\s*==\s*EquipmentSlot\." + slot + r"\)\s*\{", content)
        if not marker:
            raise ValueError(f"Missing geometry for {slot}")
        fragment, _ = balanced(content, marker.end()-1)
        while True:
            match = loop.search(fragment)
            if not match:
                break
            interior, end = balanced(fragment, match.end()-1)
            for side in (-1,1):
                pieces += boxes(interior, slot, side)
            fragment = fragment[:match.start()] + fragment[end:]
        pieces += boxes(fragment, slot)
    if len(pieces) < 60:
        raise ValueError("Not enough real armor geometry was parsed: " + str(len(pieces)))
    return pieces

def draw(pieces, output):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from mpl_toolkits.mplot3d.art3d import Poly3DCollection

    output.mkdir(parents=True, exist_ok=True)
    face_indices=((0,1,2,3),(4,5,6,7),(0,1,5,4),(2,3,7,6),(1,2,6,5),(0,3,7,4))
    def draw_parts(ax, chosen):
        mannequin=[(-4,-4,24,4,4,32),(-4,-2,12,4,2,24),(-8,-2,10,-4,2,22),(4,-2,10,8,2,22),(-4,-2,0,0,2,12),(0,-2,0,4,2,12)]
        combined=[(*box,"MANNEQUIN",0,0) for box in mannequin]+chosen
        for a,b,c,X,Y,Z,slot,u,v in combined:
            corners=[(a,b,c),(X,b,c),(X,Y,c),(a,Y,c),(a,b,Z),(X,b,Z),(X,Y,Z),(a,Y,Z)]
            faces=[[corners[i] for i in f] for f in face_indices]
            if slot=="MANNEQUIN":
                color="#747b82"
                alpha=.3
            else:
                palette=COLORS[slot]
                color=palette[2] if u>=40 else palette[1] if u>=25 else palette[0]
                alpha=1
            ax.add_collection3d(Poly3DCollection(faces,facecolors=color,edgecolors="#242029",linewidths=.13,alpha=alpha))
        ax.set(xlim=(-12,12),ylim=(-8,9),zlim=(-1,42))
        ax.set_box_aspect((24,17,43))
        ax.set_axis_off()

    fig=plt.figure(figsize=(13,7))
    for i,(az,title) in enumerate(((-90,"Front"),(90,"Back"),(0,"Side")),start=1):
        ax=fig.add_subplot(1,3,i,projection="3d")
        draw_parts(ax,pieces)
        ax.view_init(elev=10,azim=az)
        ax.set_title(title)
    fig.suptitle("Slavic Myths 2.5: equipped cuboids extracted from SistersArmorClient.java")
    fig.tight_layout()
    fig.savefig(output/"sisters_equipped_full_three_views.png",dpi=150)
    plt.close(fig)
    for slot in SLOTS:
        fig=plt.figure(figsize=(6,8))
        ax=fig.add_subplot(111,projection="3d")
        draw_parts(ax,[part for part in pieces if part[6]==slot])
        ax.view_init(elev=12,azim=-65)
        fig.suptitle(slot)
        fig.savefig(output/("equipped_"+slot.lower()+".png"),dpi=140)
        plt.close(fig)

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--out",default="build/equipped-25-previews")
    args=parser.parse_args()
    cuboids=extract()
    print("Parsed real equipped armor cuboids:",len(cuboids))
    draw(cuboids,ROOT/args.out)
